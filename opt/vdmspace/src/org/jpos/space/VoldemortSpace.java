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

package org.jpos.space;

import java.util.*;
import java.io.Serializable;

import voldemort.client.ClientConfig;
import voldemort.client.UpdateAction;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.versioning.Version;
import voldemort.versioning.Versioned;

import org.jpos.util.Log;

/**
 * VolcermortSpace
 * @author Alejandro Revilla
 */
public class VoldemortSpace<K,V> extends Log implements Space<K,V> {
    private StoreClient client;
    public  static final String BOOTSTRAP_URL   = "tcp://localhost:6666";
    public  static final String STORE_NAME      = "vdmspace";
    private static final long RELAX = 50L;
    private static final long ERROR_RELAX = 10000L;
    private static final long INITIAL_WAIT = 250L;
    private static final long WAIT_INCREMENT = 250L;
    private static final long MAX_WAIT = 5000L;
    private static final long DELETE_DELAY = 60000L;
    private static final int  MAX_INP_TRIES = 1000;
    private static final int  MAX_UPDATE_TRIES = 10000;

    private static final String GC_PREFIX = "$$GC.";
    private static final String GC_START = GC_PREFIX + "start";
    private static final long GC_RELAX = 60000L;
    private boolean running;

    public VoldemortSpace (String bootstrapUrl, String storeName) {
        super ();
        StoreClientFactory factory = 
            new SocketStoreClientFactory(
                new ClientConfig().setBootstrapUrls(bootstrapUrl)
            );
        client = (StoreClient<K,V>) factory.getStoreClient(storeName);
        running = true;
    }
    public VoldemortSpace () {
        this (BOOTSTRAP_URL, STORE_NAME);
    }
    // FIXME - remove
    public StoreClient getClient() {
        return client;
    }
    public void put (K key, V value) {
        out0 (key, value, 0L, Operation.Code.PUT);
    }
    public void out (K key, V value) {
        out0 (key, value, 0L, Operation.Code.OUT);
    }
    public void push (K key, V value) {
        out0 (key, value, 0L, Operation.Code.PUSH);
    }
    public void out (K key, V value, long timeout) {
        out0 (key, value, timeout, Operation.Code.OUT);
    }
    public void push (K key, V value, long timeout) {
        out0 (key, value, timeout, Operation.Code.OUT);
    }
    public boolean existAny (K[] keys) {
        for (K key : keys) {
            if (rdp(key) != null)
                return true;
        }
        return false;
    }
    public boolean existAny (K[] keys, long timeout) {
        long wait = INITIAL_WAIT;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((now = System.currentTimeMillis()) < end) {
            for (K key : keys) {
                if (rdp(key) != null)
                    return true;
            }
            if (now + wait > end) {
                wait = end - now;
                if (wait < 0L)
                    break;
            }
            try {
                Thread.sleep (wait);
            } catch (InterruptedException e) {
                throw new SpaceError (e);
            }
            wait = Math.min (wait + WAIT_INCREMENT, MAX_WAIT);
        }
        return false;
    }
    public V rdp (Object key) {
        Head head = (Head) client.getValue (key);
        if (head != null) {
            Ref ref = head.getNext (client, false, false);
            if (ref != null) 
                return (V) client.getValue (ref.getUUID());
        }
        return null;
    }
    public V inp (Object key) {
        return (V) inp0 (key, true, false);
    }
    public void gc (Object key) {
        inp0 (key, false, true);
    }
    public V rd (Object key) {
        long wait = INITIAL_WAIT;
        while (true) {
            V v = rdp (key);
            if (v != null)
                return v;
            try {
                Thread.sleep (wait);
                wait = Math.min (wait + WAIT_INCREMENT, MAX_WAIT);
            } catch (InterruptedException e) {
                throw new SpaceError (e);
            }
        }
    }
    public V rd (Object key, long timeout) {
        long wait = INITIAL_WAIT;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((now = System.currentTimeMillis()) < end) {
            V v = rdp (key);
            if (v != null)
                return v;
            if (now + wait > end) {
                wait = end - now;
                if (wait < 0L)
                    break;
            }
            try {
                Thread.sleep (wait);
            } catch (InterruptedException e) {
                throw new SpaceError (e);
            }
            wait = Math.min (wait + WAIT_INCREMENT, MAX_WAIT);
        }
        return null;
    }
    public V in (Object key) {
        long wait = INITIAL_WAIT;
        while (true) {
            V v = inp (key);
            if (v != null)
                return v;
            sleep (wait);
            wait = Math.min (
                wait 
                + WAIT_INCREMENT 
                + Math.abs(new Random().nextInt ()) % RELAX, 
                MAX_WAIT
            );
        }
    }
    public V in (Object key, long timeout) {
        long wait = INITIAL_WAIT;
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while ((now = System.currentTimeMillis()) < end) {
            V v = inp (key);
            if (v != null)
                return v;
            if (now + wait > end) {
                wait = end - now;
                if (wait < 0L)
                    break;
            }
            sleep (wait);
            wait = Math.min (wait + WAIT_INCREMENT, MAX_WAIT);
        }
        return null;
    }
    public void startGC () {
        new Thread (new GC()).start();
    }
    public void stop () {
        running = false;
    }
    private void out0 (
            Object key, Object value, long timeout, 
            Operation.Code opCode) 
    {
        if (timeout > 0) 
            gcLater (key, timeout);
        UUID uuid = UUID.randomUUID();
        client.put (uuid, value);
        Updater updater = new Updater (
            new Operation (opCode, key, new Ref (uuid, timeout))
        );
        if (!client.applyUpdate (updater, MAX_UPDATE_TRIES)) {
            client.delete (uuid);
            throw new SpaceError 
                ("Could not out key='" + key + "', value='" + value + "'");
        }
    }
    private Object inp0 (Object key, boolean delete, boolean purge) {
        Object value = null;
        for (int i=0; value == null && i<MAX_INP_TRIES; i++) {
            Versioned<Head> vHead = client.get (key);
            if (vHead == null)
                break;
            Head head = vHead.getValue();
            if (head != null) {
                Ref ref = head.getNext (client, delete, purge);
                if (putOrDelete (key, vHead, purge)) {
                    if (ref != null) {
                        UUID uuid = ref.getUUID ();
                        if (uuid != null) {
                            value = client.getValue (uuid);
                            client.delete (uuid);
                            if (!ref.hasTimeout ())
                                gcLater (key, DELETE_DELAY);
                        }
                    } 
                } 
            } 
        }
        return value;
    }
    private boolean putOrDelete 
        (Object key, Versioned<Head> vHead, boolean delete) 
    {
        if (delete && vHead.getValue().size() == 0) {
            client.delete (key, vHead.getVersion());
            return true;
        }
        else
            return client.putIfNotObsolete (key, vHead);
    }
    private void gcLater (Object key, long timeout) {
        String queue = getQueue(timeout);
        GCUpdater updater = new GCUpdater (queue, key);
        if (!client.applyUpdate (updater, MAX_UPDATE_TRIES)) {
            throw new SpaceError (
                "Could not register key "+ key + " for later check"
            );
        }
    }
    private void sleep (long timeout) {
        try {
            Thread.sleep (timeout);
        } catch (InterruptedException ignored) { }
    }
    public String getQueue (long timeout) {
        long expires = System.currentTimeMillis() + timeout;
        return GC_PREFIX + Long.toString(expires / 60000L);
    }

    private class GC implements Runnable {
        public void run() {
            long start = getStart();
            while (running) {
                try {
                    long end = getEnd();
                    if (start < end) {
                        while (start < end) {
                            processQueue (start++);
                        }
                        setStart (end);
                    }
                    sleep (GC_RELAX);
                } catch (Exception e) {
                    error (e);
                    sleep (ERROR_RELAX);
                }
            }
        }
        private long getEnd () {
            return (System.currentTimeMillis() / 60000L) - 1L;
        }
        private long getStart () {
            Long start = (Long) ((Space)VoldemortSpace.this).rdp (GC_START);
            return start != null ? start.longValue() : getEnd();
        }
        private void setStart (long start) {
            ((VoldemortSpace) VoldemortSpace.this).put (
                GC_START, start
            );
        }
        private void processQueue (long l) {
            String queue = GC_PREFIX + Long.toString (l);
            Versioned<Set> vSet = client.get (queue);
            if (vSet != null) {
                Set set = vSet.getValue();
                for (Object key : set) {
                    gc (key);
                    sleep (RELAX);
                }
                client.delete (queue);
            }
        }
    }
    public static class Operation {
        public enum Code {
            OUT,
            PUSH,
            PUT
        }
        Object key;
        Code code;
        Ref ref;
        long timeout;
        public Operation (Code code, Object key, Ref ref) {
            this (code, key, ref, 0L);
        }
        public Operation (Code code, Object key, Ref ref, long timeout) {
            this.key  = key;
            this.code = code;
            this.ref  = ref;
            this.timeout = timeout;
        }
        public Code getCode () {
            return code;
        }
        public Object getKey () {
            return key;
        }
        public UUID getUUID () {
            return ref.getUUID();
        }
        public Ref getRef() {
            return ref;
        }
        public long getTimeout() {
            return timeout;
        }
    }
    public static class Updater extends UpdateAction {
        Operation oper;
        Version version;
        public Updater (Operation oper) {
            this.oper = oper;
            this.version = null;
        }
        public Version getVersion() {
            return version;
        }
        public void update (StoreClient client) {
            Versioned<Head> vHead = client.get (oper.getKey());
            if (vHead == null)
                vHead = new Versioned<Head>(new Head());
            Head head = vHead.getValue();
            switch (oper.getCode()) {
                case OUT:
                    head.add (oper.getRef());
                    break;
                case PUSH:
                    head.push (oper.getRef());
                    break;
                case PUT:
                    head.clear (client);
                    head.put (oper.getRef());
                    break;
            }
            client.put (oper.getKey(), vHead);
            version = vHead.getVersion();
        }
    }
    public static class GCUpdater extends UpdateAction {
        String gcKey;
        Object key;
        public GCUpdater (String gcKey, Object key) {
            this.gcKey = gcKey;
            this.key = key;
        }
        public void update (StoreClient client) {
            Versioned<Set> vSet = client.get (gcKey);
            if (vSet == null)
                vSet = new Versioned<Set>(new HashSet());
            Set set = vSet.getValue();
            if (!set.contains (key)) {
                set.add (key);
                client.put (gcKey, vSet);
            }
        }
    }
    static class Head implements Serializable {
        static final long serialVersionUID = 6568713451896666394L;
        List<Ref> refs = new ArrayList<Ref>();
        public void add (Ref ref) {
            if (!refs.contains (ref))
                refs.add (ref);
        }
        public void push (Ref ref) {
            if (!refs.contains (ref))
                refs.add (0, ref);
        }
        public void put (Ref ref) {
            refs.clear();
            refs.add (ref);
        }
        public int size() {
            return refs.size();
        }
        public UUID rdp0 () {
            while (refs.size() > 0) {
                Ref ref = refs.remove (0);
                if (ref.isActive())
                    return ref.getUUID();
            }
            return null;
        }
        public void clear (StoreClient client) {
            for (Ref ref : refs) {
                client.delete (ref.getUUID());
            }
        }
        public Ref getNext (StoreClient client, boolean remove, boolean deep) {
            Iterator<Ref> iter = refs.iterator();
            Ref retRef = null;
            while (iter.hasNext() && (retRef == null || deep)) {
                Ref ref = iter.next();
                if (ref.isActive()) {
                    if (retRef == null) {
                        retRef = ref;
                        if (remove)
                            iter.remove();
                    }
                } else {
                    client.delete (ref.getUUID());
                    iter.remove();
                }
            }
            return retRef;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder(getClass().getName());
            sb.append ('@');
            sb.append (Integer.toHexString(hashCode()));
            sb.append ('[');
            int i=0;
            for (Ref ref : refs) {
                if (i > 0)
                    sb.append (",");
                sb.append (ref.toString());
            }
            sb.append (']');
            return sb.toString();
        }

    }
    static class Ref implements Serializable {
        UUID uuid;
        long expires;
        static final long serialVersionUID = -6335014902889649331L;

        public Ref (UUID uuid, long timeout) {
            super();
            setUUID (uuid);
            setTimeout (timeout);
        }
        public void setUUID (UUID uuid) {
            this.uuid = uuid;
        }
        public UUID getUUID () {
            return uuid;
        }
        public void setTimeout (long timeout) {
            expires = timeout > 0L ? 
                System.currentTimeMillis() + timeout : 0L;
        }
        public boolean hasTimeout () {
            return expires != 0L;
        }
        public boolean isExpired () {
            return expires > 0L && expires < System.currentTimeMillis ();
        }
        public boolean isActive () {
            return !isExpired();
        }
        public String toString() {
            return getClass().getName() 
                + "@" + Integer.toHexString(hashCode())
                + ":[uuid=" + uuid
                + (isExpired() ? ",expired" : 
                    (",expires-in=" + Long.toString (Math.max(0L, expires - System.currentTimeMillis()))))
                + "]";
        }
    }
}

