/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.CallTenderAttrConfig;
import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.utils.helpers.StringHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CallTenderExcelServiceImpl implements CallTenderExcelService {

  protected static final int COL_PRODUCT_CODE = 0;
  protected static final int COL_PRODUCT_NAME = 1;
  protected static final int COL_DESCRIPTION = 2;
  protected static final int COL_QTY = 3;
  protected static final int COL_UNIT = 4;
  protected static final int COL_DATE = 5;
  protected static final int COL_DELIVERY_TIME = 6;
  protected static final int COL_UNIT_PRICE = 7;
  protected static final int COL_CUSTOM_FIELDS_START = 8;

  protected final AppBaseService appBaseService;
  protected final MetaFiles metaFiles;

  @Inject
  public CallTenderExcelServiceImpl(AppBaseService appBaseService, MetaFiles metaFiles) {
    this.appBaseService = appBaseService;
    this.metaFiles = metaFiles;
  }

  @Override
  public MetaFile generateExcelFile(List<CallTenderOffer> offerList) throws IOException {

    Partner supplier = null;
    String callTenderName = null;
    if (!offerList.isEmpty()) {
      supplier = offerList.get(0).getSupplierPartner();
      callTenderName = offerList.get(0).getCallTender().getName();
    }

    String fileName =
        StringHelper.cutTooLongString(
            String.format(
                "CFT%s-%s-%s",
                callTenderName,
                Optional.ofNullable(supplier).map(Partner::getSimpleFullName).orElse(""),
                DateTimeFormatter.ofPattern("ddMMyyyyhhmm")
                    .format(appBaseService.getTodayDateTime())));

    File file = File.createTempFile(fileName, ".xlsx");
    fileName += ".xlsx";

    List<MetaJsonField> customFields = collectCustomFields(offerList);

    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Call Tender");

      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle textStyle = createTextStyle(workbook);
      createHeaderRow(sheet, headerStyle, customFields);
      createDataRows(sheet, offerList, customFields, textStyle);
      autoSizeColumns(sheet, customFields.size());

      try (FileOutputStream fos = new FileOutputStream(file)) {
        workbook.write(fos);
      }
    }

    try (FileInputStream inStream = new FileInputStream(file)) {
      return metaFiles.upload(inStream, fileName);
    }
  }

  protected CellStyle createHeaderStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    return style;
  }

  protected CellStyle createTextStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    DataFormat dataFormat = workbook.createDataFormat();
    style.setDataFormat(dataFormat.getFormat("@"));
    return style;
  }

  protected void createHeaderRow(
      Sheet sheet, CellStyle headerStyle, List<MetaJsonField> customFields) {
    Row headerRow = sheet.createRow(0);
    String[] headers = getHeaders(customFields);
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }
  }

  protected void createDataRows(
      Sheet sheet,
      List<CallTenderOffer> offerList,
      List<MetaJsonField> customFields,
      CellStyle textStyle) {

    int rowNum = 1;
    for (CallTenderOffer offer : offerList) {
      Row row = sheet.createRow(rowNum++);
      row.createCell(COL_PRODUCT_CODE).setCellValue(offer.getProduct().getCode());
      row.createCell(COL_PRODUCT_NAME).setCellValue(offer.getProduct().getName());
      row.createCell(COL_DESCRIPTION)
          .setCellValue(
              Optional.ofNullable(offer.getCallTenderNeed())
                  .map(need -> need.getDescription())
                  .orElse(""));
      row.createCell(COL_QTY).setCellValue(offer.getRequestedQty().doubleValue());
      row.createCell(COL_UNIT)
          .setCellValue(
              Optional.ofNullable(offer.getRequestedUnit()).map(unit -> unit.getName()).orElse(""));

      Cell dateCell = row.createCell(COL_DATE);
      dateCell.setCellValue(
          Optional.ofNullable(offer.getRequestedDate()).map(LocalDate::toString).orElse(""));
      dateCell.setCellStyle(textStyle);

      Cell deliveryTimeCell = row.createCell(COL_DELIVERY_TIME);
      deliveryTimeCell.setCellValue(
          Optional.ofNullable(offer.getRequestedDeliveryTime()).map(String::valueOf).orElse(""));
      deliveryTimeCell.setCellStyle(textStyle);

      row.createCell(COL_UNIT_PRICE).setCellValue("");

      writeCustomFieldCells(row, offer, customFields, textStyle);

      row.createCell(COL_CUSTOM_FIELDS_START + customFields.size()).setCellValue("");
    }
  }

  protected void writeCustomFieldCells(
      Row row, CallTenderOffer offer, List<MetaJsonField> customFields, CellStyle textStyle) {

    CallTenderNeed need = offer.getCallTenderNeed();
    Map<String, Object> attrs = parseAttrs(need == null ? null : need.getAttrs());
    for (int i = 0; i < customFields.size(); i++) {
      MetaJsonField field = customFields.get(i);
      Cell cell = row.createCell(COL_CUSTOM_FIELDS_START + i);
      if (belongsToNeed(field, need)) {
        Object value = attrs.get(field.getName());
        cell.setCellValue(value == null ? "" : value.toString());
      } else {
        cell.setCellValue("");
      }
      cell.setCellStyle(textStyle);
    }
  }

  protected void autoSizeColumns(Sheet sheet, int customFieldCount) {
    int totalCols = COL_CUSTOM_FIELDS_START + customFieldCount + 1;
    for (int i = 0; i < totalCols; i++) {
      sheet.autoSizeColumn(i);
    }
  }

  protected String[] getHeaders(List<MetaJsonField> customFields) {
    List<String> headers = new ArrayList<>();
    headers.add(I18n.get("Product code"));
    headers.add(I18n.get("Product name"));
    headers.add(I18n.get("Description"));
    headers.add(I18n.get("Qty"));
    headers.add(I18n.get("Unit"));
    headers.add(I18n.get("Date"));
    headers.add(I18n.get("Delivery time"));
    headers.add(I18n.get("Unit price"));
    for (MetaJsonField field : customFields) {
      headers.add(Optional.ofNullable(field.getTitle()).orElse(field.getName()));
    }
    headers.add(I18n.get("Comment"));
    return headers.toArray(new String[0]);
  }

  protected List<MetaJsonField> collectCustomFields(List<CallTenderOffer> offerList) {
    Set<Long> seenConfigIds = new HashSet<>();
    Map<Long, MetaJsonField> uniqueFields = new LinkedHashMap<>();

    for (CallTenderOffer offer : offerList) {
      CallTenderAttrConfig config = resolveNeedConfig(offer);
      if (config == null
          || !seenConfigIds.add(config.getId())
          || config.getCustomFieldList() == null) {
        continue;
      }
      for (MetaJsonField field : config.getCustomFieldList()) {
        if (field.getId() != null) {
          uniqueFields.putIfAbsent(field.getId(), field);
        }
      }
    }

    List<MetaJsonField> fields = new ArrayList<>(uniqueFields.values());
    fields.sort(
        Comparator.comparing(
                MetaJsonField::getSequence, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(
                f -> Optional.ofNullable(f.getTitle()).orElse(""),
                Comparator.nullsLast(Comparator.naturalOrder())));
    return fields;
  }

  protected boolean belongsToNeed(MetaJsonField field, CallTenderNeed need) {
    if (need == null) {
      return false;
    }
    CallTenderAttrConfig needConfig = resolveNeedConfig(need);
    if (needConfig == null) {
      return false;
    }
    CallTenderAttrConfig sourceConfig = field.getCallTenderAttrConfig();
    return sourceConfig != null && Objects.equals(sourceConfig.getId(), needConfig.getId());
  }

  protected CallTenderAttrConfig resolveNeedConfig(CallTenderOffer offer) {
    return resolveNeedConfig(offer == null ? null : offer.getCallTenderNeed());
  }

  protected CallTenderAttrConfig resolveNeedConfig(CallTenderNeed need) {
    if (need == null) {
      return null;
    }
    if (need.getCallTenderAttrConfig() != null) {
      return need.getCallTenderAttrConfig();
    }
    Product product = need.getProduct();
    return product == null ? null : product.getCallTenderAttrConfig();
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> parseAttrs(String attrs) {
    if (attrs == null || attrs.isEmpty()) {
      return Collections.emptyMap();
    }
    try {
      return new ObjectMapper().readValue(attrs, Map.class);
    } catch (IOException e) {
      return Collections.emptyMap();
    }
  }
}
