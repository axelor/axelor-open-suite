package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ContractSaleOrderGenerationImpl implements ContractSaleOrderGeneration {

  protected AppBaseService appBaseService;
  protected SaleOrderRepository saleOrderRepository;
  protected SaleOrderCreateService saleOrderCreateService;
  protected SaleOrderComputeService saleOrderComputeService;
  protected AnalyticLineModelFromContractService analyticLineModelFromContractService;

  @Inject
  public ContractSaleOrderGenerationImpl(
      AppBaseService appBaseService,
      SaleOrderRepository saleOrderRepository,
      SaleOrderCreateService saleOrderCreateService,
      SaleOrderComputeService saleOrderComputeService,
      AnalyticLineModelFromContractService analyticLineModelFromContractService) {
    this.appBaseService = appBaseService;
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderCreateService = saleOrderCreateService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.analyticLineModelFromContractService = analyticLineModelFromContractService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder generateSaleOrder(Contract contract) throws AxelorException {
    Partner clientPartner = contract.getPartner();

    SaleOrder saleOrder = saleOrderCreateService.createSaleOrder(contract.getCompany());
    saleOrder.setCurrency(contract.getCurrency());
    saleOrder.setClientPartner(clientPartner);
    saleOrder.setInvoicedPartner(contract.getInvoicedPartner());
    saleOrder.setPaymentMode(contract.getCurrentContractVersion().getPaymentMode());
    saleOrder.setPaymentCondition(contract.getInvoicedPartner().getPaymentCondition());
    saleOrder.setContract(contract);

    if (appBaseService.getAppBase().getActivatePartnerRelations()) {
      saleOrder.setDeliveredPartner(clientPartner);
    }

    for (ContractLine contractLine : contract.getCurrentContractVersion().getContractLineList()) {
      createSaleOrderLineFromContractLine(contractLine, saleOrder);
    }

    for (ContractLine contractLine : contract.getAdditionalBenefitContractLineList()) {
      createSaleOrderLineFromContractLine(contractLine, saleOrder);
    }
    saleOrderComputeService.computeSaleOrder(saleOrder);
    return saleOrderRepository.save(saleOrder);
  }

  protected void createSaleOrderLineFromContractLine(
      ContractLine contractLine, SaleOrder saleOrder) {

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

    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    analyticLineModelFromContractService.copyAnalyticsDataFromContractLine(
        contractLine, analyticLineModel);
  }
}
