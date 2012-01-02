/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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
