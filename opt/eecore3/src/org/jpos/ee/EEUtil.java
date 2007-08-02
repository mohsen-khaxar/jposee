/*
 *  jPOS Extended Edition
 *  Copyright (C) 2004 Alejandro P. Revilla
 *  jPOS.org (http://jpos.org)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jpos.ee;

import org.jpos.iso.ISOUtil;
import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Alejandro P. Revilla
 * @version $Revision: 1.5 $ $Date: 2004/12/09 19:45:33 $
 *
 * Assorted helpers
 */
public class EEUtil {
    public static String getHash (String userName, String pass) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance ("MD5");
            md.update (userName.getBytes());
            hash = ISOUtil.hexString (
                md.digest (pass.getBytes())
            ).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            // should never happen
        }   
        return hash;
    }
    public static String getHash (String s) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance ("MD5");
            hash = ISOUtil.hexString (md.digest (s.getBytes())).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            // should never happen
        }   
        return hash;
    }
    public static String getRandomHash () {
        return getHash (
            Double.toString (Math.random()),
            Double.toString (Math.random())
        );
    }
}

