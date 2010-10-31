/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
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

package org.jpos.jruby;

import org.jdom.Element;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.Log;
import org.apache.bsf.BSFManager;

public class Ruby extends QBeanSupport implements Runnable {
    BSFManager mgr;
    public void initService() throws Exception {
        BSFManager.registerScriptingEngine(
            "jruby", "org.jruby.embed.bsf.JRubyEngine", new String[] { "rb" }
        );
        mgr = new BSFManager();
        mgr.declareBean("qbean", this, Ruby.class);
        mgr.declareBean("log", getLog(), Log.class);
    }
    public void startService() {
        new Thread (this).start ();
    }
    public void run () {
        Element config = getPersist();
        try {
            mgr.exec("jruby", getName(), 0, 0, config.getText());
        } catch (Throwable e) {
            getLog().warn (e);
        }
    }
}

