/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.q2.nserver.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.nserver.NullMessage;
import org.jpos.q2.nserver.ProtocolHandler;
import org.jpos.q2.nserver.SessionISOSource;

import java.util.Arrays;

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public class DefaultISOMsgDecoder extends CumulativeProtocolDecoder implements ProtocolDecoder
{
    private ProtocolHandler protocolHandler;

    private final static String DECODER_STATE_KEY = DefaultISOMsgDecoder.class.getName() + ".DECODER_STATE";

    private static class DecoderCtx
    {
        boolean messageLenRead;
        boolean headerRead;
        boolean payloadRead;
        boolean trailerRead;

        int len = -1;
        byte[] header;
        byte[] payload;
        byte[] trailer;
    }

    public DefaultISOMsgDecoder(ProtocolHandler protocolHandler)
    {
        this.protocolHandler = protocolHandler;
    }

    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception
    {
        boolean decoded = false;

        DecoderCtx ctx = (DecoderCtx) session.getAttribute(DECODER_STATE_KEY);
        if (ctx == null)
        {
            ctx = new DecoderCtx();
            session.setAttribute(DECODER_STATE_KEY, ctx);
        }

        if (protocolHandler.isLengthEncoded() && !ctx.messageLenRead)
        {
            if (isDataAvailable(in, protocolHandler.getMessageLengthByteSize()))
            {
                ctx.len = protocolHandler.readMessageLength(in);
                if(ctx.len==0 && protocolHandler.isUseZeroLengthAsKeepalive())
                {
                    byte[] b=new byte[protocolHandler.getMessageLengthByteSize()];
                    Arrays.fill(b,(byte)0);
                    out.write(new NullMessage(b));
                    return true;
                }
                else if(ctx.len>0 && ctx.len<= protocolHandler.getMaxPacketLength())
                {
                    ctx.messageLenRead = true;
                }
                else
                {
                    throw new ISOException(
                        "receive length " +ctx.len + " seems strange - maxPacketLength = " + protocolHandler.getMaxPacketLength());
                }
            }
        }

        if (protocolHandler.containsHeader() && !ctx.headerRead)
        {
            if (isDataAvailable(in, protocolHandler.getHeaderLength()))
            {
                ctx.header = protocolHandler.readHeader(in, protocolHandler.getHeaderLength());
                ctx.len -= ctx.header.length;
                ctx.headerRead = true;
            }
        }

        if (!ctx.payloadRead)
        {
            if (!protocolHandler.isLengthEncoded())
            {
                ctx.payload = protocolHandler.readStream(in);
                ctx.payloadRead = true;
            }
            else if (isDataAvailable(in, ctx.len))
            {
                ctx.payload = protocolHandler.readPayload(in, ctx.len);
                ctx.payloadRead = true;
            }
        }

        if (ctx.payloadRead)
        {
            out.write(buildMessage(session, ctx));
            session.removeAttribute(DECODER_STATE_KEY);
            decoded = true;
        }
        return decoded;
    }

    private boolean isDataAvailable(IoBuffer in, int len)
    {
        return in.remaining() >= len;
    }

    private Object buildMessage(IoSession session, DecoderCtx ctx) throws ISOException
    {
        ISOMsg m = protocolHandler.getPackager().createISOMsg();
        m.setSource(new SessionISOSource(session));
        m.setPackager(protocolHandler.getPackager());
        m.setHeader(protocolHandler.getDynamicHeader(ctx.header));
        protocolHandler.unpack(m, ctx.payload);
        m.setDirection(ISOMsg.INCOMING);
        return m;
    }
}