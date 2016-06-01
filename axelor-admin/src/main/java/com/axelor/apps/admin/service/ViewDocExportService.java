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
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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
	
	private static final String[] PANEL_HEADERS = new String[]{
		"Module", 
		"Object", 
		"View", 
		"",
		"Panel name",
		"Panel title(EN)", 
		"Panel title(FR)"
	};
	
	private static final Set<String>fieldTypes = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);{
		fieldTypes.add("Toolbar Menu");
		fieldTypes.add("Toolbar MenuItem");
		fieldTypes.add("Panel");
		fieldTypes.add("Button");
		fieldTypes.add("Label");
		fieldTypes.add("Dashlet");
		fieldTypes.add("STRING");
		fieldTypes.add("INTEGER");
		fieldTypes.add("DECIMAL");
		fieldTypes.add("BOOLEAN");
		fieldTypes.add("TEXT");
		fieldTypes.add("DATE");
		fieldTypes.add("DATETIME");
		fieldTypes.add("LOCALDATETIME");
		fieldTypes.add("LOCALDATE");
		fieldTypes.add("LOCALTIME");
		fieldTypes.add("ONE_TO_MANY");
		fieldTypes.add("MANY_TO_ONE");
		fieldTypes.add("ONE_TO_ONE");
		fieldTypes.add("MANY_TO_MANY");
		fieldTypes.add("BINARY");
	};
	
	private XSSFWorkbook workBook;
	
	private XSSFWorkbook oldBook;
	
	private XSSFSheet sheet;
	
	private XSSFSheet oldSheet;
	
	private CellStyle style;
	
	private int rowCount;
	
	private String[] menuPath;
	
	private String rootMenu;
	
	private boolean onlyPanel;
	
	private List<String> processedMenus = new ArrayList<String>();
	
	private Map<String, Integer> docMap = new HashMap<String, Integer>();
	
	private Map<String, List<Integer>> commentMap = new HashMap<String, List<Integer>>();
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private MetaTranslationRepository translationRepo; 
	
	@Inject
	private MetaFiles metaFiles;
	
	@Inject
	private ViewDocXmlProcessor viewDocXmlProcessor;
	
	public MetaFile export(MetaFile docFile, boolean onlyPanel){
		
		this.onlyPanel = onlyPanel;
		
		List<MetaMenu> menus = metaMenuRepo.all().filter("self.parent is null and self.left = true").order("order").fetch();
		
		workBook = new XSSFWorkbook();
		addStyle();
		
		if(docFile != null && !onlyPanel){
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
	
	public boolean getOnlyPanel(){
		return onlyPanel;
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
		
		if(oldBook != null){
			oldSheet = oldBook.getSheet(sheet.getSheetName());
		}

		if(onlyPanel){
			writeRow(PANEL_HEADERS);
		}
		else{
			writeRow(HEADERS);
		}
	}
	
	protected void writeRow(String[] values){
		
		rowCount += 1;
		XSSFRow row = sheet.createRow(rowCount);
		
		int count = 0;
		count = writeCell(row, values, count, true);
		
		if(rowCount > 0 && !onlyPanel){
			count = writeCell(row, menuPath, count, true);
		}
		
		addDoc(row, values, count);
		
		menuPath = new String[]{"",""};
		
	}
	
	protected void writeRow(Integer rowIndex){
		
		rowCount += 1;
		XSSFRow row = sheet.createRow(rowCount);
		
		writeCell(row, rowIndex, 0, true);
		
		menuPath = new String[]{"",""};
		
	}
	
	private int writeCell(XSSFRow row, String[] values,  int count, boolean addStyle){
		
		int cellCount = 0;
		for(String value : values){
			cellCount++;
			if(onlyPanel){
				if(cellCount == 4){
					continue;
				}
				if(cellCount > 7){
					break;
				}
			}
			XSSFCell cell = row.createCell(count);
			if(addStyle){
				cell.setCellStyle(style);
			}
			cell.setCellValue(value);
			count++;
		}
		
		return count;
	}
	
	private void writeCell(XSSFRow row, Integer oldRowIndex,  int count, boolean addStyle){
		
		XSSFRow oldRow = oldSheet.getRow(oldRowIndex);
		
		while(count < oldRow.getLastCellNum()){
			XSSFCell oldCell = oldRow.getCell(count);

			XSSFCellStyle oldStyle = oldCell.getCellStyle();
			XSSFCellStyle newStyle = workBook.createCellStyle();
			newStyle.setFillBackgroundColor(oldStyle.getFillBackgroundXSSFColor());
			newStyle.setBorderBottom(oldStyle.getBorderBottom());
			newStyle.setBorderLeft(oldStyle.getBorderLeft());
			newStyle.setBorderRight(oldStyle.getBorderRight());
			newStyle.setBorderTop(oldStyle.getBorderTop());
			
			Cell cell = row.createCell(count);
			cell.setCellValue(oldCell.getStringCellValue());
			cell.setCellStyle(newStyle);
			count++;
		}
		
	}
	
	private void addDoc(XSSFRow row, String[] values, int count){
		
		 String name = values[4];
		 if(name == null){
			 name = values[5];
		 }
		 
		 String key = null;
		 if(row.getRowNum() == 0){
			 key = sheet.getSheetName();
		 }
		 else{
			 key =  values[2] + "," + values[3] + "," + name;
		 }
		 
		 if(docMap.containsKey(key) && oldSheet != null){
			 writeCell(row, docMap.get(key), count, false);	
		 }

		 if(commentMap.containsKey(key) && oldSheet != null){
			log.debug("Comment map rows: {}", commentMap.get(key).size());
			for(Integer rowIndex : commentMap.get(key)){
				writeRow(rowIndex);
			}
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
			
			oldBook = book;

			for(XSSFSheet sheet : book){
				String lastKey = sheet.getSheetName();
				Iterator<Row> rowIter = sheet.rowIterator();
				while(rowIter.hasNext()){
					Row row = rowIter.next();

					String key = null;
					if(row.getRowNum() == 0){
						key = sheet.getSheetName();
					}
					else{
						String name = getCellValue(row.getCell(4));
						if(Strings.isNullOrEmpty(name)){
							name =  getCellValue(row.getCell(5));
						}
						
						String type = getCellValue(row.getCell(3));
						if(type == null){
							continue;
						}
						key =  getCellValue(row.getCell(2)) 
							   + "," + type 
							   + "," +  name;
						
						if(addComment(lastKey, type, row)){
							continue;
						}
						else{
							lastKey = key;
						}
					}
					
					docMap.put(key, row.getRowNum());
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
	
	
	private boolean addComment(String lastKey, String type, Row row){
		
		String mType = type;
		if(type.contains("(")){
			mType = type.substring(0, type.indexOf("("));
			mType = mType.replace("-", "_");
		}
		
		if(!fieldTypes.contains(mType)){
			List<Integer> rowIndexs = new ArrayList<Integer>();
			if(commentMap.containsKey(lastKey)){
				rowIndexs = commentMap.get(lastKey);
			}
			
			rowIndexs.add(row.getRowNum());
			
			commentMap.put(lastKey, rowIndexs);
			
			return true;
		}
		
		return false;
	}
	
}
