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
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.CallTenderSupplier;
import com.axelor.apps.purchase.db.TenderReportConfig;
import com.axelor.apps.purchase.db.TenderReportConfigLine;
import com.axelor.apps.purchase.db.repo.TenderReportConfigRepository;
import com.axelor.common.Inflector;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.utils.helpers.context.FullContext;
import com.axelor.utils.helpers.context.FullContextHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CallTenderReportServiceImpl implements CallTenderReportService {

  public static final Inflector INFLECTOR = Inflector.getInstance();
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private static final DateTimeFormatter TIME_PARSER =
      new DateTimeFormatterBuilder()
          .appendOptional(DateTimeFormatter.ISO_LOCAL_TIME)
          .appendOptional(TIME_FORMATTER)
          .toFormatter();

  protected TenderReportConfigRepository tenderReportConfigRepository;
  protected AppBaseService appBaseService;
  protected DateService dateService;

  @Inject
  public CallTenderReportServiceImpl(
      TenderReportConfigRepository tenderReportConfigRepository,
      AppBaseService appBaseService,
      DateService dateService) {
    this.tenderReportConfigRepository = tenderReportConfigRepository;
    this.appBaseService = appBaseService;
    this.dateService = dateService;
  }

  @Override
  public List<Map<String, Object>> getReportData(CallTender callTender, int reportType) {

    if (callTender == null
        || ObjectUtils.isEmpty(callTender.getCallTenderNeedList())
        || ObjectUtils.isEmpty(callTender.getCallTenderSupplierList())) {
      return Collections.emptyList();
    }

    TenderReportConfig config = getReportConfig(callTender, reportType);
    if (config == null || ObjectUtils.isEmpty(config.getTenderReportConfigLineList())) {
      return Collections.emptyList();
    }

    List<TenderReportConfigLine> configLines =
        config != null && ObjectUtils.notEmpty(config.getTenderReportConfigLineList())
            ? config.getTenderReportConfigLineList().stream()
                .filter(
                    line ->
                        !(ObjectUtils.isEmpty(line.getMetaField())
                            && ObjectUtils.isEmpty(line.getCustomField())))
                .sorted(
                    Comparator.comparing(
                        TenderReportConfigLine::getSequence,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList())
            : Collections.emptyList();

    Map<String, CallTenderOffer> offerMap = buildOfferMap(callTender);

    List<CallTenderNeed> sortedNeeds =
        callTender.getCallTenderNeedList().stream()
            .sorted(
                Comparator.comparing(
                    need -> need.getProduct() != null ? need.getProduct().getName() : "",
                    String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());

    List<CallTenderSupplier> sortedSuppliers =
        callTender.getCallTenderSupplierList().stream()
            .sorted(
                Comparator.comparing(
                    s ->
                        s.getSupplierPartner() != null
                            ? s.getSupplierPartner().getSimpleFullName()
                            : "",
                    String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());

    List<Map<String, Object>> result = new ArrayList<>();
    result.add(buildColumnsMeta(configLines));

    for (CallTenderNeed need : sortedNeeds) {
      for (CallTenderSupplier tenderSupplier : sortedSuppliers) {
        Partner supplier = tenderSupplier.getSupplierPartner();
        if (supplier == null) {
          continue;
        }
        Map<String, Object> row =
            reportType == TenderReportConfigRepository.REPORT_TYPE_SUPPLIER_RESPOND
                ? buildSupplierRespondRow(need, supplier, offerMap, configLines)
                : buildNeedSupplierRow(need, supplier, offerMap, configLines);
        result.add(row);
      }
    }

    return result;
  }

  protected Map<String, Object> buildNeedSupplierRow(
      CallTenderNeed need,
      Partner supplier,
      Map<String, CallTenderOffer> offerMap,
      List<TenderReportConfigLine> configLines) {

    CallTenderOffer offer =
        need.getProduct() != null
            ? offerMap.get(need.getProduct().getId() + "_" + supplier.getId())
            : null;

    Map<String, Object> row = new LinkedHashMap<>();
    row.put("product", need.getProduct() != null ? need.getProduct().getName() : "");
    row.put("productId", need.getProduct() != null ? need.getProduct().getId() : null);
    row.put("supplier", supplier.getSimpleFullName());

    BigDecimal salePrice =
        need.getProduct() != null
            ? need.getProduct().getSalePrice()
            : offer != null ? offer.getProposedPrice() : null;
    row.put(
        "unit_price",
        (ObjectUtils.notEmpty(salePrice) ? salePrice : BigDecimal.ZERO)
            .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));

    String requestedUnitName =
        need.getUnit() != null
            ? need.getUnit().getName()
            : offer != null && offer.getProposedUnit() != null
                ? offer.getProposedUnit().getName()
                : null;
    row.put("unit", requestedUnitName);

    BigDecimal requestedQty =
        ObjectUtils.notEmpty(need.getRequestedQty())
            ? need.getRequestedQty()
            : offer != null ? offer.getProposedQty() : null;
    row.put(
        "qty",
        (ObjectUtils.notEmpty(requestedQty) ? requestedQty : BigDecimal.ZERO)
            .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));

    LocalDate baseDate =
        ObjectUtils.notEmpty(need.getRequestedDate())
            ? need.getRequestedDate()
            : (offer != null ? offer.getRequestedDate() : null);
    Integer deliveryDays =
        ObjectUtils.notEmpty(need.getRequestedDeliveryTime())
            ? need.getRequestedDeliveryTime()
            : (offer != null ? offer.getRequestedDeliveryTime() : null);
    row.put(
        "delivery_time",
        ObjectUtils.notEmpty(baseDate) && ObjectUtils.notEmpty(deliveryDays)
            ? baseDate.plusDays(deliveryDays)
            : null);

    for (int i = 0; i < configLines.size(); i++) {
      Object t = resolveConfigLineValue(configLines.get(i), need, offer);
      row.put("col_" + i, t);
    }

    return row;
  }

  protected Map<String, Object> buildSupplierRespondRow(
      CallTenderNeed need,
      Partner supplier,
      Map<String, CallTenderOffer> offerMap,
      List<TenderReportConfigLine> configLines) {

    CallTenderOffer offer =
        need.getProduct() != null
            ? offerMap.get(need.getProduct().getId() + "_" + supplier.getId())
            : null;

    Map<String, Object> row = new LinkedHashMap<>();
    row.put("product_id", need.getProduct() != null ? need.getProduct().getId() : null);
    row.put("product_name", need.getProduct() != null ? need.getProduct().getName() : "");
    BigDecimal requestedPrice = need.getProduct() != null ? need.getProduct().getSalePrice() : null;
    row.put(
        "requested_price",
        (ObjectUtils.notEmpty(requestedPrice) ? requestedPrice : BigDecimal.ZERO)
            .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
    row.put("requested_unit", need.getUnit() != null ? need.getUnit().getName() : null);
    BigDecimal requestedQty = need.getRequestedQty();
    row.put(
        "requested_qty",
        (ObjectUtils.notEmpty(requestedQty) ? requestedQty : BigDecimal.ZERO)
            .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));

    LocalDate reqDate = need.getRequestedDate();
    Integer reqDays = need.getRequestedDeliveryTime();
    row.put(
        "requested_delivery_date",
        ObjectUtils.notEmpty(reqDate) && ObjectUtils.notEmpty(reqDays)
            ? reqDate.plusDays(reqDays)
            : null);

    row.put("supplier_id", supplier.getId());
    row.put("supplier_name", supplier.getSimpleFullName());

    BigDecimal proposedPrice = offer != null ? offer.getProposedPrice() : null;
    row.put(
        "proposed_price",
        (ObjectUtils.notEmpty(proposedPrice) ? proposedPrice : BigDecimal.ZERO)
            .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
    row.put(
        "proposed_unit",
        offer != null && offer.getProposedUnit() != null
            ? offer.getProposedUnit().getName()
            : null);
    BigDecimal proposedQty = offer != null ? offer.getProposedQty() : null;
    row.put(
        "proposed_qty",
        (ObjectUtils.notEmpty(proposedQty) ? proposedQty : BigDecimal.ZERO)
            .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));

    LocalDate propDate =
        Optional.ofNullable(offer).map(CallTenderOffer::getRequestedDate).orElse(null);
    Integer propDays = ObjectUtils.notEmpty(offer) ? offer.getRequestedDeliveryTime() : null;
    row.put(
        "proposed_delivery_date",
        ObjectUtils.notEmpty(propDate) && ObjectUtils.notEmpty(propDays)
            ? propDate.plusDays(propDays)
            : null);

    for (int i = 0; i < configLines.size(); i++) {
      row.put("col_" + i, resolveConfigLineValue(configLines.get(i), need, offer));
    }

    return row;
  }

  protected TenderReportConfig getReportConfig(CallTender callTender, int reportType) {

    if (reportType == TenderReportConfigRepository.REPORT_TYPE_PRODUCT_ATTRS
        && callTender.getProductAttrReportConfig() != null) {
      return callTender.getProductAttrReportConfig();
    }

    if (reportType == TenderReportConfigRepository.REPORT_TYPE_SUPPLIER_RESPOND
        && callTender.getSupplierRespondReportConfig() != null) {
      return callTender.getSupplierRespondReportConfig();
    }

    return tenderReportConfigRepository
        .all()
        .filter("self.reportType = :reportType AND self.isDefault = true")
        .bind("reportType", reportType)
        .fetchOne();
  }

  protected Map<String, Object> buildColumnsMeta(List<TenderReportConfigLine> configLines) {

    List<Map<String, String>> columns = new ArrayList<>();
    for (int i = 0; i < configLines.size(); i++) {
      TenderReportConfigLine line = configLines.get(i);
      Map<String, String> col = new HashMap<>();
      col.put("key", "col_" + i);
      col.put("label", resolveColumnLabel(line));
      columns.add(col);
    }
    Map<String, Object> meta = new HashMap<>();
    meta.put("_type", "columns");
    meta.put("columns", columns);

    return meta;
  }

  protected String resolveColumnLabel(TenderReportConfigLine line) {

    if (line.getMetaField() != null) {
      String label = line.getMetaField().getLabel();
      return ObjectUtils.notEmpty(label)
          ? label
          : INFLECTOR.titleize(line.getMetaField().getName());
    }

    if (line.getCustomField() != null) {
      String title = line.getCustomField().getTitle();
      return ObjectUtils.notEmpty(title)
          ? title
          : INFLECTOR.titleize(line.getCustomField().getName());
    }

    return "";
  }

  protected Map<String, CallTenderOffer> buildOfferMap(CallTender callTender) {

    if (ObjectUtils.isEmpty(callTender.getCallTenderOfferList())) {
      return Collections.emptyMap();
    }

    Map<String, CallTenderOffer> map = new HashMap<>();
    for (CallTenderOffer offer : callTender.getCallTenderOfferList()) {
      if (offer.getProduct() != null && offer.getSupplierPartner() != null) {
        map.put(offer.getProduct().getId() + "_" + offer.getSupplierPartner().getId(), offer);
      }
    }
    return map;
  }

  protected Object resolveConfigLineValue(
      TenderReportConfigLine line, CallTenderNeed need, CallTenderOffer offer) {

    if (line.getMetaField() != null) {
      String modelName =
          line.getMetaField().getMetaModel() != null
              ? line.getMetaField().getMetaModel().getName()
              : null;
      String fieldName = line.getMetaField().getName();
      if ("CallTenderNeed".equals(modelName)) {
        return getFieldValue(need, fieldName);
      } else if ("CallTenderOffer".equals(modelName) && offer != null) {
        return getFieldValue(offer, fieldName);
      }

    } else if (line.getCustomField() != null) {
      String model = line.getCustomField().getModel();
      String fieldName = line.getCustomField().getName();
      String fieldType = line.getCustomField().getType();
      if (ObjectUtils.notEmpty(model) && model.endsWith("CallTenderNeed")) {
        return getJsonFieldValue(need, fieldName, fieldType);
      } else if (ObjectUtils.notEmpty(model)
          && model.endsWith("CallTenderOffer")
          && offer != null) {
        return getJsonFieldValue(offer, fieldName, fieldType);
      }
    }

    return null;
  }

  protected Object getFieldValue(Object entity, String fieldName) {

    Object value = null;
    String fieldType = null;
    try {
      Mapper mapper = Mapper.of(entity.getClass());
      Property property = mapper.getProperty(fieldName);

      if (ObjectUtils.isEmpty(property)) return null;

      value = property.get(entity);
      fieldType = property.getType().toString();

      if (value instanceof Model) {
        Model related = (Model) value;
        Mapper relMapper = Mapper.of(related.getClass());
        Property nameField = relMapper.getNameField();
        value =
            ObjectUtils.notEmpty(nameField) ? nameField.get(related) : Objects.toString(related);
      }
      if (value instanceof BigDecimal) {
        value =
            ((BigDecimal) value)
                .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
        fieldType = "decimal";
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      return null;
    }

    return formatFieldValue(value, fieldType);
  }

  protected Object getJsonFieldValue(Model entity, String fieldName, String fieldType) {

    Object value = null;
    try {
      FullContext ctx = FullContextHelper.create(entity);
      value = ctx.get(fieldName);
    } catch (IllegalArgumentException e) {
      value = getRawAttrValue(entity, fieldName);
    } catch (Exception e) {
      TraceBackService.trace(e);
      return value;
    }

    return formatFieldValue(value, fieldType);
  }

  protected Object formatFieldValue(Object value, String fieldType) {

    if (ObjectUtils.isEmpty(value) || ObjectUtils.isEmpty(fieldType)) {
      return value;
    }

    try {
      if ("decimal".equalsIgnoreCase(fieldType)) {
        return new BigDecimal(value.toString())
            .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
      } else if ("date".equalsIgnoreCase(fieldType)) {
        DateTimeFormatter dateFormatter = dateService.getDateFormat();
        DateTimeFormatter dateParser =
            new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendOptional(dateFormatter)
                .toFormatter();
        if (value instanceof LocalDate) {
          return ((LocalDate) value).format(dateFormatter);
        }
        return LocalDate.parse(value.toString(), dateParser).format(dateFormatter);
      } else if ("datetime".equalsIgnoreCase(fieldType)) {
        DateTimeFormatter dateTimeFormatter = dateService.getDateTimeFormat();
        DateTimeFormatter dateTimeParser =
            new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .appendOptional(dateTimeFormatter)
                .toFormatter();
        if (value instanceof LocalDateTime) {
          return ((LocalDateTime) value).format(dateTimeFormatter);
        }
        TemporalAccessor ta =
            dateTimeParser.parseBest(value.toString(), OffsetDateTime::from, LocalDateTime::from);
        LocalDateTime ldt =
            (ta instanceof OffsetDateTime)
                ? ((OffsetDateTime) ta).toLocalDateTime()
                : (LocalDateTime) ta;
        return ldt.format(dateTimeFormatter);
      } else if ("time".equalsIgnoreCase(fieldType)) {
        if (value instanceof LocalTime) {
          return ((LocalTime) value).format(TIME_FORMATTER);
        }
        return LocalTime.parse(value.toString(), TIME_PARSER).format(TIME_FORMATTER);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      return null;
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  private Object getRawAttrValue(Model entity, String fieldName) {

    try {
      Property attrsProperty = Mapper.of(entity.getClass()).getProperty("attrs");
      if (attrsProperty == null) return null;
      Object attrsValue = attrsProperty.get(entity);
      if (!(attrsValue instanceof String) || ((String) attrsValue).isEmpty()) return null;
      Map<String, Object> attrsMap = OBJECT_MAPPER.readValue((String) attrsValue, Map.class);
      return attrsMap.get(fieldName);
    } catch (Exception e) {
      return null;
    }
  }
}
