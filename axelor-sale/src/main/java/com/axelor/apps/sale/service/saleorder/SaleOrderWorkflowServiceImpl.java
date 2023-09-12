/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.BlockedSaleOrderException;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;

public class SaleOrderWorkflowServiceImpl implements SaleOrderWorkflowService {

  protected SequenceService sequenceService;
  protected PartnerRepository partnerRepo;
  protected SaleOrderRepository saleOrderRepo;
  protected AppSaleService appSaleService;
  protected AppCrmService appCrmService;
  protected UserService userService;
  protected SaleOrderLineService saleOrderLineService;
  protected BirtTemplateService birtTemplateService;
  protected SaleOrderService saleOrderService;
  protected SaleConfigService saleConfigService;

  @Inject
  public SaleOrderWorkflowServiceImpl(
      SequenceService sequenceService,
      PartnerRepository partnerRepo,
      SaleOrderRepository saleOrderRepo,
      AppSaleService appSaleService,
      AppCrmService appCrmService,
      UserService userService,
      SaleOrderLineService saleOrderLineService,
      BirtTemplateService birtTemplateService,
      SaleOrderService saleOrderService,
      SaleConfigService saleConfigService) {

    this.sequenceService = sequenceService;
    this.partnerRepo = partnerRepo;
    this.saleOrderRepo = saleOrderRepo;
    this.appSaleService = appSaleService;
    this.appCrmService = appCrmService;
    this.userService = userService;
    this.saleOrderLineService = saleOrderLineService;
    this.birtTemplateService = birtTemplateService;
    this.saleOrderService = saleOrderService;
    this.saleConfigService = saleConfigService;
  }

  @Override
  @Transactional
  public Partner validateCustomer(SaleOrder saleOrder) {

    Partner clientPartner = partnerRepo.find(saleOrder.getClientPartner().getId());
    clientPartner.setIsCustomer(true);
    clientPartner.setIsProspect(false);

    return partnerRepo.save(clientPartner);
  }

  @Override
  public String getSequence(Company company) throws AxelorException {

    String seq =
        sequenceService.getSequenceNumber(
            SequenceRepository.SALES_ORDER, company, SaleOrder.class, "saleOrderSeq");
    if (seq == null) {
      throw new AxelorException(
          company,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SaleExceptionMessage.SALES_ORDER_1),
          company.getName());
    }
    return seq;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelSaleOrder(
      SaleOrder saleOrder, CancelReason cancelReason, String cancelReasonStr)
      throws AxelorException {

    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(SaleOrderRepository.STATUS_DRAFT_QUOTATION);
    authorizedStatus.add(SaleOrderRepository.STATUS_FINALIZED_QUOTATION);
    if (saleOrder.getStatusSelect() == null
        || !authorizedStatus.contains(saleOrder.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_CANCEL_WRONG_STATUS));
    }

    Query q =
        JPA.em()
            .createQuery(
                "select count(*) FROM SaleOrder as self WHERE self.statusSelect in (?1 , ?2) AND self.clientPartner = ?3 ");
    q.setParameter(1, SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    q.setParameter(2, SaleOrderRepository.STATUS_ORDER_COMPLETED);
    q.setParameter(3, saleOrder.getClientPartner());
    if ((long) q.getSingleResult() == 0) {
      saleOrder.getClientPartner().setIsCustomer(false);
      saleOrder.getClientPartner().setIsProspect(true);
    }
    saleOrder.setStatusSelect(SaleOrderRepository.STATUS_CANCELED);
    saleOrder.setCancelReason(cancelReason);
    if (Strings.isNullOrEmpty(cancelReasonStr)) {
      saleOrder.setCancelReasonStr(cancelReason.getName());
    } else {
      saleOrder.setCancelReasonStr(cancelReasonStr);
    }
    saleOrderRepo.save(saleOrder);
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
        saleOrderRepo.save(saleOrder);
        String reason =
            blocking.getBlockingReason() != null ? blocking.getBlockingReason().getName() : "";
        throw new BlockedSaleOrderException(
            partner, I18n.get("Client is sale blocked:") + " " + reason);
      }
    }

    if (saleOrder.getVersionNumber() == 1
        && sequenceService.isEmptyOrDraftSequenceNumber(saleOrder.getSaleOrderSeq())) {
      saleOrder.setSaleOrderSeq(this.getSequence(saleOrder.getCompany()));
    }

    saleOrder.setStatusSelect(SaleOrderRepository.STATUS_FINALIZED_QUOTATION);
    if (appSaleService.getAppSale().getPrintingOnSOFinalization()) {
      this.saveSaleOrderPDFAsAttachment(saleOrder);
    }

    Opportunity opportunity = saleOrder.getOpportunity();
    if (opportunity != null) {
      opportunity.setOpportunityStatus(appCrmService.getSalesPropositionStatus());
    }

    saleOrderRepo.save(saleOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirmSaleOrder(SaleOrder saleOrder) throws AxelorException {
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

    saleOrderRepo.save(saleOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void completeSaleOrder(SaleOrder saleOrder) throws AxelorException {

    if (saleOrder.getStatusSelect() == null
        || saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_COMPLETE_WRONG_STATUS));
    }

    saleOrder.setStatusSelect(SaleOrderRepository.STATUS_ORDER_COMPLETED);
    saleOrder.setOrderBeingEdited(false);

    saleOrderRepo.save(saleOrder);
  }

  @Override
  public void saveSaleOrderPDFAsAttachment(SaleOrder saleOrder) throws AxelorException {

    saleOrderService.checkPrintingSettings(saleOrder);
    BirtTemplate saleOrderBirtTemplate =
        saleConfigService.getSaleOrderBirtTemplate(saleOrder.getCompany());

    birtTemplateService.generateBirtTemplateLink(
        saleOrderBirtTemplate,
        EntityHelper.getEntity(saleOrder),
        Map.of("ProformaInvoice", false),
        saleOrderService.getFileName(saleOrder) + " - ${date}",
        true,
        ReportSettings.FORMAT_PDF);
  }

  /**
   * Throws exceptions to block the finalization of given sale order.
   *
   * @param saleOrder a sale order being finalized
   */
  protected void checkSaleOrderBeforeFinalization(SaleOrder saleOrder) throws AxelorException {
    saleOrderService.checkUnauthorizedDiscounts(saleOrder);
  }
}
