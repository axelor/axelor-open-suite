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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
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
  protected PurchaseOrderCreateService purchaseOrderCreateService;
  protected AnalyticLineModelFromContractService analyticLineModelFromContractService;

  @Inject
  public ContractPurchaseOrderGenerationImpl(
      AppBaseService appBaseService,
      PurchaseOrderRepository purchaseOrderRepository,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateService purchaseOrderCreateService,
      AnalyticLineModelFromContractService analyticLineModelFromContractService) {
    this.appBaseService = appBaseService;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderCreateService = purchaseOrderCreateService;
    this.analyticLineModelFromContractService = analyticLineModelFromContractService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder generatePurchaseOrder(Contract contract) throws AxelorException {

    PurchaseOrder purchaseOrder =
        purchaseOrderCreateService.createPurchaseOrder(
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
    purchaseOrder.setPaymentCondition(contract.getPartner().getOutPaymentCondition());
    purchaseOrder.setContract(contract);
    purchaseOrder.setTradingName(contract.getTradingName());

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
    purchaseOrderLine.setIsTitleLine(
        contractLine.getTypeSelect() == ContractLineRepository.TYPE_TITLE);
    purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);

    AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine, purchaseOrder);
    analyticLineModelFromContractService.copyAnalyticsDataFromContractLine(
        contractLine, analyticLineModel);
  }
}
