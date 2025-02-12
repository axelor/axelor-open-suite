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
import com.axelor.apps.sale.service.saleorder.SaleOrderGeneratorService;
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
  protected SaleOrderGeneratorService saleOrderGeneratorService;

  @Inject
  public ContractSaleOrderGenerationImpl(
      AppBaseService appBaseService,
      SaleOrderRepository saleOrderRepository,
      SaleOrderCreateService saleOrderCreateService,
      SaleOrderComputeService saleOrderComputeService,
      AnalyticLineModelFromContractService analyticLineModelFromContractService,
      SaleOrderGeneratorService saleOrderGeneratorService) {
    this.appBaseService = appBaseService;
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderCreateService = saleOrderCreateService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.analyticLineModelFromContractService = analyticLineModelFromContractService;
    this.saleOrderGeneratorService = saleOrderGeneratorService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder generateSaleOrder(Contract contract) throws AxelorException {
    Partner clientPartner = contract.getPartner();

    SaleOrder saleOrder =
        saleOrderGeneratorService.createSaleOrder(
            clientPartner, contract.getCompany(), null, contract.getCurrency(), null);

    saleOrder.setInvoicedPartner(contract.getInvoicedPartner());
    saleOrder.setPaymentMode(contract.getCurrentContractVersion().getPaymentMode());
    saleOrder.setPaymentCondition(contract.getInvoicedPartner().getPaymentCondition());
    saleOrder.setContract(contract);
    saleOrder.setTradingName(contract.getTradingName());

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
    saleOrderLine.setTypeSelect(contractLine.getTypeSelect());

    saleOrder.addSaleOrderLineListItem(saleOrderLine);

    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    analyticLineModelFromContractService.copyAnalyticsDataFromContractLine(
        contractLine, analyticLineModel);
  }
}
