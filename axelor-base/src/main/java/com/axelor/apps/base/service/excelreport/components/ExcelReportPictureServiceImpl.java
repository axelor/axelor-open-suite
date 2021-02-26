/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.excelreport.components;

import com.axelor.apps.base.service.excelreport.config.ExcelReportConstants;
import com.axelor.common.ObjectUtils;
import com.itextpdf.awt.geom.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Shape;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;

public class ExcelReportPictureServiceImpl implements ExcelReportPictureService {

  @Override
  public void getPictures(
      Sheet sheet,
      Map<String, List<ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>>>>
          pictureInputMap,
      List<CellRangeAddress> mergedCellsRangeAddressList,
      String sheetName) {
    ImmutablePair<Integer, Integer> pair;
    ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>> triple;
    List<ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>>> tripleList =
        new ArrayList<>();

    Drawing<?> drawing = sheet.getDrawingPatriarch();
    if (ObjectUtils.notEmpty(drawing)) {
      for (Shape shape : ((XSSFDrawing) drawing).getShapes()) {
        if (shape instanceof Picture) {
          Picture picture = (XSSFPicture) shape;
          pair =
              new ImmutablePair<>(
                  picture.getClientAnchor().getRow1(), picture.getClientAnchor().getRow2());
          triple =
              new ImmutableTriple<>(
                  picture, this.getDimensions(sheet, picture, mergedCellsRangeAddressList), pair);

          tripleList.add(triple);
        }
      }
      pictureInputMap.put(sheetName, tripleList);
    }
  }

  @Override
  public Dimension getDimensions(
      Sheet sheet, Picture picture, List<CellRangeAddress> mergedCellsList) {
    int width = 0;
    int height = 0;

    int firstRow;
    int lastRow;
    int firstColumn;
    int lastColumn;

    Set<CellRangeAddress> mergedCellsSet = new HashSet<>(mergedCellsList);
    firstRow = picture.getClientAnchor().getRow1();
    firstColumn = picture.getClientAnchor().getCol1();

    CellRangeAddress cellR = null;

    for (CellRangeAddress cellRange : mergedCellsSet) {
      if (cellRange.isInRange(firstRow, firstColumn)) {
        cellR = cellRange;
        break;
      }
    }

    if (ObjectUtils.notEmpty(cellR)) {

      lastRow = cellR.getLastRow();
      lastColumn = cellR.getLastColumn();
      for (int i = firstRow; i <= lastRow; i++) {
        if (ObjectUtils.notEmpty(sheet.getRow(i))) height += sheet.getRow(i).getHeight() / 20f;
      }
      for (int i = firstColumn; i <= lastColumn; i++) {

        width += sheet.getColumnWidthInPixels(i);
      }
    }

    return new Dimension(width / 2f, height);
  }

  @Override
  public void resetPictureMap(
      Map<String, List<ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>>>>
          pictureInputMap) {
    List<ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>>> tripleList =
        pictureInputMap.get(ExcelReportConstants.TEMPLATE_SHEET_TITLE);

    if (ObjectUtils.isEmpty(tripleList)) return;

    for (ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>> triple : tripleList) {
      triple.getLeft().getClientAnchor().setRow1(triple.getRight().getLeft());
      triple.getLeft().getClientAnchor().setRow2(triple.getRight().getRight());
    }

    if (ObjectUtils.isEmpty(tripleList)) return;

    for (ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>> triple : tripleList) {
      triple.getLeft().getClientAnchor().setRow1(triple.getRight().getLeft());
      triple.getLeft().getClientAnchor().setRow2(triple.getRight().getRight());
    }
  }

  @Override
  public void setPictureRowShiftMap(
      Map<String, List<ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>>>>
          pictureInputMap,
      Map<String, Map<String, List<ImmutablePair<Integer, Integer>>>> pictureRowShiftMap,
      String sheetName,
      String sheetType,
      int rowThreshold,
      int record) {
    List<ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>>> pictureTripleList =
        pictureInputMap.get(sheetType);
    ClientAnchor anchor;

    if (ObjectUtils.isEmpty(pictureTripleList)) return;

    if (!sheetType.equalsIgnoreCase(ExcelReportConstants.TEMPLATE_SHEET_TITLE)) return;

    List<ImmutablePair<Integer, Integer>> pairList = new ArrayList<>();

    for (ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>> pictureTriple :
        pictureTripleList) {
      Picture picture = pictureTriple.getLeft();

      if (picture.getClientAnchor().getRow1() > rowThreshold) {
        anchor = pictureTriple.getLeft().getClientAnchor();
        pairList.add(new ImmutablePair<>(anchor.getRow1(), record));
      }
    }

    if (pictureRowShiftMap.containsKey(sheetName)) {
      if (pictureRowShiftMap.get(sheetName).containsKey(sheetType)) {
        pictureRowShiftMap.get(sheetName).get(sheetType).addAll(pairList);
      } else {
        pictureRowShiftMap.get(sheetName).put(sheetType, pairList);
      }

    } else {
      Map<String, List<ImmutablePair<Integer, Integer>>> newMap = new HashMap<>();
      newMap.put(sheetType, pairList);
      pictureRowShiftMap.put(sheetName, newMap);
    }
  }

  @Override
  public int getLastPictureRow(
      List<ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>>>
          pictureTripleList) {
    int lastPictureRow = 0;
    for (ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>> pair :
        pictureTripleList) {
      if (lastPictureRow < pair.getLeft().getClientAnchor().getRow2())
        lastPictureRow = pair.getLeft().getClientAnchor().getRow2();
    }
    return lastPictureRow;
  }

  @Override
  public void setPictureRowOffset(
      Map<String, Map<String, List<ImmutablePair<Integer, Integer>>>> pictureRowShiftMap,
      List<ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>>> pictureTripleList,
      int rowOffset,
      String sheetName,
      String sheetType) {
    ClientAnchor anchor;

    if (sheetType.equalsIgnoreCase(ExcelReportConstants.TEMPLATE_SHEET_TITLE)) {
      List<ImmutablePair<Integer, Integer>> pairList =
          pictureRowShiftMap.get(sheetName).get(sheetType);
      List<ImmutablePair<Integer, Integer>> newPairList = new ArrayList<>();
      for (ImmutablePair<Integer, Integer> pair : pairList) {
        newPairList.add(new ImmutablePair<>(pair.getLeft() + rowOffset, pair.getRight()));
      }

      pictureRowShiftMap.get(sheetName).replace(sheetType, newPairList);
    }

    for (ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>> triple :
        pictureTripleList) {
      anchor = triple.getLeft().getClientAnchor();
      anchor.setRow1(anchor.getRow1() + rowOffset);
      anchor.setRow2(anchor.getRow2() + rowOffset);
    }
  }

  @Override
  public void writePictures(
      Sheet sheet,
      Map<String, Map<String, List<ImmutablePair<Integer, Integer>>>> pictureRowShiftMap,
      List<ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>>> pictureTripleList,
      String sheetType) {
    Workbook workbook = sheet.getWorkbook();
    Picture picture;

    if (ObjectUtils.isEmpty(pictureTripleList)) {
      return;
    }

    for (ImmutableTriple<Picture, Dimension, ImmutablePair<Integer, Integer>> triple :
        pictureTripleList) {
      picture = triple.getLeft();
      int pictureIndex =
          workbook.addPicture(picture.getPictureData().getData(), Workbook.PICTURE_TYPE_PNG);
      CreationHelper helper = workbook.getCreationHelper();
      Drawing<?> drawing = sheet.createDrawingPatriarch();
      ClientAnchor anchor = helper.createClientAnchor();
      anchor.setCol1(picture.getClientAnchor().getCol1());
      anchor.setCol2(picture.getClientAnchor().getCol2() + 1);

      int offset = 0;
      if (sheetType.equalsIgnoreCase(ExcelReportConstants.TEMPLATE_SHEET_TITLE)) {
        int firstRow = picture.getClientAnchor().getRow1();
        Optional<ImmutablePair<Integer, Integer>> optionalPair =
            pictureRowShiftMap.get(sheet.getSheetName())
                .get(ExcelReportConstants.TEMPLATE_SHEET_TITLE).stream()
                .filter(p -> p.getLeft() == firstRow)
                .findFirst();
        if (optionalPair.isPresent()) offset = optionalPair.get().getRight();
      }

      anchor.setRow1(picture.getClientAnchor().getRow1() + offset);
      anchor.setRow2(picture.getClientAnchor().getRow2() + offset + 1);
      drawing.createPicture(anchor, pictureIndex);
    }
  }
}
