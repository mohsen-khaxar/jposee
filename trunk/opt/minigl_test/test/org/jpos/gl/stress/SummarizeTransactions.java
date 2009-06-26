package org.jpos.gl.stress;

import org.jpos.gl.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import org.hibernate.Transaction;

public class SummarizeTransactions extends TestBase {
    public void testSummarize() throws Exception {
        Date POSTDATE = Util.parseDateTime ("20050104120000");
        Journal tj = gls.getJournal ("TestJournal");
        System.out.println ("Creating summarized Transaction");
        Transaction tx = gls.beginTransaction();
        gls.summarize (tj, POSTDATE, POSTDATE, "Summarized Stress Txn", new short[] { 0 });
        tx.commit();
    }
}

