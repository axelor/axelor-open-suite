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
package com.axelor.apps.base.service.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.reader.DataReader;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;

public class AsciidocExporterService {
	
	private static final Logger log = LoggerFactory.getLogger(AsciidocExporterService.class);
	
	private File imgDir = null;
	
	private File asciiDoc = null;
	
	private String header = null;
	
	private boolean hasOverView = false;
	
	private boolean hasRow = false;
	
	private int headerRowNum = 0;
	
	private int rowNum = 0; 
	
	private String menu = "";
	
	private String[] menuArr;
	
	public File export(MetaFile input, DataReader reader, String name, String imgPath, String finalPath) throws IOException, AxelorException {
		
		if (input == null) {
			return null;
		}
		
		if (reader == null) {
			return null;
		}
		
		reader.initialize(input);
		
		validateInput(reader);
		
		if (imgPath != null) {
			imgDir = new File(imgPath);
		}
		
		log.debug("Doc image path: {}", imgPath);
		
		return createAscciDoc(reader, name);
	}
	
	private void validateInput(DataReader reader) throws IOException, AxelorException {
		
		String[] keys = reader.getKeys();
		
		for (String key : keys) {
			if (key.equals("OverView") || key.equals("menus") || key.equals("Features") || key.equals("configurations")) {
				continue;
			}
		}
	}
	
	private File createAscciDoc(DataReader reader, String name) throws IOException {
		
		asciiDoc = File.createTempFile("Doc_" + name, ".adoc");
		FileWriter fw = new FileWriter(asciiDoc);
		
		fw.write("\n\n= " + name);

		processReader(reader, fw);
		
		fw.close();
		
		return asciiDoc;
	}
	
	private void processReader(DataReader reader, FileWriter fw) throws IOException {
		
		String[] keys = reader.getKeys();
		if (keys != null) {
			for (String key : keys) {
				int totalLines = reader.getTotalLines(key);
				this.createHeader(key, fw);
				if (key.equals("Menus")) {
					String[] headerRow = reader.read(key, 0);
					for (int i = 0; i <= headerRow.length - 1; i++) {
						if (headerRow[i] != null && headerRow[i].contains("Help"))
							rowNum = i;
					}
					menuArr = new String[rowNum];
				}
				processSections(key, totalLines, reader, fw);
			}
		}
	}
	
	private void createHeader(String key, FileWriter fw) throws IOException {
		header = "\n\n=== " + key;
		fw.write(header);
	}
	
	private void processSections(String key, int totalLines, DataReader reader, FileWriter fw) throws IOException {
		
		log.debug("Processing sheet: {}, Total lines: {}", key, totalLines);
		for (int ind = 1; ind < totalLines; ind++) {
			
			String[] row = reader.read(key, ind);
			if (row == null) {
				continue;
			}
			
			if (key.equals("OverView")) {
				processOverView(row, key, fw);
				
			} else if (key.equals("Menus")) {
				processMenus(reader, row, key, fw, ind);
			
			} else if (key.equals("Features")) {
				processFeatures(row, key, fw);
				
			} else if (key.equals("Configurations")) {
				processConfiguration(row, key, fw);
				
			} 
			hasRow = false;
		}
	}
	
	private void processOverView(String[] row, String key, FileWriter fw) throws IOException {
		
		String doc = row[CommonService.GENERAL_CONCEPTS];
								
		String keyField = row[CommonService.KEY_FIELDS];
		
		if (!Strings.isNullOrEmpty(doc) && !Strings.isNullOrEmpty(keyField)) {
			hasOverView = true;
		} else {
			doc = null;
		}

		if (hasOverView) {
			processContent(doc, keyField, fw);
		}
	}
	
	private void processContent(String doc, String keyField, FileWriter fw) throws IOException {
		
		if (Strings.isNullOrEmpty(doc)) {
			return;
		}
		
		if (header != null) {
			if (!Strings.isNullOrEmpty(keyField)) {
				fw.write("\n" + doc);
				fw.write("\n\n==== Key field");
				fw.write("\n\n" + keyField);
			} else
				fw.write("\n" + doc);
			return;
		}
	}
	
	private void processMenus(DataReader reader, String[] row, String key, FileWriter fw, int ind) throws IOException {
		
		row = reader.read(key, 0);
		for (int i = 0; i <= row.length - 1; i++) {
			if (row[i] != null && row[i].contains("Help")) {
				headerRowNum = i;
				break;
			}
		}
		row = reader.read(key, ind);
		
		String doc = row[headerRowNum];
		if (!Strings.isNullOrEmpty(doc)) {
			hasOverView = true;
		} else {
			doc = null;
		}
		
		if (hasOverView) {
			processMenuContent(row, fw, doc);
		}
	}
	
	private void processMenuContent(String[] row, FileWriter fw, String doc) throws IOException {
		
		fw.write("\n\n*");
		for (int i = 0; i < row.length; i++) {
			if (row[i] != null) {
				hasRow = true;
				if (row[i] != row[rowNum]) {
					menu = row[i] + "/";
					fw.write(menu);
					menuArr[i] = menu;
				} else {
					break;
				}
			} else {
				if (!hasRow) {
					fw.write(menuArr[i]);
				}
			}
		}
		if (doc == null) {
			fw.write("*");
		} else {
			fw.write("* : " + doc);
		}
	}
	
	private void processFeatures(String[] row, String key, FileWriter fw) throws IOException {
		
		String chapter = row[CommonService.CHAPITRES];
		String feature = row[CommonService.FEATURES];
		String doc = row[CommonService.HELP];
		String type = row[CommonService.TYPE];
		String image = row[CommonService.IMAGE];
		String model = row[CommonService.MODEL];
		String field = row[CommonService.FIELD];
		
		processFeaturesContent(chapter, feature, doc, type, image, model, field, fw);
	}
	
	private void processFeaturesContent(String chapter, String feature, String doc, String type, String image,
			String model, String field, FileWriter fw) throws IOException {

		if (!Strings.isNullOrEmpty(chapter))
			fw.write("\n\n==== " + chapter);

		if (!Strings.isNullOrEmpty(feature))
			fw.write("\n\n*" + feature + "*");
		
		if (!Strings.isNullOrEmpty(doc)) {
			if (Strings.isNullOrEmpty(feature)) {
				if (!Strings.isNullOrEmpty(type)) {
					if (type.equals("S"))
						fw.write("\n\n" + doc);
					else if (type.equals("A"))
						fw.write("\n\nWARNING: " + doc);
					else if (type.equals("T"))
						fw.write("\n\nTIP: " + doc);
					else if (type.equals("N"))
						fw.write("\n\nNOTE: " + doc);
				} else {
					fw.write("\n\n" + doc);
				}
			} else {
				fw.write("\n\n" + doc);
			}
		}
		
		if (!Strings.isNullOrEmpty(model))
			fw.write("\n\nModel : " + model);

		if (!Strings.isNullOrEmpty(field))
			fw.write("\n\nField : " + field);

		if (!Strings.isNullOrEmpty(image)) {
			if (imgDir != null && new File(imgDir, image).exists()) {
				fw.write("\n\nimage::" + imgDir.getName() + File.separator + image + "[\"" + image + "\", align=\"center\"]");
			}
		}
	}
	
	private void processConfiguration(String[] row, String key, FileWriter fw) throws IOException {
		
		String doc = row[CommonService.CONFIGURATIONS];
		
		String image = row[CommonService.CON_IMAGE];
		
		if (!Strings.isNullOrEmpty(doc)) {
			hasOverView = true;
		} else 
			doc = null;
			
		if (hasOverView) {
			processContent(doc, null, fw);
		
			if (!Strings.isNullOrEmpty(image)) {
				if (imgDir != null 
						&& new File(imgDir, image).exists()) {
					fw.write("\n\nimage::" + imgDir.getName() + File.separator + image + "[\"" + image + "\", align=\"center\"]");
				}
			}
		}
	}
}
