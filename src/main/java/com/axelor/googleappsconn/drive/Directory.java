package com.axelor.googleappsconn.drive;

import java.util.ArrayList;
import java.util.List;

import com.axelor.googleappsconn.sharing.SharingPermission;
import com.axelor.googleappsconn.utils.Utils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;

/**
 * This class represent a directory in the google drive. It contains list of
 * files in a directory and the folder as File in drive.
 */
public class Directory {
	
	List<GoogleFile> googleFiles;
	File folder;
	String directoryId;
	String directoryName;
	String parentId;
	boolean trashed;
	
	public Directory()	{		
	}
	/**
	 * This constructor is used when user creates a new directory.
	 * @param dirId String id got from drive after creating a new directory.
	 * @param dirName String name of directory.
	 * @param parentId
	 */
	public Directory(String dirId, String dirName, String parentId) {
		this.directoryId = dirId;
		this.directoryName = dirName;
		this.parentId = parentId;
	}
	/**
	 * This constructor is used to build the Directory object when sync is done.
	 * @param folder com.google.api.services.drive.model.File;
	 * @param driveService GoogleDrive
	 * @throws Exception
	 */
	public Directory(File folder, GoogleDrive driveService) throws Exception {
		
		this.folder = folder;
		this.directoryId = folder.getId();
		this.directoryName = folder.getTitle();
		if (folder.getParents().size() > 0) {
			this.parentId = folder.getParents().get(0).getId();			
		}
		googleFiles = new ArrayList<GoogleFile>();
		ChildList allFilesFromFolder = driveService.getService().children().list(folder.getId()).execute();
		if (allFilesFromFolder.getItems().size() > 0) {
			for (ChildReference singleChildFile : allFilesFromFolder.getItems()) {
				Drive.Files.Get filesFromChild = driveService.getService().files().get(singleChildFile.getId());
				File fileFromChild = filesFromChild.execute();
				GoogleFile googleFile;
				if (FileTypes.ALL_SUPPORTED_DRIVE().contains(
						fileFromChild.getMimeType())) {
					googleFile = new GoogleFile(fileFromChild);
					if(fileFromChild.getExplicitlyTrashed()!=null && fileFromChild.getExplicitlyTrashed().booleanValue())
						googleFile.setTrashed(true);
					else
						googleFile.setTrashed(false);
					googleFile.setFileId(fileFromChild.getId());
					googleFile.setFileName(fileFromChild.getTitle());
					googleFile.setFileType(fileFromChild.getMimeType());
					googleFile.setLastModified(Utils.getDateFormated(fileFromChild.getModifiedByMeDate()));
					if (fileFromChild.getFileSize() != null)
						googleFile.setFileSize(fileFromChild.getFileSize().longValue());
					else
						googleFile.setFileSize(fileFromChild.getQuotaBytesUsed().longValue());
					PermissionList perlist = driveService.getService().permissions()
							.list(fileFromChild.getId()).execute();
					List<Permission> permissionList = perlist.getItems();
					List<SharingPermission> sharingPermissions = new ArrayList<SharingPermission>();
					for(int i=0;i<permissionList.size();i++) {
						// skipping first permission for the user himself as b'cz he's the owner of file
						if(i==0) continue;  
						SharingPermission sharingPermission = new SharingPermission();
						sharingPermission.setName(permissionList.get(i).getName());
						sharingPermission.setRole(permissionList.get(i).getRole());
						sharingPermission.setType(permissionList.get(i).getType());
						sharingPermission.setPermissionId(permissionList.get(i).getId());
						sharingPermissions.add(sharingPermission);
					}
					googleFile.setSharingPermissions(sharingPermissions);
					googleFiles.add(googleFile);
				}
			}
		}
	}
	/**
	 * Gives the Root directory id for the current user from google drive.
	 * @param driveService
	 * @return
	 * @throws Exception
	 */
	public String getRootDirectoryId(GoogleDrive driveService) throws Exception	{
		About about=driveService.getService().about().get().execute();
  		return about.getRootFolderId();
	}
	public boolean isTrashed() {
		return trashed;
	}
	public void setTrashed(boolean trashed) {
		this.trashed = trashed;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getDirectoryId() {
		return directoryId;
	}
	public void setDirectoryId(String directoryId) {
		this.directoryId = directoryId;
	}
	public String getDirectoryName() {
		return directoryName;
	}
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}
	public List<GoogleFile> getGoogleFiles() {
		return googleFiles;
	}
	public void setGoogleFiles(List<GoogleFile> googleFiles) {
		this.googleFiles = googleFiles;
	}
	public File getFolder() {
		return folder;
	}
	public void setFolder(File folder) {
		this.folder = folder;
	}
}
