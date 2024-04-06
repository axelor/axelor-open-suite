/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MultiLevelSaleOrderLineServiceImpl implements MultiLevelSaleOrderLineService {

  protected final SaleOrderComputeService saleOrderComputeService;

  protected final SaleOrderRepository saleOrderRepository;

  protected final SaleOrderLineRepository saleOrderLineRepository;

  protected final TaxService taxService;

  protected final AppBaseService appBaseService;

  protected final SaleOrderLineService saleOrderLineService;

  @Inject
  public MultiLevelSaleOrderLineServiceImpl(
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderRepository saleOrderRepository,
      TaxService taxService,
      AppBaseService appBaseService,
      SaleOrderLineService saleOrderLineService,
      SaleOrderLineRepository saleOrderLineRepository) {
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.taxService = taxService;
    this.appBaseService = appBaseService;
    this.saleOrderLineService = saleOrderLineService;
  }

  @Override
  public List<SaleOrderLine> updateRelatedLines(SaleOrderLine dirtyLine, SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> expendableSaleOrderLineList = saleOrder.getExpendableSaleOrderLineList();
    updateChild(dirtyLine, saleOrder);
    replaceDirtyLine(dirtyLine, expendableSaleOrderLineList);
    updateParent(dirtyLine, expendableSaleOrderLineList, saleOrder);
    return expendableSaleOrderLineList;
  }

  protected void updateChild(SaleOrderLine orderLine, SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> subSaleOrderLineList = orderLine.getSubSaleOrderLineList();
    BigDecimal qtyCoef = getQtyCoef(orderLine);
    BigDecimal priceCoef = getPriceCoef(orderLine);
    if (ObjectUtils.isEmpty(subSaleOrderLineList)
        || (BigDecimal.ONE.compareTo(qtyCoef) == 0 && BigDecimal.ONE.compareTo(priceCoef) == 0)) {
      return;
    }
    for (SaleOrderLine saleOrderLine : subSaleOrderLineList) {
      updateValues(saleOrderLine, qtyCoef, priceCoef, saleOrder);
    }
  }

  protected void updateValues(
      SaleOrderLine saleOrderLine, BigDecimal qtyCoef, BigDecimal priceCoef, SaleOrder saleOrder)
      throws AxelorException {
    updateQty(saleOrderLine, qtyCoef);
    updatePrice(saleOrderLine, priceCoef);
    saleOrderLine.setExTaxTotal(saleOrderLine.getQty().multiply(saleOrderLine.getPrice()));
    computeAllValues(saleOrderLine, saleOrder);
    saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
    saleOrderLine.setQtyBeforeUpdate(saleOrderLine.getQty());
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (ObjectUtils.isEmpty(subSaleOrderLineList)) {
      return;
    }
    for (SaleOrderLine saleOrderLine1 : subSaleOrderLineList) {
      updateValues(saleOrderLine1, qtyCoef, priceCoef, saleOrder);
    }
  }

  protected void updateQty(SaleOrderLine saleOrderLine, BigDecimal qtyCoef) {
    BigDecimal qty =
        saleOrderLine.getQty().equals(BigDecimal.ZERO) ? BigDecimal.ONE : saleOrderLine.getQty();
    saleOrderLine.setQty(qtyCoef.multiply(qty).setScale(2, RoundingMode.HALF_EVEN));
  }

  protected void updatePrice(SaleOrderLine saleOrderLine, BigDecimal priceCoef) {
    BigDecimal newPrice =
        priceCoef
            .multiply(
                saleOrderLine.getPrice().signum() == 0 ? BigDecimal.ONE : saleOrderLine.getPrice())
            .setScale(4, RoundingMode.HALF_EVEN);
    saleOrderLine.setPrice(newPrice);
  }

  protected boolean replaceDirtyLine(
      SaleOrderLine dirtyLine, List<SaleOrderLine> subSaleOrderLineList) {
    if (subSaleOrderLineList == null) {
      return false;
    }

    int i = 0;
    for (SaleOrderLine saleOrderLine : subSaleOrderLineList) {
      if (isEqual(saleOrderLine, dirtyLine)) {
        subSaleOrderLineList.set(i, dirtyLine);
        return true;
      }
      if (saleOrderLine.getSubSaleOrderLineList() != null
          && replaceDirtyLine(dirtyLine, saleOrderLine.getSubSaleOrderLineList())) {
        return true;
      }
      i++;
    }
    return false;
  }

  protected void updateParent(
      SaleOrderLine dirtyLine, List<SaleOrderLine> expendableSaleOrderLineList, SaleOrder saleOrder)
      throws AxelorException {
    for (SaleOrderLine saleOrderLine : expendableSaleOrderLineList) {
      if (isOrHasDirtyLine(saleOrderLine, dirtyLine)) {
        compute(saleOrderLine, saleOrder);
      }
    }
  }

  protected void compute(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> items = saleOrderLine.getSubSaleOrderLineList();
    if (ObjectUtils.isEmpty(items)) {
      return;
    }
    BigDecimal quantity = saleOrderLine.getQty();
    BigDecimal totalPrice = BigDecimal.ZERO;
    for (SaleOrderLine line : items) {
      compute(line, saleOrder);
      totalPrice = totalPrice.add(line.getExTaxTotal());
    }
    totalPrice = quantity.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : totalPrice;
    BigDecimal price =
        quantity.equals(BigDecimal.ZERO)
            ? BigDecimal.ZERO
            : totalPrice.divide(quantity, 4, RoundingMode.HALF_EVEN);
    saleOrderLine.setPrice(price);
    saleOrderLine.setExTaxTotal(totalPrice);
    computeAllValues(saleOrderLine, saleOrder);
    saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
    saleOrderLine.setQtyBeforeUpdate(saleOrderLine.getQty());
  }

  protected boolean isOrHasDirtyLine(SaleOrderLine saleOrderLine, SaleOrderLine dirtyLine) {

    if (isEqual(saleOrderLine, dirtyLine)) {
      return true;
    }

    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (subSaleOrderLineList == null) {
      return false;
    }

    for (SaleOrderLine line : subSaleOrderLineList) {
      if (isOrHasDirtyLine(line, dirtyLine)) {
        return true;
      }
    }
    return false;
  }

  protected BigDecimal getPriceCoef(SaleOrderLine saleOrderLine) {
    BigDecimal oldPrice =
        saleOrderLine.getPriceBeforeUpdate().signum() == 0
            ? BigDecimal.ONE
            : saleOrderLine.getPriceBeforeUpdate();
    return saleOrderLine.getPrice().divide(oldPrice, 4, RoundingMode.HALF_EVEN);
  }

  protected BigDecimal getQtyCoef(SaleOrderLine saleOrderLine) {
    BigDecimal oldQty =
        saleOrderLine.getQtyBeforeUpdate().equals(BigDecimal.ZERO)
            ? BigDecimal.ONE
            : saleOrderLine.getQtyBeforeUpdate();
    return saleOrderLine.getQty().divide(oldQty, 4, RoundingMode.HALF_EVEN);
  }

  @SuppressWarnings("unchecked")
  public SaleOrderLine findDirtyLine(List<Map<String, Object>> list) {
    if (list == null) {
      return null;
    }
    for (Map<String, Object> saleOrderLine : list) {
      Object saleOrderLineList = saleOrderLine.get("subSaleOrderLineList");
      if (isChanged(saleOrderLine)) {
        return getDirtyLine(saleOrderLine);
      }
      if (saleOrderLineList == null) {
        continue;
      }
      SaleOrderLine dirtyLine = findDirtyLine((List<Map<String, Object>>) saleOrderLineList);
      if (dirtyLine != null) {
        return dirtyLine;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected SaleOrderLine getDirtyLine(Map<String, Object> saleOrderLine) {
    SaleOrderLine bean = Mapper.toBean(SaleOrderLine.class, saleOrderLine);
    if (saleOrderLine.get("_original") != null) {
      SaleOrderLine oldValue =
          Mapper.toBean(SaleOrderLine.class, (Map<String, Object>) saleOrderLine.get("_original"));
      bean.setQtyBeforeUpdate(oldValue.getQty());
      bean.setPriceBeforeUpdate(oldValue.getPrice());
    }
    if (bean.getId() != null && bean.getSubSaleOrderLineList() == null) {
      SaleOrderLine line2 = saleOrderLineRepository.find(bean.getId());
      bean.setSubSaleOrderLineList(line2.getSubSaleOrderLineList());
    }
    return bean;
  }

  protected boolean isChanged(Map<String, Object> isChanged) {
    return Boolean.TRUE.equals(isChanged.get("_changed"));
  }

  protected boolean isEqual(SaleOrderLine slo1, SaleOrderLine slo2) {
    boolean isId = slo2.getId() != null && slo1.getId() != null;
    return isId
        ? slo1.getId().equals(slo2.getId())
        : (slo1.getCid() != null && slo1.getCid().equals(slo2.getCid()));
  }

  protected void computeAllValues(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    BigDecimal exTaxPrice = saleOrderLine.getPrice();
    Set<TaxLine> taxLineSet = saleOrderLine.getTaxLineSet();
    BigDecimal inTaxPrice =
        taxService.convertUnitPrice(
            false, taxLineSet, exTaxPrice, appBaseService.getNbDecimalDigitForUnitPrice());
    saleOrderLine.setInTaxPrice(inTaxPrice);
    saleOrderLineService.computeValues(saleOrder, saleOrderLine);
  }

  @Override
  public SaleOrderLine setSOLineStartValues(SaleOrderLine saleOrderLine, Context context) {

    saleOrderLine.setTypeSelect(SaleOrderLineRepository.TYPE_NORMAL);

    if (saleOrderLine.getLineIndex() == null) {
      Context parentContext = context.getParent();
      if (parentContext != null && parentContext.getContextClass().equals(SaleOrder.class)) {
        SaleOrder parent = parentContext.asType(SaleOrder.class);
        if (parent.getExpendableSaleOrderLineList() != null) {
          saleOrderLine.setLineIndex(calculateParentSolLineIndex(parent));
        } else {
          saleOrderLine.setLineIndex("1");
        }
      }

      if (parentContext != null && parentContext.getContextClass().equals(SaleOrderLine.class)) {
        SaleOrderLine parent = parentContext.asType(SaleOrderLine.class);
        int size = 0;
        if (parent.getSubSaleOrderLineList() != null) {
          size = parent.getSubSaleOrderLineList().size();
        }
        saleOrderLine.setLineIndex(parent.getLineIndex() + "." + (size + 1));
      }
    }
    return saleOrderLine;
  }

  protected String calculateParentSolLineIndex(SaleOrder saleOrder) {
    return saleOrder.getExpendableSaleOrderLineList().stream()
        .filter(slo -> slo.getLineIndex() != null)
        .map(slo -> slo.getLineIndex().split("\\.")[0])
        .mapToInt(Integer::parseInt)
        .boxed()
        .collect(Collectors.maxBy(Integer::compareTo))
        .map(max -> String.valueOf(max + 1))
        .orElse("1");
  }
}
