/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.auth.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.axelor.app.AppSettings;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.IMessage;
import com.axelor.auth.db.Permission;
import com.axelor.auth.db.PermissionAssistant;
import com.axelor.auth.db.repo.GroupRepository;
import com.axelor.auth.db.repo.PermissionAssistantRepository;
import com.axelor.auth.db.repo.PermissionRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaPermission;
import com.axelor.meta.db.MetaPermissionRule;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaPermissionRepository;
import com.axelor.meta.db.repo.MetaPermissionRuleRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class PermissionAssistantService {

	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	private MetaPermissionRepository metaPermissionRepository;

	@Inject
	private PermissionRepository permissionRepository;

	@Inject
	private MetaPermissionRuleRepository  ruleRepository;

	@Inject
	private GroupRepository groupRepository;

	@Inject
	private MetaModelRepository modelRepository;

	private String errorLog = "";


	@SuppressWarnings("serial")
	private List<String> groupHeader = new ArrayList<String>(){{
			add("");
			add(I18n.get("Read"));
			add(I18n.get("Write"));
			add(I18n.get("Create"));
			add(I18n.get("Delete"));
			add(I18n.get("Export"));
			add(I18n.get("Readonly If"));
			add(I18n.get("Hide If"));
	}};

	private String getFileName(PermissionAssistant assistant){

		String userCode = assistant.getCreatedBy().getCode();
		String dateString = LocalDateTime.now().toString("yyyyMMddHHmm");
		String fileName = userCode + "-" + dateString + ".csv";

		return fileName;
	}

	public void createFile(PermissionAssistant assistant){

		AppSettings appSettings = AppSettings.get();
		File permFile = new File(appSettings.get("file.upload.dir"), getFileName(assistant));

		try {
			
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(permFile, "utf-8");
			CSVWriter csvWriter =  new CSVWriter(fileWriter, ';');
			writeGroup(csvWriter, assistant);
			csvWriter.close();

			createMetaFile(permFile, assistant);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Transactional
	public void createMetaFile(File permFile, PermissionAssistant assistant) {

		MetaFile metaFile = new MetaFile();
		metaFile.setFileName(permFile.getName());
		metaFile.setFilePath(permFile.getName());

		assistant.setMetaFile(metaFile);

		Beans.get(PermissionAssistantRepository.class).save(assistant);
	}

	private void writeGroup(CSVWriter csvWriter, PermissionAssistant assistant) {

		@SuppressWarnings("serial")
		List<String> headerRow = new ArrayList<String>(){{
			add("Object");
			add("Field");
			add("Title");
		}};

		String[] groupRow = new String[assistant.getGroupSet().size()*8+3];
		Integer count = 3;
		for(Group group : assistant.getGroupSet()){
			groupRow[count+1] = group.getCode();
			headerRow.addAll(groupHeader);
			count += 8;
		}

		LOG.debug("Header row created: {}",headerRow);

		csvWriter.writeNext(groupRow);
		csvWriter.writeNext(headerRow.toArray(groupRow));

		writeObject(csvWriter, assistant, groupRow.length);
	}

	public Comparator<Object> compareField() {

		 return new Comparator<Object>() {
	            @Override
	            public int compare(Object field1, Object field2) {
	                return ((MetaField)field1).getName().compareTo(((MetaField)field2).getName());
	            }
	        };
	}

	private void writeObject(CSVWriter csvWriter, PermissionAssistant assistant, Integer size) {

		String language = assistant.getLanguage();
		language = language != null ? language : "en";

		LOG.debug("Language selected: {}",language);
		ResourceBundle bundle = I18n.getBundle(new Locale(language));

		for(MetaModel object : assistant.getObjectSet()){

			String[] row = new String[size];
			row[0] = object.getFullName();

			Set<Group> groupSet = assistant.getGroupSet();
			int colIndex = 4;
			
			for (Group group : groupSet) {
				for (Permission perm : group.getPermissions()) {
					if (perm.getObject().equals(object.getFullName())) {
						row[colIndex++] = perm.getCanRead() == false ? "" : "x";
						row[colIndex++] = perm.getCanWrite() == false ? "" : "x";
						row[colIndex++] = perm.getCanCreate() == false ? "" : "x";
						row[colIndex++] = perm.getCanRemove() == false ? "" : "x";
						row[colIndex++] = perm.getCanExport() == false ? "" : "x";
						row[colIndex++] = ""; // readonly if
						row[colIndex++] = ""; // hide if
						break;
					}
				}
				colIndex++;
			}
			
			csvWriter.writeNext(row);
			List<MetaField> fieldList = object.getMetaFields();
			Collections.sort(fieldList, compareField());

			for(MetaField field : fieldList){

				colIndex = 4;

				row = new String[size];
				row[1] = field.getName();

				String title = field.getLabel();
				if(!Strings.isNullOrEmpty(title)){
					title = bundle.getString(title);
				}
				row[2] = title;

				for (Group group : groupSet) {
					Set<MetaPermission> fieldPermission = group.getMetaPermissions();
					for (MetaPermission perm : fieldPermission) {
						if (perm.getObject().equals(object.getFullName())) {
							for (MetaPermissionRule fieldPerm : perm.getRules()) {
								if (field.getName().equals(fieldPerm.getField())) {
									row[colIndex++] = fieldPerm.getCanRead() == false ? "" : "x";
									row[colIndex++] = fieldPerm.getCanWrite() == false ? "" : "x";
									row[colIndex++] = "";
									row[colIndex++] = "";
									row[colIndex++] = fieldPerm.getCanExport() == false ? "" : "x";
									row[colIndex++] = Strings.isNullOrEmpty(fieldPerm.getReadonlyIf()) ? "" : fieldPerm.getReadonlyIf(); // readonly if
									row[colIndex++] = Strings.isNullOrEmpty(fieldPerm.getHideIf()) ? "" : fieldPerm.getHideIf(); // hide if
								}
							}
						}
 					}
					colIndex++;
				}
				csvWriter.writeNext(row);
			}
		}
	}
	
	private boolean checkHeaderRow(String[] headerRow){

		@SuppressWarnings("serial")
		List<String> headerList = new ArrayList<String>(){{
			add("Object");
			add("Field");
			add("Title");
		}};

		Integer count = 3;
		while(count < headerRow.length){
			headerList.addAll(groupHeader);
			count += 8;
		}
		LOG.debug("Standard Headers: {}",headerList);

		String[] headers =  headerList.toArray(new String[headerList.size()]);
		LOG.debug("File Headers: {}",Arrays.asList(headerRow));

		return Arrays.equals(headers, headerRow);

	}

	public String importPermissions(PermissionAssistant permissionAssistant){

		try {
			MetaFile metaFile = permissionAssistant.getMetaFile();
			File csvFile = MetaFiles.getPath(metaFile).toFile();
			CSVReader csvReader = new CSVReader(new FileReader(csvFile), ';');

			String[] groupRow = csvReader.readNext();
			if(groupRow == null || groupRow.length < 11){
				errorLog = I18n.get(IMessage.BAD_FILE);
			}

			String[] headerRow = csvReader.readNext();
			if(headerRow == null){
				errorLog =  I18n.get(IMessage.NO_HEADER);
			}
			if(!checkHeaderRow(headerRow)){
				errorLog =  I18n.get(IMessage.BAD_HEADER)+Arrays.asList(headerRow);
			}

			if(!errorLog.equals("")){
				csvReader.close();
				return errorLog;
			}

			Map<String,Group> groupMap = checkBadGroups(groupRow);
			Map<String, MetaPermission> metaPermissionDict = new HashMap<String, MetaPermission>();
			processCSV(csvReader, groupRow, null, metaPermissionDict, groupMap);

			saveGroups(groupMap);

		} catch (Exception e) {
			e.printStackTrace();
			errorLog += "\n"+String.format(I18n.get(IMessage.ERR_IMPORT_WITH_MSG), e.getMessage());
		}

		return errorLog;
	}

	@Transactional
	public void saveGroups(Map<String, Group> groupMap) {

		for(Group group: groupMap.values()){
			groupRepository.save(group);
		}

	}

	private Map<String,Group> checkBadGroups(String[] groupRow){

		List<String> badGroups = new ArrayList<String>();
		Map<String,Group> groupMap = new HashMap<String, Group>();

		for(Integer glen = 4; glen<groupRow.length; glen+=8){

			String groupName = groupRow[glen];
			Group group = groupRepository.all().filter("self.code = ?1", groupName).fetchOne();
			if(group == null){
				badGroups.add(groupName);
			}
			else{
				groupMap.put(groupName, group);
			}
		}

		if(!badGroups.isEmpty()){
			errorLog += "\n"+String.format(I18n.get(IMessage.NO_GROUP), badGroups);
		}

		return groupMap;
	}

	private String checkObject(String objectName){

		MetaModel model = modelRepository.all().filter("self.fullName = ?1", objectName).fetchOne();

		if(model == null){
			errorLog += "\n"+String.format(I18n.get(IMessage.NO_OBJECT), objectName);
			return null;
		}

		return objectName;
	}

	private void processCSV(CSVReader csvReader, String[] groupRow,

		String objectName, Map<String, MetaPermission> metaPermissionDict, Map<String,Group> groupMap) throws IOException{

		String[] row = csvReader.readNext();

		if(row == null){
			return;
		}

		for(Integer groupIndex = 4; groupIndex < row.length; groupIndex += 8){

			String groupName = groupRow[groupIndex];
			if(!groupMap.containsKey(groupName)) {continue;}

			String[] rowGroup = (String[]) Arrays.copyOfRange(row, groupIndex, groupIndex + 8);

			if(!Strings.isNullOrEmpty(groupName) && !Strings.isNullOrEmpty(row[0])){
				objectName = checkObject(row[0]);
				if(objectName == null){
					break;
				}
				metaPermissionDict.put(groupName, getMetaPermission(groupMap.get(groupName), objectName));
				updatePermission(groupMap.get(groupName), objectName, rowGroup);
			}
			else if(objectName != null && !Strings.isNullOrEmpty(row[1])){
				updateFieldPermission(metaPermissionDict.get(groupName), row[1], rowGroup);
			}

		}

		processCSV(csvReader, groupRow, objectName, metaPermissionDict, groupMap);

	}


	public MetaPermission getMetaPermission(Group group, String objectName){

		String[] objectNames = objectName.split("\\.");
		String groupName = group.getCode();
		String permName = groupName + "." + objectNames[objectNames.length - 1];
		MetaPermission metaPermission = metaPermissionRepository.all().filter("self.name = ?1",  permName).fetchOne();

		if(metaPermission == null){
			LOG.debug("Create metaPermission group: {}, object: {}", groupName, objectName);

			metaPermission = new MetaPermission();
			metaPermission.setName(permName);
			metaPermission.setObject(objectName);

			group.addMetaPermission(metaPermission);

		}

		return metaPermission;
	}

	public MetaPermission updateFieldPermission(MetaPermission metaPermission, String field, String[] row) {

		MetaPermissionRule permissionRule = ruleRepository.all().filter("self.field = ?1 and self.metaPermission.name = ?2",
				field,metaPermission.getName()).fetchOne();

		if(permissionRule == null){
			permissionRule = new MetaPermissionRule();
			permissionRule.setMetaPermission(metaPermission);
			permissionRule.setField(field);
		}

		permissionRule.setCanRead(row[0].equalsIgnoreCase("x"));
		permissionRule.setCanWrite(row[1].equalsIgnoreCase("x"));
		permissionRule.setCanExport(row[4].equalsIgnoreCase("x"));
		permissionRule.setReadonlyIf(row[5]);
		permissionRule.setHideIf(row[6]);
		metaPermission.addRule(permissionRule);

		return metaPermission;
	}

	public void updatePermission(Group group, String objectName, String[] row) {

		String[] objectNames = objectName.split("\\.");
		String groupName = group.getCode();
		String permName = groupName + "." + objectNames[objectNames.length-1];

		Permission permission = permissionRepository.all().filter("self.name = ?1", permName).fetchOne();
		boolean newPermission = false;

		if(permission == null){
			newPermission = true;
			permission = new Permission();
			permission.setName(permName);
			permission.setObject(objectName);
		}

		permission.setCanRead(row[0].equalsIgnoreCase("x"));
		permission.setCanWrite(row[1].equalsIgnoreCase("x"));
		permission.setCanCreate(row[2].equalsIgnoreCase("x"));
		permission.setCanRemove(row[3].equalsIgnoreCase("x"));
		permission.setCanExport(row[4].equalsIgnoreCase("x"));

		if(newPermission){
			group.addPermission(permission);
		}
	}
}
