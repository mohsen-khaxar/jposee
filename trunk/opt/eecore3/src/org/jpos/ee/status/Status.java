package org.jpos.ee.status;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;


/** @author Hibernate CodeGenerator */
public class Status extends org.jpos.ee.status.StatusBase implements Serializable {

    /** identifier field */
    private String id;

    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private String state;

    /** nullable persistent field */
    private String detail;

    /** nullable persistent field */
    private String groupName;

    /** nullable persistent field */
    private Date lastTick;

    /** nullable persistent field */
    private long timeout;

    /** nullable persistent field */
    private String timeoutState;

    /** nullable persistent field */
    private String command;

    /** nullable persistent field */
    private String validCommands;

    /** nullable persistent field */
    private boolean expired;

    /** nullable persistent field */
    private int maxEvents;

    /** persistent field */
    private Set events;

    /** persistent field */
    private Set revisions;

    /** full constructor */
    public Status(String id, String name, String state, String detail, String groupName, Date lastTick, long timeout, String timeoutState, String command, String validCommands, boolean expired, int maxEvents, Set events, Set revisions) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.detail = detail;
        this.groupName = groupName;
        this.lastTick = lastTick;
        this.timeout = timeout;
        this.timeoutState = timeoutState;
        this.command = command;
        this.validCommands = validCommands;
        this.expired = expired;
        this.maxEvents = maxEvents;
        this.events = events;
        this.revisions = revisions;
    }

    /** default constructor */
    public Status() {
    }

    /** minimal constructor */
    public Status(String id, Set events, Set revisions) {
        this.id = id;
        this.events = events;
        this.revisions = revisions;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDetail() {
        return this.detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Date getLastTick() {
        return this.lastTick;
    }

    public void setLastTick(Date lastTick) {
        this.lastTick = lastTick;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getTimeoutState() {
        return this.timeoutState;
    }

    public void setTimeoutState(String timeoutState) {
        this.timeoutState = timeoutState;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getValidCommands() {
        return this.validCommands;
    }

    public void setValidCommands(String validCommands) {
        this.validCommands = validCommands;
    }

    public boolean isExpired() {
        return this.expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public int getMaxEvents() {
        return this.maxEvents;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public Set getEvents() {
        return this.events;
    }

    public void setEvents(Set events) {
        this.events = events;
    }

    public Set getRevisions() {
        return this.revisions;
    }

    public void setRevisions(Set revisions) {
        this.revisions = revisions;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
