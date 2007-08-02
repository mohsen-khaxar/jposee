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

import java.util.Date;
import java.beans.Expression;
import java.beans.Introspector;
import java.beans.Statement;

public class RevisionManager {
    DB db;
    public RevisionManager (DB db) {
        this.db = db;
    }
    /*
    public RevisionEntry set
        (Object obj, String propName, String newValue, User user) 
        throws BLException
    {
        try {
            Expression expr = new Expression (
                obj,Introspector.decapitalize ("get" + propName), new Object[0]
            );
            expr.execute();

            String oldValue = (String) expr.getValue ();

            if (oldValue == null) {
                if (newValue == null)
                    return null;
            } else {
                if (oldValue.equals (newValue))
                    return null;
            }
            new Statement (
                obj, Introspector.decapitalize ("set" + propName),
                new Object[] { newValue }
            ).execute ();

            RevisionEntry re = new RevisionEntry ();
            re.setDate (new Date());
            re.setUser (user);
            re.setProp (propName);
            re.setOldValue ((String) expr.getValue ());
            re.setNewValue (newValue);
            db.save (re);
            return re;
        }
        catch (Exception e) {
            throw new BLException ("Error setting "+propName, e);
        }
    }
    */
}

