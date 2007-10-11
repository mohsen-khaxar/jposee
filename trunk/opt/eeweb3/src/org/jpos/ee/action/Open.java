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
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.Visitor;
import org.jpos.ee.VisitorManager;
import javax.servlet.http.Cookie;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import javax.servlet.http.HttpServletRequest;

public class Open extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        // some standard stuff
        context.put (DATE, new Date());

        context.getSyslog().info (" OPEN: " + context.getRequest().getRequestURI());
        DB db = getDB (context);
        try {
            db.open ();
            Session hs = db.session();
            User u = (User) context.getSession().getAttribute (USER);
            if (u != null) {
                u = (User) hs.load (User.class, new Long (u.getId()));
                context.put (USER, u);
            } 
            setVisitor (context, hs);
            context.put (HS, hs);
        } catch (Throwable t) {
            context.getSyslog().warn (t);
        }
    }

    private void setVisitor (JPublishContext context, Session hs) 
        throws HibernateException
    {
        HttpServletRequest request = context.getRequest();
        Cookie [] cookies = request.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];
        VisitorManager vmgr = new VisitorManager (hs, cookies);
        Transaction tx = hs.beginTransaction();
        tx.setTimeout (5);
        Visitor visitor = vmgr.getVisitor (true);
        vmgr.set (visitor, "IP", request.getRemoteAddr());
        vmgr.set (visitor, "HOST", request.getRemoteHost());
        vmgr.update (visitor);
        context.getResponse().addCookie (vmgr.getCookie());
        context.put (VISITOR, visitor);

        User u = visitor.getUser();
        if (u != null && !u.isDeleted()) {
            context.getSession().setAttribute (USER, u);
            context.put (USER, u);
        } 
        try {
            tx.commit ();
        } catch (RuntimeException ex) {
            try {
                tx.rollback();
            } catch (RuntimeException rte) {
                context.getSyslog().warn (rte);
            }
            throw ex;
        }
    }
}

