package com.axelor.studio.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class TestExcel {

	@Test
	public void test() throws IOException {
		
		File file = new File("/home/axelor/Downloads/App generation doc Test.xlsx");
		
		FileInputStream fis = new FileInputStream(file);
		
		XSSFWorkbook book = new XSSFWorkbook(fis);
		
		XSSFSheet sheet = book.getSheetAt(1);
		
		extractRow(sheet.rowIterator());
	}
	
	public void extractRow(Iterator<Row> rowIter){

		if(!rowIter.hasNext()){
			return;
		}
		
		Row row = rowIter.next();
		
		System.out.println(row.getRowNum());
		
		extractRow(rowIter);
		
	}
	

}
