package com.axelor.studio.service.data.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;

public class DataReaderExcel implements DataReader {
	
	private XSSFWorkbook book = null;
	
	@Override
	public boolean initialize(MetaFile input){
		
		if (input == null) {
			return false;
		}
		
		File inFile = MetaFiles.getPath(input).toFile();
		if (!inFile.exists()) {
			return false;
		}
		
		try {
			FileInputStream inSteam = new FileInputStream(inFile);
			book = new XSSFWorkbook(inSteam);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public String[] read(String key, int index) {
		
		if (key == null || book == null) {
			return null;
		}
		
		XSSFSheet sheet = book.getSheet(key);
		if (sheet == null) {
			return null;
		}
		
		XSSFRow row = sheet.getRow(index);
		
		String[] vals = new String[row.getPhysicalNumberOfCells()];
		
		Iterator<Cell> rowIter = row.cellIterator();
		
		int count = 0;
		
		while (rowIter.hasNext()) {
			Cell cell = rowIter.next();
			if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				String value =  cell.getStringCellValue();
				if (!Strings.isNullOrEmpty(value)) {
					vals[count] = value.trim();
				}
				else {
					vals[count] = null;
				}
			}
			count++;
		}
		
		return vals;
	}

	@Override
	public String[] getKeys() {
		
		if (book == null) {
			return null;
		}
		
		String[] keys = new String[book.getNumberOfSheets()];
		
		for (int count = 0; count < keys.length; count++) {
			keys[count] = book.getSheetName(count);
		}
		
		return keys;
	}

	@Override
	public int getTotalLines(String key) {
		
		if (book == null || key == null) {
			return 0;
		}
		
		return book.getSheet(key).getPhysicalNumberOfRows();
	}

}
