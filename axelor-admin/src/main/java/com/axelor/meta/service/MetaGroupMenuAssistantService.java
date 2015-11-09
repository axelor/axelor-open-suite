package com.axelor.meta.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.joda.time.LocalDateTime;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.axelor.app.AppSettings;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.IMessage;
import com.axelor.auth.db.repo.GroupRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaGroupMenuAssistant;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaGroupMenuAssistantRepository;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MetaGroupMenuAssistantService {

	@Inject
	private MetaGroupMenuAssistantRepository menuAssistantRepository;

	@Inject
	private GroupRepository groupRepository;

	@Inject
	private MetaMenuRepository menuRepository;

	private List<String> badGroups = new ArrayList<String>();

	private String errorLog = "";

	private List<MetaMenu> updatedMenu = new ArrayList<MetaMenu>();

	private String getFileName(MetaGroupMenuAssistant groupMenuAssistant){

		String userCode = groupMenuAssistant.getCreatedBy().getCode();
		String dateString = LocalDateTime.now().toString("yyyyMMddHHmm");
		String fileName = "GroupMenu" + "-" + userCode + "-" + dateString + ".csv";

		return fileName;
	}


	public void createGroupMenuFile(MetaGroupMenuAssistant groupMenuAssistant){

		AppSettings appSettings = AppSettings.get();
		File groupMenuFile = new File(appSettings.get("file.upload.dir"), getFileName(groupMenuAssistant));

		try {
			
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(groupMenuFile, "utf-8");
			CSVWriter csvWriter =  new CSVWriter(fileWriter, ';');
			String language = groupMenuAssistant.getLanguage();
			language = language != null ? language : "en";

			ResourceBundle bundle = I18n.getBundle(new Locale(language));

			List<String> groupList = new ArrayList<String>();
			groupList.add(I18n.get("Name"));
			groupList.add(I18n.get("Title"));

			for(Group group : groupMenuAssistant.getGroupSet()){
				groupList.add(group.getCode());
			}

			String[] groupRow = groupList.toArray(new String[groupList.size()]);
			csvWriter.writeNext(groupRow);

			for(MetaMenu menu : groupMenuAssistant.getMenuSet()){

				String title = menu.getTitle();
				String translation = bundle.getString(title);

				if(!Strings.isNullOrEmpty(translation)){
					title = translation;
				}

				String[] menus = new String[groupRow.length];
				menus[0] = menu.getName();
				menus[1] = title;
				csvWriter.writeNext(menus);

			}
			csvWriter.close();

			createMetaFile(groupMenuFile, groupMenuAssistant);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	public void createMetaFile(File groupMenuFile, MetaGroupMenuAssistant groupMenuAssistant) {

		MetaFile metaFile = new MetaFile();
		metaFile.setFileName(groupMenuFile.getName());
		metaFile.setFilePath(groupMenuFile.getName());
		groupMenuAssistant.setMetaFile(metaFile);

		menuAssistantRepository.save(groupMenuAssistant);

	}

	private Map<String,Group> checkGroups(String[] groupRow){

		Map<String,Group> groupMap = new HashMap<String, Group>();

		for(Integer glen = 2; glen < groupRow.length; glen++){

			Group group = groupRepository.all().filter("self.code = ?1",
					groupRow[glen]).fetchOne();

			if(group == null){
				badGroups.add(groupRow[glen]);
			}
			else{
				groupMap.put(groupRow[glen], group);
			}

		}

		if(!badGroups.isEmpty()){
			errorLog += "\n"+String.format(I18n.get(IMessage.NO_GROUP),badGroups);
		}

		return groupMap;
	}

	public String importGroupMenu(MetaGroupMenuAssistant groupMenuAssistant){

		try {
			MetaFile metaFile = groupMenuAssistant.getMetaFile();
			File csvFile = MetaFiles.getPath(metaFile).toFile();
			CSVReader csvReader = new CSVReader(new FileReader(csvFile), ';');

			String[] groupRow = csvReader.readNext();
			if(groupRow == null || groupRow.length < 3){
				csvReader.close();
				return I18n.get(IMessage.BAD_FILE);
			}

			Map<String,Group> groupMap = checkGroups(groupRow);
			importMenus(csvReader, groupRow, groupMap);

			csvReader.close();

			saveMenus();

		}catch(Exception e){
			e.printStackTrace();
			errorLog += "\n"+String.format(I18n.get(IMessage.ERR_IMPORT_WITH_MSG), e.getMessage());
		}

		return errorLog;
	}

	@Transactional
	public void saveMenus() {

		for(MetaMenu menu : updatedMenu){
			menuRepository.save(menu);
		}

	}


	public void importMenus(CSVReader csvReader, String[] groupRow,
			Map<String, Group> groupMap) throws IOException {

		String[] row = csvReader.readNext();
		if(row == null){
			return;
		}

		MetaMenu menu =  menuRepository.all().filter("self.name = ?1", row[0]).fetchOne();

		if(menu == null){
			errorLog += "\n"+String.format(I18n.get(IMessage.NO_MENU), row[0]);
			return;
		}

		for(Integer mIndex = 2; mIndex < row.length; mIndex++ ){

			String groupCode = groupRow[mIndex];

			if(groupMap.containsKey(groupCode)){
				Group group = groupMap.get(groupCode);
				if(row[mIndex].equalsIgnoreCase("x")){
					menu.addGroup(group);
					if(!updatedMenu.contains(menu)){
						updatedMenu.add(menu);
					}
				}
				else if(menu.getGroups().contains(group)){
					menu.removeGroup(group);
					if(!updatedMenu.contains(menu)){
						updatedMenu.add(menu);
					}
				}

			}

		}

		importMenus(csvReader, groupRow, groupMap);

	}
}
