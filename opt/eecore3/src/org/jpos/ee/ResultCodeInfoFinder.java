package org.jpos.ee;

import java.io.Serializable;
import java.util.List;
import java.sql.SQLException;

import org.hibernate.*;
import org.hibernate.type.Type;

/** Automatically generated Finder class for ResultCodeInfoFinder.
 * @author Hibernate FinderGenerator  **/
public class ResultCodeInfoFinder implements Serializable {

    public static List findAll(Session session) throws SQLException, HibernateException {
        return session.createQuery(
            "from ResultCodeInfo in class org.jpos.ee.ResultCodeInfo"
        ).list();
    }
}
