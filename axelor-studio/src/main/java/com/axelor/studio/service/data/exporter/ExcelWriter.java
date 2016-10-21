package com.axelor.studio.service.data.exporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.service.data.CommonService;

public class ExcelWriter implements DataWriter {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private XSSFWorkbook workBook;
	
	private XSSFCellStyle style;
	
	private XSSFCellStyle green;
	
	private XSSFCellStyle lavender;
	
	private XSSFCellStyle violet;
	
	private XSSFCellStyle header;
	
	private MetaFiles metaFiles;
	
	@Override
	public boolean initialize(MetaFiles metaFiles) {
		workBook = new XSSFWorkbook();
		this.metaFiles = metaFiles;
		addStyle();
		return true;
	}
	
	@Override
	public void write(String key, Integer index, String[] values) {
		
		if (key == null || values == null) {
			return;
		}

		XSSFSheet sheet = workBook.getSheet(key);
		
		if (sheet == null) {
			sheet = workBook.createSheet(key);
		}
		
		if (index == null) {
			index = sheet.getPhysicalNumberOfRows();
		}
		else if (sheet.getPhysicalNumberOfRows() - 1 > index) {
			sheet.shiftRows(index, sheet.getPhysicalNumberOfRows(), 1);
		}
		
		XSSFRow row = sheet.createRow(index);
		
		for (int count = 0; count < values.length; count++) {
			XSSFCell cell = row.createCell(count);
			cell.setCellValue(values[count]);
		}
		
		String type = null;
		if (values.length == CommonService.HEADERS.length) {
			type = values[CommonService.TYPE];
		}
		setStyle(type, row, index);
	}
	
	@Override
	public MetaFile export(MetaFile input) {
		
		if (workBook == null) {
			return input;
		}
		
		setColumnWidth();
		
		String date = LocalDateTime.now().toString("ddMMyyyy HH:mm:ss");
		String fileName = "Export " + date + ".xlsx";
		
		try {
			File file = File.createTempFile("Export", ".xlsx");
			removeBlankSheets();
			FileOutputStream  outStream = new FileOutputStream(file);
			workBook.write(outStream);
			outStream.close();
			
			log.debug("File created: {}, Size: {}", file.getName(), file.getTotalSpace());
			log.debug("Meta files: {}", metaFiles);
			FileInputStream inStream = new FileInputStream(file);
			if (input != null) {
				input.setFileName(fileName);
				input = metaFiles.upload(inStream, input);
			}
			else{
				input = metaFiles.upload(inStream, fileName);
			}
			
			inStream.close();
			
			file.delete();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
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
		header.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.index);
		XSSFFont font = workBook.createFont();
		font.setBold(true);
		header.setFont(font);
	}
	
	private void setStyle(String type, XSSFRow row, int index) {
		
		XSSFCellStyle applyStyle = null;
		if (index == 0) {
			applyStyle = header;
		}
		else if (type == null) {
			applyStyle = style;
		}
		else {
			switch (type) {
				case "general":
					applyStyle = green;
					break;
				case "panel":
					applyStyle = violet;
					break;
				case "paneltab":
					applyStyle = violet;
					break;
				case "panelbook":
					applyStyle = lavender;
					break;
				default:
					applyStyle = style;
			}
		}
		
		Iterator<Cell> cellIter = row.cellIterator();
		while(cellIter.hasNext()) {
			Cell cell = cellIter.next();
			cell.setCellStyle(applyStyle);
		}
	}
	
	private void setColumnWidth() {
		
		Iterator<XSSFSheet> sheets = workBook.iterator();

		while (sheets.hasNext()) {
			XSSFSheet sheet = sheets.next();
			sheet.createFreezePane(0, 1, 0, 1);
			int count = 0;
			while (count < CommonService.HEADERS.length) {
				sheet.autoSizeColumn(count);
				count++;
			}
		}
	}
	
	private void removeBlankSheets() {
		
		Iterator<XSSFSheet> sheetIter = workBook.iterator();
		sheetIter.next();
		
		List<String> removeSheets = new ArrayList<String>();
		while(sheetIter.hasNext()) {
			XSSFSheet sheet = sheetIter.next();
			if (sheet.getPhysicalNumberOfRows() < 2) {
				removeSheets.add(sheet.getSheetName());
			}
		}
		
		for (String name : removeSheets) {
			workBook.removeSheetAt(workBook.getSheetIndex(name));
		}
	}

	
}
