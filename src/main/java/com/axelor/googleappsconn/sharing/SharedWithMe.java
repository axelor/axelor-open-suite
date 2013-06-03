package com.axelor.googleappsconn.sharing;

import com.axelor.googleappsconn.drive.GoogleFile;

/**
 * Class used to pass the four parameters of the files shared with Current user
 * by other users.
 */
public class SharedWithMe {

	GoogleFile googleFile;
	String sharedBy;
	String sharedDate;
	String role;
	String type;
	
	public SharedWithMe(GoogleFile googleFile, String sharedBy, String sharedDate) {
		this.googleFile = googleFile;
		this.sharedBy = sharedBy;
		this.sharedDate = sharedDate;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public GoogleFile getGoogleFile() {
		return googleFile;
	}
	public void setGoogleFile(GoogleFile googleFile) {
		this.googleFile = googleFile;
	}
	public String getSharedBy() {
		return sharedBy;
	}
	public void setSharedBy(String sharedBy) {
		this.sharedBy = sharedBy;
	}
	public String getSharedDate() {
		return sharedDate;
	}
	public void setSharedDate(String sharedDate) {
		this.sharedDate = sharedDate;
	}
}
