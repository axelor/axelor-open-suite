package com.axelor.googleappsconn.spreadsheet;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Class used to format the cells of the spreadsheet created by users.
 * e.g. Bold text for the header field.
 */
public class SpreadsheetFormat {

	HSSFFont font;
	HSSFCellStyle style;

	public SpreadsheetFormat(HSSFWorkbook workbook) {
		font = workbook.createFont();
		style = workbook.createCellStyle();
	}
	private Cell set(Cell cell) {
		style.setFont(font);
		cell.setCellStyle(style);
		return cell;
	}
	public Cell setStyleBold(Cell cell) {
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		return set(cell);
	}
	public Cell setStyleItalic(Cell cell) {
		font.setItalic(true);
		return set(cell);
	}
	public Cell setFontName(Cell cell, String fontName) {
		font.setFontName(fontName);
		return set(cell);
	}
	public Cell setFontHeight(Cell cell, short height) {
		font.setFontHeight(height);
		return set(cell);
	}
	public Cell highlight(Cell cell) {
		font.setColor(HSSFFont.COLOR_RED);
		return set(cell);
	}
}
