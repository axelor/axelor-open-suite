/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.data.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.exception.AxelorException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.service.data.CommonService;
import com.axelor.studio.service.data.importer.DataReader;
import com.axelor.studio.service.data.validator.ValidatorService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class AsciidocExporter {
	
	private static final Logger log = LoggerFactory.getLogger(AsciidocExporter.class);
	
	private static final List<String> COMMENT_TYPES = Arrays.asList(
			new String[]{"tip", "general", "warn"});

	private static final List<String> ASCIIDOC_TYPES = Arrays.asList(
			new String[]{"TIP", "NOTE", "WARNING"});
	
	private boolean hasMenu = false;
	
	private List<String> processedMenus = new ArrayList<String>();
	
	private File imgDir = null;
	
	private String header = null;

	private boolean setHorizontal = false;
	
	private String lang = null;
	
	private Map<String, Integer> countMap = new HashMap<String, Integer>();
	
	@Inject
	private MetaFiles metaFiles;
	
	@Inject
	private ValidatorService validatorService;
	
	public MetaFile export(MetaFile input, DataReader reader, String lang, String name) throws IOException, AxelorException {
		
		if (input == null) {
			return null;
		}
		
		if (reader == null) {
			return null;
		}
		
		reader.initialize(input);
		
		validateInput(reader);
		
		String docImgPath = AppSettings.get().get("studio.doc.dir");
		if (docImgPath != null) {
			imgDir = new File(docImgPath);
		}
		
		this.setHorizontal = false;
		this.lang = lang;
		
		log.debug("Doc image path: {}", docImgPath);
		
		File exportFile = exportAscci(reader, lang, name);
		
		return metaFiles.upload(exportFile);
	}
	
	private void validateInput(DataReader reader) throws IOException, AxelorException {
		
		String[] keys = reader.getKeys();
		
		for (String key : keys) {
			if (key.equals("Modules") || key.equals("Menu") || key.equals("Actions")) {
				continue;
			}
			
			if(!validatorService.validateModelHeaders(reader, key)) {
				throw new AxelorException("Invalid headers for sheets '%s'", 1, key);
			}
			
		}
		
	}

	private File exportAscci(DataReader reader,  String lang, String name) {
		
		try {
			File asciiDoc = File.createTempFile(name, ".txt");
			
			FileWriter fw = new FileWriter(asciiDoc);
			
			if (lang != null && lang.equals("fr")) {
				fw.write(":warning-caption: Attention\n");
				fw.write(":tip-caption: Astuce\n");
				fw.write("= Specifications Détaillées\n:toc:\n:toclevels: 4");
			}
			else {
				fw.write("= Documentation\n:toc:\n:toclevels: 4");
			}
			
			processReader(reader, fw);
			
			fw.close();
			
			return asciiDoc;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private void processReader(DataReader reader, FileWriter fw)
			throws IOException {
		
		hasMenu = false;
		
		String[] keys = reader.getKeys();
		
		if (keys != null) {
			
			for (String key : reader.getKeys()) {
				
				int totalLines = reader.getTotalLines(key);
				
				log.debug("Processing sheet: {}, Total lines: {}", key, totalLines);
				for (int ind = 1; ind < totalLines; ind++) {
					if (key.equals("Modules") || key.equals("Menu") || key.equals("Actions")) {
						continue;
					}
					String[] row = reader.read(key, ind);
					if (row == null) {
						continue;
					}
					String type = row[CommonService.TYPE];
					if (type != null) {
						String menu = row[CommonService.MENU];
						if (lang != null && lang.equals("fr")) {
							menu = row[CommonService.MENU_FR];
						}
						String view = row[CommonService.VIEW];
						if (!Strings.isNullOrEmpty(menu) 
								&& type.equals("general")){
							processMenu(menu, view);
							hasMenu = true;
						}
						else {
							menu = null;
						}

						if(hasMenu){ 
							processView(row, type, fw);
						}
					}
				}
			}
		
		}
	}

	private void processMenu(String menu, String view) throws IOException{
		
		if (menu.contains("-form(")) {
			String[] menus = menu.split("-form\\(");
			String parent = menus[0] + "-form";
			if (countMap.containsKey(parent)) { 
				menu = menus[menus.length-1];
				Integer count = countMap.get(parent);
				header += "\n\n==" 
					+ StringUtils.repeat("=", count)
					+ " " 
					+ menu.substring(0, menu.length()-1);
				countMap.put(view, count + 1);
			}
		}
		else {
			String[] menus = menu.split("/", 4);
			int count = -1;
			
			header = "";
			String checkMenu = "";
			for (String mn : menus){
				count++;
				checkMenu += mn + "/";
				if (!processedMenus.contains(checkMenu)){
					processedMenus.add(checkMenu);
					header += "\n\n==" 
							+ StringUtils.repeat("=", count)
							+ " " 
							+ mn;
					countMap.put(view, count + 1);
				}
			}
		}
			
		if (imgDir != null 
				&& new File(imgDir, view + ".png").exists()) {
			header += "\nimage::" + view + ".png[" + menu + ", align=\"center\"]";
		}
		
		
	}
	
	private void processView(String[] values, String type, FileWriter fw) 
			throws IOException{
		
		String modelVal = values[CommonService.MODEL];
		String viewVal = values[CommonService.VIEW];
		
		if (Strings.isNullOrEmpty(modelVal) 
				&& Strings.isNullOrEmpty(viewVal)) {
			return;
		}
		
		String doc = values[CommonService.HELP];
		if (Strings.isNullOrEmpty(doc)) {
			doc =  values[CommonService.HELP_FR];
		}
		if (Strings.isNullOrEmpty(doc)) {
			return;
		}
		
		String title =  values[CommonService.TITLE];
		if (lang != null && lang.equals("fr") && values[CommonService.TITLE_FR] != null) {
			title = values[CommonService.TITLE_FR];
		}
		
		if (title == null) {
			title = values[CommonService.TITLE_FR];
		}
		
		if (Strings.isNullOrEmpty(title)) { 
			title = type;
		}
		
		if (COMMENT_TYPES.contains(type)) {
			title = ASCIIDOC_TYPES.get(COMMENT_TYPES.indexOf(type));
			if (header != null) {
				fw.write(header);
				header = null;
				setHorizontal = true;
				fw.write("\n\n" + title + ": "+ doc );
				return;
			}
		}
		
		if (type.contains("(")) {
			type = type.substring(0, type.indexOf("("));
		}
		
		if (CommonService.FIELD_TYPES.containsKey(type) || CommonService.VIEW_ELEMENTS.containsKey(type) || header != null) {
			if (header != null) {
				fw.write(header);
				header = null;
				fw.write("\n\n[horizontal]");
			}
			
			if (setHorizontal) {
				fw.write("\n\n[horizontal]");
				setHorizontal = false;
			}
			
			if (type.toUpperCase().contains("PANEL")) {
				fw.write("\n[red]#" + title + "#:: " + doc );
			}
			else {
				fw.write("\n" + title + ":: " + doc);
			}
			
		}
		else {
			if (!setHorizontal) {
				fw.write("\n+\n" + title + ": "+ doc );
			}
			else {
				fw.write("\n\n" + title + ": "+ doc );
			}
		}
	
	}
	
}