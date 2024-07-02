package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SaleOrderLinePackService {

  /**
   * Get unique values of type field from pack lines
   *
   * @param packLineList
   * @return
   */
  Set<Integer> getPackLineTypes(List<PackLine> packLineList);

  /**
   * To create non standard SaleOrderLine from Pack.
   *
   * @param pack
   * @param saleOrder
   * @param packQty
   * @param saleOrderLineList
   * @param sequence
   * @return
   */
  List<SaleOrderLine> createNonStandardSOLineFromPack(
      Pack pack,
      SaleOrder saleOrder,
      BigDecimal packQty,
      List<SaleOrderLine> saleOrderLineList,
      Integer sequence);

  /**
   * To create 'Start of pack' and 'End of pack' type {@link SaleOrderLine}.
   *
   * @param pack
   * @param saleOrder
   * @param packQty
   * @param packLine
   * @param typeSelect
   * @param sequence
   * @return
   */
  SaleOrderLine createStartOfPackAndEndOfPackTypeSaleOrderLine(
      Pack pack,
      SaleOrder saleOrder,
      BigDecimal packQty,
      PackLine packLine,
      Integer typeSelect,
      Integer sequence);

  /**
   * To check that saleOrderLineList has "End of pack" type line.
   *
   * @param saleOrderLineList
   * @return
   */
  boolean hasEndOfPackTypeLine(List<SaleOrderLine> saleOrderLineList);

  /**
   * To check that Start of pack type line quantity changed or not.
   *
   * @param saleOrderLineList
   * @return
   */
  boolean isStartOfPackTypeLineQtyChanged(List<SaleOrderLine> saleOrderLineList);

  /**
   * Fill price for standard line from pack line.
   *
   * @param saleOrderLine
   * @param saleOrder
   * @return
   * @throws AxelorException
   */
  Map<String, Object> fillPriceFromPackLine(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  /**
   * A function used to get the ex tax unit price of a sale order line from pack line
   *
   * @param saleOrder the sale order containing the sale order line
   * @param saleOrderLine
   * @return the ex tax unit price of the sale order line
   * @throws AxelorException
   */
  BigDecimal getExTaxUnitPriceFromPackLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException;

  /**
   * A function used to get the in tax unit price of a sale order line from pack line
   *
   * @param saleOrder the sale order containing the sale order line
   * @param saleOrderLine
   * @return the in tax unit price of the sale order line
   * @throws AxelorException
   */
  BigDecimal getInTaxUnitPriceFromPackLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException;
}
