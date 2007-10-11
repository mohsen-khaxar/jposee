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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.DB;
import org.jpos.ee.Visitor;
import org.hibernate.Transaction;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Logout extends ActionSupport {

    /**
     * Permits to override 
     * @param context
     * @param cfg
     */
    protected void logout(JPublishContext context, Configuration cfg){
        DB db = getDB (context);
        HttpSession session          = context.getSession();
        
        session.removeAttribute (USER);
        context.remove (USER);
        Visitor v = (Visitor) context.get (VISITOR);
        if (v.getUser() != null) {
            Transaction tx = db.beginTransaction();
            v.setUser (null);
            tx.commit();
        }
        
    }
    
    public void execute (JPublishContext context, Configuration cfg) {
        HttpSession session          = context.getSession();

        try {
            logout(context,cfg);
//          clear the session, so that next login begins clear
            session.invalidate();
        } catch (Exception e) {
            context.getSyslog().error (e);
        }
    }
}

