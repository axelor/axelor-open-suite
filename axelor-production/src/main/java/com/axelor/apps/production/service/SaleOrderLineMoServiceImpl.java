package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMoveLine;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

public class SaleOrderLineMoServiceImpl implements SaleOrderLineMoService {
  protected ManufOrderRepository manufOrderRepository;
  protected SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public SaleOrderLineMoServiceImpl(
      ManufOrderRepository manufOrderRepository, SaleOrderLineRepository saleOrderLineRepository) {
    this.manufOrderRepository = manufOrderRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  @Transactional
  @Override
  public BigDecimal fillQtyProduced(SaleOrderLine saleOrderLine) {
    SaleOrder saleOrder = saleOrderLine.getSaleOrder();
    ManufOrder manufOrder =
        manufOrderRepository
            .all()
            .filter("self.product.id=:productId and :saleOrder in self.saleOrderSet.id")
            .bind("saleOrder", saleOrder.getId())
            .bind("productId", saleOrderLine.getProduct().getId())
            .fetchOne();
    if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_CONFIRMED
        && manufOrder != null) {
      // saleOrderLine=saleOrderLineRepository.find(saleOrderLine.getId());
      Optional<BigDecimal> quantityProduced =
          manufOrder.getProducedStockMoveLineList().stream()
              .filter(
                  stockMoveLine ->
                      stockMoveLine.getStockMove().getStatusSelect()
                          == StockMoveLineProductionServiceImpl.TYPE_OUT_PRODUCTIONS)
              .map(StockMoveLine::getQty)
              .reduce(BigDecimal::add);

      return quantityProduced.get();
    }
    return null;
  }
}
