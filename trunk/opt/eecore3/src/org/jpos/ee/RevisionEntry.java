package org.jpos.ee;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class RevisionEntry implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private Date date;

    /** nullable persistent field */
    private String info;

    /** nullable persistent field */
    private org.jpos.ee.User author;

    /** full constructor */
    public RevisionEntry(Date date, String info, org.jpos.ee.User author) {
        this.date = date;
        this.info = info;
        this.author = author;
    }

    /** default constructor */
    public RevisionEntry() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public org.jpos.ee.User getAuthor() {
        return this.author;
    }

    public void setAuthor(org.jpos.ee.User author) {
        this.author = author;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
