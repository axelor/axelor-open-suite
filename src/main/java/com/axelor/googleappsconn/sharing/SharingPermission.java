package com.axelor.googleappsconn.sharing;

/**
 * Class used to set the data if file sharing permission when user share a file to
 * multiple users.
 */
public class SharingPermission {
	
	String emailId;
	String role;
	String type;
	String name;
	String permissionId;
	Boolean notifyEmail;
	
	public SharingPermission() {
	}
	public SharingPermission(String emailId, String role, String type) {
		this.emailId = emailId;
		this.role = role;
		this.type = type;
	}
	public Boolean getNotifyEmail() {
		return notifyEmail;
	}
	public void setNotifyEmail(Boolean notifyEmail) {
		this.notifyEmail = notifyEmail;
	}
	public String getName() {
		return name;
	}
	public String getPermissionId() {
		return permissionId;
	}
	public void setPermissionId(String permissionId) {
		this.permissionId = permissionId;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
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
}
