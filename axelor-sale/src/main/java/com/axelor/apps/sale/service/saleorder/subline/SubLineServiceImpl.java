/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorder.subline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.subline.SubLineService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SubLineServiceImpl implements SubLineService {

  protected AppBaseService appBaseService;

  @Inject
  public SubLineServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public <T extends Model, U extends Model> T updateSubLinesQty(BigDecimal oldQty, T line, U parent)
      throws AxelorException {
    List<T> subSoLineList = getSubLineList(line);
    if (oldQty.signum() == 0 || CollectionUtils.isEmpty(subSoLineList)) {
      return line;
    }
    BigDecimal coef = getQty(line).divide(oldQty, MathContext.DECIMAL128);
    for (T subLine : subSoLineList) {
      setSubLinesQty(coef, subLine, parent);
    }
    return line;
  }

  @Override
  public <T extends Model, U extends Model> T updateSubLinesPrice(
      BigDecimal oldPrice, T line, U parent) throws AxelorException {
    List<T> subSoLineList = getSubLineList(line);
    if (oldPrice.signum() == 0 || CollectionUtils.isEmpty(subSoLineList)) {
      return line;
    }
    BigDecimal coef = getPrice(line).divide(oldPrice, MathContext.DECIMAL128);
    for (T subLine : subSoLineList) {
      setSubLinesPrice(coef, subLine, parent);
    }
    return line;
  }

  @Override
  public <T extends Model> boolean isChildCounted(T line) {
    List<T> subSoLineList = getSubLineList(line);
    boolean isCountable = !isNotCountable(line);
    if (isCountable || CollectionUtils.isEmpty(subSoLineList)) {
      return isCountable;
    }
    return subSoLineList.stream().anyMatch(this::isChildCounted);
  }

  @Override
  public <T extends Model> T updateIsNotCountable(T line) {
    List<T> subSoLineList = getSubLineList(line);
    if (CollectionUtils.isEmpty(subSoLineList)) {
      return line;
    }

    Boolean isNotCountable = isNotCountable(line);
    for (T subLine : subSoLineList) {
      setIsCountedOnChildren(subLine, isNotCountable);
    }
    return line;
  }

  protected <T extends Model, U extends Model> void setSubLinesQty(
      BigDecimal coef, T line, U parent) throws AxelorException {
    BigDecimal qty =
        coef.multiply(getQty(line))
            .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
    setQty(line, qty);
    updateValues(parent, line);
    List<T> subSoLineList = getSubLineList(line);
    if (CollectionUtils.isNotEmpty(subSoLineList)) {
      for (T subLine : subSoLineList) {
        setSubLinesQty(coef, subLine, parent);
      }
    }
  }

  protected <T extends Model, U extends Model> void setSubLinesPrice(
      BigDecimal coef, T line, U parent) throws AxelorException {
    setPrice(line, coef.multiply(getPrice(line)));
    updateValues(parent, line);
    List<T> subLineList = getSubLineList(line);
    if (CollectionUtils.isEmpty(subLineList)) {
      return;
    }
    for (T subLine : subLineList) {
      setSubLinesPrice(coef, subLine, parent);
    }
  }

  protected <T extends Model> void setIsCountedOnChildren(T line, boolean isCounted) {
    List<T> subSoLineList = getSubLineList(line);

    if (CollectionUtils.isEmpty(subSoLineList)) {
      setIsNotCountable(line, !isCounted);
      return;
    }

    for (T subLine : subSoLineList) {
      setIsCountedOnChildren(subLine, isCounted);
    }
  }

  protected <T extends Model> void setQty(T line, BigDecimal qty) {
    if (line instanceof SaleOrderLine) {
      ((SaleOrderLine) line).setQty(qty);
      ((SaleOrderLine) line).setQtyBeforeUpdate(qty);
    }
  }

  protected <T extends Model> BigDecimal getQty(T line) {
    if (line instanceof SaleOrderLine) {
      return ((SaleOrderLine) line).getQty();
    }
    return null;
  }

  protected <T extends Model> void setPrice(T line, BigDecimal price) {
    if (line instanceof SaleOrderLine) {
      ((SaleOrderLine) line).setPrice(price);
      ((SaleOrderLine) line).setPriceBeforeUpdate(price);
    }
  }

  protected <T extends Model> BigDecimal getPrice(T line) {
    if (line instanceof SaleOrderLine) {
      return ((SaleOrderLine) line).getPrice();
    }
    return null;
  }

  protected <T, U extends Model> void updateValues(U parent, T line) throws AxelorException {
    if (line instanceof SaleOrderLine) {
      SaleOrderLine saleOrderLine = (SaleOrderLine) line;
      Beans.get(SaleOrderLineService.class).updateSubLinesPrice((SaleOrder) parent, saleOrderLine);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T extends Model> List<T> getSubLineList(T line) {
    if (line instanceof SaleOrderLine) {
      return (List<T>) ((SaleOrderLine) line).getSubSoLineList();
    }
    return Collections.emptyList();
  }

  protected <T extends Model> boolean isNotCountable(T line) {
    if (line instanceof SaleOrderLine) {
      return ((SaleOrderLine) line).getIsNotCountable();
    }
    return false;
  }

  protected <T extends Model> void setIsNotCountable(T line, boolean isNotCountable) {
    if (line instanceof SaleOrderLine) {
      ((SaleOrderLine) line).setIsNotCountable(isNotCountable);
    }
  }
}
