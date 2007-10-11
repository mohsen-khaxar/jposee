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

package org.jpos.ee.action;

import java.util.Date;
import org.jpublish.JPublishContext;
import org.jpublish.action.Action;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.DB;

public class Close implements Action, ContextConstants {
    public void execute (JPublishContext context, Configuration cfg) {
        Date start = (Date) context.get (DATE);
        long elapsed = System.currentTimeMillis() - start.getTime();
        context.getSyslog().info ("CLOSE: " + context.getRequest().getRequestURI() + " elapsed=" + elapsed);
        try {
            DB db = (DB) context.get (DB);
            if (db != null) {
                db.close();
                context.remove (DB);
            }
        } catch (Throwable t) {
            context.getSyslog().warn (t);
        } 
    }
}

