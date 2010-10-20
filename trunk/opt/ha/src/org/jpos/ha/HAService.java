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

package org.jpos.ha;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Iterator;

import org.jgroups.Channel;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.PullPushAdapter;
import org.jgroups.ChannelException;
import org.jgroups.MembershipListener;
import org.jgroups.conf.XmlConfigurator;

import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.core.Configuration;
import org.jpos.q2.QBeanSupport;

/**
 * @author Alejandro Revilla
 * @author Dave Bergert
 *
 * High Availability Service
 */
public class HAService extends QBeanSupport 
    implements MembershipListener, Runnable {
    Channel channel;
    PullPushAdapter adapter;
    String workQueue;
    Space sp;
    boolean master;

    public void initService () {
        workQueue = "HAService." + Integer.toString (this.hashCode());
        sp = SpaceFactory.getSpace();
        try {
            initChannel();
        } catch (Throwable t) {
            getLog().error (t);
        }
    }
    public void startService () {
        new Thread(this).start();
    }
    public void stopService () {
        sp.out (workQueue, cfg.get ("down"));
        channel.close ();
    }
    public void block() {
        getLog().info ("block");
    }
    public void suspect (Address suspectedMember) {
        getLog().info ("suspected member " + suspectedMember.toString());
    }
    public void run() {
        while (running()) {
            Object obj = sp.in (workQueue);
            if (obj instanceof String) {
                try {
                    String command = (String) obj;
                    if (command.length() > 0) {
                        getLog().info ("exec " + command);
                        Runtime.getRuntime().exec (command);
                    }
                } catch (Throwable t) {
                    getLog().warn (t);
                }
            } else if (obj instanceof View) {
                LogEvent evt = getLog().createLogEvent ("operator");
                View view = (View) obj;
                evt.addMessage ("HA Status " + view.toString());
                Logger.log (evt);
            }
        }
    }
    public void viewAccepted (View view) {
        LogEvent evt = getLog().createInfo ("view-accepted");
        evt.addMessage (view.toString());
        Address owner = (Address) view.getMembers().elementAt(0);
        if (channel.getLocalAddress().equals (owner)) {

     	    if (master)  {
                evt.addMessage ("MASTER -- Existing Master");
		sp.out (workQueue, cfg.get ("changed"));
                sp.out (workQueue, view);   // Send message to operator
    	    }
	    else {
                evt.addMessage ("MASTER -- New Master");
                sp.out (workQueue, cfg.get ("up"));
                sp.out (workQueue, view);   // Send message to operator
                master = true;
	    }
        }
        else {
            evt.addMessage ("SLAVE");
            sp.out (workQueue, cfg.get ("down"));
            master = false;
        }
        Logger.log (evt);
    }
    private void initChannel () throws ChannelException, IOException {
        InputStream config = new FileInputStream (cfg.get ("group-config"));
        XmlConfigurator conf = XmlConfigurator.getInstance (config);
        String props = conf.getProtocolStackString();
        getLog().info (props);
        channel = new JChannel (props);

        channel.setOpt(Channel.GET_STATE_EVENTS, Boolean.TRUE);
        channel.setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
        channel.connect (cfg.get ("group-name", this.getClass().getName()));
        adapter = new PullPushAdapter (channel, this);
        getLog().info ("member: " + channel.getLocalAddress().toString());
    }
}

