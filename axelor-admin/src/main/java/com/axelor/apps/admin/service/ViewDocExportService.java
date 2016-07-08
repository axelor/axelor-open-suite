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
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
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
import com.axelor.studio.service.importer.ImporterService;
import com.axelor.studio.service.importer.ModelImporterService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ViewDocExportService {
	
	protected Set<String>fieldTypes = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);{
		fieldTypes.add("Toolbar Menu");
		fieldTypes.add("Toolbar MenuItem");
		fieldTypes.add("Panel");
		fieldTypes.add("SubPanel");
		fieldTypes.add("Button");
		fieldTypes.add("Label");
		fieldTypes.add("Dashlet");
		fieldTypes.add("STRING");
		fieldTypes.add("INTEGER");
		fieldTypes.add("DECIMAL");
		fieldTypes.add("BOOLEAN");
		fieldTypes.add("TEXT");
		fieldTypes.add("DATE");
		fieldTypes.add("LONG");
		fieldTypes.add("TIME");
		fieldTypes.add("DATETIME");
		fieldTypes.add("LOCALDATETIME");
		fieldTypes.add("LOCALDATE");
		fieldTypes.add("LOCALTIME");
		fieldTypes.add("ONE_TO_MANY");
		fieldTypes.add("MANY_TO_ONE");
		fieldTypes.add("ONE_TO_ONE");
		fieldTypes.add("MANY_TO_MANY");
		fieldTypes.add("BINARY");
		fieldTypes.add("general");
		fieldTypes.add("EMPTY");
		fieldTypes.add("MENU");
	};
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private static final String[] HEADERS = new String[]{
		"Module", 
		"Object", 
		"View", 
		"Field name", 
		"Field title(EN)", 
		"Field title(FR)",
		"Field type", 
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
	
	private XSSFWorkbook workBook;
	
//	private XSSFWorkbook oldBook;
	
	private XSSFSheet sheet;
	
//	private XSSFSheet oldSheet;
	
	private XSSFCellStyle style;
	
	private XSSFCellStyle green;
	
	private XSSFCellStyle lavender;
	
	private XSSFCellStyle violet;
	
	private String[] menuPath = null;
	
	private String rootMenu;
	
	private boolean onlyPanel = false;
	
	private List<String> processedMenus = new ArrayList<String>();
	
	private Map<String, Row> docMap = new HashMap<String, Row>();
	
	private Map<String, List<Row>> commentMap = new HashMap<String, List<Row>>();
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private MetaTranslationRepository translationRepo; 
	
	@Inject
	private MetaFiles metaFiles;
	
	@Inject
	private ViewDocXmlProcessor viewDocXmlProcessor;
	
	@Inject
	private ModelImporterService modelImporterService;
	
	public MetaFile export(MetaFile docFile, boolean onlyPanel){
		
		this.onlyPanel = onlyPanel;
		
		List<MetaMenu> menus = metaMenuRepo.all().filter("self.parent is null "
				+ "and self.left = true and self.action is null "
				+ "and self.module != 'axelor-core'").order("order").fetch();
		
		workBook = new XSSFWorkbook();
		addStyle();
		
		if(docFile != null && !onlyPanel){
			updateDocMap(docFile);
		}
		
		processRootMenu(menus.iterator());
		
		setColumnWidth();

		return createExportFile(docFile);
	}
	
	private void addStyle(){
		
		style = workBook.createCellStyle();
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		
		green = workBook.createCellStyle();
		green.cloneStyleFrom(style);
		green.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.index);
		green.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		lavender = workBook.createCellStyle();
		lavender.cloneStyleFrom(green);
		lavender.setFillForegroundColor(IndexedColors.LAVENDER.index);
		
		violet = workBook.createCellStyle();
		violet.cloneStyleFrom(green);
		violet.setFillForegroundColor(IndexedColors.VIOLET.index);
		
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
			processMenu(name, 0);
			processedMenus.add(name);
		}
		
		processRootMenu(rootMenuIter);
	}
	
	protected void setMenuPath(String[] menuPath){
		this.menuPath = menuPath;
	}
	
	private void setColumnWidth(){
		
		Iterator<XSSFSheet> sheets = workBook.iterator();

		while(sheets.hasNext()){
			sheet = sheets.next();
			sheet.createFreezePane(0, 1, 0, 1);
			int count = 0;
			while(count < HEADERS.length){
				sheet.autoSizeColumn(count);
				count++;
			}
		}
	}
	
	private void addGeneralRow(String[] values){
			
		XSSFRow row = sheet.createRow(sheet.getPhysicalNumberOfRows());
		String[] vals = { 
			values[0],
			values[1],
			values[2],
			"", "", "", "general", "", "", "", ""
		};
		
		if(menuPath != null){
			vals[9] = menuPath[0];
			vals[10] = menuPath[1];
		}
			
		int count = writeCell(row, vals, 0, green);
		
		addDoc(row, vals, count, green, null);
		
	}
	
	private void createSheet(){
		
		log.debug("Root menu: {}", rootMenu);
		menuPath = null;
		sheet = workBook.createSheet(I18n.get(rootMenu));
		
//		if(oldBook != null){
//			oldSheet = oldBook.getSheet(sheet.getSheetName());
//		}
		
		if(onlyPanel){
			writeRow(PANEL_HEADERS, false, false, false);
		}
		else{
			writeRow(HEADERS, false, false, false);
		}
		
	}
	
	protected void writeRow(String[] values, boolean newForm, boolean newPanel, boolean newSubPanel){
		
		if(newForm){
			addGeneralRow(values);
			menuPath = new String[]{"",""};
		}
		
		XSSFRow row = sheet.createRow(sheet.getPhysicalNumberOfRows());
		XSSFCellStyle cellStyle = style;
		 
		if(!onlyPanel){
			if(newPanel){
				cellStyle = violet;
			}
			else if (newSubPanel) {
				cellStyle = lavender;
			}
		}
		
		int count = writeCell(row, values, 0, cellStyle);
		
		if(!onlyPanel){ 
			if(menuPath != null){
				count = writeCell(row, menuPath, count, cellStyle);
			}
			
			addDoc(row, values, count, cellStyle, null);
		}
		
		menuPath = new String[]{"",""};
		
	}
	
	protected Integer writeRow(Row oldRow, XSSFCellStyle cellStyle, int count, Integer rowCount){
		
		XSSFRow row = null;
		
		int totalRows = sheet.getPhysicalNumberOfRows();
		if(rowCount == null){
			row = sheet.createRow(totalRows);
		}
		else{
			rowCount++;
			if(rowCount < totalRows){
				sheet.shiftRows(rowCount, totalRows, 1);
			}
			row = sheet.createRow(rowCount);
		}
		
		writeCell(row, oldRow, count, cellStyle);
		
		menuPath = new String[]{"",""};
		
		return rowCount;
	}
	
	private int writeCell(XSSFRow row, String[] values, int count,  XSSFCellStyle cellStyle){
		
		int cellCount = 0;
		
		for(String value : values){
			
			if(onlyPanel){
				cellCount++;
				if(cellCount == 4){
					continue;
				}
				if(cellCount > 7){
					break;
				}
			}
			
			XSSFCell cell = row.createCell(count);
			cell.setCellStyle(cellStyle);
			
			cell.setCellValue(value);
			
			count++;
		}
		
		return count;
		
	}
	
	private void writeCell(XSSFRow row, Row oldRow, int count, XSSFCellStyle cellStyle){
		
		while(count < oldRow.getLastCellNum()){
			Cell oldCell = oldRow.getCell(count);
			Cell cell = row.createCell(count);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(oldCell.getStringCellValue());
			count++;
		}
		
	}
	
	private Integer addDoc(XSSFRow row, String[] values, int count, XSSFCellStyle cellStyle, Integer rowCount){
		
		 String name = values[3];
		 if(name == null){
			 name = values[4];
		 }
		 
		 String key = null;
		 if(row.getRowNum() == 0){
			 key = sheet.getSheetName();
		 }
		 else{
			 String model = values[1];
			 if (model != null) {
				 String[] modelSplit = values[1].split("\\.");
				 model = modelSplit[modelSplit.length - 1];
			 }
			 String type = values[6].toUpperCase();
			 if (type != null && type.contains("PANEL")) {
				 type = "PANEL";
			 }
			 key =  model
					+ "," + type 
				    + "," + name;
			 
		 }
		 
		 if (docMap.containsKey(key)) {
			 writeCell(row, docMap.get(key), count, cellStyle);	
		 }

		 if (commentMap.containsKey(key)) {
			for(Row oldRow : commentMap.get(key)){
				rowCount = writeRow(oldRow, style, 0, rowCount);
			}
		 }
		 
		 return rowCount;
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
	
	private int processMenu(String parentMenu, int count){
		
		List<MetaMenu> subMenus = metaMenuRepo.all()
				.filter("self.parent.name = ?", parentMenu)
				.order("order").fetch();
		
		if(sheet == null){
			createSheet();
		}
		
		for(MetaMenu subMenu : subMenus){
			
			count++;
//			log.debug("Processing sub menu: {}", subMenu.getName());
			
			MetaAction action = subMenu.getAction();
			
			if(action == null){
				if(!onlyPanel){
					String[] paths = updateMenuPath(subMenu);
					count = addMenu(subMenu, "", paths, count);
				}
				count = processMenu(subMenu.getName(), count);
				continue;
			}	
			
			String model = action.getModel();
			
			String[] paths = updateMenuPath(subMenu);
			if(action.getType().equals("action-view")){
				viewDocXmlProcessor.processModel(model, action);
			}
			
			if(!onlyPanel){
				count = addMenu(subMenu, model, paths, count);
			}
			
		}
		
		return count;
	}
	
	private int addMenu(MetaMenu subMenu, String model, String[] paths, int rowCount){
		
		sheet.shiftRows(rowCount, sheet.getPhysicalNumberOfRows(), 1);
		
		XSSFRow row = sheet.createRow(rowCount);
		
		String[] menuEn = paths[0].split("/");
		String[] menuFr = paths[1].split("/");
		
		String[] values = new String[]{
		    subMenu.getModule(),
		    model,
		    "",
		    subMenu.getName(),
		    menuEn[menuEn.length - 1],
		    menuFr[menuFr.length - 1],
		    "MENU",
		    "",
		    "",
		    paths[0],
		    paths[1]
		};
		
		int count = writeCell(row, values, 0, style);
		
		return addDoc(row, values, count, style, rowCount);
	}
	
	private String[] updateMenuPath(MetaMenu metaMenu){
		
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
		
		String[] paths = new String[]{menuEN,menuFR};
		menuPath = paths;
		
		return paths;
	}
	
	private void addParentMenus(List<String> menus, MetaMenu metaMenu){
		
		MetaMenu parentMenu = metaMenu.getParent();
		
		if(parentMenu != null){
			menus.add(parentMenu.getTitle());
			addParentMenus(menus, parentMenu);
		}
	}
	

	private void updateDocMap(MetaFile docFile) {
		
		try {
			File doc = MetaFiles.getPath(docFile).toFile();
			FileInputStream inSteam = new FileInputStream(doc);
			XSSFWorkbook book = new XSSFWorkbook(inSteam);
			
//			oldBook = book;

			for(XSSFSheet sheet : book) {
				String lastKey = sheet.getSheetName();
				Iterator<Row> rowIter = sheet.rowIterator();
				
				while(rowIter.hasNext()) {
					Row row = rowIter.next();

					String key = null;
					if(row.getRowNum() == 0){
						key = sheet.getSheetName();
						continue;
					}
					
					String name = getFieldName(row);
					String type = getFieldType(row);
					if (type == null) {
						continue;
					}
					
					String model = getCellValue(row.getCell(1));
					if (model != null) {
						model = modelImporterService.inflector.camelize(model);
					}
					key =  model + "," + type + "," +  name;
					 
					if(addComment(lastKey, type, row)){
						continue;
					}
					lastKey = key;
					log.debug("Put key: {}", key);
					docMap.put(key, row);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getCellValue(Cell cell){
		
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
			log.debug("Not contains: {}", mType);
//			if(firstCell != null){
				List<Row> rows = new ArrayList<Row>();
				if(commentMap.containsKey(lastKey)){
					rows = commentMap.get(lastKey);
				}
				
				rows.add(row);
				
				commentMap.put(lastKey, rows);
				
				return true;
//			}
		}
		
		return false;
	}
	
	private String getFieldName(Row row) {
		
		String name = getCellValue(row.getCell(3));
		if(Strings.isNullOrEmpty(name)){
			name =  getCellValue(row.getCell(4));
			if(Strings.isNullOrEmpty(name)){
				name =  getCellValue(row.getCell(5));
			}
			if(!Strings.isNullOrEmpty(name)){
				name = modelImporterService.getFieldName(name);
			}
		}
		
		return name;
	}
	
	private String getFieldType(Row row) {
		
		String type = getCellValue(row.getCell(6));
		if(type == null){
			return type;
		}
		type = type.trim();
		
		if (ImporterService.typeMap.containsKey(type)) {
			type = ImporterService.typeMap.get(type);
		}
		
		type = type.toUpperCase();
		
		if (type.startsWith("PANEL")) {
			return "PANEL";
		}
		
		return type;
	}
}
