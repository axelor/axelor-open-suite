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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.CallTenderOfferImportHistory;
import com.axelor.apps.purchase.db.repo.CallTenderOfferImportHistoryRepository;
import com.axelor.apps.purchase.db.repo.CallTenderOfferRepository;
import com.axelor.apps.purchase.db.repo.CallTenderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CallTenderOfferImportServiceImpl implements CallTenderOfferImportService {

  protected static final int EXPECTED_MIN_COLUMNS = 9;

  protected static final int COL_PRODUCT_CODE = 0;
  protected static final int COL_QTY = 3;
  protected static final int COL_UNIT = 4;
  protected static final int COL_DATE = 5;
  protected static final int COL_DELIVERY_TIME = 6;
  protected static final int COL_UNIT_PRICE = 7;
  protected static final int COL_COMMENT = 8;

  protected final ProductRepository productRepository;
  protected final UnitRepository unitRepository;
  protected final CallTenderOfferRepository callTenderOfferRepository;
  protected final CallTenderRepository callTenderRepository;
  protected final CallTenderOfferImportHistoryRepository importHistoryRepository;
  protected final AppBaseService appBaseService;
  protected final CallTenderOfferService callTenderOfferService;
  protected final MetaFiles metaFiles;

  @Inject
  public CallTenderOfferImportServiceImpl(
      ProductRepository productRepository,
      UnitRepository unitRepository,
      CallTenderOfferRepository callTenderOfferRepository,
      CallTenderRepository callTenderRepository,
      CallTenderOfferImportHistoryRepository importHistoryRepository,
      AppBaseService appBaseService,
      CallTenderOfferService callTenderOfferService,
      MetaFiles metaFiles) {
    this.productRepository = productRepository;
    this.unitRepository = unitRepository;
    this.callTenderOfferRepository = callTenderOfferRepository;
    this.callTenderRepository = callTenderRepository;
    this.importHistoryRepository = importHistoryRepository;
    this.appBaseService = appBaseService;
    this.callTenderOfferService = callTenderOfferService;
    this.metaFiles = metaFiles;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public CallTenderOfferImportHistory importOffers(
      CallTender callTender, Partner supplier, MetaFile file) throws AxelorException, IOException {

    callTender = callTenderRepository.find(callTender.getId());
    File excelFile = MetaFiles.getPath(file).toFile();

    List<String> errors = new ArrayList<>();
    Map<Integer, String> errorsByRow = new HashMap<>();
    int importedCount = 0;

    try (Workbook workbook = new XSSFWorkbook(new FileInputStream(excelFile))) {
      Sheet sheet = workbook.getSheetAt(0);

      validateSheet(sheet);

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) {
          continue;
        }
        int lineNumber = i + 1;

        String lineError = processRow(row, lineNumber, callTender, supplier, errors);
        if (lineError != null) {
          errorsByRow.put(i, lineError);
        } else {
          importedCount++;
        }
      }
    }

    MetaFile errorFile = generateErrorFile(excelFile, errorsByRow);
    return createImportHistory(callTender, file, errorFile, importedCount, errors);
  }

  protected void validateSheet(Sheet sheet) throws AxelorException {
    if (sheet.getLastRowNum() < 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_EMPTY_FILE));
    }

    Row headerRow = sheet.getRow(0);
    if (headerRow == null || headerRow.getLastCellNum() < EXPECTED_MIN_COLUMNS) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_INVALID_FORMAT));
    }
  }

  protected String processRow(
      Row row, int lineNumber, CallTender callTender, Partner supplier, List<String> errors) {
    try {
      if (row.getLastCellNum() < EXPECTED_MIN_COLUMNS) {
        String error =
            String.format(
                I18n.get("Line %d: Invalid number of columns (expected %d, got %d)"),
                lineNumber,
                EXPECTED_MIN_COLUMNS,
                row.getLastCellNum());
        errors.add(error);
        return error;
      }

      String productCode = getCellStringValue(row.getCell(COL_PRODUCT_CODE)).trim();
      String qtyStr = getCellStringValue(row.getCell(COL_QTY)).trim();
      String unitName = getCellStringValue(row.getCell(COL_UNIT)).trim();
      String dateStr = getCellStringValue(row.getCell(COL_DATE)).trim();
      String deliveryTimeStr = getCellStringValue(row.getCell(COL_DELIVERY_TIME)).trim();
      String unitPriceStr = getCellStringValue(row.getCell(COL_UNIT_PRICE)).trim();
      String comment = getCellStringValue(row.getCell(COL_COMMENT)).trim();

      Product product = productRepository.findByCode(productCode);
      if (product == null) {
        String error =
            String.format(
                I18n.get("Line %d: Product with code '%s' not found"), lineNumber, productCode);
        errors.add(error);
        return error;
      }

      CallTenderNeed matchingNeed = findMatchingNeed(callTender, product);
      if (matchingNeed == null) {
        String error =
            String.format(
                I18n.get("Line %d: No need found for product '%s'"), lineNumber, productCode);
        errors.add(error);
        return error;
      }

      CallTenderOffer existingOffer = findExistingOffer(callTender, supplier, product);

      List<String> rowErrors = new ArrayList<>();

      if (existingOffer != null) {
        updateOffer(
            existingOffer,
            qtyStr,
            unitName,
            dateStr,
            deliveryTimeStr,
            unitPriceStr,
            comment,
            lineNumber,
            rowErrors);
      } else {
        createOffer(
            callTender,
            supplier,
            matchingNeed,
            qtyStr,
            unitName,
            dateStr,
            deliveryTimeStr,
            unitPriceStr,
            comment,
            lineNumber,
            rowErrors);
      }

      if (!rowErrors.isEmpty()) {
        String error = String.join("; ", rowErrors);
        errors.addAll(rowErrors);
        return error;
      }

      return null;

    } catch (Exception e) {
      String error = String.format(I18n.get("Line %d: %s"), lineNumber, e.getMessage());
      errors.add(error);
      return error;
    }
  }

  protected String getCellStringValue(Cell cell) {
    if (cell == null) {
      return "";
    }
    if (cell.getCellType() == CellType.NUMERIC) {
      double value = cell.getNumericCellValue();
      if (value == Math.floor(value)) {
        return String.valueOf((long) value);
      }
      return String.valueOf(value);
    }
    return cell.getStringCellValue();
  }

  protected void setProposedFields(
      CallTenderOffer offer,
      String qtyStr,
      String unitName,
      String dateStr,
      String deliveryTimeStr,
      String unitPriceStr,
      String comment,
      int lineNumber,
      List<String> rowErrors) {

    if (!comment.isEmpty()) {
      offer.setOfferComment(comment);
    }

    // Proposed Qty
    if (!qtyStr.isEmpty()) {
      try {
        offer.setProposedQty(new BigDecimal(qtyStr));
      } catch (NumberFormatException e) {
        String error =
            String.format(I18n.get("Line %d: Invalid quantity '%s'"), lineNumber, qtyStr);
        rowErrors.add(error);
        appendImportError(offer, "Invalid quantity '" + qtyStr + "'");
      }
    }

    // Proposed Unit
    if (!unitName.isEmpty()) {
      Unit unit =
          unitRepository.all().filter("self.name = :name").bind("name", unitName).fetchOne();
      if (unit != null) {
        offer.setProposedUnit(unit);
      } else {
        String error =
            String.format(I18n.get("Line %d: Unit '%s' not found"), lineNumber, unitName);
        rowErrors.add(error);
        appendImportError(offer, "Unit '" + unitName + "' not found");
      }
    }

    // Proposed Date
    if (!dateStr.isEmpty()) {
      try {
        offer.setProposedDate(LocalDate.parse(dateStr));
      } catch (DateTimeParseException e) {
        String error = String.format(I18n.get("Line %d: Invalid date '%s'"), lineNumber, dateStr);
        rowErrors.add(error);
        appendImportError(offer, "Invalid date '" + dateStr + "'");
      }
    }

    // Delivery time
    if (!deliveryTimeStr.isEmpty()) {
      try {
        offer.setRequestedDeliveryTime(Integer.parseInt(deliveryTimeStr));
      } catch (NumberFormatException e) {
        String error =
            String.format(
                I18n.get("Line %d: Invalid delivery time '%s'"), lineNumber, deliveryTimeStr);
        rowErrors.add(error);
        appendImportError(offer, "Invalid delivery time '" + deliveryTimeStr + "'");
      }
    }

    // Proposed Price
    if (!unitPriceStr.isEmpty()) {
      try {
        offer.setProposedPrice(new BigDecimal(unitPriceStr));
      } catch (NumberFormatException e) {
        String error =
            String.format(I18n.get("Line %d: Invalid unit price '%s'"), lineNumber, unitPriceStr);
        rowErrors.add(error);
        appendImportError(offer, "Invalid unit price '" + unitPriceStr + "'");
      }
    }
  }

  protected void appendImportError(CallTenderOffer offer, String errorDetail) {
    offer.setOfferComment(
        Optional.ofNullable(offer.getOfferComment()).orElse("")
            + "\n[Import error: "
            + errorDetail
            + "]");
  }

  protected CallTenderOfferImportHistory createImportHistory(
      CallTender callTender,
      MetaFile originalFile,
      MetaFile errorFile,
      int importedCount,
      List<String> errors) {

    CallTenderOfferImportHistory history = new CallTenderOfferImportHistory();
    history.setMetaFile(errorFile != null ? errorFile : originalFile);
    history.setImportUser(AuthUtils.getUser());
    history.setImportDateTime(appBaseService.getTodayDateTime().toLocalDateTime());
    history.setCallTender(callTender);
    history.setLog(buildLog(importedCount, errors));

    return importHistoryRepository.save(history);
  }

  protected String buildLog(int importedCount, List<String> errors) {
    StringBuilder logBuilder = new StringBuilder();
    logBuilder.append(String.format(I18n.get("Imported %d line(s)."), importedCount));

    if (!errors.isEmpty()) {
      logBuilder.append("\n\n").append(I18n.get("Errors:")).append("\n");
      for (String error : errors) {
        logBuilder.append(error).append("\n");
      }
    }

    return logBuilder.toString();
  }

  protected MetaFile generateErrorFile(File originalExcelFile, Map<Integer, String> errorsByRow)
      throws IOException {

    if (errorsByRow.isEmpty()) {
      return null;
    }

    try (Workbook workbook = new XSSFWorkbook(new FileInputStream(originalExcelFile))) {
      Sheet sheet = workbook.getSheetAt(0);

      CellStyle errorStyle = createErrorStyle(workbook);

      Row headerRow = sheet.getRow(0);
      int errorsColIndex = headerRow.getLastCellNum();
      Cell errorsHeaderCell = headerRow.createCell(errorsColIndex);
      errorsHeaderCell.setCellValue(I18n.get("Errors"));

      CellStyle headerErrorStyle = workbook.createCellStyle();
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerErrorStyle.setFont(headerFont);
      errorsHeaderCell.setCellStyle(headerErrorStyle);

      for (Map.Entry<Integer, String> entry : errorsByRow.entrySet()) {
        int rowIndex = entry.getKey();
        String errorMsg = entry.getValue();
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
          continue;
        }

        for (int j = 0; j < row.getLastCellNum(); j++) {
          Cell cell = row.getCell(j);
          if (cell != null) {
            cell.setCellStyle(errorStyle);
          }
        }

        Cell errorCell = row.createCell(errorsColIndex);
        errorCell.setCellValue(errorMsg);
        errorCell.setCellStyle(errorStyle);
      }

      sheet.autoSizeColumn(errorsColIndex);

      File tempFile = File.createTempFile("import-errors", ".xlsx");
      try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        workbook.write(fos);
      }

      try (FileInputStream inStream = new FileInputStream(tempFile)) {
        return metaFiles.upload(inStream, "import-errors.xlsx");
      }
    }
  }

  protected CellStyle createErrorStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setColor(IndexedColors.RED.getIndex());
    style.setFont(font);
    return style;
  }

  protected CallTenderNeed findMatchingNeed(CallTender callTender, Product product) {
    if (callTender.getCallTenderNeedList() == null) {
      return null;
    }
    return callTender.getCallTenderNeedList().stream()
        .filter(need -> need.getProduct().equals(product))
        .findFirst()
        .orElse(null);
  }

  protected CallTenderOffer findExistingOffer(
      CallTender callTender, Partner supplier, Product product) {
    if (callTender.getCallTenderOfferList() == null) {
      return null;
    }
    return callTender.getCallTenderOfferList().stream()
        .filter(
            offer ->
                offer.getSupplierPartner().equals(supplier) && offer.getProduct().equals(product))
        .findFirst()
        .orElse(null);
  }

  protected void updateOffer(
      CallTenderOffer offer,
      String qtyStr,
      String unitName,
      String dateStr,
      String deliveryTimeStr,
      String unitPriceStr,
      String comment,
      int lineNumber,
      List<String> rowErrors) {

    setProposedFields(
        offer,
        qtyStr,
        unitName,
        dateStr,
        deliveryTimeStr,
        unitPriceStr,
        comment,
        lineNumber,
        rowErrors);
    offer.setStatusSelect(CallTenderOfferRepository.STATUS_REPLIED);
  }

  protected void createOffer(
      CallTender callTender,
      Partner supplier,
      CallTenderNeed need,
      String qtyStr,
      String unitName,
      String dateStr,
      String deliveryTimeStr,
      String unitPriceStr,
      String comment,
      int lineNumber,
      List<String> rowErrors) {

    CallTenderOffer offer = new CallTenderOffer();
    offer.setProduct(need.getProduct());
    offer.setSupplierPartner(supplier);
    offer.setCallTenderNeed(need);
    offer.setRequestedQty(need.getRequestedQty());
    offer.setRequestedDate(need.getRequestedDate());
    offer.setRequestedUnit(need.getUnit());
    offer.setRequestedDeliveryTime(need.getRequestedDeliveryTime());
    offer.setStatusSelect(CallTenderOfferRepository.STATUS_REPLIED);

    setProposedFields(
        offer,
        qtyStr,
        unitName,
        dateStr,
        deliveryTimeStr,
        unitPriceStr,
        comment,
        lineNumber,
        rowErrors);

    callTenderOfferService.setCounter(offer, callTender);
    callTender.addCallTenderOfferListItem(offer);
    callTenderOfferRepository.save(offer);
  }
}
