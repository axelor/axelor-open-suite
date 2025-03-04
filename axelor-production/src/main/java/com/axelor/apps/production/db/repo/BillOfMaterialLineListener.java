package com.axelor.apps.production.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.SaleOrderLineDetailsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.PreRemove;
import org.apache.commons.collections.CollectionUtils;

public class BillOfMaterialLineListener {

  protected final int DISPLAY_LIMIT = 5;

  @PreRemove
  public void onPreRemove(BillOfMaterialLine billOfMaterialLine) throws AxelorException {
    Set<SaleOrder> saleOrderSet = getSaleOrders(billOfMaterialLine);

    int saleOrdersCount = saleOrderSet.size();
    if (saleOrdersCount < DISPLAY_LIMIT) {
      if (CollectionUtils.isNotEmpty(saleOrderSet)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(ProductionExceptionMessage.BOM_LINE_LINKED_TO_SALE_ORDER_DELETE_ERROR_1),
            billOfMaterialLine.getProduct().getFullName(),
            StringHtmlListBuilder.formatMessage(
                saleOrderSet.stream().map(SaleOrder::getFullName).collect(Collectors.toList())));
      }
    } else {
      List<SaleOrder> filteredSaleOrders =
          saleOrderSet.stream().limit(DISPLAY_LIMIT).collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(filteredSaleOrders)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(ProductionExceptionMessage.BOM_LINE_LINKED_TO_SALE_ORDER_DELETE_ERROR_2),
            billOfMaterialLine.getProduct().getFullName(),
            StringHtmlListBuilder.formatMessage(
                filteredSaleOrders.stream()
                    .map(SaleOrder::getFullName)
                    .collect(Collectors.toList())),
            saleOrdersCount - DISPLAY_LIMIT);
      }
    }
  }

  protected Set<SaleOrder> getSaleOrders(BillOfMaterialLine billOfMaterialLine) {
    Set<SaleOrder> saleOrderSet = new HashSet<>();
    List<SaleOrderLine> saleOrderLineList =
        Beans.get(SaleOrderLineRepository.class)
            .all()
            .autoFlush(false)
            .filter("self.billOfMaterialLine = :billOfMaterialLine")
            .bind("billOfMaterialLine", billOfMaterialLine)
            .fetch();
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        Beans.get(SaleOrderLineDetailsRepository.class)
            .all()
            .autoFlush(false)
            .filter("self.billOfMaterialLine = :billOfMaterialLine")
            .bind("billOfMaterialLine", billOfMaterialLine)
            .fetch();

    if (CollectionUtils.isNotEmpty(saleOrderLineList)) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        saleOrderSet.add(SaleOrderLineUtils.getParentSol(saleOrderLine).getSaleOrder());
      }
    }
    SaleOrderLineDetailsService saleOrderLineDetailsService =
        Beans.get(SaleOrderLineDetailsService.class);
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      for (SaleOrderLineDetails saleOrderLineDetails : saleOrderLineDetailsList) {
        saleOrderSet.add(saleOrderLineDetailsService.getParentSaleOrder(saleOrderLineDetails));
      }
    }
    return saleOrderSet;
  }
}
