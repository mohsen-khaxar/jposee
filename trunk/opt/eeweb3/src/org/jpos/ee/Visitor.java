package org.jpos.ee;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Visitor extends org.jpos.ee.VisitorBase implements Serializable {

    /** identifier field */
    private String id;

    /** nullable persistent field */
    private Date lastUpdate;

    /** nullable persistent field */
    private org.jpos.ee.User user;

    /** persistent field */
    private Map props;

    /** full constructor */
    public Visitor(String id, Date lastUpdate, org.jpos.ee.User user, Map props) {
        this.id = id;
        this.lastUpdate = lastUpdate;
        this.user = user;
        this.props = props;
    }

    /** default constructor */
    public Visitor() {
    }

    /** minimal constructor */
    public Visitor(String id, Map props) {
        this.id = id;
        this.props = props;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public org.jpos.ee.User getUser() {
        return this.user;
    }

    public void setUser(org.jpos.ee.User user) {
        this.user = user;
    }

    public Map getProps() {
        return this.props;
    }

    public void setProps(Map props) {
        this.props = props;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
