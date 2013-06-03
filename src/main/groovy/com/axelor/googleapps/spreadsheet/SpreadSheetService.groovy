package com.axelor.googleapps.spreadsheet

import groovy.util.slurpersupport.GPathResult;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.axelor.auth.db.User
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;

import com.axelor.apps.base.db.FilePermission
import com.axelor.apps.base.db.GoogleDirectory
import com.axelor.apps.base.db.TemplateSpreadSheet;
import com.axelor.apps.base.db.TemplateSpreadSheetCriteria;
import com.axelor.apps.base.db.UserProfile;
import com.axelor.googleapps.syncDrive.SynchronizeGoogleDriveController;
import com.axelor.googleapps.syncDrive.SynchronizeWithGoogleDriveService;
import com.axelor.googleapps.document.DocumentService
import com.axelor.googleapps.userutils.Utils;
import com.axelor.apps.base.db.AppsCredentials;
import com.axelor.apps.base.db.GoogleAppsConfig;
import com.axelor.googleappsconn.document.DocumentOperations
import com.axelor.googleappsconn.drive.Directory;
import com.axelor.googleappsconn.drive.FileTypes;
import com.axelor.googleappsconn.drive.GoogleDrive;
import com.axelor.googleappsconn.drive.GoogleFile
import com.axelor.googleappsconn.sharing.SharingOperation
import com.axelor.googleappsconn.sharing.SharingPermission;
import com.axelor.googleappsconn.spreadsheet.SpreadsheetFormat;
import com.axelor.googleappsconn.spreadsheet.SpreadsheetOperations

import com.axelor.rpc.Response;
import com.google.inject.Inject;
import com.google.inject.matcher.Matchers.Returns;
import com.google.inject.persist.Transactional;
//import com.mysql.jdbc.interceptors.SessionAssociationInterceptor;


class SpreadSheetService {

	@Inject Utils userUtils
	@Inject SynchronizeWithGoogleDriveService syncService
	@Inject GoogleDrive googleDrive
	
	@Transactional
	public File createSpreadSheetFile(com.axelor.apps.base.db.GoogleFile googleFileContext, TemplateSpreadSheet templateSpreadSheet) {
		
		def idOfTemplateSpreadsheet = templateSpreadSheet.getId()
		templateSpreadSheet = TemplateSpreadSheet.all().filter("id=?", idOfTemplateSpreadsheet).fetchOne()
		String klassName = templateSpreadSheet.getTemplateModel().getFullName()
		Class classObject = Class.forName(klassName)
		String tableName = templateSpreadSheet.getTemplateModel().getName()
		
		// List out selected Fields of Template 
		List<String> selectedFileds=new ArrayList<String>()
		templateSpreadSheet.getTemplateFields().each {
			selectedFileds.add(it.getName())
		}
		
		String query = ""
		if(templateSpreadSheet.getCriteria()?.size()==0)
			query = "select a from " + tableName + " a "
		else {
			String criteria = getCriteria(templateSpreadSheet);
			query = "select a from "+ tableName + " a  where " + criteria + "";
		}
		
		List<Object> listOfRecords = JPA.em().createQuery(query).getResultList()
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet(googleFileContext.getFileName());
		// row numbering starts from 0
		Row row;
		Cell cell;
		Mapper m = Mapper.of(classObject)
		int cols = 0
		int rows = 0
		SpreadsheetFormat format = new SpreadsheetFormat((HSSFWorkbook) workbook);
		row = sheet.createRow(rows);
		
		for (Property p : m.getProperties()) {
			Object v = p.getName()
			if(selectedFileds.contains(p.getName())) {
			cell = row.createCell(cols);
			cell.setCellValue(p.getName())
			cols++
			format.setStyleBold(cell)
			}
		}
		rows++
		
		for(Object objectModel:listOfRecords) {
			Mapper m2 = Mapper.of(objectModel.getClass())
			rows++
			row=sheet.createRow(rows)
			cols = 0
			
			for(Property p2: m2.getProperties()) {
				
				if(selectedFileds.contains(p2.getName())) {
					cell = row.createCell(cols)
				
					if(p2.getType() == PropertyType.MANY_TO_ONE){
						Mapper inMapper = Mapper.of(p2.get(objectModel).getClass())
						String nameFieldVal = inMapper.getNameField()?.get(p2.get(objectModel))
						
						if(nameFieldVal == null)
							cell.setCellValue("")
						else
							cell.setCellValue(nameFieldVal)
				
					}
					else if(p2.getType() == PropertyType.ONE_TO_MANY) {
						Object o2m = p2.get(objectModel)
						
						if(o2m.toString() == "[]")
							cell.setCellValue("")
						else
							cell.setCellValue(o2m.toString())
					
					}
					else {
					
						if(p2.get(objectModel) == null)
							cell.setCellValue("")
						else
							cell.setCellValue(p2.get(objectModel).toString())
					}
					cols++
				}	
			}
		}
		
		java.io.File file = new java.io.File(GoogleDrive.USER_HOME_DOCUMENTS, googleFileContext.getFileName()+".xlsx");
		FileOutputStream fos = new FileOutputStream(file);
		workbook.write(fos);
		fos.flush();
		fos.close();
		return file
	}
	@Transactional
	public com.axelor.apps.base.db.GoogleFile createSpreadSheetWithTemplate(User currentUser,
				com.axelor.apps.base.db.GoogleFile googleFileContext, TemplateSpreadSheet templateSpreadSheet) {
				
		syncService.checkAndFillRootDirectory(currentUser)
		userUtils.startApps(currentUser)
		File localFile = createSpreadSheetFile(googleFileContext,templateSpreadSheet)
		GoogleFile createGoogleFile = new SpreadsheetOperations(googleDrive)
				.createSpreadSheet(googleFileContext.getFileName(),googleFileContext.getGoogleDirectory().getDirectoryId(), localFile)

		googleFileContext.setFileId(createGoogleFile.getFileId())
		googleFileContext.setDriveUser(currentUser)
		googleFileContext.setFileSize(createGoogleFile.getFileSize()?.longValue().toString() + "Bytes")
		googleFileContext.setFileType(createGoogleFile.getFileType())
		googleFileContext.setLastModified(createGoogleFile.getLastModified())

		SharingPermission permission = null
		if(googleFileContext.getFilePermissions() != null) {
			List<SharingPermission> sharingPerList = new ArrayList<SharingPermission>();
			
			for(FilePermission filePermission: googleFileContext.getFilePermissions()) {
				permission=new SharingPermission();
				
				if(filePermission.getEditable().booleanValue())
					permission.setRole("writer")
				else
					permission.setRole("reader")
				
				if(filePermission.getSharingUser() != null){
					permission.setEmailId(filePermission.getSharingUser().getEmailId())
					permission.setName(filePermission.getSharingUser().getName())
				} else {
					permission.setEmailId(filePermission.getShareToEmail())
					permission.setName(filePermission.getShareToEmail())
				}
				permission.setType("user")
				permission.setNotifyEmail(filePermission.getNotifyEmail())
				sharingPerList.add(permission)
			}
			sharingPerList=new SharingOperation(googleDrive).shareFile(sharingPerList,createGoogleFile.getFileId())
		}
		return googleFileContext
	}
	@Transactional
	public String getCriteria(TemplateSpreadSheet templateSpreadSheet) {

		String condtion = "";
		List<TemplateSpreadSheetCriteria> listTemplateSpreadSheetCriteria =  templateSpreadSheet.getCriteria()
		int criteriaIterator = 0
		
		while(criteriaIterator < listTemplateSpreadSheetCriteria.size()) {
			Mapper inMapper ;
			
			if(listTemplateSpreadSheetCriteria.get(criteriaIterator).getOperator() != null
			&& listTemplateSpreadSheetCriteria.get(criteriaIterator).getValue() != null ) {
				String fieldname = listTemplateSpreadSheetCriteria.get(criteriaIterator).getField().getName()
				String value = listTemplateSpreadSheetCriteria.get(criteriaIterator).getValue();
				Boolean checkM2o = false;
				Boolean checkString = false;
				Boolean checkDate = false;
				int idOfM2O = 0;
				String classNAME = listTemplateSpreadSheetCriteria.get(criteriaIterator).getField().getMetaModel().getFullName()
				Class inKlass = Class.forName(classNAME)
				inMapper = Mapper.of(inKlass)
			
				for(Property property :  inMapper.getProperties()) {
					
					if(property.getName().equals(fieldname)) {
			
						if(property.getType() == PropertyType.MANY_TO_ONE) {
							Object objectModel =	property.getTarget()
							Class objectClass = Class.forName(objectModel.getName())
							Mapper innerMapper = Mapper.of(objectClass)
							String o2mFieldName;
							Property tempProp = innerMapper.getNameField()
							String getFieldOfM2o = tempProp.getName()
					
							for(Property pIn: innerMapper.getProperties()) {
								
								if(pIn.getName().equals(getFieldOfM2o)) {
									o2mFieldName = pIn.getName()
									checkM2o=true
								}
							}
							
							Class getClass = Class.forName(objectModel.getName())
							String tableNameofObject = objectModel.getName().toString().substring(objectModel.getName().toString().lastIndexOf(".")+1)
							Object objectOfM2o =  JPA.em().createQuery("select a from "+ tableNameofObject + " a  where UPPER(a." + o2mFieldName + ") like '"+ value.toUpperCase()+"'").getResultList().get(0)
				
							if(objectOfM2o != null) {
								idOfM2O = objectOfM2o.getId()
								value = null
								value = idOfM2O.toString()
							}
						}
						else if(property.getType() == PropertyType.STRING) {
							checkString = true
						}
						else if(property.getType() == PropertyType.DATE) {
							checkDate = true
						}
					}
				}
				
				String operator = listTemplateSpreadSheetCriteria.get(criteriaIterator).getOperator();
				String queryString;
				
				if(checkM2o == true && operator.equals("eq")) 
					queryString = " a." + fieldname + "=" + value + " "
	
				if(operator.equals("eq") && checkM2o == false) {
						
					if(checkString == true)
							queryString = " UPPER(a." + fieldname + ") like  '" + value.toUpperCase() + "'"
						else if(checkDate == true)
							queryString = " a." + fieldname + "='" + value + "'"
						else
							queryString = " a." + fieldname + "=" + value
				}
				else if(operator.equals("lt")) {
				
					if(checkDate == true)
						queryString = " a." + fieldname + " < '" + value + "'"
					else
						queryString = " a." + fieldname + " < " + value
				}
				else if(operator.equals("gt")) {
					
					if(checkDate == true)
						queryString = " a." + fieldname + " > '" + value + "'"
					else
						queryString = " a." + fieldname + " > " + value
				}
				
				condtion = condtion?.concat(queryString)
				
				if(criteriaIterator < listTemplateSpreadSheetCriteria.size()-1) 
					condtion = condtion?.concat(" AND ")
				
				checkM2o = false
			}
			criteriaIterator++
		}
		return condtion
	}

	@Transactional
	public com.axelor.apps.base.db.GoogleFile createBlankSpreadSheet(User currentUser,com.axelor.apps.base.db.GoogleFile googleFile)
	{
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet(googleFile.getFileName());
		java.io.File file = new java.io.File(GoogleDrive.USER_HOME_DOCUMENTS, googleFile.getFileName()+".xlsx");
		FileOutputStream fos = new FileOutputStream(file);
		workbook.write(fos);
		fos.flush();
		fos.close();
		
		if(googleFile.getGoogleDirectory() == null)
			googleFile.setGoogleDirectory(syncService.checkAndFillRootDirectory(currentUser))
		
		GoogleFile createGoogleFile = new SpreadsheetOperations(googleDrive)
				.createSpreadSheet(googleFile.getFileName(), googleFile.getGoogleDirectory().getDirectoryId(), file)
		googleFile.setFileId(createGoogleFile.getFileId())
		googleFile.setDriveUser(currentUser)
		googleFile.setFileSize(createGoogleFile.getFileSize()?.longValue().toString() + "Bytes")
		googleFile.setFileType(createGoogleFile.getFileType())
		googleFile.setLastModified(createGoogleFile.getLastModified())
		
		SharingPermission permission = null
		
		if(googleFile.getFilePermissions() != null) {
			List<SharingPermission> sharingPerList = new ArrayList<SharingPermission>();
		
			for(FilePermission filePermission: googleFile.getFilePermissions()) {
				permission = new SharingPermission();
			
				if(filePermission.getEditable().booleanValue())
					permission.setRole("writer")
				else
					permission.setRole("reader")		
				
				if(filePermission.getSharingUser() != null){
					permission.setEmailId(filePermission.getSharingUser().getEmailId())
					permission.setName(filePermission.getSharingUser().getName())
				} else {
					permission.setEmailId(filePermission.getShareToEmail())
					permission.setName(filePermission.getShareToEmail())
				}
				permission.setType("user")
				permission.setNotifyEmail(filePermission.getNotifyEmail())
				sharingPerList.add(permission)
			}
			sharingPerList = new SharingOperation(googleDrive).shareFile(sharingPerList,createGoogleFile.getFileId())
		} 
		googleFile.getFilePermissions().each {
			it.setGoogleFile(null)
		}
		googleFile.persist()
		com.axelor.apps.base.db.GoogleFile newGoogleFile = googleFile
		newGoogleFile.getFilePermissions().each {
			it.setGoogleFile(googleFile)
		}
		newGoogleFile.merge()
		return googleFile
	}	
	@Transactional
	public com.axelor.apps.base.db.GoogleFile updateSpreadSheet(User currentUser, com.axelor.apps.base.db.GoogleFile googleFile) {
		
		userUtils.startApps(currentUser)
		com.axelor.apps.base.db.GoogleFile oldGoogleFile = com.axelor.apps.base.db.GoogleFile.all().
				filter("driveUser=? and fileId=?",currentUser,googleFile.getFileId()).fetchOne()
		
		// Rename File
		if(!oldGoogleFile.getFileName().equals(googleFile.getFileName())) {
			new DocumentOperations(googleDrive).renameDocument(googleFile.getFileId(), googleFile.getFileName())
		}
		
		// move file
		if(!oldGoogleFile.getGoogleDirectory().equals(googleFile.getGoogleDirectory())) {
			googleDrive.moveFile(googleFile. getFileId(),
					oldGoogleFile.getGoogleDirectory().getDirectoryId(),
					googleFile.getGoogleDirectory().getDirectoryId())
		}
		
		//Sharing Google File On
		SharingPermission permission = null
		if(googleFile.getFilePermissions() != null) {
			List<SharingPermission> sharingPerList = new ArrayList<SharingPermission>();
		
			for(FilePermission filePermission: googleFile.getFilePermissions()) {
				// check filepermission already shared
			
				if(filePermission.getId() == null) {
					permission=new SharingPermission();
				
					if(filePermission.getEditable().booleanValue())
						permission.setRole("writer")
					else
						permission.setRole("reader")
					
				if(filePermission.getSharingUser() != null){
					permission.setEmailId(filePermission.getSharingUser().getEmailId())
					permission.setName(filePermission.getSharingUser().getName())
				} else {
					permission.setEmailId(filePermission.getShareToEmail())
					permission.setName(filePermission.getShareToEmail())
				}
					permission.setType("user")
					permission.setNotifyEmail(filePermission.getNotifyEmail())
					sharingPerList.add(permission)
				} 
			}
			sharingPerList = new SharingOperation(googleDrive).shareFile(sharingPerList,googleFile.getFileId())
			//unshare file
			com.axelor.apps.base.db.GoogleFile oldGoogleFiletoUnshare = com.axelor.apps.base.db.GoogleFile.all().filter("driveUser=? and fileId=?", currentUser,googleFile.getFileId()).fetchOne()
			List<String> oldFilePermisisonIdList = new ArrayList<String>();
			
			int j = 0;
			while(j < oldGoogleFiletoUnshare.getFilePermissions().size()) {
				oldFilePermisisonIdList.add(oldGoogleFiletoUnshare.getFilePermissions().get(j).getSharingUser().getPermissionId())
				j++;
			}
			List<String> newFilePermisisonIdList = new ArrayList<String>();
			
			int k = 0;
			while(k < googleFile.getFilePermissions().size()) {
				newFilePermisisonIdList.add(googleFile.getFilePermissions().get(k).getSharingUser().getPermissionId())
				k++;
			}
			List<String> listOfUnshareFile = new ArrayList<String>()
			
			for(String perId:oldFilePermisisonIdList) {
			
				if(!newFilePermisisonIdList.contains(perId)) {
					listOfUnshareFile.add(perId)
				}
		
			}
			new SharingOperation(googleDrive).unShareFile(listOfUnshareFile, googleFile.getFileId())
		}
		
		return googleFile
	}

	public String readSpreadsheetInDrive(String fileId, String fileType) {
		
		if(fileType.equals(FileTypes.GOOGLE_SPREADSHEET_FILE)) 
			return "https://docs.google.com/spreadsheet/ccc?key="+fileId 
		else 
			return "https://docs.google.com/file/d/"+ fileId +"/edit"
		
	}

	public String getFileDownloadURL(User currentUser, String fileId, String fileType) {
		
		userUtils.startApps(currentUser)
		
		if(fileType.equals(FileTypes.GOOGLE_SPREADSHEET_FILE)) 
			return googleDrive.getGoogleFileDownloadURL(fileId)
		else
			return googleDrive.getFileDownloadURL(fileId)
	}
}

