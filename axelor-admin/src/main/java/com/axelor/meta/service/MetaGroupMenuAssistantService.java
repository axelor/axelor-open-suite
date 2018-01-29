/*
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
package com.axelor.meta.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.io.output.FileWriterWithEncoding;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

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
		String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
		String fileName = "GroupMenu" + "-" + userCode + "-" + dateString + ".csv";

		return fileName;
	}


	public void createGroupMenuFile(MetaGroupMenuAssistant groupMenuAssistant){

		AppSettings appSettings = AppSettings.get();
		File groupMenuFile = new File(appSettings.get("file.upload.dir"), getFileName(groupMenuAssistant));
		
		try {
			
			List<String[]> rows = createHeader(groupMenuAssistant);
			
			addMenuRows(groupMenuAssistant, rows);
			
			addGroupAccess(rows);
			
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(groupMenuFile, "utf-8");
			
			CSVWriter csvWriter =  new CSVWriter(fileWriter, ';');
			
			csvWriter.writeAll(rows);
			
			csvWriter.close();

			createMetaFile(groupMenuFile, groupMenuAssistant);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private List<String[]> createHeader(MetaGroupMenuAssistant groupMenuAssistant) throws IOException{
		
		CSVReader csvReader = null;
		MetaFile metaFile = groupMenuAssistant.getMetaFile();
		List<String[]> rows = new ArrayList<String[]>();
		if(metaFile != null){
			File csvFile = MetaFiles.getPath(metaFile).toFile();
			System.out.println("File name: " + csvFile.getAbsolutePath());
			System.out.println("File length: " + csvFile.length());
			csvReader = new CSVReader(new FileReader(csvFile), ';');
			rows = csvReader.readAll();
			System.out.println("Rows size: " + rows.size());
			csvReader.close();
		}
		if(!rows.isEmpty()){
			rows.set(0, getGroupRow(rows.get(0), groupMenuAssistant.getGroupSet()));
		}
		else{
			rows.add(getGroupRow(null, groupMenuAssistant.getGroupSet()));
		}
		
		return rows;
		
	}
	
	private String[] getGroupRow(String[] row, Set<Group> groupSet) throws IOException{
		
		
		List<String> groupList = new ArrayList<String>();
		if(row != null) {
			groupList.addAll(Arrays.asList(row));
		}
		else{
			groupList.add(I18n.get("Name"));
			groupList.add(I18n.get("Title"));
		}

		for(Group group : groupSet){
			String code = group.getCode();
			if(!groupList.contains(code)){
				groupList.add(code);
			}
		}
		
		return groupList.toArray(new String[groupList.size()]);
	}
	
	private void addMenuRows(MetaGroupMenuAssistant groupMenuAssistant, List<String[]> rows){
		
		String language = groupMenuAssistant.getLanguage();
		language = language != null ? language : "en";
		ResourceBundle bundle = I18n.getBundle(new Locale(language));
		
		List<String> names = new ArrayList<String>();
		String[] groupRow = rows.get(0);
		rows.remove(0);
		
		for(String[] row : rows){
			names.add(row[0]);
		}
		
		
		for(MetaMenu metaMenu : groupMenuAssistant.getMenuSet()){

			String name = metaMenu.getName();
			
			if(names.contains(name)){
				continue;
			}
			
			String title = metaMenu.getTitle();
			String translation = bundle.getString(title);

			if(!Strings.isNullOrEmpty(translation)){
				title = translation;
			}
			
			String[] menu = new String[groupRow.length];
			menu[0] = name;
			menu[1] = title;
			rows.add(menu);
		}
		
		Collections.sort(rows, new Comparator<String[]>() {

			@Override
			public int compare(String[] first, String[] second) {
				return first[0].compareTo(second[0]);
			}
			
		});
		
		rows.add(0, groupRow);
		
	}
	
	private void addGroupAccess(List<String[]> rows) {
		
		ListIterator<String[]> rowIter = rows.listIterator();
		
		String[] header = rowIter.next();
		
		while (rowIter.hasNext()) {
			String[] row = rowIter.next();
			MetaMenu menu = menuRepository.all().filter("self.name = ?1", row[0]).order("-priority").fetchOne();
			
			if (row.length < header.length) {
				row = Arrays.copyOf(row, header.length);
				rowIter.set(row);
			}
			
			for (int i=2; i<header.length; i++) {
				for (Group group : menu.getGroups()) {
					if (header[i] != null && header[i].equals(group.getCode())) {
						row[i] = "x";
					}
				}
			}
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
			Group admin = groupRepository.findByCode("admins");
			importMenus(csvReader, groupRow, groupMap, admin);

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


	private void importMenus(CSVReader csvReader, String[] groupRow,
			Map<String, Group> groupMap, Group admin) throws IOException {

		String[] row = csvReader.readNext();
		if(row == null){
			return;
		}

		List<MetaMenu>  menus =  menuRepository.all().filter("self.name = ?1", row[0]).order("-priority").fetch();

		if(menus.isEmpty()){
			errorLog += "\n"+String.format(I18n.get(IMessage.NO_MENU), row[0]);
			return;
		}
		
		Iterator<MetaMenu> menuIter = menus.iterator();
		
		while(menuIter.hasNext()){
			
			MetaMenu menu = menuIter.next();
			
			boolean noAccess = true;
			
			for(Integer mIndex = 2; mIndex < row.length; mIndex++ ){
	
				String groupCode = groupRow[mIndex];
	
				if(groupMap.containsKey(groupCode)){
					Group group = groupMap.get(groupCode);
					if(row[mIndex].equalsIgnoreCase("x")){
						noAccess = false;
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
			
			if(noAccess && admin != null){
				menu.addGroup(admin);
			}
		}

		importMenus(csvReader, groupRow, groupMap, admin);

	}
	
	
	
}
