package com.axelor.apps.base.interfaces;

import java.math.BigDecimal;

public interface GlobalDiscounterLine {

  BigDecimal getPrice();

  BigDecimal getQty();

  Integer getTypeSelect();

  Integer getTypeSelectNormal();

  void setDiscountTypeSelect(Integer discountTypeSelect);

  BigDecimal getDiscountAmount();

  void setDiscountAmount(BigDecimal discountAmount);

  BigDecimal getPriceDiscounted();
}
