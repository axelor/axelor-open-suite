package com.axelor.googleappsconn.directory;

import java.io.IOException;

import com.axelor.googleappsconn.drive.Directory;
import com.axelor.googleappsconn.drive.GoogleDrive;

public class DirectoryOperations {
	
	GoogleDrive driveService;
	/**
	 * Constructor that takes GoogleDrive object to do the directory operations on User's 
	 * Google Drive operations.
	 * @param passedDriveService GoogleDrive
	 */
	public DirectoryOperations(GoogleDrive passedDriveService) {
		this.driveService = passedDriveService;
	}
	/**
	 * creates a new directory on user's google drive in the parent directory provided by user as parentId.
	 * @param directoryName String name of directory
	 * @param parentId String directory Id of the parent directory.
	 * @return createdDirectory Directory
	 * @throws IOException
	 */
	public Directory createDirectory(String directoryName, String parentId) throws IOException {
		Directory directory = driveService.createDirectory(directoryName, parentId);
		return directory;
	}
	/**
	 * Removes the directory from user's google drive permanently 
	 * with all its child files and directories
	 * @param directoryId String
	 * @return removed boolean returns true if the directory removal succeeded
	 * @throws Exception
	 */
	public boolean removeDirectory(String directoryId) throws Exception {		
		String message = driveService.removeDirectory(directoryId, true);
		if(message.equals("yes"))
			return true;
		return false;
	}
	/**
	 * Trash the directory from user's google drive
	 * with all its child files and directories
	 * @param directoryId String
	 * @return trashed boolean returns true if the directory trash successfull.
	 * @throws Exception
	 */
	public boolean trashDirectory(String directoryId) throws Exception {
		String message = driveService.removeDirectory(directoryId, false);
		if(message.equals("yes"))
			return true;
		return false;		
	}
	/**
	 * Restore the directory from user's google drive from trash
	 * with all its child files and directories
	 * @param directoryId String
	 * @return restored boolean returns true if restore successfull.
	 * @throws Exception
	 */
	public boolean untrashDirectory(String directoryId) throws Exception {
		return driveService.untrashDirectory(directoryId);
	}
	/**
	 * Rename a directory.
	 * @param directoryId String
	 * @param newName String
	 * @throws IOException
	 */
	public void renameDirectory(String directoryId, String newName)	throws IOException {
		driveService.rename(directoryId, newName);
	}	
}
