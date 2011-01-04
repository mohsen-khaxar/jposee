/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2011 Alejandro P. Revilla
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

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jpos.util.NameRegistrar;
import org.jpos.q2.Q2;
import org.jpos.util.Profiler;

public class VoldemortSpaceTestCase extends TestCase {
    static Q2 q2;
    static VoldemortSpace sp;
    static VoldemortSpace sp1;
    static boolean doSetup = true;
    static boolean doTearDown = false;
    public void setUp() throws Exception {
        if (doSetup) {
            q2 = new Q2("../test/org/jpos/space");
            q2.start();
            waitFor ("voldemort-node-0", 15000L);
            waitFor ("voldemort-node-1", 15000L);
            waitFor ("vdm:space", 15000L);
            sp = (VoldemortSpace) NameRegistrar.get ("vdm:space");
            sp1 = new VoldemortSpace("tcp://localhost:6667", "vdmspace");
            doSetup = !doSetup;
        }
    }
    public void testOutAndPush() throws Exception {
        sp.out ("k", "b");
        assertEquals ("b", sp.rdp ("k"));
        assertEquals ("b", sp1.rdp ("k"));
        sp.out ("k", "c");
        assertEquals ("b", sp.rdp ("k")); // still b
        assertEquals ("b", sp1.rdp ("k")); // still b
        sp.push ("k", "a");
        assertEquals ("a", sp.rdp ("k")); // after push, should be a
        assertEquals ("a", sp1.rdp ("k")); // after push, should be a

        assertEquals ("a", sp.inp ("k")); 
        assertEquals ("b", sp1.inp ("k")); 
        assertEquals ("c", sp.inp ("k")); 
        assertNull (sp.rdp ("k")); 
        assertNull (sp.inp ("k")); 
    }
    public void testWithExpiration () throws Exception {
        sp.out ("k", "willExpire", 1000L);
        assertEquals ("willExpire", sp.rdp ("k"));
        Thread.sleep (1000L);
        assertNull (sp.rdp ("k")); 
        assertNull (sp.inp ("k")); 
        Thread.sleep (1000L);
    }
    public void testReadWithWait () throws Exception {
        new Thread() {
            public void run () {
                try {
                    Thread.sleep (1000L);
                    sp.out ("k", "e");
                } catch (Exception e) {
                    fail (e.getMessage());
                }
            }
        }.start();
        assertNull (sp.rdp ("k")); 
        assertNull (sp.rd ("k", 500L)); 
        assertEquals ("e", sp.rd ("k")); 
        assertEquals ("e", sp.in ("k")); 
        assertNull (sp.rdp ("k")); 
    }
    public void testMultipleReadersAndWriters() throws Exception {
        // create readers
        boolean running = true;
        int TRIES = 100;
        for (int i=0; i<TRIES; i++) {
            if (i % 2 == 0) {
                new Thread() {
                    public void run () {
                        String v = (String) sp1.in ("k");
                        sp.out ("k1", v);
                    }
                }.start();
            } else {
                new Thread() {
                    public void run () {
                        String v = (String) sp.in ("k");
                        sp1.out ("k1", v);
                    }
                }.start();
            }
        }
        for (int i=0; i<TRIES; i++) {
            sp.out ("k", Integer.toString(i));
        }
        // System.out.println ("Wait before pull");
        // Thread.sleep (10000L);
        // Pull the entries from k1
        for (int i=0; i<TRIES; i++) {
            System.out.println ("--------------------> " + i);
            if (i % 2 == 0)
                System.out.println (i + " [sp0] k1=" + sp.in ("k1"));
            else
                System.out.println (i + " [sp1] k1=" + sp1.in ("k1"));
        }
    }
    public void testGetAll () {
        List keys = new ArrayList();
        Profiler prof = new Profiler();
        for (int i=0; i<10000; i++) {
            String k = "K." + Integer.toString(i);
            keys.add (k);
            if (i % 10 == 0)
                sp.out (k, k);
        }
        prof.checkPoint ("Output 1000 entries out of 10000");
        Map m = sp.getClient().getAll (keys);
        prof.checkPoint ("getAll size is " + m.size());
        prof.dump (System.out, "");
        for (int i=0; i<10000; i++) {
            String k = "K." + Integer.toString(i);
            System.out.println (k + "=" + sp.inp (k));
        }
    }
    public void testDone() {
        doTearDown = true;
    }
    public void tearDown() throws Exception {
        if (doTearDown) {
            System.out.println ("waiting to tear down");
            Thread.sleep (5000L);
            q2.stop ();
        }
    }
    private void waitFor (String name, long maxWait) throws Exception {
        long end = System.currentTimeMillis() + maxWait;
        while (System.currentTimeMillis() < end) {
            if (NameRegistrar.getIfExists (name) != null)
                return;
            Thread.sleep (100L);
        }
        fail (name + " not available");
    }
}

