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

import com.axelor.app.internal.AppFilter;
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
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
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
  protected static final int COL_CUSTOM_FIELDS_START = 8;

  protected final ProductRepository productRepository;
  protected final UnitRepository unitRepository;
  protected final CallTenderOfferRepository callTenderOfferRepository;
  protected final CallTenderRepository callTenderRepository;
  protected final CallTenderOfferImportHistoryRepository importHistoryRepository;
  protected final AppBaseService appBaseService;
  protected final CallTenderOfferService callTenderOfferService;
  protected final CallTenderOfferImportErrorFileService errorFileService;
  protected final CallTenderOfferImportCustomFieldService customFieldService;

  @Inject
  public CallTenderOfferImportServiceImpl(
      ProductRepository productRepository,
      UnitRepository unitRepository,
      CallTenderOfferRepository callTenderOfferRepository,
      CallTenderRepository callTenderRepository,
      CallTenderOfferImportHistoryRepository importHistoryRepository,
      AppBaseService appBaseService,
      CallTenderOfferService callTenderOfferService,
      CallTenderOfferImportErrorFileService errorFileService,
      CallTenderOfferImportCustomFieldService customFieldService) {
    this.productRepository = productRepository;
    this.unitRepository = unitRepository;
    this.callTenderOfferRepository = callTenderOfferRepository;
    this.callTenderRepository = callTenderRepository;
    this.importHistoryRepository = importHistoryRepository;
    this.appBaseService = appBaseService;
    this.callTenderOfferService = callTenderOfferService;
    this.errorFileService = errorFileService;
    this.customFieldService = customFieldService;
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

    try (FileInputStream fis = new FileInputStream(excelFile);
        Workbook workbook = new XSSFWorkbook(fis)) {
      Sheet sheet = workbook.getSheetAt(0);
      validateSheet(sheet);

      Row headerRow = sheet.getRow(0);
      int commentColIdx = findCommentColumnIndex(headerRow);

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) {
          continue;
        }

        String lineError =
            processRow(row, i + 1, callTender, supplier, errors, headerRow, commentColIdx);
        if (lineError != null) {
          errorsByRow.put(i, lineError);
        } else {
          importedCount++;
        }
      }

      MetaFile errorFile = errorFileService.generateErrorFile(workbook, errorsByRow);
      return createImportHistory(callTender, file, errorFile, importedCount, errors);
    }
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
      Row row,
      int lineNumber,
      CallTender callTender,
      Partner supplier,
      List<String> errors,
      Row headerRow,
      int commentColIdx) {

    try {
      if (row.getLastCellNum() < EXPECTED_MIN_COLUMNS) {
        String error =
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_INVALID_COLUMN_COUNT),
                lineNumber,
                EXPECTED_MIN_COLUMNS,
                row.getLastCellNum());
        errors.add(error);
        return error;
      }

      String productCode = getCellStringValue(row.getCell(COL_PRODUCT_CODE)).trim();
      Product product = productRepository.findByCode(productCode);
      if (product == null) {
        String error =
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_PRODUCT_NOT_FOUND),
                lineNumber,
                productCode);
        errors.add(error);
        return error;
      }

      CallTenderNeed matchingNeed = findMatchingNeed(callTender, product);
      if (matchingNeed == null) {
        String error =
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_NO_NEED_FOR_PRODUCT),
                lineNumber,
                productCode);
        errors.add(error);
        return error;
      }

      List<String> rowErrors = new ArrayList<>();
      String qtyStr = getCellStringValue(row.getCell(COL_QTY)).trim();
      String unitName = getCellStringValue(row.getCell(COL_UNIT)).trim();
      String dateStr = getCellStringValue(row.getCell(COL_DATE)).trim();
      String deliveryTimeStr = getCellStringValue(row.getCell(COL_DELIVERY_TIME)).trim();
      String unitPriceStr = getCellStringValue(row.getCell(COL_UNIT_PRICE)).trim();
      String comment = getCellStringValue(row.getCell(commentColIdx)).trim();

      CallTenderOffer existingOffer = findExistingOffer(callTender, supplier, product);
      CallTenderOffer offer;

      if (existingOffer != null) {
        offer = existingOffer;
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
      } else {
        offer =
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

      customFieldService.applyCustomFieldValues(
          offer, product, row, headerRow, commentColIdx, lineNumber, rowErrors);

      if (!rowErrors.isEmpty()) {
        String error = String.join("; ", rowErrors);
        errors.addAll(rowErrors);
        return error;
      }
      return null;

    } catch (Exception e) {
      String error =
          String.format(
              I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_LINE_ERROR),
              lineNumber,
              e.getMessage());
      errors.add(error);
      return error;
    }
  }

  protected int findCommentColumnIndex(Row headerRow) {
    String expected = I18n.get("Comment");
    int last = headerRow.getLastCellNum() - 1;
    for (int i = COL_CUSTOM_FIELDS_START; i <= last; i++) {
      String value = getCellStringValue(headerRow.getCell(i)).trim();
      if (expected.equalsIgnoreCase(value) || "Comment".equalsIgnoreCase(value)) {
        return i;
      }
    }
    return last;
  }

  protected String getCellStringValue(Cell cell) {
    if (cell == null) {
      return "";
    }
    if (cell.getCellType() == CellType.NUMERIC) {
      if (DateUtil.isCellDateFormatted(cell)) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy", AppFilter.getLocale())
            .format(cell.getLocalDateTimeCellValue().toLocalDate());
      }
      return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
    }
    return new DataFormatter().formatCellValue(cell);
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

    offer.setOfferComment(comment);

    if (!qtyStr.isEmpty()) {
      try {
        offer.setProposedQty(new BigDecimal(qtyStr));
      } catch (NumberFormatException e) {
        String error =
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_LINE_INVALID_QTY),
                lineNumber,
                qtyStr);
        rowErrors.add(error);
        appendImportError(
            offer,
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_INVALID_QTY), qtyStr));
      }
    }

    if (!unitName.isEmpty()) {
      Unit unit =
          unitRepository.all().filter("self.name = :name").bind("name", unitName).fetchOne();
      if (unit != null) {
        offer.setProposedUnit(unit);
      } else {
        String error =
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_LINE_UNIT_NOT_FOUND),
                lineNumber,
                unitName);
        rowErrors.add(error);
        appendImportError(
            offer,
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_UNIT_NOT_FOUND),
                unitName));
      }
    }

    if (!dateStr.isEmpty()) {
      try {
        offer.setProposedDate(
            LocalDate.parse(
                dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy", AppFilter.getLocale())));
      } catch (DateTimeParseException e) {
        String error =
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_LINE_INVALID_DATE),
                lineNumber,
                dateStr);
        rowErrors.add(error);
        appendImportError(
            offer,
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_INVALID_DATE), dateStr));
      }
    }

    if (!deliveryTimeStr.isEmpty()) {
      try {
        offer.setRequestedDeliveryTime(Integer.parseInt(deliveryTimeStr));
      } catch (NumberFormatException e) {
        String error =
            String.format(
                I18n.get(
                    PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_LINE_INVALID_DELIVERY_TIME),
                lineNumber,
                deliveryTimeStr);
        rowErrors.add(error);
        appendImportError(
            offer,
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_INVALID_DELIVERY_TIME),
                deliveryTimeStr));
      }
    }

    if (!unitPriceStr.isEmpty()) {
      try {
        offer.setProposedPrice(new BigDecimal(unitPriceStr));
      } catch (NumberFormatException e) {
        String error =
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_LINE_INVALID_UNIT_PRICE),
                lineNumber,
                unitPriceStr);
        rowErrors.add(error);
        appendImportError(
            offer,
            String.format(
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_INVALID_UNIT_PRICE),
                unitPriceStr));
      }
    }
  }

  protected void appendImportError(CallTenderOffer offer, String errorDetail) {
    offer.setOfferComment(
        String.format(
                "%s\n[%s: %s]",
                Optional.ofNullable(offer.getOfferComment()).orElse(""),
                I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_ERROR),
                errorDetail)
            .trim());
  }

  protected CallTenderOfferImportHistory createImportHistory(
      CallTender callTender,
      MetaFile originalFile,
      MetaFile errorFile,
      int importedCount,
      List<String> errors) {

    CallTenderOfferImportHistory history = new CallTenderOfferImportHistory();
    history.setMetaFile(errorFile != null ? errorFile : originalFile);
    history.setImportDateTime(appBaseService.getTodayDateTime().toLocalDateTime());
    history.setCallTender(callTender);
    history.setLog(buildLog(importedCount, errors));

    return importHistoryRepository.save(history);
  }

  protected String buildLog(int importedCount, List<String> errors) {
    StringBuilder logBuilder = new StringBuilder();
    logBuilder.append(
        String.format(
            I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_IMPORTED_LINES),
            importedCount));
    if (!errors.isEmpty()) {
      logBuilder
          .append("<br/><br/>")
          .append(I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_IMPORT_ERRORS))
          .append("<br/>");
      for (String error : errors) {
        logBuilder.append(error).append("<br/>");
      }
    }

    return logBuilder.toString();
  }

  protected CallTenderNeed findMatchingNeed(CallTender callTender, Product product) {
    if (callTender.getCallTenderNeedList() == null) {
      return null;
    }
    return callTender.getCallTenderNeedList().stream()
        .filter(need -> need.getProduct() != null && need.getProduct().equals(product))
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
                Objects.equals(supplier, offer.getSupplierPartner())
                    && product.equals(offer.getProduct()))
        .findFirst()
        .orElse(null);
  }

  protected CallTenderOffer createOffer(
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
    offer.setCallTenderAttrConfig(need.getCallTenderAttrConfig());
    offer.setAttrs(need.getAttrs());
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
    return callTenderOfferRepository.save(offer);
  }
}
