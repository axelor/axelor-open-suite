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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.service.data.CommonService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ExportAsciidoc extends CommonService {
	
	private static final Logger log = LoggerFactory.getLogger(ExportAsciidoc.class);
	
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
	
	public MetaFile export(MetaFile dataFile, String lang) throws IOException {
		
		String docImgPath = AppSettings.get().get("doc.images.path");
		
		setHorizontal = false;
		
		if (docImgPath != null) {
			imgDir = new File(docImgPath);
		}
		
		log.debug("Doc image path: {}", docImgPath);
		if (dataFile == null) {
			return null;
		}
		
		this.lang = lang;
		File data = MetaFiles.getPath(dataFile).toFile();
		if (data == null || !data.exists()) {
			return null;
		}
		
		File exportFile = export(data, null, lang);
		
		return metaFiles.upload(exportFile);
	}
	
	public File export(File dataFile, File asciiDoc, String lang) {
		
		if (dataFile == null) {
			return null;
		}
		
		try {
			FileInputStream inStream = new FileInputStream(dataFile);
			
			XSSFWorkbook workbook = new XSSFWorkbook(inStream);
			
			if (asciiDoc == null) {
				String fileName = dataFile.getName().replace(".xlsx", "");
				asciiDoc = File.createTempFile(fileName, ".txt");
			}
			
			FileWriter fw = new FileWriter(asciiDoc);
			
			if (lang != null && lang.equals("fr")) {
				fw.write(":warning-caption: Attention\n");
				fw.write(":tip-caption: Astuce\n");
				fw.write("= Specifications Détaillées\n:toc:\n:toclevels: 4");
			}
			else {
				fw.write("= Documentation\n:toc:\n:toclevels: 4");
			}
			
			processSheet(workbook.iterator(), fw);
			
			fw.close();
			
			return asciiDoc;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private void processSheet(Iterator<XSSFSheet> iterator, FileWriter fw)
			throws IOException {
		
		if (!iterator.hasNext()) {
			return;
		}
		
		XSSFSheet sheet = iterator.next();
		
		hasMenu = false;
		
		processRow(sheet.rowIterator(), fw);
		
		processSheet(iterator, fw);
	}

	private void processRow(Iterator<Row> rowIterator, FileWriter fw)
			throws IOException {
		
		if (!rowIterator.hasNext()) {
			return;
		}
		
		Row row = rowIterator.next();
		
		String type = getValue(row, TYPE);
		
		if (type != null) {
			String menu = getValue(row, MENU);
			String view = getValue(row, 2);
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
		
		processRow(rowIterator, fw);
		
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
				if (!Strings.isNullOrEmpty(checkMenu)
						&& !processedMenus.contains(checkMenu)){
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
	
	private void processView(Row row, String type, FileWriter fw) 
			throws IOException{
		
		String modelVal = getValue(row, 1);
		String viewVal = getValue(row, 2);
		
		if (Strings.isNullOrEmpty(modelVal) 
				&& Strings.isNullOrEmpty(viewVal)) {
			return;
		}
		
		String doc = getValue(row, HELP);
		if (Strings.isNullOrEmpty(doc)) {
			doc = getValue(row, HELP_FR);
		}
		if (Strings.isNullOrEmpty(doc)) {
			return;
		}
		
		String title = getValue(row, TITLE);
		if (lang != null && lang.equals("fr")) {
			title = getValue(row, TITLE_FR);
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