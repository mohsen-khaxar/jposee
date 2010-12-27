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

package org.jpos.qcluster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import org.jpos.ee.DB;
import org.jpos.q2.QBeanSupport;
import org.jpos.iso.ISOUtil;
import org.jpos.util.DateUtil;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.HibernateException;

public class QCluster extends QBeanSupport implements Runnable {
    DB db;
    long interval;
    String node;
    File deployDir;

    public void initService () {
        db = new DB();
        interval = cfg.getLong ("interval", 15000L);
        node = cfg.get ("node", "undefined");
        deployDir = getServer().getDeployDir();
    }
    public void startService() {
        new Thread (this).start();
    }
    public void run() {
        long start = System.currentTimeMillis();
        long lastUpdate = 0L;
        while (running()) {
            try {
                db.open ();
                lastUpdate = scan (node, lastUpdate, false);
                lastUpdate = scan (node, lastUpdate, true);
            } catch (Throwable t) {
                getLog().error (t);
            } finally {
                close();
            }
            ISOUtil.sleep (interval);
        }
    }
    private void close() {
        try {
            db.close();
        } catch (HibernateException e) {
            getLog().error (e);
        }
    }
    private long scan (String node, long lastUpdate, boolean deploy) 
        throws HibernateException, IOException
    {
        Query q = db.session().createQuery (
            "from qbean in class org.jpos.qcluster.QBeanInfo where"
            + " active = :active and timestamp > :lastupdate"
            + " and ((node like :nodename) or (node = '*'))"
        );
        q.setParameter ("active", deploy ? "1" : "0");
        q.setParameter ("lastupdate", Long.toString(lastUpdate));
        q.setParameter ("nodename", node);
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
            QBeanInfo qbean = (QBeanInfo) iter.next();
            if (qbean.getTimestamp () > lastUpdate)
                lastUpdate = qbean.getTimestamp();
            if (deploy)
                deploy (qbean);
            else
                undeploy (qbean);
        }
        return lastUpdate;
    }
    private void deploy (QBeanInfo qbean) throws IOException {
        File tmp = new File (deployDir, qbean.getName() + ".tmp");
        File run = new File (deployDir, qbean.getName() + ".xml");
        FileOutputStream f = new FileOutputStream (tmp);
        f.write (qbean.getConfig().getBytes ("UTF-8"));
        f.close();
        tmp.renameTo (run);
    }
    private void undeploy (QBeanInfo qbean) throws IOException {
        File run = new File (deployDir, qbean.getName() + ".xml");
        run.delete ();
    }
}

