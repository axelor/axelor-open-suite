package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.auth.AuthUtils;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ContractPurchaseOrderGenerationImpl implements ContractPurchaseOrderGeneration {

  protected AppBaseService appBaseService;
  protected PurchaseOrderRepository purchaseOrderRepository;
  protected PurchaseOrderService purchaseOrderService;
  protected AnalyticLineModelFromContractService analyticLineModelFromContractService;

  @Inject
  public ContractPurchaseOrderGenerationImpl(
      AppBaseService appBaseService,
      PurchaseOrderRepository purchaseOrderRepository,
      PurchaseOrderService purchaseOrderService,
      AnalyticLineModelFromContractService analyticLineModelFromContractService) {
    this.appBaseService = appBaseService;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.purchaseOrderService = purchaseOrderService;
    this.analyticLineModelFromContractService = analyticLineModelFromContractService;
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

    AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine, purchaseOrder);
    analyticLineModelFromContractService.copyAnalyticsDataFromContractLine(
        contractLine, analyticLineModel);
  }
}
