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

package org.jpos.ee;

import junit.framework.TestCase;
import org.hibernate.Transaction;
import org.joda.time.DateMidnight;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;


public class ThingsTestCase extends TestCase {
    DB db;
    Transaction tx;
    ThingsManager mgr;
    long now = System.currentTimeMillis() / 1000L * 1000L;
    public static final BigDecimal ONE_THOUSAND = new BigDecimal("1000.00");


    @Override
    public void setUp() throws Exception {
        db = new DB();
        db.open();
        mgr = new ThingsManager(db);
        tx = db.beginTransaction();
    }

    public void testCreateSchema () {
        db.createSchema (null, true);
    }

    public void testPut() {
        Date d = new Date(now);

        for (int i=0; i<10; i++) {
            Thing t = mgr.create ("Test");
            t.put ("String", i < 5 ? "First five" : "Greater than 5");
            t.put ("Long", Long.MAX_VALUE);
            t.put ("L", i);
            t.put ("Date", d);
            t.put ("Timestamp", new Timestamp(now));
            t.put ("BigDecimal", ONE_THOUSAND);
            t.putText("Text", "The quick brown fox is brown and the dog is lazy, and jumps.");
        }
    }

    public void testGet() {
        DateMidnight midnight = new DateMidnight(now);
        Thing t = (Thing) db.session().get (Thing.class, 2L);
        assertNotNull(t);
        assertEquals ("First five", t.getString ("String"));
        assertEquals (Long.MAX_VALUE, t.getLong ("Long"));
        assertEquals ("Date should be " + midnight, midnight, new DateMidnight (t.getDate ("Date").getTime()));
    }
    public void testGetByType () {
        List l = mgr.getAll ("Test");
        assertEquals (10, l.size());
    }
    public void testListByStringName() {
        List<Thing> l = mgr.listByStringName ("Test", "String");
        assertNotNull (l);
        assertEquals ("List size should be 10", 10, l.size());
    }
    public void testListByStringValue() {
        List<Thing> l1 = mgr.listByStringValue("Test", "First five");
        List<Thing> l2 = mgr.listByStringValue("Test", "Greater than 5");

        assertNotNull (l1);
        assertNotNull (l2);
        assertEquals ("l1 size should be 5", 5, l1.size());
        assertEquals ("l2 size should be 5", 5, l2.size());
    }
    public void testListByStringNameValue() {
        List<Thing> l = mgr.listByStringNameValue ("Test", "String", "First five");
        assertNotNull (l);
        assertEquals ("List size should be 5", 5, l.size());
    }
    public void testRemove () {
        List<Thing> l = mgr.listByStringValue("Test", "Greater than 5");
        for (Thing t : l) {
            db.delete(t);
        }
    }
    public void testListByTextValue() {
        List<Thing> l = mgr.listByTextValue("Test", "The quick brown fox is brown and the dog is lazy, and jumps.");
        assertEquals ("Size should be 5", 5, l.size());
    }
    public void testListByLongValue() {
        List<Thing> l = mgr.listByLongValue("Test", Long.MAX_VALUE);
        assertEquals ("Size should be 5", 5, l.size());
        l = mgr.listByLongValue("Test", 4);
        assertEquals ("Size should be 1", 1, l.size());
        l = mgr.listByLongValue("Test", 6);
        assertEquals ("Size should be 0", 0, l.size());
    }

    public void testListByDateValue() {
        DateMidnight midnight = new DateMidnight(now);
        List<Thing> l = mgr.listByDateValue("Test", new Date (midnight.getMillis()));
        Thing t = l.get(0);
        DateMidnight dbMidnight = new DateMidnight(t.getDate("Date").getTime());
        assertEquals ("Date should be " + midnight, midnight, dbMidnight);
    }
    public void testListByTimestampValue () {
        Timestamp dtnow = new Timestamp(now);
        List<Thing> l = mgr.listByTimestampValue("Test", dtnow);
        Thing t = l.get(0);
        Timestamp dbts = t.getTimestamp ("Timestamp");
        assertEquals ("Date should be " + dtnow, dtnow, dbts);
    }
    public void testListByBigDecimalName () {
        List<Thing> l = mgr.listByBigDecimalName("Test", "BigDecimal");
        assertEquals ("Size should be 5", 5, l.size()); // > 5 have been removed by testRemove()
    }

    public void testListByBigDecimalValue () {
        List<Thing> l = mgr.listByBigDecimalValue("Test", ONE_THOUSAND);
        Thing t = l.get(0);
        BigDecimal v = t.getBigDecimal ("BigDecimal");
        assertEquals ("BigDecimal should be " + ONE_THOUSAND, ONE_THOUSAND, v);
    }

    @Override
    public void tearDown() {
        tx.commit ();
        db.close ();
    }
}
