package org.jpos.ee;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class UserRevisionEntry extends RevisionEntry implements Serializable {

    /** nullable persistent field */
    private org.jpos.ee.User user;

    /** full constructor */
    public UserRevisionEntry(Date date, String info, org.jpos.ee.User author, org.jpos.ee.User user) {
        super(date, info, author);
        this.user = user;
    }

    /** default constructor */
    public UserRevisionEntry() {
    }

    public org.jpos.ee.User getUser() {
        return this.user;
    }

    public void setUser(org.jpos.ee.User user) {
        this.user = user;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
