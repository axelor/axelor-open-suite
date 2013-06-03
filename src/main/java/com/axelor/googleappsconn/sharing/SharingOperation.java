package com.axelor.googleappsconn.sharing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.axelor.googleappsconn.drive.GoogleDrive;
import com.axelor.googleappsconn.drive.GoogleFile;
import com.axelor.googleappsconn.utils.Utils;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;

public class SharingOperation {
	
	GoogleDrive driveService;

	public SharingOperation(GoogleDrive passedService) {
		this.driveService = passedService;
	}
	/**
	 * share a file to list of users.
	 * @param permissionList containing the email ids of users to whom this file is to be share.
	 * @param fileId String
	 * @return permissionList List<SharingPermission>
	 * @throws Exception
	 */
	public void shareFile(List<SharingPermission> permissionList, String fileId) throws Exception {
		int i = 0;
		for (SharingPermission permission : permissionList) {
			Permission filePermission = new Permission();
			filePermission.setValue(permission.getEmailId());
			filePermission.setRole(permission.getRole());
			filePermission.setType(permission.getType());
			String perid = driveService.getService().permissions()
					.insert(fileId, filePermission)
					.setSendNotificationEmails(permission.getNotifyEmail()).execute()
					.getId();
			permissionList.get(i).setPermissionId(perid);
			i++;
		}
	}
	public void unShareFile(List<String> permissionIdList, String fileId) throws IOException {
		for (String permissionId : permissionIdList) {
			driveService.getService().permissions().delete(fileId, permissionId).execute();
		}
	}
	/**
	 * gives the list of googlefiles with the user who shared and date.
	 * @return sharedGoogleFiles List<SharedWithMe>
	 * @throws Exception
	 */
	public List<SharedWithMe> getAllSharedWithMeFiles() throws Exception {

		List<SharedWithMe> sharedGoogleFiles = new ArrayList<SharedWithMe>();
		Files.List request = driveService.getService().files().list();
		request.setQ("sharedWithMe");
		FileList fileList = request.execute();
		List<File> files = fileList.getItems();
		SharedWithMe fileShared;
		Permission permission;
		for (File file : files) {
			GoogleFile googleFile = new GoogleFile(file);
			googleFile.setFileId(file.getId());
			googleFile.setFileName(file.getTitle());
			googleFile.setLastModified(Utils.getDateFormated(file.getModifiedDate()));
			if (file.getFileSize() != null)
				googleFile.setFileSize(file.getFileSize().longValue());
			else
				googleFile.setFileSize(file.getQuotaBytesUsed().longValue());
			PermissionList perlist = driveService.getService().permissions().list(file.getId()).execute();
			googleFile.setFileType(file.getMimeType());

			permission = perlist.getItems().get(0);
			String userWhoShared = permission.getName();
			String sharedDate = Utils.getDateFormated(file.getSharedWithMeDate());
			fileShared = new SharedWithMe(googleFile, userWhoShared, sharedDate);
			permission = file.getUserPermission();
			fileShared.setRole(permission.getRole());
			fileShared.setType(permission.getType());
			sharedGoogleFiles.add(fileShared);
		}
		return sharedGoogleFiles;
	}
	/**
	 * To check list Of sharing user of File on any time
	 * @param fileId String
	 * @return sharingPermissions List<SharingPermission>
	 * @throws IOException
	 */
	public List<SharingPermission> getPermissionListOfFile(String fileId) throws IOException {
		PermissionList perlist = driveService.getService().permissions().list(fileId).execute();
		List<SharingPermission> sharingPermisisonList = new ArrayList<SharingPermission>();
		int i = 0;
		for (Permission permission : perlist.getItems()) {
			if (i == 0) {
				i++;
				continue;
			}
			SharingPermission sharingPerNew = new SharingPermission();
			sharingPerNew.setType(permission.getType());
			sharingPerNew.setRole(permission.getRole());
			sharingPerNew.setName(permission.getName());
			sharingPerNew.setPermissionId(permission.getId());
			sharingPermisisonList.add(sharingPerNew);
			i++;
		}
		return sharingPermisisonList;
	}
	/**
	 * gives permissionId for the file for current user,
	 * which means it returns user's account id.
	 * @param fileId String
	 * @return permissionId String - account id
	 * @throws Exception
	 */
	public String getPermissionId(String fileId) throws Exception {
		PermissionList perList = driveService.getService().permissions().list(fileId).execute();
		return perList.getItems().get(0).getId();
	}
}
