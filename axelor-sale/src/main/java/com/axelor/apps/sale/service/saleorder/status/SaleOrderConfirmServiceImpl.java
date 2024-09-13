package com.axelor.apps.sale.service.saleorder.status;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.event.SaleOrderConfirm;
import com.axelor.event.Event;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class SaleOrderConfirmServiceImpl implements SaleOrderConfirmService {

  protected Event<SaleOrderConfirm> saleOrderConfirmEvent;

  protected AppSaleService appSaleService;
  protected UserService userService;
  protected AppCrmService appCrmService;
  protected SaleOrderRepository saleOrderRepository;
  protected PartnerRepository partnerRepository;

  @Inject
  public SaleOrderConfirmServiceImpl(
      Event<SaleOrderConfirm> saleOrderConfirmEvent,
      AppSaleService appSaleService,
      UserService userService,
      AppCrmService appCrmService,
      SaleOrderRepository saleOrderRepository,
      PartnerRepository partnerRepository) {
    this.saleOrderConfirmEvent = saleOrderConfirmEvent;
    this.appSaleService = appSaleService;
    this.userService = userService;
    this.appCrmService = appCrmService;
    this.saleOrderRepository = saleOrderRepository;
    this.partnerRepository = partnerRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public String confirmSaleOrder(SaleOrder saleOrder) {
    SaleOrderConfirm saleOrderConfirm = new SaleOrderConfirm(saleOrder);
    saleOrderConfirmEvent.fire(saleOrderConfirm);
    return saleOrderConfirm.getNotifyMessage();
  }

  @Transactional(rollbackOn = {Exception.class})
  public void confirmProcess(SaleOrder saleOrder) throws AxelorException {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(SaleOrderRepository.STATUS_FINALIZED_QUOTATION);
    authorizedStatus.add(SaleOrderRepository.STATUS_ORDER_COMPLETED);
    if (saleOrder.getStatusSelect() == null
        || !authorizedStatus.contains(saleOrder.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_CONFIRM_WRONG_STATUS));
    }

    saleOrder.setStatusSelect(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    saleOrder.setConfirmationDateTime(appSaleService.getTodayDateTime().toLocalDateTime());
    saleOrder.setConfirmedByUser(userService.getUser());

    this.validateCustomer(saleOrder);

    if (appSaleService.getAppSale().getCloseOpportunityUponSaleOrderConfirmation()) {
      Opportunity opportunity = saleOrder.getOpportunity();
      if (opportunity != null) {
        opportunity.setOpportunityStatus(appCrmService.getClosedWinOpportunityStatus());
      }
    }

    saleOrderRepository.save(saleOrder);
  }

  @Transactional
  protected Partner validateCustomer(SaleOrder saleOrder) {

    Partner clientPartner = partnerRepository.find(saleOrder.getClientPartner().getId());
    clientPartner.setIsCustomer(true);
    clientPartner.setIsProspect(false);

    return partnerRepository.save(clientPartner);
  }
}
