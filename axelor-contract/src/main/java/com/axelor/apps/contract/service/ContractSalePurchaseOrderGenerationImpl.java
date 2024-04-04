package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ContractSalePurchaseOrderGenerationImpl
    implements ContractSalePurchaseOrderGeneration {

  protected SaleOrderRepository saleOrderRepository;
  protected PurchaseOrderRepository purchaseOrderRepository;
  protected AnalyticMoveLineRepository analyticMoveLineRepo;
  protected SaleOrderCreateService saleOrderCreateService;

  @Inject
  public ContractSalePurchaseOrderGenerationImpl(
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      AnalyticMoveLineRepository analyticMoveLineRepo,
      SaleOrderCreateService saleOrderCreateService) {
    this.saleOrderRepository = saleOrderRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.analyticMoveLineRepo = analyticMoveLineRepo;
    this.saleOrderCreateService = saleOrderCreateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder generateSaleOrder(Contract contract) throws AxelorException {

    SaleOrder saleOrder = saleOrderCreateService.createSaleOrder(contract.getCompany());
    saleOrder.setCurrency(contract.getCurrency());
    saleOrder.setClientPartner(contract.getPartner());
    saleOrder.setInvoicedPartner(contract.getInvoicedPartner());
    saleOrder.setPaymentMode(contract.getCurrentContractVersion().getPaymentMode());
    saleOrder.setPaymentCondition(contract.getInvoicedPartner().getPaymentCondition());
    saleOrder.setContract(contract);

    for (ContractLine contractLine : contract.getCurrentContractVersion().getContractLineList()) {
      createSaleOrderLineFromContractLine(contractLine, saleOrder);
    }

    for (ContractLine contractLine : contract.getAdditionalBenefitContractLineList()) {
      createSaleOrderLineFromContractLine(contractLine, saleOrder);
    }
    return saleOrderRepository.save(saleOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder generatePurchaseOrder(Contract contract) throws AxelorException {

    PurchaseOrder purchaseOrder = new PurchaseOrder();
    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_DRAFT);
    purchaseOrder.setCompany(contract.getCompany());
    purchaseOrder.setSupplierPartner(contract.getPartner());
    purchaseOrder.setCurrency(contract.getCurrency());
    purchaseOrder.setPaymentMode(contract.getCurrentContractVersion().getPaymentMode());
    purchaseOrder.setPaymentCondition(contract.getPartner().getPaymentCondition());
    purchaseOrder.setContract(contract);

    for (ContractLine contractLine : contract.getCurrentContractVersion().getContractLineList()) {
      createPurchaseOrderLineFromContractLine(contractLine, purchaseOrder);
    }

    for (ContractLine contractLine : contract.getAdditionalBenefitContractLineList()) {
      createPurchaseOrderLineFromContractLine(contractLine, purchaseOrder);
    }

    return purchaseOrderRepository.save(purchaseOrder);
  }

  public void createSaleOrderLineFromContractLine(ContractLine contractLine, SaleOrder saleOrder) {

    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setProduct(contractLine.getProduct());
    saleOrderLine.setProductName(contractLine.getProductName());

    saleOrderLine.setDescription(contractLine.getDescription());
    saleOrderLine.setQty(contractLine.getQty());
    saleOrderLine.setUnit(contractLine.getUnit());

    saleOrderLine.setPrice(contractLine.getPrice());
    saleOrderLine.setExTaxTotal(contractLine.getExTaxTotal());
    saleOrderLine.setDiscountTypeSelect(contractLine.getDiscountTypeSelect());
    saleOrderLine.setDiscountAmount(contractLine.getDiscountAmount());

    saleOrderLine.setPriceDiscounted(contractLine.getPriceDiscounted());

    saleOrderLine.setTaxLineSet(Sets.newHashSet(contractLine.getTaxLineSet()));

    saleOrder.addSaleOrderLineListItem(saleOrderLine);

    copyAnalyticsDataToSaleOrderLine(contractLine, saleOrderLine);
  }

  public void createPurchaseOrderLineFromContractLine(
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

  public void copyAnalyticsDataToSaleOrderLine(
      ContractLine contractLine, SaleOrderLine saleOrderLine) {

    saleOrderLine.setAnalyticDistributionTemplate(contractLine.getAnalyticDistributionTemplate());

    saleOrderLine.setAxis1AnalyticAccount(contractLine.getAxis1AnalyticAccount());
    saleOrderLine.setAxis2AnalyticAccount(contractLine.getAxis2AnalyticAccount());
    saleOrderLine.setAxis3AnalyticAccount(contractLine.getAxis3AnalyticAccount());
    saleOrderLine.setAxis4AnalyticAccount(contractLine.getAxis4AnalyticAccount());
    saleOrderLine.setAxis5AnalyticAccount(contractLine.getAxis5AnalyticAccount());

    for (AnalyticMoveLine originalAnalyticMoveLine : contractLine.getAnalyticMoveLineList()) {
      AnalyticMoveLine analyticMoveLine =
          analyticMoveLineRepo.copy(originalAnalyticMoveLine, false);

      analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);
      analyticMoveLine.setContractLine(null);
      saleOrderLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }
  }

  public void copyAnalyticsDataToPurchaseOrderLine(
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
