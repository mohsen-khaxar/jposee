package org.jpos.qcluster;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class QBeanInfo implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private String node;

    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private String config;

    /** nullable persistent field */
    private long timestamp;

    /** nullable persistent field */
    private boolean active;

    /** full constructor */
    public QBeanInfo(String node, String name, String config, long timestamp, boolean active) {
        this.node = node;
        this.name = name;
        this.config = config;
        this.timestamp = timestamp;
        this.active = active;
    }

    /** default constructor */
    public QBeanInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNode() {
        return this.node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfig() {
        return this.config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
