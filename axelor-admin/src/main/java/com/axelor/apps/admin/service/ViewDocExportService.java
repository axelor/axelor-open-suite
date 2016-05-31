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
package com.axelor.apps.admin.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ViewDocExportService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private static final String[] HEADERS = new String[]{
		"Module", 
		"Object", 
		"View", 
		"Field type", 
		"Field name", 
		"Field title(EN)", 
		"Field title(FR)",
		"Selection(EN)",
		"Selection(FR)",
		"Menu(EN)",
		"Menu(FR)",
	};
	
	private XSSFWorkbook workBook;
	
	private XSSFSheet sheet;
	
	private CellStyle style;
	
	private int rowCount;
	
	private String[] menuPath;
	
	private String rootMenu;
	
	private List<String> processedMenus = new ArrayList<String>();
	
	private Map<String, String[]> docMap = new HashMap<String, String[]>();
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private MetaTranslationRepository translationRepo; 
	
	@Inject
	private MetaFiles metaFiles;
	
	@Inject
	private ViewDocXmlProcessor viewDocXmlProcessor;
	
	public MetaFile export(MetaFile docFile){
		
		List<MetaMenu> menus = metaMenuRepo.all().filter("self.parent is null and self.left = true").order("order").fetch();
		
		workBook = new XSSFWorkbook();
		addStyle();
		
		if(docFile != null){
			updateDocMap(docFile);
		}
		
		processRootMenu(menus.iterator());
		
		updateColumnWidth();

		return createExportFile(docFile);
	}
	
	private void addStyle(){
		
		style = workBook.createCellStyle();
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		
	}
	
	private void processRootMenu(Iterator<MetaMenu> rootMenuIter){
		
		if(!rootMenuIter.hasNext()){
			return;
		}
		
		MetaMenu menu = rootMenuIter.next();
		String name = menu.getName();
		
		if(!processedMenus.contains(name)){
			String title = menu.getTitle();
			if(workBook.getSheetIndex(title) >= 0){
				title = title + "(" + menu.getId() + ")";
			}
			rootMenu = title;
			sheet = null;
			processMenu(name);
			processedMenus.add(name);
		}
		
		processRootMenu(rootMenuIter);
	}
	
	protected void setMenuPath(String[] menuPath){
		this.menuPath = menuPath;
	}
	
	protected String[] getMenuPath(){
		return menuPath;
	}
	
	private void updateColumnWidth(){
		
		Iterator<XSSFSheet> sheets = workBook.iterator();
		
		while(sheets.hasNext()){
			sheet = sheets.next();
			int count = 0;
			while(count < HEADERS.length){
				sheet.autoSizeColumn(count);
				count++;
			}
		}
	}
	
	private void createSheet(){
		
		sheet = workBook.createSheet(I18n.get(rootMenu));
		rowCount = -1;

		writeRow(HEADERS);
		
	}
	
	protected void writeRow(String[] values){
		
		rowCount += 1;
		XSSFRow row = sheet.createRow(rowCount);
		
		int count = 0;
		count = writeCell(row, values, count, true);
		
		if(rowCount > 0){
			count = writeCell(row, menuPath, count, true);
		}
		
		addDoc(row, values, count);
		
		menuPath = new String[]{"",""};
		
	}
	
	private int writeCell(XSSFRow row, String[] values,  int count, boolean addStyle){
		
		for(String value : values){
			XSSFCell cell = row.createCell(count);
			if(addStyle){
				cell.setCellStyle(style);
			}
			cell.setCellValue(value);
			count++;
		}
		
		return count;
	}
	
	private void addDoc(XSSFRow row, String[] values, int count){
		
		 String name = values[4];
		 if(name == null){
			 name = values[5];
		 }
		 
		 String[] obj = values[1].split("\\.");
		 if(obj[0].isEmpty()){
			 obj = new String[]{values[2]};
		 }
		 
		 String key = null;
		 if(row.getRowNum() == 0){
			 key = sheet.getSheetName();
		 }
		 else{
			 key =  obj[obj.length - 1] + "," + values[3] + "," + name;
		 }
		 
		 String[] docs = docMap.get(key);
		 if(docs != null){
			 writeCell(row, docs, count, false);
		 }
	}
	
	protected String translate(String key, String lang){
		
		MetaTranslation translation = translationRepo.findByKey(key, lang);
		
		if(translation != null){
			String msg = translation.getMessage();
			if(!Strings.isNullOrEmpty(msg)){
				return msg;
			}
		}
		
		return key;
	}
	
	private void processMenu(String parentMenu){
		
		List<MetaMenu> subMenus = metaMenuRepo.all().filter("self.parent.name = ?", parentMenu).order("order").fetch();
		
		if(subMenus.isEmpty()){
			log.debug("No sub menus for parent : {}", parentMenu);
		}
		
		for(MetaMenu subMenu : subMenus){
			
			log.debug("Processing sub menu: {}", subMenu.getName());
			
			MetaAction action = subMenu.getAction();
			
			if(action == null){
				processMenu(subMenu.getName());
				continue;
			}	
			
			String model = action.getModel();
			
			if(action.getType().equals("action-view")){
				if(sheet == null){
					log.debug("Creating sheet: {}, model: {}", rootMenu, model);
					createSheet();
				}
				updateMenuPath(subMenu);
				viewDocXmlProcessor.processModel(model, action);
			}
		}
		
	}
	
	private void updateMenuPath(MetaMenu metaMenu){
		
		List<String> menus = new ArrayList<String>();
		menus.add(metaMenu.getTitle());
		
		addParentMenus(menus, metaMenu);
		
		Collections.reverse(menus);
		
		String menuEN = null;
		String menuFR = null;
		
		for(String menu : menus){
			
			if(menuEN == null){
				menuEN = translate(menu, "en");
				menuFR = translate(menu, "fr");
			}
			else{
				menuEN += "/" + translate(menu, "en");
				menuFR += "/" + translate(menu, "fr");
			}
		}
		
		menuPath = new String[]{menuEN,menuFR};
	}
	
	private void addParentMenus(List<String> menus, MetaMenu metaMenu){
		
		MetaMenu parentMenu = metaMenu.getParent();
		
		if(parentMenu != null){
			menus.add(parentMenu.getTitle());
			addParentMenus(menus, parentMenu);
		}
	}
	

	private void updateDocMap(MetaFile docFile){
		
		try {
			File doc = MetaFiles.getPath(docFile).toFile();
			FileInputStream inSteam = new FileInputStream(doc);
			XSSFWorkbook book = new XSSFWorkbook(inSteam);
			
			for(XSSFSheet sheet : book){

				for(Row row : sheet){
					
					String key = null;
					if(row.getRowNum() == 0){
						key = sheet.getSheetName();
					}
					else{
						String name = getCellValue(row.getCell(4));
						if(Strings.isNullOrEmpty(name)){
							name =  getCellValue(row.getCell(5));
						}
						
						String[] obj = getCellValue(row.getCell(1)).split("\\.");
						if(obj[0].isEmpty()){
							obj = new String[]{getCellValue(row.getCell(2))};
						}
						
						key =  obj[obj.length-1] + "," + getCellValue(row.getCell(3)) +  "," +  name;
					}
					
					List<String> values = new ArrayList<String>();
					int count = 11;
					short lastCellNo = row.getLastCellNum();
					while(count < lastCellNo){
						values.add(getCellValue(row.getCell(count)));
						count++;
					};

					String[] vals = new String[]{};
					docMap.put(key, values.toArray(vals));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getCellValue(Cell cell){
		
		if(cell != null){
			return cell.getStringCellValue();
		}
		
		return null;
	}
	
	private MetaFile createExportFile(MetaFile metaFile){
		
		String date = LocalDateTime.now().toString("ddMMyyyy HH:mm:ss");
		String fileName = "Export " + date + ".xlsx";
		
		try {
			File file = File.createTempFile("Export", ".xlsx");
			FileOutputStream  outStream = new FileOutputStream(file);
			workBook.write(outStream);
			outStream.close();
			
			FileInputStream inStream = new FileInputStream(file);
			if(metaFile != null){
				metaFile.setFileName(fileName);
				metaFile = metaFiles.upload(inStream, metaFile);
			}
			else{
				metaFile = metaFiles.upload(inStream, fileName);
			}
			
			inStream.close();
			
			file.delete();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return metaFile;
	}
	
	
}
