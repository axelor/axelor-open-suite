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
package com.axelor.studio.service.data.importer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.data.DataCommonService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class DataValidatorService extends DataCommonService {
	
	private final static List<String> ignoreNames = new ArrayList<String>();
	{	
		ignoreNames.add("panel");
		ignoreNames.add("panelside");
		ignoreNames.add("panelbook");
		ignoreNames.add("error");
		ignoreNames.add("warning");
		ignoreNames.add("onsave");
		ignoreNames.add("onnew");
		ignoreNames.add("onload");
		ignoreNames.add("spacer");
		ignoreNames.add("label");
		ignoreNames.add("dashlet");
	}
	
	private static final String sumPattern = "sum\\(([^;^:]+;[^;^:]+(:[^:^;]+)?)\\)";
	
	private static final String seqPattern = "seq\\(([\\d]+(:[^:]+)?(:[^:]+)?)\\)";
	
	private static final List<String> panelTabTypes = Arrays.asList(new String[]{"o2m","m2m","dashlet","paneltab"});
	
	private File logFile;
	
	private XSSFWorkbook logBook;
	
	private Map<String, List<String>> modelMap;
	
	private Map<String, Object[]> viewPanelMap;
	
	private Map<String, Row> invalidModelMap;
	
	private Map<String, String> referenceMap;
	
	private Map<String, Map<String, Row>> invalidFieldMap;
	
	private List<String> menus;
	
	private Row row;
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private ConfigurationService configService;
	
	public File validate(XSSFWorkbook workBook) throws IOException {
	
		logFile = null;
		modelMap = new HashMap<String, List<String>>();
		viewPanelMap = new HashMap<String, Object[]>();
		invalidModelMap = new HashMap<String, Row>();
		invalidFieldMap = new HashMap<String, Map<String,Row>>();
		referenceMap = new HashMap<String, String>();
		menus = new ArrayList<String>();
		Iterator<XSSFSheet> sheetIter = workBook.iterator();
		validateModules(sheetIter.next());
		
		while (sheetIter.hasNext()) {
			XSSFSheet sheet = sheetIter.next();
			Iterator<Row> rowIter = sheet.rowIterator();
			if (rowIter.hasNext()) {
				rowIter.next();
			}
			validateRow(rowIter);
		}
		
		checkInvalid();
		
		if (logBook != null) {
			logBook.write(new FileOutputStream(logFile));
		}
			
		return logFile;
	}
	
	private void validateModules(XSSFSheet sheet) throws IOException {
		
		Iterator<Row> rowIterator = sheet.rowIterator();
		
		while (rowIterator.hasNext()) {
			
			Row row = rowIterator.next();
			if (row.getRowNum() == 0 ) {
				continue;
			}
			
			String name = getValue(row, 0);
			try {
				configService.validateModuleName(name);
			} catch (AxelorException e) {
				addLog(e.getMessage(), row);
			}
			
			String depends = getValue(row, 1);
			if (depends != null && Arrays.asList(depends.split(",")).contains(name)) {
				addLog(I18n.get("Module's depends must not contain its name"), row);
			}
			
			String title = getValue(row, 2);
			String version = getValue(row, 3);
			if (title == null || version == null) {
				addLog(I18n.get("Title or module version is empty"), row);
			}
		}
		
	}
	
	private void validateRow(Iterator<Row> rowIter) throws IOException{
		
		if(!rowIter.hasNext()){
			return;
		}
		
		this.row = rowIter.next();
		
		String module = getValue(row, MODULE);
		if (module == null) {
			validateRow(rowIter);
			return;
		}
		
		module = module.replace("*", "");
		if (configService.getNonCustomizedModules().contains(module)) {
			validateRow(rowIter);
			return;
		}
		
		try {
			configService.validateModuleName(module);
		} catch (AxelorException e) {
			addLog(e.getMessage());
		}
		
		String model = getModel();
		
		if (validateField(model)) {
			try {
				configService.validateModelName(model);
			} catch (AxelorException e) {
				addLog(e.getMessage());
			}
		}
		
		validateRow(rowIter);
	}
	
	private String getModel() throws IOException {
		
		String model = getValue(row, MODEL);
		
		if (model == null) {
			return null;
		}
		
		String[] models = model.split("\\(");
		model = models[0];
		
		if (referenceMap.containsKey(model)) {
			model = referenceMap.get(model);
		}
		
		if (!modelMap.containsKey(model)) {
			modelMap.put(model, new ArrayList<String>());
		}
		
		if (models.length > 1){
			String reference = models[1].replace(")","");
			if (!modelMap.get(model).contains(reference)) {
				addLog("No nested reference field found");
			}
		}
		
		invalidModelMap.remove(model);
			
		return model;
		
	}
	
	private boolean validateField(String model) throws IOException {
		
		boolean modelRequired = false;
		
		String type = validateType();
		if (type != null) {
			if (ignoreTypes.contains(type)) {
				return modelRequired;
			}
			if(!type.startsWith("menu") && !type.startsWith("dashlet")) {
				modelRequired = true;
			}
		}
		
		modelRequired = validateName(type, model, modelRequired);
		
		if(type != null && type.startsWith("menu")){
			return false;
		}
		
		modelRequired = checkSelect(row, type, modelRequired);
		modelRequired = checkFormula(modelRequired);
		modelRequired = checkEvents(model, modelRequired);
		
		if (modelRequired && type == null) {
			addLog(I18n.get("No type defined"));
		}
		
		return modelRequired;
	}
	
	private boolean validateName(String type, String obj,
			boolean consider) throws IOException {
		
		String name = getValue(row, NAME);
		String title = getValue(row, TITLE);
		if (title == null) {
			title = getValue(row, TITLE_FR);
		}
		if (name == null) {
			name = title;
		}
		if (name != null) {
			if (type != null && type.startsWith("menu")) {
				if (title == null) {
					addLog(I18n.get("Title required for menu."));
				}
				menus.add(name);
			}
			name = getFieldName(name);
		}
		
		if (!Strings.isNullOrEmpty(name)) {
			if (type == null) {
				consider = true;
			}
			if (modelMap.containsKey(obj)) {
				modelMap.get(obj).add(name);
			}
			if (invalidFieldMap.containsKey(obj)) {
				invalidFieldMap.get(obj).remove(name);
			}
			try {
				configService.validateFieldName(name);
			} catch (AxelorException e) {
				addLog(e.getMessage());
			}
		}
		else if (type != null 
				&& !ignoreNames.contains(type) 
				&& !ignoreTypes.contains(type)) {
			addLog(I18n.get("Name and title empty or name is invalid."));
		}
		
		return consider;
		
	}

	private boolean checkSelect(Row row, String type, boolean consider) throws IOException {
		
		String select = getValue(row, SELECT);
		
		if (select == null) {
			select = getValue(row, SELECT_FR);
		}
		
		if (select != null
					&& type != null 
					&& !type.equals("select") 
					&& !type.equals("multiselect")){
				addLog(I18n.get("Selection defined for non select field. "
						+ "Please check the type"));
			
			return true;
		}

		return consider;
	}
	
	private void addLog(String log) throws IOException {
		addLog(log, null);
	}
	
	
	private void addLog(String log, Row row) throws IOException {
		
		if (row == null) {
			row = this.row;
		}
		
		if (logFile == null) {
			logFile = File.createTempFile("ModelImportLog", ".xlsx");
			logBook = new XSSFWorkbook();
		}
		
		String sheetName = row.getSheet().getSheetName();
		XSSFSheet sheet = logBook.getSheet(sheetName);
		
		if (sheet == null) {
			sheet = logBook.createSheet(sheetName);
			XSSFRow titleRow = sheet.createRow(0);
			titleRow.createCell(0).setCellValue("Row Number");
			titleRow.createCell(1).setCellValue("Issues");
		}
		
		Iterator<Row> rowIterator = sheet.rowIterator();
		Row logRow = null;
		while (rowIterator.hasNext()) {
			Row sheetRow = rowIterator.next();
			Cell cell = sheetRow.getCell(0);
			if(cell.getCellType() != Cell.CELL_TYPE_NUMERIC){
				continue;
			}
			double value = cell.getNumericCellValue();
			if (value == row.getRowNum() + 1) {
				logRow = sheetRow;
				break;
			}
		}
	
		if (logRow == null){
			logRow = sheet.createRow(sheet.getPhysicalNumberOfRows());
		}
		
		Cell cell = logRow.getCell(0);
		if (cell == null) {
			cell = logRow.createCell(0);
			cell.setCellValue(row.getRowNum() + 1);
		}
		cell = logRow.getCell(1);
		if (cell == null) {
			cell = logRow.createCell(1);
		}
		String oldValue = cell.getStringCellValue();
		if (oldValue == null) {
			cell.setCellValue(log);
		}
		else { 
			cell.setCellValue(oldValue + "\n" + log);
		}
		
		
	}
	
	private String validateType() throws IOException {
		
		String type = getValue(row, TYPE);
		if(type == null){
			return type;
		}
		type = type.trim();
		
		String reference = null;
		if (type.contains("(")) {
			String[] ref = type.split("\\(");
			if(ref.length > 1){
				reference = ref[1].replace(")","");
			}
			type = ref[0];
		}
		
		if (!fieldTypes.containsKey(type) 
				&& !frMap.containsKey(type) && !viewElements.containsKey(type)) {
			addLog(I18n.get("Invalid type"));
		}
		
		if (referenceTypes.contains(type)) { 
			if (reference == null) {
				addLog(I18n.get("Reference is empty for type"));
			}
			else  if (!modelMap.containsKey(reference) 
					&& !invalidModelMap.containsKey(reference)) {
				invalidModelMap.put(reference, row);
				referenceMap.put(getValue(row, MODEL) + "(" + getValue(row, NAME) + ")" , reference);
			}
			
		}
		
		if (type.equals("menu") && reference != null) {
			MetaMenu menu = metaMenuRepo.all().filter("self.name = ?1" , reference).fetchOne();
			if(!menus.contains(reference) && menu == null){
				addLog(I18n.get("No parent menu defined"));
			}
		}
		
		if(type != null && !ignoreTypes.contains(type) && !type.equals("menu")){
			checkViewPanelType(type, reference);
		}
		
		return type;
	}
	
	private boolean checkEvents(String obj, boolean consider) throws IOException {
		
		String formula = getValue(row, FORMULA);
		String event = getValue(row, EVENT);
		
		if (event == null) {
			return consider;
		}
		
		consider = true;
		
		if (formula == null) {
			addLog(I18n.get("Formula is empty but event specified"));
		}
		for (String evt : event.split(",")) {
			evt = evt.trim();
			if (evt.equals("save") || evt.equals("new")) {
				continue;
			}
			if (modelMap.containsKey(obj) 
					&& modelMap.get(obj).contains(evt)) {
				continue;
			}
			if (!invalidFieldMap.containsKey(obj)) {
				invalidFieldMap.put(obj, new HashMap<String, Row>());
			}
			invalidFieldMap.get(obj).put(evt, row);
		}
		
		return consider;
		
	}
	
	private void checkInvalid() throws IOException {
		
		if (!invalidModelMap.isEmpty()) {
			List<MetaModel> models = metaModelRepo.all()
					.filter("self.name in ?1", invalidModelMap.keySet())
					.fetch();
			
			for(MetaModel model : models){
				invalidModelMap.remove(model.getName());
			}
			
			for (Row row : invalidModelMap.values()) {
				addLog("Invalid reference model", row);
			}
		}
		
		for (Map<String, Row> fieldMap : invalidFieldMap.values()) {
			
			Set<Row> rowSet = new HashSet<Row>();
			rowSet.addAll(fieldMap.values());
			
			for (Row row : rowSet) {
				addLog("Invalid event field reference", row);
			}
		}
		
		for(Object[] panel : viewPanelMap.values()) {
			if(panel[0].equals("panelbook")) {
				addLog(I18n.get("Panelbook must follow by paneltab"));
			}
		}
		
	}
	
	private void checkViewPanelType(String type, String reference) throws IOException{
		
		String view = getValue(row, VIEW);
		if (view == null) {
			view = getValue(row, MODEL);
		}
		
		if (view == null) {
			return;
		}
		
		Object[] lastPanel = viewPanelMap.get(view);
		if (lastPanel == null) {
			if (type.startsWith("panel")) {
				if (type.equals("paneltab")) {
					addLog(I18n.get("Paneltab not allowed without panelbook"));
				}
				else {
					viewPanelMap.put(view, new Object[]{type, row});
				}
			}
			else if (type.equals("button") 
					&& (reference != null && !reference.equals("toolbar")) 
					|| !type.equals("button")) {
				viewPanelMap.put(view, new Object[]{"main", row});
			}
		}
		else if (lastPanel[0].equals("panelbook") 
				&& !panelTabTypes.contains(type)) {
			addLog(I18n.get("Panelbook must follow by paneltab"));
		}
//		else if (type.equals("paneltab") 
//				&& !lastPanel[0].equals("panelbook")
//				&& !panelTabTypes.contains(lastPanel[0])) {
//			addLog(I18n.get("Paneltab not allowed without panelbook"));
//		}
		else if (type.startsWith("panel") || panelTabTypes.contains(type)) {
			viewPanelMap.put(view, new Object[]{type, row});
		}
		
	}
	
	private boolean checkFormula(boolean consider) throws IOException{
		
		String formula = getValue(row, FORMULA);
		
		if (formula == null) {
			return consider;
		}
		consider = true;
		
		for(String expr : formula.split(",")) {
			expr = expr.trim();
			if(expr.startsWith("sum(") 
					&& !expr.matches(sumPattern)){
				addLog(I18n.get("Invalid sum formula syntax"));
			}
			else if(expr.startsWith("seq(") 
					&& !expr.matches(seqPattern)){
				addLog(I18n.get("Invalid sequence formula syntax"));
			}
		}
		
		return consider;
	}
	
}
