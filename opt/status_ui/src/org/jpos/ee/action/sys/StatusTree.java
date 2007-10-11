/*
 *  jPOS Extended Edition
 *  Copyright (C) 2005 Alejandro P. Revilla
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

package org.jpos.ee.action.sys;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.jpublish.JPublishContext;
import org.jpublish.action.Action;
import com.anthonyeden.lib.config.Configuration;
import org.jpos.ee.action.ActionSupport;

import org.jpos.ee.DB;
import org.jpos.ee.Visitor;
import org.jpos.ee.status.StatusManager;
import org.jpos.util.DateUtil;
import org.jpos.util.Validator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;

public class StatusTree extends ActionSupport {
    public void execute (JPublishContext context, Configuration cfg) {
        int PAGE_SIZE = 25;
        HttpServletRequest request = context.getRequest();
        int page = getPage (request.getParameter ("page"));
        try {
            DB db = getDB (context);
            StatusManager mgr = new StatusManager (db);
            mgr.check();    // just in case heartbeat is not running

            Session hs = db.session();
            StringBuffer qs = new StringBuffer (
              "from status_tag in class org.jpos.ee.status.StatusTag"
            );
            qs.append (" group by tag");

            Query query = db.session().createQuery (qs.toString());
            context.put ("tags", query.iterate());
        } catch (Exception e) {
            error (context, e.getMessage());
            context.getSyslog().error (e);
        }
    }
}

