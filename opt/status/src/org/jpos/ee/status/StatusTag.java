package org.jpos.ee.status;

public class StatusTag {
    private long id;
    private String tag;
    private Status status;

    public StatusTag () {
        super ();
    }
    public StatusTag (String tag) {
        super ();
        setTag(tag);
    }
    /**
     * internal id 
     */
    public void setId (long id) {
        this.id = id;
    }
    /**
     * @return internal Id
     */
    public long getId() {
        return id;
    }
    public void setTag (String tag) {
        this.tag = tag;
    }
    public String getTag () {
        return tag;
    }
    public void setStatus (Status status) {
        this.status = status;
    }
    public Status getStatus () {
        return status;
    }
    public boolean equals (Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj instanceof StatusTag) {
            return getId() == ((StatusTag)obj).getId();
	}
	return false;
    }
    public String toString () {
        return new Long(getId()).toString();
    }
}

