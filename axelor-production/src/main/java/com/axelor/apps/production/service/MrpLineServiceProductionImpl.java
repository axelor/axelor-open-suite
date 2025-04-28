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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderService.ManufOrderOriginTypeProduction;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineOriginRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.service.MrpLineServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderCreateSupplychainService;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;

public class MrpLineServiceProductionImpl extends MrpLineServiceImpl {

  protected ManufOrderService manufOrderService;
  protected ManufOrderRepository manufOrderRepository;
  protected OperationOrderRepository operationOrderRepository;
  protected BillOfMaterialService billOfMaterialService;
  protected ProdProcessLineService prodProcessLineService;
  protected ProductionConfigService productionConfigService;
  protected final ProdProcessComputationService prodProcessComputationService;

  @Inject
  public MrpLineServiceProductionImpl(
      AppBaseService appBaseService,
      PurchaseOrderCreateSupplychainService purchaseOrderCreateSupplychainService,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      StockRulesService stockRulesService,
      SaleOrderLineRepository saleOrderLineRepo,
      PurchaseOrderLineRepository purchaseOrderLineRepo,
      MrpForecastRepository mrpForecastRepo,
      ManufOrderService manufOrderService,
      ManufOrderRepository manufOrderRepository,
      OperationOrderRepository operationOrderRepository,
      MrpLineRepository mrpLineRepo,
      BillOfMaterialService billOfMaterialService,
      ProdProcessLineService prodProcessLineService,
      ProductionConfigService productionConfigService,
      ProdProcessComputationService prodProcessComputationService) {
    super(
        appBaseService,
        purchaseOrderCreateSupplychainService,
        purchaseOrderService,
        purchaseOrderLineService,
        purchaseOrderRepo,
        stockRulesService,
        saleOrderLineRepo,
        purchaseOrderLineRepo,
        mrpForecastRepo,
        mrpLineRepo);
    this.manufOrderService = manufOrderService;
    this.manufOrderRepository = manufOrderRepository;
    this.operationOrderRepository = operationOrderRepository;
    this.billOfMaterialService = billOfMaterialService;
    this.prodProcessLineService = prodProcessLineService;
    this.productionConfigService = productionConfigService;
    this.prodProcessComputationService = prodProcessComputationService;
  }

  @Override
  public void generateProposal(
      MrpLine mrpLine,
      Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders,
      Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier,
      boolean isProposalsPerSupplier)
      throws AxelorException {

    super.generateProposal(
        mrpLine, purchaseOrders, purchaseOrdersPerSupplier, isProposalsPerSupplier);

    if (mrpLine.getMrpLineType().getElementSelect()
            == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL
        && Beans.get(AppProductionService.class).isApp("production")) {

      this.generateManufacturingProposal(mrpLine);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void generateManufacturingProposal(MrpLine mrpLine) throws AxelorException {

    Product product = mrpLine.getProduct();
    Company company = mrpLine.getStockLocation().getCompany();

    ProductionConfig productionConfig = productionConfigService.getProductionConfig(company);

    boolean isAsapScheduling =
        productionConfig.getScheduling()
            == ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING;

    LocalDate maturityDate = mrpLine.getMaturityDate();
    BigDecimal qty = mrpLine.getQty();

    LocalDateTime plannedStartDateT = null;
    LocalDateTime plannedEndDateT = null;

    BillOfMaterial billOfMaterial = mrpLine.getBillOfMaterial();
    if (billOfMaterial == null) {
      billOfMaterial = billOfMaterialService.getDefaultBOM(product, company);
    }

    if (isAsapScheduling) {
      plannedStartDateT = maturityDate.atStartOfDay();
    } else {

      // The +2 adds 2 minutes to the plannedEndDateT to avoid the overflowing of the calculated
      // plannedStartDateT on the current date time.
      LocalDateTime maturityDateTime =
          maturityDate.isEqual(LocalDate.now())
              ? maturityDate.atTime(
                  appBaseService.getTodayDateTime(company).toLocalTime().plusMinutes(2))
              : maturityDate.atStartOfDay();
      plannedEndDateT =
          maturityDateTime.plusMinutes(
              getTotalDurationInMinutes(billOfMaterial.getProdProcess(), qty));
    }

    if (billOfMaterial.getProdProcess() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.MRP_PROD_PROCESS_REQUIRED),
          product.getName());
    }

    ManufOrder manufOrder =
        manufOrderService.generateManufOrder(
            product,
            mrpLine.getQty(),
            ManufOrderService.DEFAULT_PRIORITY,
            ManufOrderService.IS_TO_INVOICE,
            billOfMaterial,
            plannedStartDateT,
            plannedEndDateT,
            ManufOrderOriginTypeProduction
                .ORIGIN_TYPE_MRP); // TODO compute the time to produce to put the manuf order at the
    // correct day

    linkToOrder(mrpLine, manufOrder);
  }

  protected long getTotalDurationInMinutes(ProdProcess prodProcess, BigDecimal qty)
      throws AxelorException {
    long totalDuration = 0;
    if (prodProcess != null) {
      totalDuration = prodProcessComputationService.getLeadTime(prodProcess, qty);
    }
    return TimeUnit.SECONDS.toMinutes(totalDuration);
  }

  @Override
  protected String getMrpLineOriginStr(MrpLineOrigin mrpLineOrigin) {
    if (mrpLineOrigin
        .getRelatedToSelect()
        .equals(MrpLineOriginRepository.RELATED_TO_MANUFACTURING_ORDER)) {
      ManufOrder manufOrder = manufOrderRepository.find(mrpLineOrigin.getRelatedToSelectId());
      return manufOrder.getManufOrderSeq();
    }
    if (mrpLineOrigin
        .getRelatedToSelect()
        .equals(MrpLineOriginRepository.RELATED_TO_OPERATION_ORDER)) {
      OperationOrder operationOrder =
          operationOrderRepository.find(mrpLineOrigin.getRelatedToSelectId());
      return operationOrder.getName();
    }
    return super.getMrpLineOriginStr(mrpLineOrigin);
  }

  @Override
  protected String computeRelatedName(Model model) {

    if (!Beans.get(AppProductionService.class).isApp("production")) {

      return super.computeRelatedName(model);
    }

    if (model instanceof ManufOrder) {

      return ((ManufOrder) model).getManufOrderSeq();

    } else if (model instanceof OperationOrder) {

      return ((OperationOrder) model).getName();

    } else {

      return super.computeRelatedName(model);
    }
  }

  @Override
  protected Partner getPartner(Model model) {

    if (!Beans.get(AppProductionService.class).isApp("production")) {
      return super.getPartner(model);
    }

    if (model instanceof ManufOrder) {

      return ((ManufOrder) model).getClientPartner();

    } else if (model instanceof OperationOrder) {

      return ((OperationOrder) model).getManufOrder().getClientPartner();

    } else {

      return super.getPartner(model);
    }
  }
}
