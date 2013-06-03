package com.axelor.googleapps.directory

import com.axelor.auth.db.User
import com.axelor.db.JPA;
import com.axelor.apps.base.db.GoogleDirectory;
import com.axelor.apps.base.db.GoogleFile
import com.axelor.googleapps.userutils.Utils
import com.axelor.googleappsconn.directory.DirectoryOperations
import com.axelor.googleappsconn.drive.Directory
import com.axelor.googleappsconn.drive.GoogleDrive
import com.axelor.apps.base.db.AppsCredentials
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


class DirectoryService {

	@Inject Utils userUtils
	@Inject GoogleDrive googleDrive
	@Transactional
	public String removeDirectories(List<String> listDirIds, User currentUser) {
		
		int sizeToRemove = listDirIds.size()
		int sizeRemoved = 0
		if(sizeToRemove == 0)
			return "Please, First select the directories to delete."
		userUtils.startApps(currentUser)
		DirectoryOperations dirOp = new DirectoryOperations(googleDrive)
		boolean removed
		String message = ""
		
		for(String dirId : listDirIds) {
			GoogleDirectory directory = GoogleDirectory.all().filter("id=?", dirId).fetchOne()
			if (directory.getDirectoryName().equals("Root")) {
				message = "<FONT Color='#FF0000'> The Directory Removal Failed ! </FONT>"
				continue
			}
			removed = dirOp.removeDirectory(directory.getDirectoryId())
			
			if (removed) {				
				removeInnerDirectoryWithFiles(new Long(dirId))
				directory?.remove()
				sizeRemoved++
			} else {
				message = "<FONT Color='#FF0000'> The Directory Removal Failed ! </FONT>"
				break
			}
		}
		if(message.equals(""))
			message = sizeRemoved +" of " + sizeToRemove + " Directories Removed Successfully with all its child directories and files !"
		return message
	}

	public GoogleDirectory createDirectoryInDrive(User currentUser, GoogleDirectory newDirectory) {
		
		userUtils.startApps(currentUser)
		Directory driveDirectory = null
		String directoryId = ""
		if(newDirectory.getParent() == null)
			directoryId = "Root_" + currentUser.getName()
		else
			directoryId = newDirectory.getParent().getDirectoryId()
		driveDirectory = new DirectoryOperations(googleDrive).createDirectory(newDirectory.getDirectoryName(),directoryId)
		newDirectory.setDirectoryId(driveDirectory.getDirectoryId())
		newDirectory.setDirectoryName(driveDirectory.getDirectoryName())
		newDirectory.setDriveUser(currentUser)
		return newDirectory
	}
	@Transactional
	public String trashDirectories(User currentUser, List<String> listDirectoryId) {
		
		int sizeToTrash = listDirectoryId.size()
		int sizeTrashed = 0
		if(sizeToTrash == 0)
			return "Please, first select the directories to trash."
		userUtils.startApps(currentUser)
		DirectoryOperations dirOp = new DirectoryOperations(googleDrive)
		String message = ""
		boolean trashed
		GoogleDirectory oneDirRemoved = null
		
		for(String dirId : listDirectoryId) {
			GoogleDirectory directory = GoogleDirectory.all().filter("id=?", dirId).fetchOne()
			if(directory.getDirectoryName().equals("Root"))
				continue;
			oneDirRemoved = directory
			trashed = dirOp.trashDirectory(directory.getDirectoryId())
			
			if (trashed) {
				trashInnerDirectoryWithFiles(directory.getId())
				directory.setTrashed(true)
				directory.merge()				
				sizeTrashed++
			}
		}
		if(sizeTrashed==1)
			message = "Directory <FONT Color='#01DF3A'> " + oneDirRemoved.getDirectoryName() + " </FONT>moved to trash Successfully with all its child directories and files !"
		else
			message = "<FONT Color='#01DF3A'>"+ sizeTrashed + "</FONT> out of <FONT Color='#01DF3A'> " + sizeToTrash + " </FONT> directories moved to trash Successfully with all their child directories and files!"
		return message
	}

	@Transactional
	public String untrashDirectories(User currentUser, List<String> listDirectoryId) {
		
		int sizeToRestore = listDirectoryId.size()
		int sizeRestored = 0
		boolean restored
		if(sizeToRestore == 0)
			return " Please, first select the directories to Restore."
		GoogleDirectory restoredDirectory
		String message
		userUtils.startApps(currentUser)
		DirectoryOperations dirOp = new DirectoryOperations(googleDrive)
		
		for(String dirId : listDirectoryId) {
			GoogleDirectory directory = GoogleDirectory.all().filter("id=?", dirId).fetchOne()
			restoredDirectory = directory
			restored = dirOp.untrashDirectory(directory.getDirectoryId())
			
			if(restored) {
				untrashInnerDirectoryWithFiles(directory.getId())
				directory.setTrashed(false)
				directory.merge()
				sizeRestored++
			}
		}
		if(sizeRestored == 1)
			message = " Directory <FONT Color='#01DF3A'> " + restoredDirectory.getDirectoryName() + " </FONT> restored successfully with all its child directories and files !"
		else
			message = sizeRestored + " out of " + sizeToRestore + " directories restored Successfully with all their child directories and files !"
		return message
	}

	@Transactional
	private void trashInnerDirectoryWithFiles(Long dirId) {
		
		JPA.em().createQuery("update GoogleFile a set a.trashed=true where a.googleDirectory="+dirId.toString()).executeUpdate()
		List<GoogleDirectory> listDirs = GoogleDirectory.all().filter("parent="+dirId).fetch()
		if(listDirs == null || listDirs.size() == 0)
			return
			
		for(GoogleDirectory directory : listDirs) {
			directory.setTrashed(true)
			directory.merge()
			trashInnerDirectoryWithFiles(directory.getId())
		}
	}
	@Transactional
	private void untrashInnerDirectoryWithFiles(Long dirId) {
		
		JPA.em().createQuery("update GoogleFile a set a.trashed=false where a.googleDirectory="+dirId.toString()).executeUpdate()
		List<GoogleDirectory> listDirs = GoogleDirectory.all().filter("parent="+dirId).fetch()
		if(listDirs == null || listDirs.size() == 0)
			return
			
		for(GoogleDirectory directory : listDirs){
			directory.setTrashed(false)
			directory.merge()
			untrashInnerDirectoryWithFiles(directory.getId())
		}
	}
	@Transactional
	private void removeInnerDirectoryWithFiles(Long dirId) {
		
		JPA.em().createQuery("delete from GoogleFile a where a.googleDirectory="+dirId.toString()).executeUpdate()
		List<GoogleDirectory> listDirs = GoogleDirectory.all().filter("parent="+dirId).fetch()
		if(listDirs == null || listDirs.size() == 0)
			return
			
		for(GoogleDirectory directory : listDirs) {
			removeInnerDirectoryWithFiles(directory.getId())
			directory.remove()
		}
	}
	public GoogleDirectory updateDirectory(User currentUser, GoogleDirectory googleDirectory) {
		
		userUtils.startApps(currentUser)
		GoogleDirectory oldDirectory = GoogleDirectory.all().filter("driveUser=? and directoryId=?", currentUser ,googleDirectory.getDirectoryId()).fetchOne()
		if(!googleDirectory.getDirectoryName().equals(oldDirectory.getDirectoryName())) {
			new DirectoryOperations(googleDrive).renameDirectory(googleDirectory.getDirectoryId(), googleDirectory.getDirectoryName())
		}
		return googleDirectory
	}

	@Transactional
	public String deleteDirectoriesFromTrash(User currentUser, List<String> listDirectoryId) {
		
		int sizeToDelete = listDirectoryId.size()
		int sizeDeleted = 0
		if(sizeToDelete == 0)
			return "Please, first select the directories to delete."
		userUtils.startApps(currentUser)
		DirectoryOperations dirOp = new DirectoryOperations(googleDrive)
		String message = ""
		boolean deleted = false
		GoogleDirectory oneDirRemoved = null
		
		for(String dirId : listDirectoryId) {
			GoogleDirectory directory = GoogleDirectory.all().filter("id=?", dirId).fetchOne()
			if(directory.getDirectoryName().equals("Root"))
				continue;
			oneDirRemoved = directory
			if(dirOp.untrashDirectory(directory.getDirectoryId())) {
				deleted = dirOp.removeDirectory(directory.getDirectoryId())
			}
			if(deleted) {
				removeInnerDirectoryWithFiles(directory.getId())
				directory.remove()
				sizeDeleted++
			}
		}
		if(sizeDeleted==1 && oneDirRemoved!=null)
			message = "Directory <FONT Color='#01DF3A'> " + oneDirRemoved.getDirectoryName() + " </FONT> deleted forever Successfully with all its child directories and files !"
		else
			message ="<FONT Color='#01DF3A'> " sizeDeleted + "</FONT> out of <FONT Color='#01DF3A'>" + sizeToDelete + " </FONT> directories deleted forever Successfully with all their child directories and files!"
		return message
	}
}
