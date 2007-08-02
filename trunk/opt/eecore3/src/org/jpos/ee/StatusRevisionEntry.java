package org.jpos.ee;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jpos.ee.status.Status;


/** @author Hibernate CodeGenerator */
public class StatusRevisionEntry extends RevisionEntry implements Serializable {

    /** nullable persistent field */
    private Status status;

    /** full constructor */
    public StatusRevisionEntry(Date date, String info, org.jpos.ee.User author, Status status) {
        super(date, info, author);
        this.status = status;
    }

    /** default constructor */
    public StatusRevisionEntry() {
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
