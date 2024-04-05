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

public class SaleOrderServiceDemo {

  protected final SaleOrderComputeService saleOrderComputeService;

  protected final SaleOrderRepository saleOrderRepository;

  protected final SaleOrderLineRepository saleOrderLineRepository;

  protected final TaxService taxService;

  protected final AppBaseService appBaseService;

  protected final SaleOrderLineService saleOrderLineService;

  @Inject
  public SaleOrderServiceDemo(
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

  public List<SaleOrderLine> updateRelatedLines(SaleOrderLine dirtyLine, SaleOrder order)
      throws AxelorException {
    List<SaleOrderLine> items = order.getExpendableSaleOrderLineList();
    updateChild(dirtyLine, order);
    replaceDirtyLineInItems(dirtyLine, items);
    updateParent(dirtyLine, items, order);
    return items;
  }

  protected void updateChild(SaleOrderLine line, SaleOrder order) throws AxelorException {
    List<SaleOrderLine> items = line.getSubSaleOrderLineList();
    BigDecimal qtyCoef = getQtyCoef(line);
    BigDecimal priceCoef = getPriceCoef(line);
    if (ObjectUtils.isEmpty(items)
        || (BigDecimal.ONE.compareTo(qtyCoef) == 0 && BigDecimal.ONE.compareTo(priceCoef) == 0)) {
      return;
    }
    for (SaleOrderLine orderLine : items) {
      updateValues(orderLine, qtyCoef, priceCoef, order);
    }
  }

  protected void updateValues(
      SaleOrderLine orderLine, BigDecimal qtyCoef, BigDecimal priceCoef, SaleOrder order)
      throws AxelorException {
    updateQty(orderLine, qtyCoef);
    updatePrice(orderLine, priceCoef);
    orderLine.setExTaxTotal(orderLine.getQty().multiply(orderLine.getPrice()));
    computeAllValues(orderLine, order);
    // setDefaultSaleOrderLineProperties(orderLine, order);
    orderLine.setPriceBeforeUpdate(orderLine.getPrice());
    orderLine.setQtyBeforeUpdate(orderLine.getQty());
    List<SaleOrderLine> items = orderLine.getSubSaleOrderLineList();
    if (ObjectUtils.isEmpty(items)) {
      return;
    }
    for (SaleOrderLine line : items) {
      updateValues(line, qtyCoef, priceCoef, order);
    }
  }

  protected void updateQty(SaleOrderLine orderLine, BigDecimal qtyCoef) {
    BigDecimal qty =
        orderLine.getQty().equals(BigDecimal.ZERO) ? BigDecimal.ONE : orderLine.getQty();
    orderLine.setQty(qtyCoef.multiply(qty).setScale(2, RoundingMode.HALF_EVEN));
  }

  protected void updatePrice(SaleOrderLine orderLine, BigDecimal priceCoef) {
    BigDecimal newPrice =
        priceCoef
            .multiply(orderLine.getPrice().signum() == 0 ? BigDecimal.ONE : orderLine.getPrice())
            .setScale(4, RoundingMode.HALF_EVEN);
    orderLine.setPrice(newPrice);
  }

  protected boolean replaceDirtyLineInItems(SaleOrderLine dirtyLine, List<SaleOrderLine> items) {
    if (items == null) {
      return false;
    }

    int i = 0;
    for (SaleOrderLine orderLine : items) {
      if (isEqual(orderLine, dirtyLine)) {
        items.set(i, dirtyLine);
        return true;
      }
      if (orderLine.getSubSaleOrderLineList() != null
          && replaceDirtyLineInItems(dirtyLine, orderLine.getSubSaleOrderLineList())) {
        return true;
      }
      i++;
    }
    return false;
  }

  protected void updateParent(SaleOrderLine dirtyLine, List<SaleOrderLine> list, SaleOrder order)
      throws AxelorException {
    for (SaleOrderLine orderLine : list) {
      if (isOrHasDirtyLine(orderLine, dirtyLine)) {
        compute(orderLine, order);
      }
    }
  }

  protected void compute(SaleOrderLine orderLine, SaleOrder order) throws AxelorException {
    List<SaleOrderLine> items = orderLine.getSubSaleOrderLineList();
    if (ObjectUtils.isEmpty(items)) {
      return;
    }
    BigDecimal quantity = orderLine.getQty();
    BigDecimal totalPrice = BigDecimal.ZERO;
    for (SaleOrderLine line : items) {
      compute(line, order);
      totalPrice = totalPrice.add(line.getExTaxTotal());
    }
    totalPrice = quantity.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : totalPrice;
    BigDecimal price =
        quantity.equals(BigDecimal.ZERO)
            ? BigDecimal.ZERO
            : totalPrice.divide(quantity, 4, RoundingMode.HALF_EVEN);
    orderLine.setPrice(price);
    orderLine.setExTaxTotal(totalPrice);
    computeAllValues(orderLine, order);
    // setDefaultSaleOrderLineProperties(orderLine, order);
    orderLine.setPriceBeforeUpdate(orderLine.getPrice());
    orderLine.setQtyBeforeUpdate(orderLine.getQty());
  }

  protected boolean isOrHasDirtyLine(SaleOrderLine orderLine, SaleOrderLine dirtyLine) {

    if (isEqual(orderLine, dirtyLine)) {
      return true;
    }
    if (orderLine.getSubSaleOrderLineList() == null) {
      return false;
    }

    List<SaleOrderLine> items = orderLine.getSubSaleOrderLineList();
    /*    if (orderLine.getId() != null && items == null) {
      List<SaleOrderLine> list = saleOrderRepository.find(orderLine.getId()).getExpendableSaleOrderLineList();
      if (list != null) {
        items = list;
      }
    }*/

    for (SaleOrderLine line : items) {
      if (isOrHasDirtyLine(line, dirtyLine)) {
        return true;
      }
    }
    return false;
  }

  protected BigDecimal getPriceCoef(SaleOrderLine line) {
    BigDecimal oldPrice =
        line.getPriceBeforeUpdate().signum() == 0 ? BigDecimal.ONE : line.getPriceBeforeUpdate();
    return line.getPrice().divide(oldPrice, 4, RoundingMode.HALF_EVEN);
  }

  protected BigDecimal getQtyCoef(SaleOrderLine line) {
    BigDecimal oldQty =
        line.getQtyBeforeUpdate().equals(BigDecimal.ZERO)
            ? BigDecimal.ONE
            : line.getQtyBeforeUpdate();
    return line.getQty().divide(oldQty, 4, RoundingMode.HALF_EVEN);
  }

  @SuppressWarnings("unchecked")
  public SaleOrderLine findDirtyLine(List<Map<String, Object>> list) {
    if (list == null) {
      return null;
    }
    for (Map<String, Object> orderLine : list) {
      Object items = orderLine.get("subSaleOrderLineList");
      if (isChanged(orderLine)) {
        return getDirtyLine(orderLine);
      }
      if (items == null) {
        continue;
      }
      SaleOrderLine dirtyLine = findDirtyLine((List<Map<String, Object>>) items);
      if (dirtyLine != null) {
        return dirtyLine;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected SaleOrderLine getDirtyLine(Map<String, Object> orderLine) {
    SaleOrderLine bean = Mapper.toBean(SaleOrderLine.class, orderLine);
    if (orderLine.get("_original") != null) {
      SaleOrderLine oldValue =
          Mapper.toBean(SaleOrderLine.class, (Map<String, Object>) orderLine.get("_original"));
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

  protected boolean isEqual(SaleOrderLine a, SaleOrderLine b) {
    boolean isId = b.getId() != null && a.getId() != null;
    return isId
        ? a.getId().equals(b.getId())
        : (a.getCid() != null && a.getCid().equals(b.getCid()));
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

  protected void setDefaultSaleOrderLineProperties(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {}

  public SaleOrderLine setSOLineStartValues(SaleOrderLine saleOrderLine, Context context) {

    if (saleOrderLine.getLineIndex() == null) {
      Context parentContext = context.getParent();
      if (parentContext != null && parentContext.getContextClass().equals(SaleOrder.class)) {
        SaleOrder parent = parentContext.asType(SaleOrder.class);
        if (parent.getExpendableSaleOrderLineList() != null) {
          saleOrderLine.setLineIndex(calculatePrentSolLineIndex(parent));
        } else {
          saleOrderLine.setLineIndex("1");
        }
      }

      if (context.getParent() != null
          && context.getParent().getContextClass().equals(SaleOrderLine.class)) {
        SaleOrderLine parent = context.getParent().asType(SaleOrderLine.class);
        int size = 0;
        if(parent.getSubSaleOrderLineList()!=null){
          size = parent.getSubSaleOrderLineList().size();
        }
        saleOrderLine.setLineIndex(
            parent.getLineIndex() + "." + (size +1));
      }
    }
    return saleOrderLine;
  }

  protected String calculatePrentSolLineIndex(SaleOrder saleOrder) {
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
