package com.axelor.apps.production.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.db.repo.SaleOrderLineSupplychainRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class SaleOrderLineProductionRepository extends SaleOrderLineSupplychainRepository {

  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderLineProductionRepository(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
  }

  @Override
  public SaleOrderLine save(SaleOrderLine entity) {

    try {
      if (appSaleService.getAppSale().getActivateMultiLevelSaleOrderLines()
          && ((!entity.getIsToProduce() && !entity.getSubSaleOrderLineList().isEmpty())
              || (entity.getParentSaleOrderLine() != null
                  && !entity.getParentSaleOrderLine().getIsToProduce()))) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(ProductionExceptionMessage.SUB_SALE_ORDER_LINE_CAN_NOT_BE_CREATED));
      }
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    return super.save(entity);
  }
}
