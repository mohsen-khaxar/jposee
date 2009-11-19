/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2009 Alejandro P. Revilla
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
 
package org.jpos.transaction;

import java.io.Serializable;
import java.util.StringTokenizer;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.ee.Constants;
import org.jpos.transaction.Context;
import org.jpos.transaction.TxnSupport;
import org.jpos.transaction.AbortParticipant;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISOException;

/**
 * @author Alejandro Revilla
 * @author David Bergert
 */

public class ProtectDebugInfo extends TxnSupport implements Constants, AbortParticipant {
    
    String protectedFields;
    String wipedFields;
    
    public int prepare (long id, Serializable o) {
        return PREPARED | READONLY;
    }
    public int prepareForAbort (long id, Serializable o) {
        return PREPARED | READONLY;
    }
    public void commit (long id, Serializable o) { 
        protect ((Context) o);
    }
    public void abort  (long id, Serializable o) { 
        protect ((Context) o);
    }
    private void protect (Context ctx) {

        /* wipe by removing entries from the context  */
        StringTokenizer stw = new StringTokenizer (wipedFields, ", ");            
        while (stw.hasMoreTokens()) {
            ctx.remove(stw.nextToken());
        }        
        /* Protect entry items */
        StringTokenizer stp = new StringTokenizer (protectedFields, ", ");            
        while (stp.hasMoreTokens()) {
            String s = stp.nextToken();
            Object o = ctx.get (s);
            if (o instanceof ISOMsg){
                ISOMsg m = (ISOMsg) ctx.get (s);
                if (m != null) {
                    m = (ISOMsg) m.clone();
                    ctx.put (s, m);   // place a clone in the context
                    protect (m);
                }
            }
            if (o instanceof String){
                String p = (String) ctx.get(s);
                if (p != null){
                    ctx.put(s, protect (p));    
                }
            }  
        }
    }
    private void protect (ISOMsg m) {
        try {
            if (m != null) {
                m.set (2, protect (m.getString (2)));
                m.set (35, protect (m.getString (35)));
                m.set (45, protect (m.getString (45)));
                m.set (52, protect (m.getString (52)));
                m.set (55, protect (m.getString (55)));
            }
        } catch (ISOException e) {
            warn (e);
        }
    }
    private String protect (String s) {
        return s != null ? ISOUtil.protect (s) : s;
    }
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        super.setConfiguration (cfg);
        this.protectedFields = cfg.get("protected-fields","");
        this.wipedFields = cfg.get("wiped-fields","");
    }
}