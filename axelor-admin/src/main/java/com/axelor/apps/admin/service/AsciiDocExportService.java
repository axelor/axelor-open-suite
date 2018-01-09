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
package com.axelor.apps.admin.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class AsciiDocExportService {
	
	private int docIndex = 12;
	private int titleIndex = 5;
	private int menuIndex = 9;
	
	private Map<String, String> menuMap = new HashMap<String, String>();
	
	private boolean hasMenu = false;
	
	private boolean firstRow = false;
	
	private List<String> processedMenu = new ArrayList<String>();
	
	@Inject
	private ViewDocExportService viewDocExportService = new ViewDocExportService();
	
	@Inject
	private MetaFiles metaFiles;
	
	public MetaFile export(MetaFile metaFile, String lang) throws IOException{
		
		if(metaFile == null){
			return null;
		}
		
		File excelFile = MetaFiles.getPath(metaFile).toFile();
		if(excelFile == null || !excelFile.exists()){
			return null;
		}
		
		File exportFile = export(excelFile, null, lang);
		
		return metaFiles.upload(exportFile);
	}
	
	public File export(File excelFile, File asciiDoc, String lang){
		
		if(excelFile == null){
			return null;
		}
		
		if(lang != null && lang.equals("fr")){
			docIndex = 11;
			titleIndex = 6;
			menuIndex = 10;
		}
		
		try {
			FileInputStream inStream = new FileInputStream(excelFile);
			
			XSSFWorkbook workbook = new XSSFWorkbook(inStream);
			
			if(asciiDoc == null){
				asciiDoc = File.createTempFile(excelFile.getName().replace(".xlsx", ""), ".txt");
			}
			
			FileWriter fw = new FileWriter(asciiDoc);
			
			fw.write("= Documentation\n:toc:");
			
			processSheet(workbook.iterator(), fw);
			
			fw.close();
			
			return asciiDoc;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private void processSheet(Iterator<XSSFSheet> iterator, FileWriter fw) throws IOException {
		
		if(!iterator.hasNext()){
			return;
		}
		
		XSSFSheet sheet = iterator.next();
		
//		fw.write("\n\n== " + sheet.getSheetName());
		
		hasMenu = false;
		
		processRow(sheet.rowIterator(), fw);
		
		processSheet(iterator, fw);
	}

	private void processRow(Iterator<Row> rowIterator, FileWriter fw) throws IOException{
		
		if(!rowIterator.hasNext()){
			return;
		}
		
		Row row = rowIterator.next();
		
		String type = ViewDocExportService.getCellValue(row.getCell(3));
		if(type != null){
			String menu = ViewDocExportService.getCellValue(row.getCell(menuIndex));
			if(type.equals("MENU")){
				String doc = ViewDocExportService.getCellValue(row.getCell(docIndex));
				if(menu != null && doc != null){
					menuMap.put(menu, doc);
				}
			}
			else if(!Strings.isNullOrEmpty(menu) && type.equals("general") && !menu.contains("-form(")){
				menu = processMenu(menu, fw);
			}
			else{
				menu = null;
			}

			if(hasMenu){ 
				processView(row, type, menu, fw);
			}
		}
		
		processRow(rowIterator, fw);
		
	}
	
	private String processMenu(String menu, FileWriter fw) throws IOException{
		
		hasMenu = true;
		String[] menus = menu.split("/", 4);
		int count = -1;
		
		String checkMenu = "";
		for(String mn : menus){
			count++;
			checkMenu += mn + "/";
			if(!processedMenu.contains(checkMenu)){
				fw.write("\n\n==" 
				+ StringUtils.repeat("=", count)
				+ " " 
				+ mn);
				processedMenu.add(checkMenu);
			}
		}
			
		if(menuMap.containsKey(menu)){
			fw.write("\n" + menuMap.get(menu));
		}
		
		return menus[menus.length-1];
		
	}
	
	private void processView(Row row, String type, String menu, FileWriter fw) throws IOException{
		
		String modelVal = ViewDocExportService.getCellValue(row.getCell(1));
		String viewVal = ViewDocExportService.getCellValue(row.getCell(2));
		
		if(Strings.isNullOrEmpty(modelVal) || Strings.isNullOrEmpty(viewVal)){
			return;
		}
		
		String doc = ViewDocExportService.getCellValue(row.getCell(docIndex));
		
		if(menu != null && !processedMenu.contains(menu)){
			processedMenu.add(menu);
			fw.write("\nimage::" + viewVal + ".png[" + menu + ", align=\"center\"]");
			if(type.equals("general") && !Strings.isNullOrEmpty(doc)){
				fw.write("\n" + doc);
			}
			firstRow = true;
			return;
		}
		
		if(Strings.isNullOrEmpty(doc)){
			return;
		}
		
		String title = ViewDocExportService.getCellValue(row.getCell(titleIndex));
		if(Strings.isNullOrEmpty(title)){
			title = ViewDocExportService.getCellValue(row.getCell(4));
		}
		if(Strings.isNullOrEmpty(title)){
			title = type.replace("tip", "TIP");
			title = type.replace("general", "CAUTION");
			title = title.replace("warn", "WARNING");
			type = title;
		}
		
		if(type.contains("(")){
			type = type.substring(0, type.indexOf("(")).replace("-", "_");
		} 
		if(viewDocExportService.fieldTypes.contains(type) || firstRow){
			if(firstRow){
				fw.write("\n\n[horizontal]");
			}
			firstRow = false;
			fw.write("\n" + title + ":: "+ doc);
		}
		else{
			fw.write("\n" + title + ": "+ doc + " +");
		}
	
	}
	

	
}
