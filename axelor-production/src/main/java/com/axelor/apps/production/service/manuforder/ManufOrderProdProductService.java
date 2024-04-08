package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;
import java.util.List;

public interface ManufOrderProdProductService {
  void createToConsumeProdProductList(ManufOrder manufOrder);

  /**
   * Compute the quantity on generated prod product line. If the quantity of the bill of materials
   * is equal to the quantity of manuf order then the prod product line will have the same quantity
   * as configured line.
   *
   * @param bomQty quantity of the bill of materials.
   * @param manufOrderQty quantity configured of the manuf order.
   * @param lineQty quantity of the line.
   * @return the quantity for the prod product line.
   */
  BigDecimal computeToConsumeProdProductLineQuantity(
      BigDecimal bomQty, BigDecimal manufOrderQty, BigDecimal lineQty);

  void createToProduceProdProductList(ManufOrder manufOrder);

  /**
   * Updates the diff prod product list.
   *
   * @param manufOrder
   * @return the updated manufOrder
   * @throws AxelorException
   */
  ManufOrder updateDiffProdProductList(ManufOrder manufOrder) throws AxelorException;

  /**
   * Compute the difference between the two lists for the given manuf order.
   *
   * @param manufOrder
   * @param prodProductList
   * @param stockMoveLineList
   * @return a list of ProdProduct
   * @throws AxelorException
   */
  List<ProdProduct> createDiffProdProductList(
      ManufOrder manufOrder,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> stockMoveLineList)
      throws AxelorException;

  /**
   * Compute the difference between the two lists.
   *
   * @param prodProductList
   * @param stockMoveLineList
   * @return a list of ProdProduct
   * @throws AxelorException
   */
  List<ProdProduct> createDiffProdProductList(
      List<ProdProduct> prodProductList, List<StockMoveLine> stockMoveLineList)
      throws AxelorException;
}
