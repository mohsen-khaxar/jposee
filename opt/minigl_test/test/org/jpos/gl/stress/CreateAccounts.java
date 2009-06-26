package org.jpos.gl.stress;

import org.jpos.gl.*;
import org.hibernate.Transaction;

public class CreateAccounts extends TestBase {
    public void testCreateComposites () throws Exception {
        start ("testCreateComposites");
        CompositeAccount cards = gls.getCompositeAccount ("TestChart", "23");
        assertNotNull (cards);
        Transaction tx = gls.beginTransaction();
        for (int i=0; i<10; i++) {
            Account acct = new CompositeAccount ();
            acct.setCode ("23" + "." + Integer.toString (i));
            acct.setType (Account.CREDIT);
            acct.setDescription ("Group # " + i);
            gls.addAccount (cards, acct);
            gls.session().evict (acct);
        }
        end ("testCreateComposites");
        tx.commit();
        gls.session().evict (cards);
    }
    public void testCreateFinals () throws Exception {
        for (int i=0; i<10; i++) {
            createFinals (i);
        }
    }
    private void createFinals (int n) throws Exception {
        CompositeAccount parent = 
            gls.getCompositeAccount ("TestChart", "23." + n);

        assertNotNull (parent);
        Transaction tx = gls.beginTransaction();
        start ();
        for (int i=0; i<100; i++) {
            Account acct = new FinalAccount ();
            acct.setCode ("23" + "." + n + "." + Integer.toString (i));
            acct.setType (Account.CREDIT);
            acct.setDescription ("Card # " + acct.getCode());
            gls.addAccount (parent , acct);
            gls.session().evict (acct);
        }
        checkPoint ("pre-commit " + n);
        tx.commit();
        checkPoint ("commit     " + n);
        gls.session().evict (parent);
    }
}

