/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2011 Alejandro P. Revilla
 *
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
import org.jpos.q2.nserver.DecoderCtx;
import org.jpos.q2.nserver.ProtocolHandler;

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public class DefaultISOMsgDecoder extends CumulativeProtocolDecoder implements ProtocolDecoder
{
    private ProtocolHandler protocolHandler;

    private final static String DECODER_STATE_KEY = DefaultISOMsgDecoder.class.getName() + ".DECODER_STATE";

    public DefaultISOMsgDecoder(ProtocolHandler protocolHandler)
    {
        this.protocolHandler = protocolHandler;
    }

    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception
    {
        long enterTime = System.currentTimeMillis();

        DecoderCtx ctx = (DecoderCtx) session.getAttribute(DECODER_STATE_KEY);
        if (ctx == null)
        {
            ctx = protocolHandler.newDecoderContext();
            session.setAttribute(DECODER_STATE_KEY, ctx);
        }

        if (System.currentTimeMillis() - enterTime > 10000)
        {
            throw new ISOException("Decoding has aborted since we couldn't complete the decoding process in less than 10 seconds.");
        }

        final boolean b = protocolHandler.handleIncomingData(ctx, session, in, out);
        if (ctx.getStage() == DecoderCtx.STAGE_MSG_DONE && b)
        {
            session.removeAttribute(DECODER_STATE_KEY);
        }
        return b;
    }
}
