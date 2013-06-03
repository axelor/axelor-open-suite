package com.axelor.googleappsconn.drive;

import java.util.List;
import com.axelor.googleappsconn.sharing.SharingPermission;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.model.File;

/**
 * This class represent the file from the google drive and contains the
 * properties of a file from google drive.
 */
public class GoogleFile {

	File gFile;
	String fileId;
	String fileName;
	String fileType;
	long fileSize;
	InputStreamContent mediaContent;
	List<SharingPermission> sharingPermissions;
	String lastModified;
	boolean trashed;

	public boolean isTrashed() {
		return trashed;
	}
	public void setTrashed(boolean trashed) {
		this.trashed = trashed;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	public List<SharingPermission> getSharingPermissions() {
		return sharingPermissions;
	}
	public void setSharingPermissions(List<SharingPermission> sharingPermissions) {
		this.sharingPermissions = sharingPermissions;
	}
	public GoogleFile(File gFile) {
		this.gFile = gFile;
	}
	public File getgFile() {
		return gFile;
	}
	public void setgFile(File gFile) {
		this.gFile = gFile;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public InputStreamContent getMediaContent() {
		return mediaContent;
	}
	public void setMediaContent(InputStreamContent mediaContent) {
		this.mediaContent = mediaContent;
	}
}