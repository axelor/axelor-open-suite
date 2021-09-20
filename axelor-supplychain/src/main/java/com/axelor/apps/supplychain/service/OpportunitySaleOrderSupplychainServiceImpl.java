package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.OpportunitySaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.supplychain.module.SupplychainModule;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Alternative
@Priority(SupplychainModule.PRIORITY)
public class OpportunitySaleOrderSupplychainServiceImpl extends OpportunitySaleOrderServiceImpl {

  @Inject protected SaleOrderCreateService saleOrderCreateService;

  @Inject protected SaleOrderRepository saleOrderRepo;

  @Inject protected AppBaseService appBaseService;

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder createSaleOrderFromOpportunity(Opportunity opportunity) throws AxelorException {
    SaleOrder saleOrder = super.createSaleOrderFromOpportunity(opportunity);

    // Adding supplychain behaviour
    // Set default invoiced and delivered partners and address in case of partner delegations
    if (Beans.get(AppSupplychainService.class).getAppSupplychain().getActivatePartnerRelations()) {
      Beans.get(SaleOrderSupplychainService.class)
          .setDefaultInvoicedAndDeliveredPartnersAndAddresses(saleOrder);
    }

    saleOrderRepo.save(saleOrder);

    return saleOrder;
  }
}
