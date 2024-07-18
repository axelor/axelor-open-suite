package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLinePricingService {

  /**
   * Methods to compute the pricing scale of saleOrderLine <br>
   * It is supposed that only one root pricing (pricing with no previousPricing) exists with the
   * configuration of the saleOrderLine. (product, productCategory, company, concernedModel) Having
   * more than one pricing matched may result on a unexpected result
   *
   * @param saleOrderLine
   * @throws AxelorException
   */
  void computePricingScale(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;

  /**
   * Methods that checks if saleOrderLine can be can classified with a pricing line of a existing
   * and started pricing. <br>
   * It is supposed that only one root pricing (pricing with no previousPricing) exists with the
   * configuration of the saleOrderLine. (product, productCategory, company, concernedModel) Having
   * more than one pricing matched may have different result each time this method is called
   *
   * @param saleOrderLine
   * @param saleOrder
   * @return true if it can be classified, else false
   * @throws AxelorException
   */
  boolean hasPricingLine(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;
}
