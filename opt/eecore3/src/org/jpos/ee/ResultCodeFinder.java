package org.jpos.ee;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import org.hibernate.*;

/** Automatically generated Finder class for ResultCodeFinder.
 * @author Hibernate FinderGenerator  **/
public class ResultCodeFinder implements Serializable {

    public static List findByMnemonic(Session session, java.lang.String mnemonic) throws SQLException, HibernateException {
        Query q = session.createQuery (
            "from org.jpos.ee.ResultCode as resultcode where resultcode.mnemonic=:mnemonic"
        );
        q.setParameter ("mnemonic", mnemonic);
        return q.list();
    }

    public static List findAll(Session session) throws SQLException, HibernateException {
        return session.createQuery(
            "from ResultCode in class org.jpos.ee.ResultCode"
        ).list();
    }
}

