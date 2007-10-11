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

import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;

import org.jpos.ee.User;

public class CheckAccess extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        String perms = (String) context.getPage().get ("perms");
        HttpSession         session  = context.getSession();
        HttpServletRequest  request  = context.getRequest();

        User user = (User) session.getAttribute (USER);

        context.getSyslog().info ("CHECK: " 
                + context.getRequest().getRequestURI() 
                + " user=" + user + " perms=" + perms);

        if (user != null) {
            if (perms != null) {
                context.getSyslog().info ("Checking permissions: " + perms);
                StringTokenizer st = new StringTokenizer (perms);
                boolean deny = false;
                while (st.hasMoreTokens()) {
                    String permName = st.nextToken();
                    if (permName.startsWith ("!")) {
                        if (user.hasPermission (permName.substring(1)))
                            deny = true;
                    } else if (!user.hasPermission (permName)) {
                        context.getSyslog().info (" user doesn't have " + permName);
                        deny = true;
                    }
                }
                if (deny) {
                    sendRedirect (context, request.getContextPath() 
                        + "/stop.html"
                    );
                }
            }
            return; // nothing to do
        }
        String originalUri = request.getRequestURI ();
        String queryString = request.getQueryString ();

        String loginUrl    = request.getContextPath() + "/login.html";
        if (queryString != null && queryString.length() > 0)
            originalUri += "?" + queryString;
        if (!originalUri.endsWith ("login.html"))
            context.getSession().setAttribute("redirect", originalUri);

        context.getSyslog().info ("  redirecting to " + loginUrl);
        sendRedirect (context, loginUrl);
    }
}

