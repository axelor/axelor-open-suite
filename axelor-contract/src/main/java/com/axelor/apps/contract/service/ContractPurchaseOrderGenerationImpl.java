package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.auth.AuthUtils;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ContractPurchaseOrderGenerationImpl implements ContractPurchaseOrderGeneration {

  protected AppBaseService appBaseService;
  protected PurchaseOrderRepository purchaseOrderRepository;
  protected AnalyticMoveLineRepository analyticMoveLineRepo;
  protected PurchaseOrderService purchaseOrderService;

  @Inject
  public ContractPurchaseOrderGenerationImpl(
      AppBaseService appBaseService,
      PurchaseOrderRepository purchaseOrderRepository,
      AnalyticMoveLineRepository analyticMoveLineRepo,
      PurchaseOrderService purchaseOrderService) {
    this.appBaseService = appBaseService;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.analyticMoveLineRepo = analyticMoveLineRepo;
    this.purchaseOrderService = purchaseOrderService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder generatePurchaseOrder(Contract contract) throws AxelorException {

    PurchaseOrder purchaseOrder =
        purchaseOrderService.createPurchaseOrder(
            AuthUtils.getUser(),
            contract.getCompany(),
            null,
            contract.getCurrency(),
            null,
            null,
            null,
            appBaseService.getTodayDate(contract.getCompany()),
            null,
            contract.getPartner(),
            null);
    purchaseOrder.setPaymentMode(contract.getCurrentContractVersion().getPaymentMode());
    purchaseOrder.setPaymentCondition(contract.getPartner().getPaymentCondition());
    purchaseOrder.setContract(contract);

    for (ContractLine contractLine : contract.getCurrentContractVersion().getContractLineList()) {
      createPurchaseOrderLineFromContractLine(contractLine, purchaseOrder);
    }

    for (ContractLine contractLine : contract.getAdditionalBenefitContractLineList()) {
      createPurchaseOrderLineFromContractLine(contractLine, purchaseOrder);
    }

    purchaseOrderService.computePurchaseOrder(purchaseOrder);
    return purchaseOrderRepository.save(purchaseOrder);
  }

  protected void createPurchaseOrderLineFromContractLine(
      ContractLine contractLine, PurchaseOrder purchaseOrder) {

    PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
    purchaseOrderLine.setProduct(contractLine.getProduct());
    purchaseOrderLine.setProductName(contractLine.getProductName());

    purchaseOrderLine.setDescription(contractLine.getDescription());
    purchaseOrderLine.setQty(contractLine.getQty());
    purchaseOrderLine.setUnit(contractLine.getUnit());

    purchaseOrderLine.setPrice(contractLine.getPrice());
    purchaseOrderLine.setExTaxTotal(contractLine.getExTaxTotal());
    purchaseOrderLine.setDiscountTypeSelect(contractLine.getDiscountTypeSelect());
    purchaseOrderLine.setDiscountAmount(contractLine.getDiscountAmount());

    purchaseOrderLine.setPriceDiscounted(contractLine.getPriceDiscounted());

    purchaseOrderLine.setTaxLineSet(Sets.newHashSet(contractLine.getTaxLineSet()));
    purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);

    copyAnalyticsDataToPurchaseOrderLine(contractLine, purchaseOrderLine);
  }

  protected void copyAnalyticsDataToPurchaseOrderLine(
      ContractLine contractLine, PurchaseOrderLine purchaseOrderLine) {

    purchaseOrderLine.setAnalyticDistributionTemplate(
        contractLine.getAnalyticDistributionTemplate());

    purchaseOrderLine.setAxis1AnalyticAccount(contractLine.getAxis1AnalyticAccount());
    purchaseOrderLine.setAxis2AnalyticAccount(contractLine.getAxis2AnalyticAccount());
    purchaseOrderLine.setAxis3AnalyticAccount(contractLine.getAxis3AnalyticAccount());
    purchaseOrderLine.setAxis4AnalyticAccount(contractLine.getAxis4AnalyticAccount());
    purchaseOrderLine.setAxis5AnalyticAccount(contractLine.getAxis5AnalyticAccount());

    for (AnalyticMoveLine originalAnalyticMoveLine : contractLine.getAnalyticMoveLineList()) {
      AnalyticMoveLine analyticMoveLine =
          analyticMoveLineRepo.copy(originalAnalyticMoveLine, false);

      analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);
      analyticMoveLine.setContractLine(null);
      purchaseOrderLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }
  }
}
