package org.jpos.gl.stress;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jpos.gl.ImportTest;

public class StressTests {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest (new TestSuite (ImportTest.class));
        suite.addTest (new TestSuite (CreateAccounts.class));
        suite.addTest (new TestSuite (CreateTransactions.class));
        suite.addTest (new TestSuite (SummarizeTransactions.class));
        return suite;
    }
}

