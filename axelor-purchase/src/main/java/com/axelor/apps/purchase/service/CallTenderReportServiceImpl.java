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
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CallTenderReportServiceImpl implements CallTenderReportService {

  public static final Inflector INFLECTOR = Inflector.getInstance();
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Map<Class<?>, Method> GET_ATTRS_CACHE = new ConcurrentHashMap<>();
  private static final Map<Model, Map<String, Object>> ATTRS_CACHE = new ConcurrentHashMap<>();

  protected final TenderReportConfigRepository tenderReportConfigRepository;

  @Inject
  public CallTenderReportServiceImpl(TenderReportConfigRepository tenderReportConfigRepository) {
    this.tenderReportConfigRepository = tenderReportConfigRepository;
  }

  @Override
  public List<Map<String, Object>> getReportData(CallTender callTender, int reportType) {

    if (ObjectUtils.isEmpty(callTender)
        || ObjectUtils.isEmpty(callTender.getCallTenderNeedList())
        || ObjectUtils.isEmpty(callTender.getCallTenderSupplierList())) {
      return Collections.emptyList();
    }

    TenderReportConfig config = getReportConfig(callTender, reportType);

    if (reportType == TenderReportConfigRepository.REPORT_TYPE_PRODUCT_ATTRS
        && (ObjectUtils.isEmpty(config)
            || ObjectUtils.isEmpty(config.getTenderReportConfigLineList()))) {
      return Collections.emptyList();
    }

    List<TenderReportConfigLine> configLines =
        ObjectUtils.notEmpty(config) && ObjectUtils.notEmpty(config.getTenderReportConfigLineList())
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

    List<Map<String, Object>> result = new ArrayList<>();
    result.add(buildColumnsMeta(configLines));

    for (CallTenderNeed need : callTender.getCallTenderNeedList()) {
      for (CallTenderSupplier tenderSupplier : callTender.getCallTenderSupplierList()) {
        Partner supplier = tenderSupplier.getSupplierPartner();
        if (ObjectUtils.isEmpty(supplier)) {
          continue;
        }
        Map<String, Object> row =
            reportType == TenderReportConfigRepository.REPORT_TYPE_SUPPLIER_REPOND
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
        ObjectUtils.notEmpty(need.getProduct())
            ? offerMap.get(need.getProduct().getId() + "_" + supplier.getId())
            : null;

    Map<String, Object> row = new LinkedHashMap<>();
    row.put("product", ObjectUtils.notEmpty(need.getProduct()) ? need.getProduct().getName() : "");
    row.put(
        "productId", ObjectUtils.notEmpty(need.getProduct()) ? need.getProduct().getId() : null);
    row.put("supplier", supplier.getSimpleFullName());

    BigDecimal salePrice =
        ObjectUtils.notEmpty(need.getProduct())
            ? need.getProduct().getSalePrice()
            : ObjectUtils.notEmpty(offer) ? offer.getProposedPrice() : null;
    row.put("unit_price", salePrice);

    String requestedUnitName =
        ObjectUtils.notEmpty(need.getUnit())
            ? need.getUnit().getName()
            : ObjectUtils.notEmpty(offer) && ObjectUtils.notEmpty(offer.getProposedUnit())
                ? offer.getProposedUnit().getName()
                : null;
    row.put("unit", requestedUnitName);

    BigDecimal requestedQty =
        ObjectUtils.notEmpty(need.getRequestedQty())
            ? need.getRequestedQty()
            : ObjectUtils.notEmpty(offer) ? offer.getProposedQty() : null;
    row.put("qty", requestedQty);

    LocalDate baseDate =
        ObjectUtils.notEmpty(need.getRequestedDate())
            ? need.getRequestedDate()
            : (ObjectUtils.notEmpty(offer) ? offer.getRequestedDate() : null);
    Integer deliveryDays =
        ObjectUtils.notEmpty(need.getRequestedDeliveryTime())
            ? need.getRequestedDeliveryTime()
            : (ObjectUtils.notEmpty(offer) ? offer.getRequestedDeliveryTime() : null);
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
        ObjectUtils.notEmpty(need.getProduct())
            ? offerMap.get(need.getProduct().getId() + "_" + supplier.getId())
            : null;

    Map<String, Object> row = new LinkedHashMap<>();
    row.put(
        "product_id", ObjectUtils.notEmpty(need.getProduct()) ? need.getProduct().getId() : null);
    row.put(
        "product_name", ObjectUtils.notEmpty(need.getProduct()) ? need.getProduct().getName() : "");
    row.put(
        "requested_price",
        ObjectUtils.notEmpty(need.getProduct()) ? need.getProduct().getSalePrice() : null);
    row.put(
        "requested_unit", ObjectUtils.notEmpty(need.getUnit()) ? need.getUnit().getName() : null);
    row.put("requested_qty", need.getRequestedQty());

    LocalDate reqDate = need.getRequestedDate();
    Integer reqDays = need.getRequestedDeliveryTime();
    row.put(
        "requested_delivery_date",
        ObjectUtils.notEmpty(reqDate) && ObjectUtils.notEmpty(reqDays)
            ? reqDate.plusDays(reqDays)
            : null);

    row.put("supplier_id", supplier.getId());
    row.put("supplier_name", supplier.getSimpleFullName());

    row.put("proposed_price", ObjectUtils.notEmpty(offer) ? offer.getProposedPrice() : null);
    row.put(
        "proposed_unit",
        ObjectUtils.notEmpty(offer) && ObjectUtils.notEmpty(offer.getProposedUnit())
            ? offer.getProposedUnit().getName()
            : null);
    row.put("proposed_qty", ObjectUtils.notEmpty(offer) ? offer.getProposedQty() : null);

    LocalDate propDate = ObjectUtils.notEmpty(offer) ? offer.getRequestedDate() : null;
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
        && ObjectUtils.notEmpty(callTender.getProductAttrReportConfig())) {
      return callTender.getProductAttrReportConfig();
    }

    if (reportType == TenderReportConfigRepository.REPORT_TYPE_SUPPLIER_REPOND
        && ObjectUtils.notEmpty(callTender.getSupplierRespondReportConfig())) {
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

    if (ObjectUtils.notEmpty(line.getMetaField())) {
      String label = line.getMetaField().getLabel();
      return ObjectUtils.notEmpty(label)
          ? label
          : INFLECTOR.titleize(line.getMetaField().getName());
    }

    if (ObjectUtils.notEmpty(line.getCustomField())) {
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
      if (ObjectUtils.notEmpty(offer.getProduct())
          && ObjectUtils.notEmpty(offer.getSupplierPartner())) {
        map.put(offer.getProduct().getId() + "_" + offer.getSupplierPartner().getId(), offer);
      }
    }
    return map;
  }

  protected Object resolveConfigLineValue(
      TenderReportConfigLine line, CallTenderNeed need, CallTenderOffer offer) {

    if (ObjectUtils.notEmpty(line.getMetaField())) {
      String modelName =
          ObjectUtils.notEmpty(line.getMetaField().getMetaModel())
              ? line.getMetaField().getMetaModel().getName()
              : null;
      String fieldName = line.getMetaField().getName();
      if ("CallTenderNeed".equals(modelName)) {
        return getFieldValue(need, fieldName);
      } else if ("CallTenderOffer".equals(modelName) && ObjectUtils.notEmpty(offer)) {
        return getFieldValue(offer, fieldName);
      }

    } else if (ObjectUtils.notEmpty(line.getCustomField())) {
      String model = line.getCustomField().getModel();
      String fieldName = line.getCustomField().getName();
      if (ObjectUtils.notEmpty(model) && model.endsWith("CallTenderNeed")) {
        return getJsonFieldValue(need, fieldName);
      } else if (ObjectUtils.notEmpty(model)
          && model.endsWith("CallTenderOffer")
          && ObjectUtils.notEmpty(offer)) {
        return getJsonFieldValue(offer, fieldName);
      }
    }

    return null;
  }

  protected Object getFieldValue(Object entity, String fieldName) {

    try {
      Mapper mapper = Mapper.of(entity.getClass());
      Property property = mapper.getProperty(fieldName);
      if (ObjectUtils.isEmpty(property)) return null;
      Object value = property.get(entity);
      if (value instanceof Model) {
        Model related = (Model) value;
        Mapper relMapper = Mapper.of(related.getClass());
        Property nameField = relMapper.getNameField();
        return ObjectUtils.notEmpty(nameField) ? nameField.get(related) : Objects.toString(related);
      }
      return value;
    } catch (Exception e) {
      TraceBackService.trace(e);
      return null;
    }
  }

  protected Object getJsonFieldValue(Model entity, String fieldName) {

    @SuppressWarnings("unchecked")
    Map<String, Object> map =
        ATTRS_CACHE.computeIfAbsent(
            entity,
            e -> {
              try {
                Method getAttrs =
                    GET_ATTRS_CACHE.computeIfAbsent(
                        e.getClass(),
                        cls -> {
                          try {
                            return cls.getMethod("getAttrs");
                          } catch (NoSuchMethodException ex) {
                            throw new RuntimeException(ex);
                          }
                        });
                String attrs = (String) getAttrs.invoke(e);
                return ObjectUtils.notEmpty(attrs)
                    ? OBJECT_MAPPER.readValue(attrs, Map.class)
                    : Collections.emptyMap();
              } catch (Exception ex) {
                TraceBackService.trace(ex);
                return Collections.emptyMap();
              }
            });

    return map.get(fieldName);
  }
}
