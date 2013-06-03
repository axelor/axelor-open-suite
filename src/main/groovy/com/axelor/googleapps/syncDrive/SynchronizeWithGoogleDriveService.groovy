package com.axelor.googleapps.syncDrive

import java.awt.print.Printable;
import java.text.DateFormat
import java.text.SimpleDateFormat

import javax.persistence.Query;

import org.apache.poi.OldFileFormatException;

import com.axelor.auth.db.User;
import com.axelor.db.JPA;

import com.axelor.apps.base.db.FilePermission;
import com.axelor.apps.base.db.GoogleDirectory;
import com.axelor.apps.base.db.GoogleFile;
import com.axelor.apps.base.db.UserProfile;
import com.axelor.googleapps.userutils.Utils
import com.axelor.apps.base.db.AppsCredentials;
import com.axelor.googleappsconn.drive.Directory;
import com.axelor.googleappsconn.drive.FileTypes;
import com.axelor.googleappsconn.drive.GoogleDrive;
import com.axelor.googleappsconn.sharing.SharedWithMe;
import com.axelor.googleappsconn.sharing.SharingOperation;
import com.axelor.googleappsconn.sharing.SharingPermission
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

class SynchronizeWithGoogleDriveService {

	@Inject GoogleDrive googleDrive
	@Inject Utils userUtils
	@Transactional
	/**
	 * synchronize the files and directories in database from google Drive for currentUser
	 * @param driveuser User
	 * @return
	 */
	public String synchronizeDrive(User driveuser) {
		
		userUtils.startApps(driveuser)
		List<GoogleFile> listQueryGoogleFile = GoogleFile.all().filter("driveUser=?", driveuser).fetch()
		List<GoogleDirectory> listQueryGoogleDirectory = GoogleDirectory.all().filter("driveUser=?", driveuser).fetch()
		List<String> listOldFileId = new ArrayList<String>()

		for(GoogleFile fileObj:listQueryGoogleFile) {
			listOldFileId.add(fileObj.getFileId())
		}
		
		List<String> listOldDirId = new ArrayList<String>()
		for(GoogleDirectory dirObj:listQueryGoogleDirectory) {
			listOldDirId.add(dirObj.getDirectoryId())
		}
		
		// create root folder if it does not exist for current user
		checkAndFillRootDirectory(driveuser)
		//Start sync here
		// First fetch All Directories from Google Drive
		List<Directory> listDirectory = googleDrive.getDirectories()
		
		for(Directory directory : listDirectory) {
		
			if(listOldDirId.contains(directory.getFolder().getId())) {
				// Directory available in list
				if(directory.isTrashed() == true) {
					GoogleDirectory trashedDirectory = GoogleDirectory.all().filter("driveUser=? and directoryId=?", driveuser, directory.getFolder().getId()).fetchOne()
					trashedDirectory.setTrashed(true)
					trashedDirectory.merge()
					listOldDirId.remove(directory.getFolder().getId())
				} else {
					GoogleDirectory trashedDirectory = GoogleDirectory.all().filter("driveUser=? and directoryId=?", driveuser, directory.getFolder().getId()).fetchOne()
					trashedDirectory.setTrashed(false)
					trashedDirectory.merge()
					listOldDirId.remove(directory.getFolder().getId())
				}
				
			}
			else {
					GoogleDirectory googleDirectory = new GoogleDirectory()
					googleDirectory.setDirectoryId(directory.getFolder().getId())
					googleDirectory.setDriveUser(driveuser)
					googleDirectory.setDirectoryName(directory.getFolder().getTitle())
					googleDirectory.setTrashed(directory.isTrashed())
					googleDirectory.persist()				
			}
			
			for(com.axelor.googleappsconn.drive.GoogleFile googleFileDrive : directory.getGoogleFiles()) {
			
				if(listOldFileId.contains(googleFileDrive.getFileId())) {
					// Here Available --- Merge the meta-data for the file
				
					if(googleFileDrive.isTrashed() == true) {
						GoogleFile availableFile = GoogleFile.all().filter("fileId=?",googleFileDrive.getFileId()).fetchOne()
						availableFile.setLastModified(googleFileDrive.getLastModified())
						availableFile.merge()
						GoogleFile trashedFile = GoogleFile.all().filter("driveUser=? and fileId=?", driveuser,googleFileDrive.getFileId()).fetchOne()
						trashedFile.setTrashed(true)
						trashedFile.merge()
						listOldFileId.remove(googleFileDrive.getFileId())
					} else {
						GoogleFile trashedFile = GoogleFile.all().filter("driveUser=? and fileId=?", driveuser,googleFileDrive.getFileId()).fetchOne()
						trashedFile.setTrashed(false)
						trashedFile.merge()
						listOldFileId.remove(googleFileDrive.getFileId())
					}
					
				} else {
					GoogleDirectory GoogleDirectoryQuery = GoogleDirectory.all().filter("directoryId=? and driveUser=?", directory.getFolder().getId(),driveuser).fetchOne()

					GoogleFile googleFile = new GoogleFile()
					UserProfile userShare
					googleFile.setFileId(googleFileDrive.getFileId())
					googleFile.setFileName(googleFileDrive.getFileName())
					googleFile.setFileType(googleFileDrive.getFileType())
					googleFile.setFileSize(googleFileDrive.getFileSize()?.longValue().toString()+" Bytes")
					googleFile.setLastModified(googleFileDrive.getLastModified());
					googleFile.setFileContent("Not Available Yet")
					googleFile.setDriveUser(driveuser)
					googleFile.setGoogleDirectory(GoogleDirectoryQuery)
					googleFile.setTrashed(GoogleDirectoryQuery.getTrashed())
					// for sharing-users of this file
					List<FilePermission> listFilePermissions = new ArrayList<FilePermission>()
					
					if(googleFileDrive.getSharingPermissions() != null) {
						String permissionId
					
						for(SharingPermission sharingPermission : googleFileDrive.getSharingPermissions()) {
							FilePermission permission = new FilePermission()
							permissionId = sharingPermission.getPermissionId()
							
							userShare = UserProfile.all().filter("permissionId=?",permissionId).fetchOne()
						
							if(userShare == null) {
								continue
							}
							permission.setSharingUser(userShare)
							
							if(sharingPermission.getRole().equals("writer"))
								permission.setEditable(true)
							else
								permission.setEditable(false)
							
							permission.setGoogleFile(googleFile)
							permission.persist()
							listFilePermissions.add(permission)
						}
					}
					googleFile.setFilePermissions(listFilePermissions)
					googleFile.persist()

				}
			}
		}
		
		// setting the parent reference for directories
		for(Directory directory : listDirectory) {
		
			if(directory.getDirectoryId() == null || directory.getParentId() == null)
				continue
			
			GoogleDirectory googleDirectory = GoogleDirectory.all().filter("directoryId=? and driveUser=?",directory.getDirectoryId(),driveuser).fetchOne()
			GoogleDirectory parentDirectory = GoogleDirectory.all().filter("directoryId=? and driveUser=?",directory.getParentId(),driveuser).fetchOne()
			googleDirectory.setParent(parentDirectory)
			googleDirectory.merge()			
		}
		
		// get all files existing in root directory in googleDrive
		GoogleDirectory rootDir = GoogleDirectory.all().filter("driveUser=? and directoryName=?", driveuser,"Root").fetchOne()
		List<com.axelor.googleappsconn.drive.GoogleFile> listRootFiles = googleDrive.getOuterGoogleFiles()
		
		for(com.axelor.googleappsconn.drive.GoogleFile outerGoogleFile : listRootFiles) {
		
			if(listOldFileId.contains(outerGoogleFile.getFileId())) {
				// if the file exist in database
			
				if(outerGoogleFile.isTrashed() == true) {
					GoogleFile availableFile = GoogleFile.all().filter("fileId=?",outerGoogleFile.getFileId()).fetchOne()
					availableFile.setLastModified(outerGoogleFile.getLastModified())
					availableFile.merge()					
					GoogleFile trashedFile = GoogleFile.all().filter("driveUser=? and fileId=?", driveuser,outerGoogleFile.getFileId()).fetchOne()
					trashedFile.setTrashed(true)
					trashedFile.merge()					
					listOldFileId.remove(outerGoogleFile.getFileId())
				} else {
					GoogleFile trashedFile = GoogleFile.all().filter("driveUser=? and fileId=?", driveuser,outerGoogleFile.getFileId()).fetchOne()
					trashedFile.setTrashed(false)
					trashedFile.merge()
					listOldFileId.remove(outerGoogleFile.getFileId())
				}
				
			} else { // the file to be added in root directory
				
				GoogleFile rootGoogleFile = new GoogleFile()
				UserProfile userShare
				rootGoogleFile.setFileId(outerGoogleFile.getFileId())
				rootGoogleFile.setFileName(outerGoogleFile.getFileName())
				rootGoogleFile.setFileSize(outerGoogleFile.getFileSize()?.longValue().toString() + " Bytes")
				rootGoogleFile.setFileType(outerGoogleFile.getFileType())
				rootGoogleFile.setLastModified(outerGoogleFile.getLastModified());
				rootGoogleFile.setDriveUser(driveuser)
				rootGoogleFile.setGoogleDirectory(rootDir)
				// for sharing-users of this file
				List<FilePermission> listFilePermissions = new ArrayList<FilePermission>()
				
				if(outerGoogleFile.getSharingPermissions() != null) {
					String permissionId
				
					for(SharingPermission sharingPermission : outerGoogleFile.getSharingPermissions()) {
						FilePermission permission = new FilePermission()
						permissionId = sharingPermission.getPermissionId()
						
						userShare = UserProfile.all().filter("permissionId=?",permissionId).fetchOne()
					
						if(userShare == null) {
							continue;
						}
						
						permission.setSharingUser(userShare)
						
						if(sharingPermission.getRole().equals("writer")) {
							permission.setEditable(true)
							rootGoogleFile.setWritable(true)
						} else {
							permission.setEditable(false)
							rootGoogleFile.setWritable(false)
						}
						
						permission.setGoogleFile(rootGoogleFile)
						permission.persist()
						listFilePermissions.add(permission)
					}
				}
				rootGoogleFile.setFilePermissions(listFilePermissions)
				rootGoogleFile.persist()
			}
		}
		
//---->	// sync the SharedWithMe files
		List<GoogleFile> dbFilesSharedWithMe = GoogleFile.all().filter("sharedWithMe=? and driveUser=?","true",driveuser).fetch()
		UserProfile shareUser = UserProfile.all().filter("driveUser=?",driveuser).fetchOne()
		List<SharedWithMe> filesSharedWithMe = new SharingOperation(googleDrive).getAllSharedWithMeFiles();
		List<String> oldFilesList = new ArrayList<String>()
		
		dbFilesSharedWithMe.each {
			oldFilesList.add(it.getFileId())
		}
		
		for(SharedWithMe sharedFile : filesSharedWithMe) {
		
			if(listOldFileId.contains(sharedFile.getGoogleFile().getFileId())) {
				listOldFileId.remove(sharedFile.getGoogleFile().getFileId())
			} else {
				GoogleFile dbFile = new GoogleFile()
				dbFile.setDriveUser(driveuser)
				dbFile.setFileId(sharedFile.getGoogleFile().getFileId())
				dbFile.setFileName(sharedFile.getGoogleFile().getFileName())
				dbFile.setFileSize(sharedFile.getGoogleFile().getFileSize()?.longValue() + " Bytes")
				dbFile.setFileType(sharedFile.getGoogleFile().getFileType())
				dbFile.setLastModified(sharedFile.getGoogleFile().getLastModified())
				dbFile.setSharedWithMe(true)
				dbFile.setSharedBy(sharedFile.getSharedBy())
				
				if(sharedFile.getRole()?.equals("writer"))
					dbFile.setWritable(true)
				else
					dbFile.setWritable(false)
				
				dbFile.persist()
			}
		}
		// Delete all Files Which are not Available in Google Drive and it exist in Google Apps
		deleteFileFromGoogleApps(listOldFileId, driveuser)
		deleteDirectoryFromGoogleApps(listOldDirId, driveuser)
		deleteSharedWithMEFileFromGoogleApps(listOldFileId, driveuser)
		
		// storage space statistics
		Map<String,Long> aboutMap = googleDrive.getAbout()
		Long usedLong = aboutMap.getAt("used")
		Long totalLong = aboutMap.getAt("total")
		double used = usedLong / (1024*1024)
		double total = totalLong / (1024*1024)
		double free = total - used
		used = used.round(2)
		total = total.round(2)
		free = free.round(2)
		String storageStats = "<table border=1><tr><td><font color='#008000'>Used&nbsp;</font></td><td>$used MB</td></tr><tr><td><font color='#008000'>Free&nbsp;</font></td><td> $free MB </td></tr><tr><td><font color='#008000'>Total&nbsp;</font></td><td> $total MB </td></tr></table>"
		
		return "Sync successfull <br/> The Statistics of storage space on Google Drive are as : <br/> $storageStats "
	}
	
	@Transactional
	public void deleteFileFromGoogleApps(List<String> listFileIdToDelete, User driveUser) {
	
		for(String fileId : listFileIdToDelete) {
			if(fileId != null) {
				GoogleFile.all().filter("driveUser=? and fileId=?",driveUser,fileId).fetchOne()?.remove()}
		}
		
	}
	@Transactional
	public void deleteSharedWithMEFileFromGoogleApps(List<String> listFileIdToDelete, User driveUser) {
		
		for(String fileId : listFileIdToDelete) {
			if(fileId != null) {
				GoogleFile.all().filter("driveUser=? and fileId=?",driveUser,fileId).fetchOne()?.remove()}
		}
		
	}
	@Transactional
	public void deleteDirectoryFromGoogleApps(List<String> listDirectoryIdToDelete, User driveUser){
		
		userUtils.startApps(driveUser)
		GoogleDirectory checkRoot = GoogleDirectory.all().filter("driveUser=? and directoryName=?", driveUser,"Root").fetchOne()
		String rootDirId = new Directory().getRootDirectoryId(googleDrive)
		
		for(String directoryId : listDirectoryIdToDelete) {
			if(directoryId != null && rootDirId != directoryId) {
				GoogleDirectory.all().filter("driveUser=? and directoryId=?",driveUser,directoryId).fetchOne()?.remove()}
		}
		
	}
	@Transactional
	public GoogleDirectory checkAndFillRootDirectory(User driveuser) {
		
		userUtils.startApps(driveuser)
		GoogleDirectory checkRoot = GoogleDirectory.all().filter("driveUser=? and directoryName=?", driveuser,"Root").fetchOne()
		
		if(checkRoot == null) {
			GoogleDirectory createRoot = new GoogleDirectory()
			String rootDirId = new Directory().getRootDirectoryId(googleDrive)
			createRoot.setDirectoryId(rootDirId)
			createRoot.setDirectoryName("Root")
			createRoot.setDriveUser(driveuser)
			createRoot.persist()
			checkRoot = createRoot
		}
		
		return checkRoot
	}
}
