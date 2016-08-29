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
package com.axelor.studio.service.data.validator;

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
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.data.CommonService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ValidatorService {
	
	private final static List<String> IGNORE_NAMES = new ArrayList<String>();
	{	
		IGNORE_NAMES.add("panel");
		IGNORE_NAMES.add("panelside");
		IGNORE_NAMES.add("panelbook");
		IGNORE_NAMES.add("error");
		IGNORE_NAMES.add("warning");
		IGNORE_NAMES.add("onsave");
		IGNORE_NAMES.add("onnew");
		IGNORE_NAMES.add("onload");
		IGNORE_NAMES.add("spacer");
		IGNORE_NAMES.add("label");
		IGNORE_NAMES.add("dashlet");
	}
	
	private static final String SUM_PATTERN = "sum\\(([^;^:]+;[^;^:]+(:[^:^;]+)?)\\)";
	
	private static final String SEQ_PATTERN = "seq\\(([\\d]+(:[^:]+)?(:[^:]+)?)\\)";
	
	private static final List<String> PANELTAB_TYPES = Arrays.asList(new String[]{"o2m","m2m","dashlet","paneltab"});
	
	private File logFile;
	
	private XSSFWorkbook logBook;
	
	private Map<String, List<String>> modelMap;
	
	private Map<String, Object[]> panelMap;
	
	private Set<String> menubarSet;
	
	private Map<String, Row> invalidModelMap;
	
	private Map<String, String> referenceMap;
	
	private Set<String> addOnlyViews;
	
	private Map<String, Map<String, Row>> invalidFieldMap;
	
	private Row row;
	
	@Inject
	private ConfigurationService configService;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	@Inject
	private CommonService common;
	
	@Inject
	private MenuValidator menuValidator;
	
	public File validate(XSSFWorkbook workBook) throws IOException {
	
		logFile = null;
		modelMap = new HashMap<String, List<String>>();
		panelMap = new HashMap<String, Object[]>();
		invalidModelMap = new HashMap<String, Row>();
		invalidFieldMap = new HashMap<String, Map<String,Row>>();
		referenceMap = new HashMap<String, String>();
		menubarSet = new HashSet<String>();
		addOnlyViews = new HashSet<String>();

		Iterator<XSSFSheet> sheetIter = workBook.iterator();
		
		while (sheetIter.hasNext()) {
			XSSFSheet sheet = sheetIter.next();
			String name = sheet.getSheetName();
			if (name.equals("Modules")) {
				validateModules(sheet);
				continue;
			}
			if (name.equals("Menu")) {
				continue;
			}
			Iterator<Row> rowIter = sheet.rowIterator();
			if (rowIter.hasNext()) {
				rowIter.next();
			}
			validateRow(rowIter);
		}
		
		checkInvalid();
		
		menuValidator.validate(this, workBook.getSheet("Menu"));
		
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
			
			String name = CommonService.getValue(row, 0);
			if (name == null) {
				continue;
			}
			
			try {
				configService.validateModuleName(name);
			} catch (AxelorException e) {
				addLog(e.getMessage(), row);
			}
			
			String depends = CommonService.getValue(row, 1);
			if (depends != null && Arrays.asList(depends.split(",")).contains(name)) {
				addLog(I18n.get("Module's depends must not contain its name"), row);
			}
			
			String title = CommonService.getValue(row, 2);
			String version = CommonService.getValue(row, 3);
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
		
		String module = CommonService.getValue(row, CommonService.MODULE);
		if (module == null) {
			validateRow(rowIter);
			return;
		}
		
		boolean addOnly = false;
		if (module.startsWith("*")) {
			module = module.replace("*", "");
			addOnly = true;
		}
		
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
		if (addOnly) {
			addOnlyViews.add(model);
		}
		
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
		
		String model = CommonService.getValue(row, CommonService.MODEL);
		
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
		
		String type = CommonService.getValue(row, CommonService.TYPE);
		
		if (type != null) {
			if (CommonService.IGNORE_TYPES.contains(type)) {
				return modelRequired;
			}
			type = validateType(model, type);
			if(!type.startsWith("dashlet")) {
				modelRequired = true;
			}
		}
		
		modelRequired = validateName(type, model, modelRequired);
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
		
		String name = CommonService.getValue(row, CommonService.NAME);
		String title = CommonService.getValue(row, CommonService.TITLE);
		if (title == null) {
			title = CommonService.getValue(row, CommonService.TITLE_FR);
		}
		if (name == null) {
			name = title;
		}
		if (name != null) {
			name = common.getFieldName(name);
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
				&& !IGNORE_NAMES.contains(type) 
				&& !CommonService.IGNORE_TYPES.contains(type)) {
			addLog(I18n.get("Name and title empty or name is invalid."));
		}
		
		return consider;
		
	}

	private boolean checkSelect(Row row, String type, boolean consider) throws IOException {
		
		String select = CommonService.getValue(row, CommonService.SELECT);
		
		if (select == null) {
			select = CommonService.getValue(row, CommonService.SELECT_FR);
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
	
	
	public void addLog(String log, Row row) throws IOException {
		
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
	
	private String validateType(String model, String type) throws IOException {
		
		type = type.trim();
		String reference = null;
		type = type.split(",")[0];
		
		if (type.contains("(")) {
			String[] ref = type.split("\\(");
			if(ref.length > 1){
				reference = ref[1].replace(")","");
			}
			type = ref[0];
		}
		
		if (!CommonService.FIELD_TYPES.containsKey(type) 
				&& !CommonService.FR_MAP.containsKey(type) 
				&& !CommonService.VIEW_ELEMENTS.containsKey(type)) {
			addLog(I18n.get("Invalid type"));
		}
		
		if (CommonService.RELATIONAL_TYPES.containsKey(type) && !type.equals("file") || type.equals("wizard")) { 
			if (reference == null) {
				addLog(I18n.get("Reference is empty for type"));
			}
			else  if (!modelMap.containsKey(reference) && !invalidModelMap.containsKey(reference)) {
				invalidModelMap.put(reference, row);
				referenceMap.put(model + "(" + CommonService.getValue(row, CommonService.NAME) + ")" , reference);
			}
		}
		
		if (!addOnlyViews.contains(model)) {
			validateView(type, model, reference);
		}
		
		return type;
	}
	
	private boolean checkEvents(String obj, boolean consider) throws IOException {
		
		String formula = CommonService.getValue(row, CommonService.FORMULA);
		String event = CommonService.getValue(row, CommonService.EVENT);
		
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
		
		for(Object[] panel : panelMap.values()) {
			String type = (String) panel[0];
			if(type.equals("panelbook")) {
				addLog(I18n.get("Panelbook must follow by paneltab"), (Row) panel[1]);
			}
			if(panel.length == 2 && type.startsWith("panel")) {
				addLog(I18n.get("Panel must contain items or sub panels"), (Row) panel[1]);
			}
		}
		
	}
	
	private void validateView(String type, String model, String reference) throws IOException{
		
		String view = CommonService.getValue(row, CommonService.VIEW);
		if (view == null) {
			view = model;
		}
		if (view == null) {
			return;
		}
		if (checkIsHeader(type, view, reference)) {
			return;
		}
		
		Object[] panel = panelMap.get(view);
		String panelLevel = CommonService.getValue(row, CommonService.PANEL_LEVEL);
		
		if (panel == null) {
			if (type.equals("paneltab")) {
				addLog(I18n.get("Paneltab not allowed without panelbook"));
			}
			if (type.startsWith("panel")) {
				panelMap.put(view, new Object[]{type, row});
			}
			else {
				panelMap.put(view, new Object[]{"panel", row, type});
			}
		}
		else if (panel[0].equals("panelbook")) { 
			if (!PANELTAB_TYPES.contains(type)) {
				addLog(I18n.get("Panelbook must follow by paneltab"));
			}
			panelMap.put(view, new Object[]{type, row});
		}
		else if (type.startsWith("panel")) {
			if (((String) panel[0]).startsWith("panel") && panelLevel == null && panel.length == 2) {
				addLog(I18n.get("Panel must contain items or sub panel"));
			}
			panelMap.put(view, new Object[]{type, row});
		}
		else if (PANELTAB_TYPES.contains(type)) {
			panelMap.put(view, new Object[]{type, row});
		}
		else if (((String)panel[0]).startsWith("panel")) {
			panelMap.put(view, new Object[]{panel[0], panel[1], type});
		}
	}
	
	private boolean checkIsHeader(String type, String view, String reference) throws IOException {
		
		if ("toolbar".equals(reference)) {
			return true;
		}
		
		if (type.equals("menubar")) {
			menubarSet.add(view);
			return true;
		}
		
		if (menubarSet.contains(view)) {
			if (type.equals("menubar.item")) {
				menubarSet.remove(view);
			}
			else {
				addLog("Menubar must follow by 'menubar.items'");
			}
		}
		
		if (type.equals("menubar.items")) {
			return true;
		}
		
		return false;
		
	}
	
	private boolean checkFormula(boolean consider) throws IOException{
		
		String formula = CommonService.getValue(row, CommonService.FORMULA);
		
		if (formula == null) {
			return consider;
		}
		consider = true;
		
		for(String expr : formula.split(",")) {
			expr = expr.trim();
			if(expr.startsWith("sum(") 
					&& !expr.matches(SUM_PATTERN)){
				addLog(I18n.get("Invalid sum formula syntax"));
			}
			else if(expr.startsWith("seq(") 
					&& !expr.matches(SEQ_PATTERN)){
				addLog(I18n.get("Invalid sequence formula syntax"));
			}
		}
		
		return consider;
	}
	
	public boolean isValidModel(String name) {
		
		if (modelMap.containsKey(name)) {
			return true;
		}
	
		MetaModel model = metaModelRepo.findByName(name);
		if (model != null) {
			return true;
		}
		
		return false;
	}
	
}
