package edu.csus.ecs.pc2.core.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Profile information.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class Profile implements IElementObject, Serializable {

    private String name;

    private String description = "";

    private Date createDate = new Date();
    
    private ElementId elementId = null;
    
    private String profilePath = null;

    /**
     * 
     */
    private static final long serialVersionUID = -8261748390647739218L;

    /**
     * Unique identifier for this instance of Site.
     */
    private boolean active = true;

    public Profile(String name) {
        if (name == null){
            throw new IllegalArgumentException("Profile name must not be null");
        }
        elementId = new ElementId(new String(name));
        this.name = name;
    }

    public ElementId getElementId() {
        return elementId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getContestId() {
        return toString();
    }

    /**
     * Name or Title for the Profile.
     * @return
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Description for the profile.
     * @return
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean matchesIdentifier(String identifier) {
        return toString().equals(identifier);
    }

    public int getSiteNumber() {
        return elementId.getSiteNumber();
    }

    public void setSiteNumber(int siteNumber) {
        setSiteNumber(elementId.getSiteNumber());
    }

    public int versionNumber() {
        return elementId.getVersionNumber();
    }
    
    /**
     * Profile files location.
     * 
     * @return
     */
    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
    
    /**
     * @see Object#equals(java.lang.Object).
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof ElementId) {
            ElementId otherId = (ElementId) obj;
            return otherId.equals(elementId);
        } else if (obj instanceof Profile) {
            Profile otherProfile = (Profile) obj;
            return elementId.equals(otherProfile.getElementId());
        } else
            return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return toString().hashCode();
    }
}
