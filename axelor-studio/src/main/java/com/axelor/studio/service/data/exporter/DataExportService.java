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
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.data.DataCommonService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class DataExportService extends DataCommonService {
	
	public int columns = HEADERS.length;
	
	public List<String> installed;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
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
	
	private XSSFWorkbook oldBook;
	
	private XSSFSheet sheet;
	
	private XSSFSheet menuSheet;
	
	private XSSFCellStyle style;
	
	private XSSFCellStyle green;
	
	private XSSFCellStyle lavender;
	
	private XSSFCellStyle violet;
	
	private XSSFCellStyle header;
	
	private String menuPath = null;
	
	private boolean onlyPanel = false;
	
	private List<String> processedMenus = new ArrayList<String>();
	
	private Map<String, Row> docMap = new HashMap<String, Row>();
	
	private Map<String, List<Row>> commentMap = new HashMap<String, List<Row>>();
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private MetaFiles metaFiles;
	
	@Inject
	private DataXmlService dataXmlService;
	
	@Inject
	private MetaModuleRepository metaModuleRepo;
	
	public MetaFile export(MetaFile oldFile, boolean onlyPanel) throws AxelorException {
		
		this.onlyPanel = onlyPanel;
		configService.config();
		
		if (onlyPanel) {
			columns = PANEL_HEADERS.length;
		}
		
		installed = new ArrayList<String>();
		List<MetaModule> modules = metaModuleRepo.all().filter("self.installed = true").fetch();
		for (MetaModule module : modules) {
			installed.add(module.getName());
		}
		installed.add(configService.getModuleName());
		
		List<MetaMenu> menus = metaMenuRepo.all().filter("self.parent is null "
				+ "and self.left = true "
				+ "and self.action is null "
				+ "and self.xmlId is null "
				+ "and self.module != 'axelor-core' "
				+ "and self.module in ?1", installed)
				.order("order").fetch();
		
		workBook = new XSSFWorkbook();
		addStyle();
		
		if  (oldFile != null && !onlyPanel) {
			updateDocMap(oldFile);
		}
		addStudioImport();
		
		menuSheet = workBook.createSheet("Menus");
		
		sheet = menuSheet;
		
		writeRow(HEADERS, false, false, false);
		
		processRootMenu(menus.iterator());
		
		setColumnWidth();

		return createExportFile(oldFile);
	}
	
	private void addStudioImport() {
		
		XSSFSheet sheet = workBook.createSheet("StudioImport");
		
		XSSFSheet oldSheet = null;
		
		if (oldBook != null) {
			oldSheet = oldBook.getSheetAt(0);
			log.debug("Old sheet name: {}", oldSheet.getSheetName());
		}
		
		if (oldSheet == null) {
			XSSFRow row = sheet.createRow(0);
			String[] titles = new String[]{"Object", "View", "Add/Replace"};
			
			int count = 0;
			for (String title : titles) {
				Cell cell = row.createCell(count);
				cell.setCellValue(title);
				count++;
			}
		}
		else {
			Iterator<Row> rowIter = oldSheet.rowIterator();
			while (rowIter.hasNext()) {
				Row oldRow = rowIter.next();
				XSSFRow row = sheet.createRow(oldRow.getRowNum());
				Iterator<Cell> cellIter = oldRow.cellIterator();
				while (cellIter.hasNext()) {
					Cell oldCell = cellIter.next();
					Cell cell = row.createCell(oldCell.getColumnIndex());
					cell.setCellValue(oldCell.getStringCellValue());
				}
			}
		}
		
	}
	
	private void addStyle() {
		
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
		
		header = workBook.createCellStyle();
		header.cloneStyleFrom(green);
		header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
		XSSFFont font = workBook.createFont();
		font.setBold(true);
		header.setFont(font);
		
	}
	
	public boolean getOnlyPanel() {
		return onlyPanel;
	}
	
	private void processRootMenu(Iterator<MetaMenu> rootMenuIter) {
		
		if (!rootMenuIter.hasNext()) {
			return;
		}
		
		MetaMenu menu = rootMenuIter.next();
		String name = menu.getName();
		
		if (!processedMenus.contains(name)) {
			String title = menu.getTitle();
			if (workBook.getSheetIndex(title) >= 0) {
				title = title + "(" + menu.getId() + ")";
			}
			createSheet(title);
			updateMenuPath(menu);
			addMenu(menu, null, 1);
			processMenu(name, 1);
			processedMenus.add(name);
		}
		
		processRootMenu(rootMenuIter);
	}
	
	protected void setMenuPath(String menuPath) {
		this.menuPath = menuPath;
	}
	
	private void setColumnWidth() {
		
		Iterator<XSSFSheet> sheets = workBook.iterator();

		while (sheets.hasNext()) {
			sheet = sheets.next();
			sheet.createFreezePane(0, 1, 0, 1);
			int count = 0;
			while (count < HEADERS.length) {
				sheet.autoSizeColumn(count);
				count++;
			}
		}
	}
	
	private void addGeneralRow(String[] values) {
			
		XSSFRow row = sheet.createRow(sheet.getPhysicalNumberOfRows());
		String[] vals = new String[columns];
		vals[MODULE] = values[MODULE];
		vals[MODEL] = values[MODEL];
		vals[VIEW] = values[VIEW];
		vals[TYPE] = "general"; 
		
		if (menuPath != null) {
			vals[MENU] = menuPath;
			menuPath = null;
		}
			
		int count = writeCell(row, vals, 0, green);
		
		addDoc(row, vals, count, green, null);
		
	}
	
	private void createSheet(String rootMenu) {
		
		log.debug("Root menu: {}", rootMenu);
		menuPath = null;
		sheet = workBook.createSheet(I18n.get(rootMenu));
		
		if (onlyPanel) {
			writeRow(PANEL_HEADERS, false, false, false);
		}
		else{
			writeRow(HEADERS, false, false, false);
		}
		
	}
	
	protected void writeRow(String[] values, boolean newForm, 
			boolean newPanel, boolean newSubPanel) {
		
		if(newForm){
			addGeneralRow(values);
		}
		
		int size = sheet.getPhysicalNumberOfRows();
		XSSFCellStyle cellStyle = style;
		if (size == 0){
			cellStyle = header;
		}
		XSSFRow row = sheet.createRow(size);
		
		 
		if (!onlyPanel && newPanel) {
			cellStyle = violet;
		}
		
		int count = writeCell(row, values, 0, cellStyle);
		
		if (!onlyPanel) { 
			if (menuPath != null) {
				writeCell(row, 
						new String[]{menuPath}, 
						MENU, 
						cellStyle);
				menuPath = null;
			}
			
			addDoc(row, values, count, cellStyle, null);
		}
		
	}
	
	protected Integer writeRow(Row oldRow, XSSFCellStyle cellStyle, 
			int count, Integer rowCount) {
		
		XSSFRow row = null;
		
		int totalRows = sheet.getPhysicalNumberOfRows();
		
		if (rowCount == null) {
			row = sheet.createRow(totalRows);
		}
		else {
			rowCount++;
			if (rowCount < totalRows) {
				sheet.shiftRows(rowCount, totalRows, 1);
			}
			row = sheet.createRow(rowCount);
		}
		
		writeCell(row, oldRow, count, cellStyle);
		
		menuPath = "";
		
		return rowCount;
	}
	
	private int writeCell(XSSFRow row, String[] values,
			int count,  XSSFCellStyle cellStyle) {
		
		int cellCount = 0;
		
		for (String value : values) {
			
			if (onlyPanel) {
				cellCount++;
				if (cellCount == 4) {
					continue;
				}
				if (cellCount > 7) {
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
	
	private void writeCell(XSSFRow row, Row oldRow, int count,
			XSSFCellStyle cellStyle){
		
		while (count < oldRow.getLastCellNum()) {
			count++;
			
			String val = getValue(oldRow, count-1);
			if (val == null) {
				continue;
			}
			Cell cell = row.createCell(count-1);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(val);
		}
		
	}
	
	private Integer addDoc(XSSFRow row, String[] values, int count, 
			XSSFCellStyle cellStyle, Integer rowCount)  {
		
		 String name = values[NAME];
		 if (name == null) {
			 name = values[TITLE];
			 if (name == null) {
				 name = values[TITLE_FR];
			 }
		 }
		 
		 String key = null;
		 if (row.getRowNum() == 0) {
			 key = sheet.getSheetName();
		 }
		 else {
			 String model = values[MODEL];
			 if (model != null) {
				 String[] modelSplit = model.split("\\.");
				 model = modelSplit[modelSplit.length - 1];
			 }
			 key =  model
					+ "," + values[VIEW]
					+ "," + getFieldType(values[TYPE]) 
				    + "," + name;
			 
		 }
		 
		 if (docMap.containsKey(key)) {
			 writeCell(row, docMap.get(key), count, cellStyle);	
		 }

		 if (commentMap.containsKey(key)) {
			for (Row oldRow : commentMap.get(key)){
				rowCount = writeRow(oldRow, style, 0, rowCount);
			}
		 }
		 
		 return rowCount;
	}
	
	private int processMenu(String parentMenu, int count){
		
		List<MetaMenu> subMenus = metaMenuRepo.all()
				.filter("self.parent.name = ?", parentMenu)
				.order("order").fetch();
		
		for (MetaMenu subMenu : subMenus) {
			
			count++;
			
			MetaAction action = subMenu.getAction();
			
			if (action == null) {
				if (!onlyPanel) {
					updateMenuPath(subMenu);
					count = addMenu(subMenu, "", count);
				}
				count = processMenu(subMenu.getName(), count);
				continue;
			}	
			
			String model = action.getModel();
			
			updateMenuPath(subMenu);
			
			if (!onlyPanel) {
				count = addMenu(subMenu, getModelName(model), count);
			}
			
			if (action.getType().equals("action-view")) {
				dataXmlService.processAction(model, action);
			}
			
			
		}
		
		return count;
	}
	
	private int addMenu(MetaMenu subMenu, String model, int rowCount) {
		
//		menuSheet.shiftRows(rowCount, menuSheet.getPhysicalNumberOfRows(), 1);
		
		XSSFRow row = menuSheet.createRow(menuSheet.getPhysicalNumberOfRows());
		
		String[] menu = menuPath.split("/");
		
		String type = "menu";
		
		if (subMenu.getParent() != null) {
			type += "(" + subMenu.getParent().getName() + ")";
		}
		
		String[] values = new String[columns];
		values[MODULE] = subMenu.getModule();
		values[MODEL] = model;
		values[NAME] = subMenu.getName();
		values[TITLE] = menu[menu.length - 1];
		values[TITLE_FR] = getTranslation(values[TITLE], "fr");
		values[TYPE] = type;
		values[MENU] = menuPath;
		
		int count = writeCell(row, values, 0, style);
		
		return addDoc(row, values, count, style, rowCount);
	}
	
	private void updateMenuPath(MetaMenu metaMenu) {
		
		List<String> menus = new ArrayList<String>();
		menus.add(metaMenu.getTitle());
		
		addParentMenus(menus, metaMenu);
		
		Collections.reverse(menus);
		
		menuPath = Joiner.on("/").join(menus);
//		for (String menu : menus) {
//			
//			if (menuPath == null) {
//				menuPath = menu;
//			}
//			else {
//				menuPath += "/" + menu;
//			}
//		}
		
	}
	
	private void addParentMenus(List<String> menus, MetaMenu metaMenu) {
		
		MetaMenu parentMenu = metaMenu.getParent();
		
		if (parentMenu != null) {
			menus.add(parentMenu.getTitle());
			addParentMenus(menus, parentMenu);
		}
	}
	

	private void updateDocMap(MetaFile docFile) {
		
		try {
			File doc = MetaFiles.getPath(docFile).toFile();
			FileInputStream inSteam = new FileInputStream(doc);
			oldBook = new XSSFWorkbook(inSteam);
			
			for (XSSFSheet sheet : oldBook) {
				String lastKey = sheet.getSheetName();
				Iterator<Row> rowIter = sheet.rowIterator();
				
				while(rowIter.hasNext()) {
					Row row = rowIter.next();

					String key = null;
					if (row.getRowNum() == 0) {
						key = sheet.getSheetName();
						continue;
					}
					
					String name = getFieldName(row);
					String type = getValue(row, TYPE);
					if (type == null) {
						continue;
					}
					
					String model = getValue(row, MODEL);
					if (model != null) {
						model = inflector.camelize(model);
					}
					
					String view = getValue(row, VIEW);
					if (model != null && view == null) {
						view = ViewLoaderService.getDefaultViewName(model, "form");
					}
					
					key =  model + "," + view + "," + getFieldType(type) + "," +  name;
					
					if (addComment(lastKey, type, row)) {
						continue;
					}
					
					lastKey = key;
					docMap.put(key, row);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private MetaFile createExportFile(MetaFile metaFile) {
		
		String date = LocalDateTime.now().toString("ddMMyyyy HH:mm:ss");
		String fileName = "Export " + date + ".xlsx";
		
		try {
			File file = File.createTempFile("Export", ".xlsx");
			FileOutputStream  outStream = new FileOutputStream(file);
			workBook.write(outStream);
			outStream.close();
			
			FileInputStream inStream = new FileInputStream(file);
			if (metaFile != null) {
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
	
	private boolean addComment(String lastKey, String type, Row row) {
		
		if (type.contains("(")) {
			type = type.substring(0, type.indexOf("("));
		}
//		type = type.replace("-", "_");
		
		if (!fieldTypes.containsKey(type) && !viewElements.containsKey(type)) {
			log.debug("Not contains type: {}", type);
//			if(firstCell != null){
				List<Row> rows = new ArrayList<Row>();
				if (commentMap.containsKey(lastKey)) {
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
		
		String name = getValue(row, NAME);
		
		if (Strings.isNullOrEmpty(name)) {
			name =  getValue(row, TITLE);
			if (Strings.isNullOrEmpty(name)) {
				name = getValue(row, TITLE_FR);
			}
			if (!Strings.isNullOrEmpty(name)) {
				name = getFieldName(name);
			}
		}
		
		return name;
	}
	
	private String getFieldType(String type) {
		
		if (type == null) {
			return type;
		}
		type = type.trim();
		
		if (type.contains("(")) {
			type = type.substring(0, type.indexOf("("));
		}
		
		if(frMap.containsKey(type)) {
			type = frMap.get(type);
		}
		
		if (fieldTypes.containsKey(type)) {
			type = fieldTypes.get(type);
		}
		else if (viewElements.containsKey(type)) {
			type = viewElements.get(type);
		}
		
		type = type.toUpperCase();
		
		if (type.startsWith("PANEL")) {
			return "PANEL";
		}
		
		if (type.startsWith("WIZARD")) {
			return "BUTTON";
		}
		
		return type.replace("-", "_");
	}
	
	private String getModelName(String name) {
		
		if (name == null) {
			return name;
		}
		
		String[] names = name.split("\\.");
		
		return names[names.length-1];
	}
	
}
