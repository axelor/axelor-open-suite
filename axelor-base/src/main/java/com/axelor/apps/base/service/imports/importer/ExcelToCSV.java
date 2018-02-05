/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.imports.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelToCSV {
	
	public List<Map> generateExcelSheets(File file) throws IOException {
		List<Map> newSheets = new ArrayList<>();
		Object sheet = new Object();

		FileInputStream inputStream;
		Workbook workBook = null;

		try {

			inputStream = new FileInputStream(file);
			workBook = new XSSFWorkbook(inputStream);

			for (int i = 0; i < workBook.getNumberOfSheets(); i++) {
				sheet = workBook.getSheetAt(i).getSheetName();
				Map<String, Object> newSheet = new HashMap<String, Object>();
				newSheet.put("name", sheet);
				newSheets.add(newSheet);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return newSheets;
	}

	
	public void writeTOCSV(File sheetFile, Sheet sheet) throws IOException, ParseException {
		String separator = ";";
		FileWriter writer = new FileWriter(sheetFile);
		int cnt = 0;
		Iterator<Row> rowIterator = sheet.iterator();

		if (rowIterator.hasNext()) {
			Row headerRow = rowIterator.next();
			Iterator<Cell> headerCellIterator = headerRow.cellIterator();

			while (headerCellIterator.hasNext()) {
				Cell cell = headerCellIterator.next();
				writer.append(cell.getStringCellValue() + separator);
				cnt++;
			}
			writer.append("\n");

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				for (int i = 0; i < cnt; i++) {
					try {

						Cell cell = row.getCell(i);
						if (cell != null) {

							switch (cell.getCellType()) {

							case Cell.CELL_TYPE_STRING:
								String strData = cell.getStringCellValue();
								writer.append("\"" + strData + "\"" + separator);
								break;

							case Cell.CELL_TYPE_NUMERIC:
								if (DateUtil.isCellDateFormatted(cell)) {
									String dateInString = getDateValue(cell);
									writer.append("\"" + dateInString + "\"" + separator);

								} else {
									Integer val = (int) cell.getNumericCellValue();
									writer.append(val.toString() + separator);
								}
								break;

							case Cell.CELL_TYPE_BLANK:
							default:
								writer.append("" + separator);
								break;
							}
					} else {
							writer.append("" + separator);
						}
				} catch (Exception e) {
						e.printStackTrace();
					}
		}
				writer.append("\n");
			}
		}
		writer.flush();
		writer.close();
	}
	
	public static String getDateValue(Cell cell) {

		Calendar cal = Calendar.getInstance();
		Date date = cell.getDateCellValue();
		cal.setTime(date);
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		int minutes = cal.get(Calendar.MINUTE);
		int seconds = cal.get(Calendar.SECOND);

		SimpleDateFormat format;
		if (hours == 0 && minutes == 0 && seconds == 0)
			format = new SimpleDateFormat("yyyy-MM-dd");

		else if (seconds == 0)
			format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		else
			format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS");

		return format.format(date);

	}

}
