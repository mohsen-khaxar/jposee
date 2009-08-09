package org.jpos.q2.nserver;

public class DecoderCtx
{
    public static final int STAGE_MSG_LEN=1;
    public static final int STAGE_MSG_HEADER=2;
    public static final int STAGE_MSG_PAYLOAD=3;
    public static final int STAGE_MSG_TRAILER=4;
    public static final int STAGE_MSG_DONE=5;

    int stage;
    int len = 0;
    byte[] header;
    byte[] payload;
    byte[] trailer;

    public int getStage()
    {
        return stage;
    }

    public int getLen()
    {
        return len;
    }

    public byte[] getHeader()
    {
        return header;
    }

    public byte[] getPayload()
    {
        return payload;
    }

    public byte[] getTrailer()
    {
        return trailer;
    }
}
