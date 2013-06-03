package com.axelor.googleappsconn.drive;

import java.io.BufferedInputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.googleappsconn.sharing.SharingPermission;
import com.axelor.googleappsconn.utils.Utils;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
//import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.util.IOUtils;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
//import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
//import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.ChildList;
//import com.google.api.services.drive.model.ChildReference;
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
//import com.google.inject.Inject;
//import com.google.inject.name.Named;
import com.google.inject.servlet.SessionScoped;

/**
 * This class represent an interface to google drive with which the operation
 * classes can call methods and interact with Google Drive.
 */
@SessionScoped
public class GoogleDrive {

	/** represents the google drive object which is at top level and can give all files and directories */
	private Drive drive;
	public Drive getService(){
		return drive;
	}
	public static java.io.File USER_HOME_DOCUMENTS = null;	
	static {
		String GOOGLE_APPS_DOC_LOCATION = "";
		// String user_home = System.getProperty("user.home");
		String path_separator = System.getProperty("file.separator");
		GOOGLE_APPS_DOC_LOCATION = path_separator + "tmp" + path_separator;
		USER_HOME_DOCUMENTS = new java.io.File(GOOGLE_APPS_DOC_LOCATION);
		USER_HOME_DOCUMENTS.setReadable(true);
		USER_HOME_DOCUMENTS.setWritable(true);
		USER_HOME_DOCUMENTS.setExecutable(true);
		if (!USER_HOME_DOCUMENTS.exists())
			USER_HOME_DOCUMENTS.mkdir();
	}
	/**
	 * refreshes the credentials for drive service object from passed refresh token.
	 * This method is used when user is already authorized but want want to
	 * refresh credentials using refresh token.
	 * @param client_id String
	 * @param client_secret String
	 * @param refreshToken String
	 */
	public boolean refresh(String client_id,String client_secret,String refreshToken) throws Exception { 
		
		if(refreshToken==null)
			return false;
		final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
		final JsonFactory JSON_FACTORY = new JacksonFactory();
		TokenResponse tokenResponse = new TokenResponse().setRefreshToken(refreshToken);		
		GoogleCredential googleCredential = new GoogleCredential.Builder()
								.setClientSecrets(client_id, client_secret)	    	
								.setJsonFactory(JSON_FACTORY)
								.setTransport(HTTP_TRANSPORT)
								.build().setFromTokenResponse(tokenResponse);
		boolean refreshed =	googleCredential.refreshToken();
		drive = new Drive.Builder(HTTP_TRANSPORT,JSON_FACTORY, googleCredential)
					.setApplicationName("Google Apps Connector -Test").build();
		return refreshed;
	}	
	/**
	 * checks if the user is authorised or not for this particular object of GoogleDrive class.
	 * @return isAuthorised boolean
	 */
	public boolean isAuthorised() {
		if(drive!=null)
			return true;
		else return false;
	}	
	/**
	 * This method is used to get all directories from the google drive
	 * @return List of directories - List<Directory> 
	 * @throws Exception
	 */
	public List<Directory> getDirectories() throws Exception {

		FileList dirList = drive.files().list().execute();
		List<File> allFilesAndDirs = new ArrayList<File>();
		allFilesAndDirs.addAll(dirList.getItems());
		List<Directory> directories = new ArrayList<Directory>();
		for (File singleFile : allFilesAndDirs) {
			Directory directory;
			if (singleFile.getMimeType().equals(FileTypes.DIRECTORY)) {
				directory = new Directory(singleFile, this);
				if ((singleFile.getExplicitlyTrashed() != null)
						&& (singleFile.getExplicitlyTrashed().booleanValue()))
					directory.setTrashed(true);
				else if (!((singleFile.getExplicitlyTrashed() != null) && (singleFile
						.getExplicitlyTrashed().booleanValue())))
					directory.setTrashed(false);
				directories.add(directory);
			}
		}
		if (directories.size() > 0)
			return directories;
		else
			return null;
	}
	/** this method will search a file from google drive and configure
	 *  object of GoogleFile and return
	 *  @param fileId String
	 *  @return GoogleFile 
	 *  @throws Exception
	 **/
	public GoogleFile searchGoogleFile(String fileId) throws Exception {

		Drive.Files.Get getAllFilesFromID = drive.files().get(fileId);
		File singleFile = getAllFilesFromID.execute();
		GoogleFile foundFile = new GoogleFile(singleFile);
		if (singleFile.getExplicitlyTrashed() != null && singleFile.getExplicitlyTrashed().booleanValue())
			foundFile.setTrashed(true);
		else
			foundFile.setTrashed(false);
		foundFile.setFileId(singleFile.getId());
		foundFile.setFileName(singleFile.getTitle());
		foundFile.setFileType(singleFile.getMimeType());
		foundFile.setLastModified(Utils.getDateFormated(singleFile.getModifiedDate()));
		if (singleFile.getFileSize() != null)
			foundFile.setFileSize(singleFile.getFileSize().longValue());
		else
			foundFile.setFileSize(singleFile.getQuotaBytesUsed().longValue());
		return foundFile;
	}
	/**
	 * returns the dowloadURL for the user by which the user can directly download
	 * this file in his/her browser.
	 * @param fileId String
	 * @return downloadURL String
	 * @throws Exception
	 */
	public String getFileDownloadURL(String fileId) throws Exception {
		File file = this.searchGoogleFile(fileId).getgFile();
		String downloadURL = file.getWebContentLink();
		return downloadURL;
	}
	/**
	 * gives the URL for exporting a google type document or spreadsheet file
	 * as PDF by which user can download it from drive.
	 * @param fileId String
	 * @return fileDownloadURL String
	 * @throws Exception
	 */
	public String getExportInPdfFileURL(String fileId) throws Exception {
		
		File file = this.searchGoogleFile(fileId).getgFile();
		if (file.getMimeType().equals(FileTypes.GOOGLE_DOC_FILE)
				|| file.getMimeType().equals(FileTypes.GOOGLE_PPT_FILE)
				|| file.getMimeType().equals(FileTypes.GOOGLE_SPREADSHEET_FILE)) {
			String exportUrl = file.getExportLinks().get(FileTypes.PDF_DOCUMENT);
			return exportUrl;
		}
		return null;
	}
	/**
	 * gives the file's download url.
	 * @param fileId String
	 * @return downloadURL String
	 * @throws Exception
	 */
	public String getGoogleFileDownloadURL(String fileId) throws Exception {

		File file = this.searchGoogleFile(fileId).getgFile();
		String downloadUrl = null;
		if (file.getMimeType().equals(FileTypes.GOOGLE_DOC_FILE)) {
			downloadUrl = file.getExportLinks().get(FileTypes.MS_DOCUMENTX);
		} else if (file.getMimeType().equals(FileTypes.GOOGLE_PPT_FILE)) {
			downloadUrl = file.getExportLinks().get(FileTypes.MS_POWERPOINTX);
		} else if (file.getMimeType().equals(FileTypes.GOOGLE_SPREADSHEET_FILE)) {
			downloadUrl = file.getExportLinks().get(FileTypes.MS_SPREADSHEETX);
		}
		return downloadUrl;
	}
	/**
	 * method that gives the list of GoogleFile that are not in any folder but
	 * lies at root in google drive
	 * @return List<GoogleFile>
	 * @throws Exception
	 */
	public List<GoogleFile> getOuterGoogleFiles() throws Exception {

		List<GoogleFile> outerGoogleFiles = new ArrayList<GoogleFile>();
		Files.List request = drive.files().list();
		request.setQ("'root'" + " in parents");
		FileList files = null;
		files = request.execute();
		GoogleFile googleFile = null;
		for (File file : files.getItems()) {
			if (!FileTypes.ALL_SUPPORTED_DRIVE().contains(file.getMimeType())) continue;
				googleFile = new GoogleFile(file);
			if (file.getExplicitlyTrashed() != null && file.getExplicitlyTrashed().booleanValue())
				googleFile.setTrashed(true);
			else
				googleFile.setTrashed(false);
			googleFile.setFileId(file.getId());
			googleFile.setFileName(file.getTitle());
			googleFile.setFileType(file.getMimeType());
			googleFile.setLastModified(Utils.getDateFormated(file.getModifiedByMeDate()));
			if (file.getFileSize() != null)
				googleFile.setFileSize(file.getFileSize().longValue());
			else
				googleFile.setFileSize(file.getQuotaBytesUsed().longValue());
			PermissionList perlist = drive.permissions().list(file.getId()).execute();
			List<Permission> permissionList = perlist.getItems();
			List<SharingPermission> sharingPermissions = new ArrayList<SharingPermission>();
			for (int i = 0; i < permissionList.size(); i++) {
				// Skipped int i=0 because it eliminates the own permission from file.
				if (i == 0) continue;
				SharingPermission sharingPermission = new SharingPermission();
				sharingPermission.setName(permissionList.get(i).getName());
				sharingPermission.setRole(permissionList.get(i).getRole());
				sharingPermission.setType(permissionList.get(i).getType());
				sharingPermission.setPermissionId(permissionList.get(i).getId());
				sharingPermissions.add(sharingPermission);
			}
			googleFile.setSharingPermissions(sharingPermissions);
			outerGoogleFiles.add(googleFile);
		}
		return outerGoogleFiles;
	}
	/**
	 * Used to create a new file on local machine using the passed properties and 
	 * uploads it on google drive and then configure GoogleFile object and returns.
	 * @param fileName String
	 * @param fileType String
	 * @param file java.io.File 
	 * @throws Exception 
	 */
	public GoogleFile createFile(String fileName, String fileType,java.io.File file,
					String folderId, boolean convertToGoogle) throws Exception {
		
		GoogleFile googleFile = new GoogleFile(new File());
		googleFile.setMediaContent(new InputStreamContent(
					fileType,new BufferedInputStream(new FileInputStream(file))));
		googleFile.getMediaContent().setLength(file.length());
		googleFile.setFileName(fileName);
		googleFile.setFileType(fileType);		
		googleFile.getgFile().setTitle(fileName);
		if (googleFile!=null && googleFile.mediaContent!=null) {
			File uploadedFile = uploadFile(googleFile,folderId,convertToGoogle);
			googleFile.setLastModified(Utils.getDateFormated(uploadedFile.getModifiedByMeDate()));
			googleFile.setgFile(uploadedFile);	
			if (uploadedFile.getFileSize()!=null)
				googleFile.setFileSize(uploadedFile.getFileSize().longValue());
			else
				googleFile.setFileSize(uploadedFile.getQuotaBytesUsed().longValue());
			googleFile.setFileId(uploadedFile.getId());			
		} else throw new Exception("gFile or mediacontent is Null");
		file.delete();
		return googleFile;
	}
	/** 
	 * uploading a file from user's local drive.
	 *  */
	public GoogleFile uploadFile(java.io.File file,String folderId) throws Exception {
		
		String mimeType = FileTypes.getMimeTypeOfFile(file.getName());
		GoogleFile googleFile = new GoogleFile(new File());
		googleFile.setMediaContent(new InputStreamContent(mimeType,new BufferedInputStream(new FileInputStream(file))));
		googleFile.getMediaContent().setLength(file.length());
		googleFile.setFileName(file.getName());
		googleFile.setFileType(mimeType);
		googleFile.getgFile().setTitle(file.getName());
		if(googleFile!=null && googleFile.mediaContent!=null) {
			File uploadedFile = uploadFile(googleFile,folderId,false);		
			googleFile.setgFile(uploadedFile);	
			googleFile.setLastModified(Utils.getDateFormated(uploadedFile.getModifiedByMeDate()));
			if(uploadedFile.getFileSize()!=null)
				googleFile.setFileSize(uploadedFile.getFileSize().longValue());
			else
				googleFile.setFileSize(file.length());
			googleFile.setFileId(uploadedFile.getId());			
		} else throw new Exception("gFile or mediacontent is Null");		
		return googleFile;
	}
	/**
	 * updates the file with modified content
	 * @param fileId String
	 * @param fileToUpdate GoogleFile
	 * @param content java.io.File
	 * @return upatedFile GoogleFile
	 * @throws Exception
	 */
	public GoogleFile updateFile(String fileId,GoogleFile fileToUpdate,java.io.File content) throws Exception {
		
		boolean convertToGoogle = false;
		String mimeType = "";
		if (fileToUpdate.getFileType().equals(FileTypes.GOOGLE_DOC_FILE)) {
			mimeType = FileTypes.MS_DOCUMENTX;
			fileToUpdate.getgFile().setMimeType(mimeType);
			convertToGoogle = true;
		} else if (fileToUpdate.getFileType().equals(FileTypes.GOOGLE_SPREADSHEET_FILE)) {
			mimeType = FileTypes.MS_SPREADSHEETX;
			fileToUpdate.getgFile().setMimeType(mimeType);
			convertToGoogle = true;			
		} else mimeType = fileToUpdate.getFileType();
		FileContent mediaContent = new FileContent(mimeType, content);		
		File updatedFile = drive.files().update(fileId, fileToUpdate.getgFile(), mediaContent)
					.setConvert(convertToGoogle).execute();
		if(updatedFile.getFileSize()!=null)
			fileToUpdate.setFileSize(updatedFile.getFileSize().longValue());
		else
			fileToUpdate.setFileSize(content.length());
		fileToUpdate.setgFile(updatedFile);
		updatedFile = drive.files().touch(updatedFile.getId()).execute();
		fileToUpdate.setLastModified(Utils.getDateFormated(updatedFile.getModifiedByMeDate()));
		content.delete();
		return fileToUpdate;
	}
	/**
	 * This method will take googleFile as argument and upload that file to google drive
	 * This upload method called after creating a file using data provided by user in form view
	 * and then upload it.
	 * @param googleFile GoogleFile
	 * @param convertToGoogle boolean whether to convert this document to google doc format.
	 * @return File services.drive.File	  
	 * @throws Exception
	 */
	public File uploadFile(GoogleFile googleFile,String folderId,boolean convertToGoogle) throws Exception {
		
		if (!folderId.contains("Root"))		
			googleFile.getgFile().setParents(Arrays.asList(new ParentReference().setId(folderId)));
		googleFile.gFile.setFileSize(new Long(googleFile.mediaContent.getLength()));
		Drive.Files.Insert insert = drive.files().insert(googleFile.gFile, googleFile.mediaContent);		
		MediaHttpUploader uploader = insert.getMediaHttpUploader();
		uploader.setDirectUploadEnabled(true);
		// allow to convert the file to compatible type of google drive
		return insert.setConvert(convertToGoogle).execute();
	}
	/**
	* This method will download File to local drive to root folder
	* @param fileToDownload GoogleFile
	* @return downloadedFile java.io.File
	* @throws Exception
	*/
	public java.io.File downloadFile(GoogleFile fileToDownload) throws Exception {

		String fileId = null;
		String downloadURL = "";
		if (fileToDownload.fileId != null)
			fileId = fileToDownload.fileId;
		else if (fileToDownload.gFile != null)
			fileId = fileToDownload.gFile.getId();
		downloadURL = fileToDownload.gFile.getDownloadUrl();
		if (fileToDownload.fileType.equals(FileTypes.OPEN_DOCUMENT) && !fileToDownload.fileName.endsWith(".odt") ) {
			fileToDownload.fileName += ".odt";
		} else if (fileToDownload.fileType.equals(FileTypes.GOOGLE_DOC_FILE)) {
			fileToDownload.fileName += ".docx";
			downloadURL = fileToDownload.getgFile().getExportLinks().get(FileTypes.MS_DOCUMENTX);
		} else if (fileToDownload.fileType.equals(FileTypes.GOOGLE_SPREADSHEET_FILE)) {
			fileToDownload.fileName += ".xlsx";
			downloadURL = fileToDownload.getgFile().getExportLinks().get(FileTypes.MS_SPREADSHEETX);
		} else if (fileToDownload.fileType.equals(FileTypes.GOOGLE_PPT_FILE)) {
			fileToDownload.fileName += ".pptx";
			downloadURL = fileToDownload.getgFile().getExportLinks().get(FileTypes.MS_POWERPOINTX);
		}
		if (fileId != null) {
			Drive.Files.Get getDriveFiles = drive.files().get(fileId);
			MediaHttpDownloader downloader = getDriveFiles.getMediaHttpDownloader();
			//downloader.setDirectDownloadEnabled(true);
			java.io.File downloadedFile = new java.io.File(USER_HOME_DOCUMENTS , fileToDownload.fileName);
			OutputStream out = new FileOutputStream(downloadedFile);
			if (downloadURL != null && out != null) {
				if (fileToDownload.fileType.equals(FileTypes.GOOGLE_DOC_FILE) 
					|| fileToDownload.fileType.equals(FileTypes.GOOGLE_SPREADSHEET_FILE)
					|| fileToDownload.fileType.equals(FileTypes.GOOGLE_PPT_FILE)) {
			    	 GenericUrl url = new GenericUrl(downloadURL);
			    	 HttpResponse response = drive.getRequestFactory().buildGetRequest(url).execute();			    	 
			    	 IOUtils.copy(response.getContent(), out, true);			    	 
				}	
				else downloader.download(new GenericUrl(downloadURL), out);
			}				
			else
				throw new Exception( " either downloadURL or outputstream is NULL !");
			return downloadedFile;
		}
		return null;
	}	
	/**
	 * overloaded method to help user to download the file from fileId also.
	 * @param fileId String
	 * @return downloadedFile java.io.File
	 */
	public java.io.File downloadFile(String fileId) throws Exception {
		return downloadFile(searchGoogleFile(fileId));		
	}		
	/**
	 * overloaded method for removing the file from google drive by passing GoogleFile object.
	 * @param fileToRemove GoogleFile
	 * @throws Exception
	 */
	public void removeFile(GoogleFile fileToRemove) throws Exception {
		if (fileToRemove!=null && fileToRemove.getFileId()!=null)
			removeFile(fileToRemove.getFileId());
	}
	/**
	 * removes the file from google drive with passed fileId.
	 * @param fileId String
	 * @throws Exception
	 */
	public void removeFile(String fileId) throws Exception {		
		if (fileId!=null && searchGoogleFile(fileId)!=null)
			drive.files().delete(fileId).execute();
	}
	/**
	 * creates a new directory in root directory
	 * @param directoryName
	 * @return
	 * @throws IOException
	 */
	public Directory createDirectory(String directoryName, String parentId) throws IOException {
		File directory = new File();
		if (!parentId.contains("Root"))
			directory.setParents(Arrays.asList(new ParentReference().setId(parentId)));
		directory.setTitle(directoryName);
		directory.setMimeType("application/vnd.google-apps.folder");
		Drive.Files.Insert insert = drive.files().insert(directory);
		File file = insert.execute();
		Directory driveDirectory = new Directory(file.getId(), file.getTitle(),parentId);
		return driveDirectory;
	}
	/**
	 * removes the directory with passed id from google drive of current user.
	 * @param directoryId String
	 * @param removeFromTrash boolean set this true if you want to permanently remove directory.
	 */
	public String removeDirectory(String directoryId, boolean removeFromTrash) throws Exception {
		if (removeFromTrash) {
			drive.files().delete(directoryId).execute();
			return "yes";
		} else { 
			File directoryRemoved = drive.files().trash(directoryId).execute();
			if (directoryRemoved.getExplicitlyTrashed().booleanValue())
				return "yes";
			else
				return "no";
		}
	}
	/** moves the google file of user to trash 
	 * @param fileId String ID of the file to be moved to trash.
	 * */
	public void trashFile(String fileId) throws Exception {
		if (fileId!=null)
			drive.files().trash(fileId).execute();		
	}
	/**
	 * untrash the previously trashed file.
	 * @param fileId String ID of the file to be untrash.
	 * @return untrashedFile File 
	 * @throws Exception
	 */
	public GoogleFile untrashFile(String fileId) throws Exception {
		File untrashedFile = null;
		if (!(fileId == null || fileId.equals(""))) {
			untrashedFile = drive.files().untrash(fileId).execute();
		}
		if (untrashedFile != null) {
			return searchGoogleFile(fileId);
		}
		return null;
	}
	/**
	 * Restores the directory with all its child files also.
	 * @param directoryId String id of directory to restore 
	 * @return
	 * @throws Exception
	 */
	public boolean untrashDirectory(String directoryId) throws Exception {

		if (directoryId == null)
			return false;
		File untrashedDir = drive.files().untrash(directoryId).execute();
		if (untrashedDir != null)
			return true;
		return false;
	}
	/**
	 * moves the file to target directory from source directory.
	 * @param fileId String
	 * @param sourceDir String ID of source directory
	 * @param targetDir String ID of target directory
	 * @throws Exception
	 */
	public void moveFile(String fileId, String sourceDir, String targetDir) throws Exception {
		ParentReference newParent = new ParentReference();
		newParent.setId(targetDir);
		drive.parents().insert(fileId, newParent).execute();
		drive.parents().delete(fileId, sourceDir).execute();
	}
	/**
	 * renames a file or directory with newName
	 * @param fileId String
	 * @param newName String
	 * @throws IOException
	 */
	public void rename(String fileId, String newName) throws IOException {
		File file = new File();
		file.setTitle(newName);
		Files.Patch patchRequest = drive.files().patch(fileId, file);
		patchRequest.setFields("title");
		patchRequest.execute();
	}
	/**
	 * gives the storage space statistics on google drive for user.
	 * @return spaceStats Map<String,Long> 
	 * @throws Exception
	 */
	public Map<String,Long> getAbout() throws Exception {
		
		Map<String,Long> aboutMap = new HashMap<String,Long>();
		About about=drive.about().get().execute();
		Long size = about.getQuotaBytesTotal();
		aboutMap.put("total", size);
		size = about.getQuotaBytesUsed();
		aboutMap.put("used", size);
		return aboutMap;
	}
}
