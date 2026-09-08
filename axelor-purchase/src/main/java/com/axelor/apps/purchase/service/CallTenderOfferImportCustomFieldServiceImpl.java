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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.metajsonattrs.MetaJsonAttrsBuilder;
import com.axelor.apps.purchase.db.CallTenderAttrConfig;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaJsonField;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public class CallTenderOfferImportCustomFieldServiceImpl
    implements CallTenderOfferImportCustomFieldService {

  protected static final int COL_CUSTOM_FIELDS_START = 8;

  @Override
  public void applyCustomFieldValues(
      CallTenderOffer offer,
      Product product,
      Row row,
      Row headerRow,
      int commentColIdx,
      int lineNumber,
      List<String> rowErrors) {

    if (commentColIdx <= COL_CUSTOM_FIELDS_START) {
      return;
    }

    Map<String, MetaJsonField> fieldsByTitle = getCustomFieldsByTitle(offer, product);

    try {
      MetaJsonAttrsBuilder builder = new MetaJsonAttrsBuilder(offer.getAttrs());
      boolean changed = false;

      for (int colIdx = COL_CUSTOM_FIELDS_START; colIdx < commentColIdx; colIdx++) {
        String header = getCellStringValue(headerRow.getCell(colIdx)).trim();
        if (header.isEmpty()) {
          continue;
        }

        String rawValue = getCellStringValue(row.getCell(colIdx)).trim();
        MetaJsonField field = fieldsByTitle.get(header.toLowerCase());

        if (field == null) {
          if (!rawValue.isEmpty()) {
            rowErrors.add(
                String.format(
                    I18n.get(
                        "Line %d: Field '%s' is not in the offer's attribute configuration for product '%s'"),
                    lineNumber,
                    header,
                    product.getCode()));
            appendImportError(
                offer,
                "Field '"
                    + header
                    + "' is not in the offer's attribute configuration for product '"
                    + product.getCode()
                    + "'");
          }
          continue;
        }

        if (rawValue.isEmpty()) {
          continue;
        }

        try {
          Object typedValue = parseCustomFieldValue(field, rawValue);
          builder.putValue(field, typedValue);
          changed = true;
        } catch (Exception e) {
          rowErrors.add(
              String.format(
                  I18n.get("Line %d: Invalid value '%s' for field '%s'"),
                  lineNumber,
                  rawValue,
                  header));
          appendImportError(offer, "Invalid value '" + rawValue + "' for field '" + header + "'");
        }
      }

      if (changed) {
        offer.setAttrs(builder.build());
      }
    } catch (Exception e) {
      rowErrors.add(
          String.format(
              I18n.get("Line %d: Failed to set custom fields: %s"), lineNumber, e.getMessage()));
    }
  }

  protected Map<String, MetaJsonField> getCustomFieldsByTitle(
      CallTenderOffer offer, Product product) {
    Map<String, MetaJsonField> map = new HashMap<>();

    CallTenderAttrConfig config = null;
    if (offer != null) {
      config = offer.getCallTenderAttrConfig();
    }
    if (config == null && product != null) {
      config = product.getCallTenderAttrConfig();
    }
    if (config == null || config.getCustomFieldList() == null) {
      return map;
    }

    for (MetaJsonField f : config.getCustomFieldList()) {
      String title = f.getTitle();
      if (StringUtils.notEmpty(title)) {
        map.put(title.toLowerCase(), f);
      }
    }
    return map;
  }

  protected Object parseCustomFieldValue(MetaJsonField field, String rawValue) {
    String type = field.getType();
    if (type == null) {
      return rawValue;
    }
    switch (type) {
      case "integer":
        return Integer.parseInt(rawValue);
      case "long":
        return Long.parseLong(rawValue);
      case "decimal":
        return new BigDecimal(rawValue);
      case "boolean":
        if (!"true".equalsIgnoreCase(rawValue) && !"false".equalsIgnoreCase(rawValue)) {
          throw new IllegalArgumentException("Invalid boolean: " + rawValue);
        }
        return Boolean.parseBoolean(rawValue);
      case "date":
        return LocalDate.parse(rawValue, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      case "datetime":
        if (rawValue.endsWith("Z")) {
          return LocalDateTime.ofInstant(Instant.parse(rawValue), ZoneOffset.UTC);
        }
        return LocalDateTime.parse(rawValue, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
      case "time":
        return LocalTime.parse(rawValue, DateTimeFormatter.ofPattern("HH:mm"));
      case "string":
      default:
        return rawValue;
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

  protected void appendImportError(CallTenderOffer offer, String errorDetail) {
    String existing = Optional.ofNullable(offer.getOfferComment()).orElse("");
    String prefix = existing.isEmpty() ? "" : existing + "\n";
    offer.setOfferComment(prefix + "[Import error: " + errorDetail + "]");
  }
}
