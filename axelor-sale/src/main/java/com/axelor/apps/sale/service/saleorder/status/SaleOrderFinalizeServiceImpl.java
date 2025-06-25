/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service.saleorder.status;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.BlockedSaleOrderException;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderSequenceService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderFinalizeServiceImpl implements SaleOrderFinalizeService {

  protected SaleOrderRepository saleOrderRepository;
  protected SequenceService sequenceService;
  protected SaleOrderService saleOrderService;
  protected SaleOrderPrintService saleOrderPrintService;
  protected SaleConfigService saleConfigService;
  protected AppSaleService appSaleService;
  protected AppCrmService appCrmService;
  protected SaleOrderSequenceService saleOrderSequenceService;

  @Inject
  public SaleOrderFinalizeServiceImpl(
      SaleOrderRepository saleOrderRepository,
      SequenceService sequenceService,
      SaleOrderService saleOrderService,
      SaleOrderPrintService saleOrderPrintService,
      SaleConfigService saleConfigService,
      AppSaleService appSaleService,
      AppCrmService appCrmService,
      SaleOrderSequenceService saleOrderSequenceService) {
    this.saleOrderRepository = saleOrderRepository;
    this.sequenceService = sequenceService;
    this.saleOrderService = saleOrderService;
    this.saleOrderPrintService = saleOrderPrintService;
    this.saleConfigService = saleConfigService;
    this.appSaleService = appSaleService;
    this.appCrmService = appCrmService;
    this.saleOrderSequenceService = saleOrderSequenceService;
  }

  @Override
  @Transactional(
      rollbackOn = {Exception.class},
      ignore = {BlockedSaleOrderException.class})
  public void finalizeQuotation(SaleOrder saleOrder) throws AxelorException {

    if (saleOrder.getStatusSelect() == null
        || saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_DRAFT_QUOTATION) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_FINALIZE_QUOTATION_WRONG_STATUS));
    }

    Partner partner = saleOrder.getClientPartner();

    checkSaleOrderBeforeFinalization(saleOrder);

    Blocking blocking =
        Beans.get(BlockingService.class)
            .getBlocking(partner, saleOrder.getCompany(), BlockingRepository.SALE_BLOCKING);

    if (blocking != null) {
      saleOrder.setBlockedOnCustCreditExceed(true);
      if (!saleOrder.getManualUnblock()) {
        saleOrderRepository.save(saleOrder);
        String reason =
            blocking.getBlockingReason() != null ? blocking.getBlockingReason().getName() : "";
        throw new BlockedSaleOrderException(
            partner, I18n.get("Client is sale blocked:") + " " + reason);
      }
    }

    if (sequenceService.isEmptyOrDraftSequenceNumber(saleOrder.getSaleOrderSeq())) {
      saleOrder.setSaleOrderSeq(saleOrderSequenceService.getQuotationSequence(saleOrder));
    }

    saleOrder.setStatusSelect(SaleOrderRepository.STATUS_FINALIZED_QUOTATION);

    Opportunity opportunity = saleOrder.getOpportunity();
    if (opportunity != null) {
      opportunity.setOpportunityStatus(appCrmService.getSalesPropositionStatus());
    }

    saleOrderRepository.save(saleOrder);

    if (appSaleService.getAppSale().getPrintingOnSOFinalization()) {
      this.saveSaleOrderPDFAsAttachment(saleOrder);
    }
  }

  /**
   * Throws exceptions to block the finalization of given sale order.
   *
   * @param saleOrder a sale order being finalized
   */
  protected void checkSaleOrderBeforeFinalization(SaleOrder saleOrder) throws AxelorException {
    saleOrderService.checkUnauthorizedDiscounts(saleOrder);
  }

  protected void saveSaleOrderPDFAsAttachment(SaleOrder saleOrder) throws AxelorException {
    saleOrderPrintService.print(
        saleOrder,
        false,
        saleConfigService.getSaleOrderPrintTemplate(saleOrder.getCompany()),
        true);
  }
}
