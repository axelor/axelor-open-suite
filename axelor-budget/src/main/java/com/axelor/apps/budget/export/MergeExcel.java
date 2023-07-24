/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.budget.export;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeExcel {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void mergeExcels(Workbook sourceExcel, Workbook destExcel) {
    for (int sheetIndex = 0; sheetIndex < sourceExcel.getNumberOfSheets(); ++sheetIndex) {
      Sheet sheet = sourceExcel.getSheetAt(sheetIndex);

      Sheet outputSheet = destExcel.createSheet(sheet.getSheetName());
      copySheets(outputSheet, sheet, true);
    }
  }

  public void copySheets(Sheet newSheet, Sheet sheet, boolean copyStyle) {
    int maxColumnNum = 0;
    Map<Integer, CellStyle> styleMap = copyStyle ? new HashMap<>() : null;

    Collection<CellRangeAddress> mergedRegions = new ArrayList<>();
    for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); ++rowIndex) {
      Row srcRow = sheet.getRow(rowIndex);
      Row destRow = newSheet.createRow(rowIndex);
      if (srcRow != null) {
        copyRow(sheet, newSheet, srcRow, destRow, styleMap, mergedRegions);
        if (srcRow.getLastCellNum() > maxColumnNum) {
          maxColumnNum = srcRow.getLastCellNum();
        }
      }
    }

    for (int columnIndex = 0; columnIndex <= maxColumnNum; ++columnIndex) {
      newSheet.setColumnWidth(columnIndex, sheet.getColumnWidth(columnIndex));
    }
  }

  public void copyRow(
      Sheet srcSheet,
      Sheet destSheet,
      Row srcRow,
      Row destRow,
      Map<Integer, CellStyle> styleMap,
      Collection<CellRangeAddress> mergedRegions) {
    destRow.setHeight(srcRow.getHeight());

    for (int cellIndex = srcRow.getFirstCellNum();
        cellIndex <= srcRow.getLastCellNum();
        ++cellIndex) {

      Cell oldCell = srcRow.getCell(cellIndex);
      Cell newCell = destRow.getCell(cellIndex);
      if (oldCell != null) {
        if (newCell == null) {
          newCell = destRow.createCell(cellIndex);
        }

        copyCell(oldCell, newCell, styleMap);
        CellRangeAddress mergedRegion =
            getMergedRegion(srcSheet, srcRow.getRowNum(), (short) oldCell.getColumnIndex());
        if (mergedRegion != null) {
          CellRangeAddress newMergedRegion =
              new CellRangeAddress(
                  mergedRegion.getFirstRow(),
                  mergedRegion.getLastRow(),
                  mergedRegion.getFirstColumn(),
                  mergedRegion.getLastColumn());

          if (isNewMergedRegion(newMergedRegion, mergedRegions)) {
            mergedRegions.add(newMergedRegion);
            destSheet.addMergedRegion(newMergedRegion);
          }
        }
      }
    }
  }

  public void copyCell(Cell oldCell, Cell newCell, Map<Integer, CellStyle> styleMap) {
    if (styleMap != null) {
      if (oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()) {
        newCell.setCellStyle(oldCell.getCellStyle());
      } else {
        int stHashCode = oldCell.getCellStyle().hashCode();
        CellStyle newCellStyle = styleMap.get(stHashCode);
        if (newCellStyle == null) {
          newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
          newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
          styleMap.put(stHashCode, newCellStyle);
        }

        newCell.setCellStyle(newCellStyle);
      }
    }

    switch (oldCell.getCellType()) {
      case 0:
        newCell.setCellValue(oldCell.getNumericCellValue());
        break;
      case 1:
        newCell.setCellValue(oldCell.getStringCellValue());
        break;
      case 2:
        newCell.setCellFormula(oldCell.getCellFormula());
        break;
      case 3:
        newCell.setCellType(3);
        break;
      case 4:
        newCell.setCellValue(oldCell.getBooleanCellValue());
        break;
      case 5:
        newCell.setCellErrorValue(oldCell.getErrorCellValue());
        break;
      default:
        LOG.debug("Not copied. Cell type is not specified. Cell.");
        break;
    }
  }

  public CellRangeAddress getMergedRegion(Sheet sheet, int rowNum, short cellNum) {
    for (int i = 0; i < sheet.getNumMergedRegions(); ++i) {
      CellRangeAddress merged = sheet.getMergedRegion(i);
      if (merged.isInRange(rowNum, cellNum)) {
        return merged;
      }
    }
    return null;
  }

  public boolean isNewMergedRegion(
      CellRangeAddress newMergedRegion, Collection<CellRangeAddress> mergedRegions) {
    boolean isNew = true;
    Iterator<CellRangeAddress> iterator = mergedRegions.iterator();

    while (iterator.hasNext()) {
      CellRangeAddress add = (CellRangeAddress) iterator.next();
      boolean isFirstRow = add.getFirstRow() == newMergedRegion.getFirstRow();
      boolean isLastRow = add.getLastRow() == newMergedRegion.getLastRow();
      boolean isFirstColumn = add.getFirstColumn() == newMergedRegion.getFirstColumn();
      boolean isLastColumn = add.getLastColumn() == newMergedRegion.getLastColumn();
      if (areAllTrue(isFirstRow, isLastRow, isFirstColumn, isLastColumn)) {
        isNew = false;
      }
    }

    return isNew;
  }

  public boolean areAllTrue(boolean... values) {
    for (int i = 0; i < values.length; ++i) {
      if (!values[i]) {
        return false;
      }
    }
    return true;
  }
}
