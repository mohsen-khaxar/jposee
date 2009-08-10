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

package org.jpos.space;

import org.jpos.q2.QBeanSupport;
import org.jpos.space.Space;
import org.jpos.space.TSpace;
import org.jpos.space.SpaceFactory;
import org.jpos.core.ConfigurationException;
import org.jpos.util.NameRegistrar;
import org.jdom.Element;

/**
 * VoldemortSpaceAdaptor
 * @author Alejandro Revilla
 */
public class VoldemortSpaceAdaptor extends QBeanSupport {
    String spaceUri;
    VoldemortSpace sp;

    public VoldemortSpaceAdaptor () {
        super ();
    }
    public void initService() throws ConfigurationException {
        Element e = getPersist ();
        spaceUri = cfg.get ("space", "vdm:space");
    }
    public void startService() {
        String bootstrapUrl = cfg.get ("url", VoldemortSpace.BOOTSTRAP_URL);
        String storeName = cfg.get ("store", VoldemortSpace.BOOTSTRAP_URL);
        sp = new VoldemortSpace (bootstrapUrl, storeName);
        sp.setLogger (getLog().getLogger(), getName());
        NameRegistrar.register (spaceUri, sp);
        if (cfg.getBoolean ("garbage-collector", false))
            sp.startGC();
    }
    protected void stopService () throws Exception {
        NameRegistrar.unregister (spaceUri);
        if (sp != null)
            sp.stop();
    }
}

