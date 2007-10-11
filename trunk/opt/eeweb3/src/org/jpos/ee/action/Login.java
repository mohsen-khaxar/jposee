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

import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.ee.Permission;
import org.jpos.ee.Visitor;
import org.jpos.ee.UserManager;
import org.jpos.ee.BLException;
import org.jpos.util.Validator;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Login extends Logout {
    public void execute (JPublishContext context, Configuration cfg) {
        super.logout (context, cfg); // just in case - force logout

        DB db = getDB (context);
        HttpServletRequest  request  = context.getRequest();
        HttpServletResponse response = context.getResponse();

        String user = request.getParameter ("username");
        if (user != null) {
            String pass = request.getParameter ("password");
            String seed = 
                request.getSession().getId() + 
                context.getSession().getAttribute (HASH);
            boolean rememberMe = "yes".equalsIgnoreCase (
                request.getParameter ("remember")
            );
            UserManager mgr = new UserManager (db);
            try {
                if (!Validator.isName (user)) 
                    throw new BLException ("Invalid username");
                User u = mgr.getUserByNick (user, seed, pass);
                if (u.isDeleted()) {
                    throw new BLException ("User no longer exists.");
                }
                if (!u.hasPermission (Permission.LOGIN)) {
                    throw new BLException 
                        ("User doesn't have Login permission");
                }
                if (rememberMe) {
                    setUser (context, u);
                }
                HttpSession session = request.getSession();
                session.setAttribute (USER, u);
                String originalUri = (String) session.getAttribute (REDIRECT);
                if (originalUri == null)
                    originalUri = request.getContextPath() + "/";
                else
                    session.removeAttribute (REDIRECT);

                response.sendRedirect (originalUri);
            } catch (BLException e) {
                error (context, e.getMessage());
                try {
                    Thread.sleep (1000); // security delay
                } catch (InterruptedException ee) { }
            } catch (Exception e) {
                context.getSyslog().error (e);
            } 
        }
        setHash (context);
    }
    private void setUser (JPublishContext context, User u) 
        throws HibernateException, BLException
    {
        DB db = getDB (context);
        Visitor v = (Visitor) context.get (VISITOR);
        if (v == null)
            throw new BLException ("Invalid Visitor");
        Transaction tx = db.beginTransaction();
        v.setUser (u);
        tx.commit();
    }
}

