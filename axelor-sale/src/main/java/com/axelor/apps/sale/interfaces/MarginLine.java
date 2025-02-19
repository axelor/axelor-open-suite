package com.axelor.apps.sale.interfaces;

import java.math.BigDecimal;

public interface MarginLine {

  void setSubTotalCostPrice(BigDecimal subTotalCostPrice);

  BigDecimal getSubTotalCostPrice();

  void setSubTotalGrossMargin(BigDecimal subTotalGrossMargin);

  BigDecimal getSubTotalGrossMargin();

  void setSubMarginRate(BigDecimal subMarginRate);

  BigDecimal getSubMarginRate();

  void setSubTotalMarkup(BigDecimal subTotalMarkup);

  BigDecimal getSubTotalMarkup();
}
