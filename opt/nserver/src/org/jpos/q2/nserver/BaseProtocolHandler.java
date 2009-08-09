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

package org.jpos.q2.nserver;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.header.BaseHeader;
import org.jpos.util.Logger;
import static org.jpos.q2.nserver.DecoderCtx.*;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Victor Salaman (vsalaman@gmail.com)
 */
public abstract class BaseProtocolHandler implements ProtocolHandler
{
    private ISOPackager packager;
    protected byte[] header = null;
    private boolean overrideHeader;
    private boolean useZeroLengthAsKeepalive=false;
    private int maxPacketLength = 100000;

    private Configuration configuration;
    private Logger logger;
    private String realm;

    public void setPackager(ISOPackager packager)
    {
        this.packager = packager;
    }

    public boolean isOverrideHeader()
    {
        return overrideHeader;
    }

    void setOverrideHeader(boolean overrideHeader)
    {
        this.overrideHeader = overrideHeader;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) throws ConfigurationException
    {
        this.configuration = configuration;
        setOverrideHeader(configuration.getBoolean("override-header", false));
    }

    public Logger getLogger()
    {
        return logger;
    }

    void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    public void setLogger(Logger logger, String realm)
    {
        setLogger(logger);
        setRealm(realm);
    }

    public String getRealm()
    {
        return realm;
    }

    void setRealm(String realm)
    {
        this.realm = realm;
    }

    public ISOPackager getPackager()
    {
        return packager;
    }

    public int getMaxPacketLength()
    {
        return maxPacketLength;
    }

    public void setMaxPacketLength(int maxPacketLength)
    {
        this.maxPacketLength = maxPacketLength;
    }

    public void setHeader(byte[] header)
    {
        this.header = header;
    }

    public void setHeader(String header)
    {
        setHeader(header.getBytes());
    }

    public DecoderCtx newDecoderContext()
    {
        DecoderCtx ctx=new DecoderCtx();
        ctx.stage=STAGE_MSG_LEN;
        return ctx;
    }

    public boolean handleIncomingData(DecoderCtx ctx,IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception
    {
        while(in.remaining()>0)
        {
            switch(ctx.stage)
            {
                case STAGE_MSG_LEN:
                {
                    if(!isLengthEncoded())
                    {
                        ctx.stage++;
                        break;
                    }
                    if (isDataAvailable(in, getMessageLengthByteSize()))
                    {
                        ctx.len = readMessageLength(in);
                        if(ctx.len==0 && isUseZeroLengthAsKeepalive())
                        {
                            byte[] b=new byte[getMessageLengthByteSize()];
                            Arrays.fill(b,(byte)0);
                            out.write(new NullMessage(b));
                            return true;
                        }
                        else if(ctx.len>0 && ctx.len<= getMaxPacketLength())
                        {
                            ctx.stage++;
                        }
                        else
                        {
                            throw new ISOException(
                                "receive length " +ctx.len + " seems strange - maxPacketLength = " + getMaxPacketLength());
                        }
                    }
                    else return false;
                }
                break;
                case STAGE_MSG_HEADER:
                {
                    if(!containsHeader())
                    {
                        ctx.stage++;
                        break;
                    }
                    if (isDataAvailable(in, getHeaderLength()))
                    {
                        ctx.header = readHeader(in, getHeaderLength());
                        int oldCtxLen=ctx.len;
                        ctx.len -= ctx.header.length;
                        if(ctx.len<0)
                        {
                            throw new ISOException(
                                "Header is bigger than specified " +
                                "(header_len+payload_len cannot be bigger than message_len),\n " +
                                "payload size used to be: "+oldCtxLen+", is now: "+ctx.len);
                        }
                        ctx.stage++;
                    }
                    else return false;
                }
                break;
                case STAGE_MSG_PAYLOAD:
                {
                    if (!isLengthEncoded())
                    {
                        ctx.payload = readStream(in);
                        ctx.stage++;
                    }
                    else if (isDataAvailable(in, ctx.len))
                    {
                        if(ctx.len<=0 || ctx.len>getMaxPacketLength())
                        {
                            throw new ISOException(
                                "payload length " +ctx.len + " seems strange - maxPacketLength = " + getMaxPacketLength());
                        }
                        ctx.payload = readPayload(in, ctx.len);
                        ctx.stage++;
                    }
                    else return false;
                }
                break;
                case STAGE_MSG_TRAILER:
                {
                    if(!containsTrailer())
                    {
                        out.write(buildMessage(ctx));
                        ctx.stage++;
                        return true;
                    }
                    final int tlen = getTrailerLength();
                    if (isDataAvailable(in, tlen))
                    {
                        ctx.trailer = readTrailer(in, tlen);
                        out.write(buildMessage(ctx));
                        ctx.stage++;
                        return true;
                    }
                    else return false;
                }
            }
        }
        return false;
    }

    public IoBuffer writeMessage(ISOMsg m) throws ISOException
    {
        //We setup our message and pack it.
        m.setPackager(getPackager());
        m.setDirection(ISOMsg.OUTGOING);
        byte[] b = m.pack();

        //Let's create an auto-grow buffer that will initially hold msgbytes+64
        IoBuffer buffer = IoBuffer.allocate(b.length+64,true);

        //Let's create a dummy message len as as a placeholder
        writeMessageLength(buffer, m, 0);
        int startOffset=buffer.position();

        //Fill buffer
        writeHeader(buffer, m);
        writePayload(buffer, b);
        writeTrailer(buffer, b);

        int rmlen=buffer.position()-startOffset; // Real message size to put in message.

        //Let's go back to start of buffer to replace dummy msg len.
        int oldPos=buffer.position();
        buffer.position(0);
        writeMessageLength(buffer, m, rmlen);

        //And prepare for a swift return.
        buffer.position(oldPos);
        buffer.flip();
        return buffer;
    }

    private boolean isDataAvailable(IoBuffer in, int len)
    {
        return in.remaining() >= len;
    }

    protected byte[] readBytes(IoBuffer in, int len)
    {
        byte[] b = new byte[len];
        in.get(b);
        return b;
    }

    private ISOMsg buildMessage(DecoderCtx ctx) throws ISOException
    {
        ISOMsg m = createISOMsg();
        m.setPackager(getPackager());
        m.setHeader(getDynamicHeader(ctx.header));
        unpack(m, ctx.payload);
        return m;
    }

    private void unpack(ISOMsg msg, byte[] b) throws ISOException
    {
        if (b.length > 0 && !shouldIgnore(msg.getHeader()))
        {
            msg.unpack(b);
        }
    }

    private boolean isLengthEncoded()
    {
        return getMessageLengthByteSize() > 0;
    }

    private boolean containsHeader()
    {
        return getHeaderLength() > 0;
    }

    private boolean containsTrailer()
    {
        return getTrailerLength() > 0;
    }

    protected byte[] getHeader(ISOMsg m)
    {
        if (containsHeader())
        {
            if (!overrideHeader && m.getHeader() != null)
            {
                return m.getHeader();
            }
            else if (header != null)
            {
                return header;
            }
        }
        return new byte[]{};
    }

    protected boolean isUseZeroLengthAsKeepalive()
    {
        return useZeroLengthAsKeepalive;
    }

    protected ISOMsg createISOMsg()
    {
        return packager.createISOMsg();
    }

    protected int getHeaderLength()
    {
        return header != null ? header.length : 0;
    }

    protected int getMessageLengthByteSize()
    {
        return -1;
    }

    protected int getTrailerLength()
    {
        return -1;
    }

    protected int readMessageLength(IoBuffer in)
    {
        if (isLengthEncoded())
        {
            int byteSize = getMessageLengthByteSize();
            if (byteSize == 2)
            {
                return in.getShort();
            }
            else if (byteSize == 4)
            {
                return in.getInt();
            }
        }
        return -1;
    }

    protected void writeMessageLength(IoBuffer out, ISOMsg m, int len)
    {
        if (isLengthEncoded())
        {
            int byteSize = getMessageLengthByteSize();
            if (byteSize == 2)
            {
                out.putShort((short)(len));
            }
            else if (byteSize == 4)
            {
                out.putInt(len);
            }
        }
    }

    protected byte[] readHeader(IoBuffer in, int len)
    {
        return readBytes(in, len);
    }

    protected void writeHeader(IoBuffer out, ISOMsg m)
    {
        byte[] h=getHeader(m);
        if(h.length>0)
        {
            out.put(h);
        }
    }

    protected byte[] readPayload(IoBuffer in, int len)
    {
        return readBytes(in, len);
    }

    protected void writePayload(IoBuffer out, byte[] b)
    {
        out.put(b);
    }

    protected byte[] readStream(IoBuffer in) throws IOException
    {
        return new byte[0];
    }


    @SuppressWarnings({"UnusedDeclaration"})
    protected byte[] readTrailer(IoBuffer in,int len)
    {
        if(containsTrailer())
        {
            return readBytes(in,getTrailerLength());
        }
        return new byte[0];
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void writeTrailer(IoBuffer out, byte[] b)
    {
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean isRejected(byte[] b)
    {
        return false;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean shouldIgnore(byte[] b)
    {
        return false;
    }

    protected ISOHeader getDynamicHeader(byte[] image)
    {
        return image != null ? new BaseHeader(image) : null;
    }
}
