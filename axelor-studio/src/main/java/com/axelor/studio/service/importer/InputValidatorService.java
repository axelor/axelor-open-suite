package com.axelor.studio.service.importer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModel;
import com.google.common.base.Strings;

public class InputValidatorService extends ImporterService {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private final static List<String> ignoreNames = new ArrayList<String>();
	{	
		ignoreNames.add("panel");
		ignoreNames.add("panelside");
		ignoreNames.add("panelbook");
		ignoreNames.add("error");
		ignoreNames.add("warning");
	}
	
	private static String sumPattern = "sum\\(([^;^:]+;[^;^:]+(:[^:^;]+)?)\\)";
	
	private static String seqPattern = "seq\\(([\\d]+(:[^:]+)?(:[^:]+)?)\\)";
	
	private File logFile;
	
	private XSSFWorkbook logBook;
	
	private Map<String, List<String>> modelMap;
	
	private Map<String, String> viewModelMap;
	
	private Map<String, Object[]> viewPanelMap;
	
	private Map<String, Row> invalidModelMap;
	
	private Map<String, Map<String, Row>> invalidFieldMap;
	
	private List<String> menus;
	
	public File validate(XSSFWorkbook workBook) {
		
		try {
			logFile = null;
			modelMap = new HashMap<String, List<String>>();
			viewModelMap = new HashMap<String, String>();
			viewPanelMap = new HashMap<String, Object[]>();
			invalidModelMap = new HashMap<String, Row>();
			invalidFieldMap = new HashMap<String, Map<String,Row>>();
			menus = new ArrayList<String>();
			Iterator<XSSFSheet> sheetIter = workBook.iterator();
			
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
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return logFile;
	}
	
	
	private void validateRow(Iterator<Row> rowIter) throws IOException {
		
		if(!rowIter.hasNext()){
			return;
		}
		
		Row row = rowIter.next();
		
		String obj = validateModel(row);
		validateView(obj, row);
		
		if(validateField(row, obj) && obj == null) {
			addLog(I18n.get("No object defined"), row);
		}
		
		validateRow(rowIter);
	}
	
	private String validateModel(Row row) throws IOException {
		
		String obj = getString(row.getCell(MODEL));
		if (obj != null) {
			
			if (Character.isDigit(obj.charAt(0))) {
				addLog(I18n.get("Invalid object name"), row);
			}
			
			if (!modelMap.containsKey(obj)) {
				modelMap.put(obj, new ArrayList<String>());
			}
			
			invalidModelMap.remove(obj);
			
		}
		
		return obj;
		
	}
	
	private String validateView(String obj, Row row) throws IOException {
		
		String view = getString(row.getCell(VIEW));
		
		if (view != null && obj != null) {
			if (!viewModelMap.containsKey(view)) {
				viewModelMap.put(view, obj);
			}
			else if (!viewModelMap.get(view).equals(obj)) {
				addLog(String.format(
						I18n.get("Same view name can't be used for two objects %s and %s")
						, viewModelMap.get(view), obj), row);
			}
		}
		
		return view;
		
	}
	
	private boolean validateField(Row row, String obj) throws IOException {
		
		boolean consider = false;
		
		String type = validateType(row);
		if (type != null) {
			if (ignoreTypes.contains(type)) {
				return consider;
			}
			if(!type.startsWith("menu")) {
				consider = true;
			}
		}
		
		consider = validateName(type, row, obj, consider);
		
		if(type != null && type.startsWith("menu")){
			return false;
		}
		
		consider = checkSelect(row, type, consider);
		consider = checkFormula(obj, row, consider);
		consider = checkEvents(obj, row, consider);
		
		if (consider && type == null) {
			addLog(I18n.get("No type defined"), row);
		}
		
		return consider;
	}
	
	private boolean validateName(String type, Row row, String obj,
			boolean consider) throws IOException {
		
		String name = getString(row.getCell(NAME));
		String title = getString(row.getCell(TITLE));
		if (name == null) {
			name = title;
		}
		String titleTr = getString(row.getCell(TITLE_TR));
		if (name == null) {
			name = titleTr;
		}
		if (name != null) {
			menus.add(name);
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
		}
		else if (type != null 
				&& !ignoreNames.contains(type) 
				&& !ignoreTypes.contains(type)) {
			LOG.debug("Ignore names : {}", ignoreNames);
			LOG.debug("ignoreTypes : {}", ignoreTypes);
			addLog(I18n.get("Name and title empty or name is invalid."), row);
		}
		
		return consider;
		
	}

	private boolean checkSelect(Row row, String type, boolean consider) throws IOException {
		
		String select = getString(row.getCell(SELECT));
		String selectTr = getString(row.getCell(SELECT_TR));
		
		if (select != null || selectTr != null) {
			
			if (type != null 
					&& !type.equals("select") 
					&& !type.equals("multiselect")){
				addLog(I18n.get("Selection defined for non select field. "
						+ "Please check the type"), row);
			}
			
			return true;
		}

		return consider;
	}
	
//	private boolean checkExtraAttrs(Row row, String obj, String type, boolean consider) throws IOException {
//		
//		String hideIf = getString(row.getCell(HIDE_IF));
//		String requiredIf = getString(row.getCell(REQUIRED_IF));
//		String readonlyIf = getString(row.getCell(READONLY_IF));
//		String list = getString(row.getCell(LIST));
//		
//		if(!Strings.isNullOrEmpty(list)){
//			consider = true;
//		}
//		
//		
//		return consider;
//	}
	

	private void addLog(String log, Row row) throws IOException{
		
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
	
	private String validateType(Row row) throws IOException {
		
		String type = getString(row.getCell(TYPE));
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
		
		if (!typeMap.containsKey(type) 
				&& !viewElements.contains(type) 
				&& !frMap.containsKey(type)) {
			addLog(I18n.get("Invalid type"), row);
		}
		
		if ("o2m,m2m,m2o,wizard".contains(type)) { 
			if (reference == null) {
				addLog(I18n.get("Reference is empty for type"), row);
			}
			else  if (!modelMap.containsKey(reference) 
					&& !invalidModelMap.containsKey(reference)) {
				invalidModelMap.put(reference, row);
			}
		}
		
		if (type.equals("menu") && reference != null) {
			
			if(!menus.contains(reference)){
				addLog(I18n.get("No parent menu defined"), row);
			}
		}
		
		if(type != null){
			checkViewPanelType(type, reference, row);
		}
		
		return type;
	}
	
	private boolean checkEvents(String obj, Row row, boolean consider) throws IOException {
		
		String formula = getString(row.getCell(FORMULA));
		String event = getString(row.getCell(EVENT));
		
		if (event == null) {
			return consider;
		}
		
		consider = true;
		
		if (formula == null) {
			addLog(I18n.get("Formula is empty but event specified"), row);
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
		
		for(Object[] panel : viewPanelMap.values()){
			if(panel[0].equals("panelbook")){
				addLog(I18n.get("Panelbook must follow by paneltab"), 
						(Row)panel[1]);
			}
		}
		
	}
	
	private void checkViewPanelType(String type, String refernce, Row row) throws IOException{
		
		if ("error,warning,menu".contains(type)) {
			return;
		}
		
		String view = getString(row.getCell(VIEW));
		if (view == null) {
			view = getString(row.getCell(MODEL));
		}
		
		if (view == null) {
			return;
		}
		
		Object[] lastPanel = viewPanelMap.get(view);
		if (lastPanel == null) {
			if (type.startsWith("panel")) {
				if (type.equals("paneltab")) {
					addLog(I18n.get("Paneltab not allowed without panelbook"), row);
				}
				else {
					viewPanelMap.put(view, new Object[]{type, row});
				}
			}
			else if (type.equals("button") 
					&& !refernce.equals("toolbar") 
					|| !type.equals("button")) {
				viewPanelMap.put(view, new Object[]{"main", row});
			}
		}
		else if (lastPanel[0].equals("panelbook") 
				&& !type.equals("paneltab")) {
			addLog(I18n.get("Panelbook must follow by paneltab"), row);
		}
		else if (type.equals("paneltab") 
				&& !lastPanel[0].equals("panelbook")
				&& !lastPanel[0].equals("paneltab")) {
			addLog(I18n.get("Paneltab not allowed without panelbook"), row);
		}
		else if (type.startsWith("panel")) {
			viewPanelMap.put(view, new Object[]{type, row});
		}
		
	}
	
	private boolean checkFormula(String obj, Row row, boolean consider) throws IOException{
		
		String formula = getString(row.getCell(FORMULA));
		
		if (formula == null) {
			return consider;
		}
		consider = true;
		
		for(String expr : formula.split(",")) {
			expr = expr.trim();
			if(expr.startsWith("sum(") 
					&& !expr.matches(sumPattern)){
				addLog(I18n.get("Invalid sum formula syntax"), row);
			}
			else if(expr.startsWith("seq(") 
					&& !expr.matches(seqPattern)){
				addLog(I18n.get("Invalid sequence formula syntax"), row);
			}
		}
		
		return consider;
	}
	
}
