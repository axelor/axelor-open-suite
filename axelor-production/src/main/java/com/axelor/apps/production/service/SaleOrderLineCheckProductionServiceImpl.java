package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineCheckProductionServiceImpl
    implements SaleOrderLineCheckProductionService {

  protected final ManufOrderRepository manufOrderRepository;

  @Inject
  public SaleOrderLineCheckProductionServiceImpl(ManufOrderRepository manufOrderRepository) {
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  public void checkLinkedMo(SaleOrderLine saleOrderLine) throws AxelorException {
    List<ManufOrder> manufOrderList =
        manufOrderRepository
            .all()
            .filter("self.saleOrderLine = :saleOrderLine")
            .bind("saleOrderLine", saleOrderLine)
            .autoFlush(false)
            .fetch();

    if (CollectionUtils.isNotEmpty(manufOrderList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.SOL_LINKED_TO_MO_DELETE_ERROR),
          saleOrderLine.getProduct().getFullName(),
          StringHtmlListBuilder.formatMessage(
              manufOrderList.stream()
                  .map(ManufOrder::getManufOrderSeq)
                  .collect(Collectors.toList())));
    }
  }
}
