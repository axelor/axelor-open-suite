/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineOriginRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MrpLineServiceImpl implements MrpLineService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected PurchaseOrderSupplychainService purchaseOrderSupplychainService;
  protected PurchaseOrderService purchaseOrderService;
  protected PurchaseOrderLineService purchaseOrderLineService;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected StockRulesService stockRulesService;
  protected SaleOrderLineRepository saleOrderLineRepo;
  protected PurchaseOrderLineRepository purchaseOrderLineRepo;
  protected MrpForecastRepository mrpForecastRepo;
  protected MrpLineRepository mrpLineRepo;

  @Inject
  public MrpLineServiceImpl(
      AppBaseService appBaseService,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      StockRulesService stockRulesService,
      SaleOrderLineRepository saleOrderLineRepo,
      PurchaseOrderLineRepository purchaseOrderLineRepo,
      MrpForecastRepository mrpForecastRepo,
      MrpLineRepository mrpLineRepo) {

    this.appBaseService = appBaseService;
    this.purchaseOrderSupplychainService = purchaseOrderSupplychainService;
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.stockRulesService = stockRulesService;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.purchaseOrderLineRepo = purchaseOrderLineRepo;
    this.mrpForecastRepo = mrpForecastRepo;
    this.mrpLineRepo = mrpLineRepo;
  }

  @Override
  public void generateProposal(MrpLine mrpLine) throws AxelorException {
    generateProposal(mrpLine, null, null, false);
  }

  @Override
  public void generateProposal(
      MrpLine mrpLine,
      Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders,
      Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier,
      boolean isProposalsPerSupplier)
      throws AxelorException {

    if (mrpLine.getMrpLineType().getElementSelect()
        == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL) {

      this.generatePurchaseProposal(
          mrpLine, purchaseOrders, purchaseOrdersPerSupplier, isProposalsPerSupplier);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void generatePurchaseProposal(
      MrpLine mrpLine,
      Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders,
      Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier,
      boolean isProposalsPerSupplier)
      throws AxelorException {

    Product product = mrpLine.getProduct();
    StockLocation stockLocation = mrpLine.getStockLocation();
    LocalDate maturityDate = mrpLine.getMaturityDate();

    Partner supplierPartner = mrpLine.getSupplierPartner();

    if (supplierPartner == null) {
      supplierPartner = product.getDefaultSupplierPartner();

      if (supplierPartner == null) {
        throw new AxelorException(
            mrpLine,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(SupplychainExceptionMessage.MRP_LINE_1),
            product.getFullName());
      }
    }

    Company company = stockLocation.getCompany();

    Pair<Partner, LocalDate> key = null;
    PurchaseOrder purchaseOrder = null;

    if (isProposalsPerSupplier) {
      if (purchaseOrdersPerSupplier != null && !purchaseOrdersPerSupplier.isEmpty()) {
        purchaseOrder = purchaseOrdersPerSupplier.get(supplierPartner);
        if (purchaseOrder != null) {
          purchaseOrder = purchaseOrderRepo.find(purchaseOrder.getId());
        }
      }
    } else {
      if (purchaseOrders != null) {
        key = Pair.of(supplierPartner, maturityDate);
        purchaseOrder = purchaseOrders.get(key);
      }
    }

    if (purchaseOrder == null) {
      purchaseOrder =
          purchaseOrderRepo.save(
              purchaseOrderSupplychainService.createPurchaseOrder(
                  AuthUtils.getUser(),
                  company,
                  null,
                  supplierPartner.getCurrency(),
                  null,
                  this.getPurchaseOrderOrigin(mrpLine),
                  null,
                  stockLocation,
                  appBaseService.getTodayDate(company),
                  Beans.get(PartnerPriceListService.class)
                      .getDefaultPriceList(supplierPartner, PriceListRepository.TYPE_PURCHASE),
                  supplierPartner,
                  null));
      if (isProposalsPerSupplier) {
        if (purchaseOrdersPerSupplier != null) {
          purchaseOrdersPerSupplier.put(supplierPartner, purchaseOrder);
        }
      } else {
        if (purchaseOrders != null) {
          purchaseOrders.put(key, purchaseOrder);
        }
      }
      if (mrpLine.getMrpLineOriginList().size() == 1) {
        if (mrpLine
            .getMrpLineOriginList()
            .get(0)
            .getRelatedToSelect()
            .equals(MrpLineOriginRepository.RELATED_TO_SALE_ORDER_LINE)) {
          purchaseOrder.setGeneratedSaleOrderId(
              saleOrderLineRepo
                  .find(mrpLine.getMrpLineOriginList().get(0).getRelatedToSelectId())
                  .getSaleOrder()
                  .getId());
        }
      }
    }
    Unit unit = product.getPurchasesUnit();
    BigDecimal qty = mrpLine.getQty();
    if (unit == null) {
      unit = product.getUnit();
    } else {
      qty =
          Beans.get(UnitConversionService.class)
              .convert(product.getUnit(), unit, qty, qty.scale(), product);
    }
    PurchaseOrderLine poLine =
        purchaseOrderLineService.createPurchaseOrderLine(
            purchaseOrder, product, null, null, qty, unit);
    poLine.setDesiredReceiptDate(maturityDate);
    if (mrpLine.getEstimatedDeliveryMrpLine() != null) {
      poLine.setDesiredReceiptDate(mrpLine.getEstimatedDeliveryMrpLine().getMaturityDate());
    }
    poLine.setEstimatedReceiptDate(poLine.getDesiredReceiptDate());
    purchaseOrder.addPurchaseOrderLineListItem(poLine);

    purchaseOrderService.computePurchaseOrder(purchaseOrder);

    linkToOrder(mrpLine, purchaseOrder);
  }

  protected String getPurchaseOrderOrigin(MrpLine mrpLine) {
    StringBuilder origin = new StringBuilder();
    int count = 0;
    for (MrpLineOrigin mrpLineOrigin : mrpLine.getMrpLineOriginList()) {
      count++;
      origin.append(getMrpLineOriginStr(mrpLineOrigin));
      if (count < mrpLine.getMrpLineOriginList().size()) {
        origin.append(" & ");
      }
    }
    return origin.toString();
  }

  protected String getMrpLineOriginStr(MrpLineOrigin mrpLineOrigin) {
    if (mrpLineOrigin
        .getRelatedToSelect()
        .equals(MrpLineOriginRepository.RELATED_TO_SALE_ORDER_LINE)) {
      SaleOrder saleOrder =
          saleOrderLineRepo.find(mrpLineOrigin.getRelatedToSelectId()).getSaleOrder();
      return saleOrder.getSaleOrderSeq();
    }
    if (mrpLineOrigin
        .getRelatedToSelect()
        .equals(MrpLineOriginRepository.RELATED_TO_PURCHASE_ORDER_LINE)) {
      PurchaseOrder purchaseOrder =
          purchaseOrderLineRepo.find(mrpLineOrigin.getRelatedToSelectId()).getPurchaseOrder();
      return purchaseOrder.getPurchaseOrderSeq();
    }
    if (mrpLineOrigin
        .getRelatedToSelect()
        .equals(MrpLineOriginRepository.RELATED_TO_MRP_FORECAST)) {
      return mrpLineOrigin.getMrpLine().getMrp().getMrpSeq() + "-" + I18n.get("MRP forecast");
    }
    return "";
  }

  protected void linkToOrder(MrpLine mrpLine, AuditableModel order) {
    mrpLine.setProposalSelect(order.getClass().getName());
    mrpLine.setProposalSelectId(order.getId());
    mrpLine.setProposalGenerated(true);
  }

  public MrpLine createMrpLine(
      Mrp mrp,
      Product product,
      int maxLevel,
      MrpLineType mrpLineType,
      BigDecimal qty,
      LocalDate maturityDate,
      BigDecimal cumulativeQty,
      StockLocation stockLocation,
      Model model) {

    MrpLine mrpLine = new MrpLine();

    mrpLine.setMrp(mrp);
    mrpLine.setProduct(product);
    mrpLine.setMaxLevel(maxLevel);
    mrpLine.setMrpLineType(mrpLineType);
    if (mrpLineType.getTypeSelect() == MrpLineTypeRepository.TYPE_OUT) {
      mrpLine.setQty(qty.negate());
    } else {
      mrpLine.setQty(qty);
    }
    mrpLine.setMaturityDate(maturityDate);
    mrpLine.setCumulativeQty(cumulativeQty);
    mrpLine.setStockLocation(stockLocation);

    mrpLine = this.setMrpLineQty(mrpLine, product, stockLocation);

    if (mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL) {
      mrpLine.setSupplierPartner(product.getDefaultSupplierPartner());
    }

    this.updatePartner(mrpLine, model);

    this.createMrpLineOrigins(mrpLine, model);

    log.debug(
        "Create mrp line for the product {}, level {}, mrpLineType {}, qty {}, maturity date {}, cumulative qty {}, stock location {}, related to {}",
        product.getCode(),
        maxLevel,
        mrpLineType.getCode(),
        qty,
        maturityDate,
        cumulativeQty,
        stockLocation.getName(),
        mrpLine.getRelatedToSelectName());

    return mrpLine;
  }

  protected MrpLine setMrpLineQty(MrpLine mrpLine, Product product, StockLocation stockLocation) {

    StockRules stockRules =
        stockRulesService.getStockRules(
            product,
            stockLocation,
            StockRulesRepository.TYPE_FUTURE,
            StockRulesRepository.USE_CASE_USED_FOR_MRP);

    if (stockRules != null) {
      mrpLine.setMinQty(stockRules.getMinQty());
      mrpLine.setIdealQty(stockRules.getIdealQty());
      mrpLine.setReOrderQty(stockRules.getReOrderQty());
    }
    return mrpLine;
  }

  protected void createMrpLineOrigins(MrpLine mrpLine, Model model) {

    if (model != null) {

      mrpLine.addMrpLineOriginListItem(this.createMrpLineOrigin(model));
      mrpLine.setRelatedToSelectName(this.computeRelatedName(model));
    }
  }

  public MrpLineOrigin createMrpLineOrigin(Model model) {

    Class<?> klass = EntityHelper.getEntityClass(model);

    MrpLineOrigin mrpLineOrigin = new MrpLineOrigin();
    mrpLineOrigin.setRelatedToSelect(klass.getCanonicalName());
    mrpLineOrigin.setRelatedToSelectId(model.getId());

    return mrpLineOrigin;
  }

  public MrpLineOrigin copyMrpLineOrigin(MrpLineOrigin mrpLineOrigin) {

    MrpLineOrigin copyMrpLineOrigin = new MrpLineOrigin();
    copyMrpLineOrigin.setRelatedToSelect(mrpLineOrigin.getRelatedToSelect());
    copyMrpLineOrigin.setRelatedToSelectId(mrpLineOrigin.getRelatedToSelectId());

    return copyMrpLineOrigin;
  }

  @Override
  public void updateProposalToProcess(List<Integer> mrpLineIds, boolean proposalToProcess) {
    MrpLine mrpLine;
    for (Integer mrpId : mrpLineIds) {
      mrpLine = mrpLineRepo.find(Long.valueOf(mrpId));

      updateProposalToProcess(mrpLine, proposalToProcess);
    }
  }

  protected String computeRelatedName(Model model) {

    if (model instanceof SaleOrderLine) {

      return ((SaleOrderLine) model).getSaleOrder().getSaleOrderSeq();

    } else if (model instanceof PurchaseOrderLine) {

      return ((PurchaseOrderLine) model).getPurchaseOrder().getPurchaseOrderSeq();

    } else if (model instanceof MrpForecast) {

      MrpForecast mrpForecast = (MrpForecast) model;
      return mrpForecast.getId() + "-" + mrpForecast.getForecastDate();
    }
    return null;
  }

  protected void updatePartner(MrpLine mrpLine, Model model) {

    if (model != null) {
      mrpLine.setPartner(this.getPartner(model));
    }
  }

  protected Partner getPartner(Model model) {

    if (model instanceof SaleOrderLine) {

      return ((SaleOrderLine) model).getSaleOrder().getClientPartner();

    } else if (model instanceof PurchaseOrderLine) {

      return ((PurchaseOrderLine) model).getPurchaseOrder().getSupplierPartner();

    } else if (model instanceof MrpForecast) {

      return ((MrpForecast) model).getPartner();
    }
    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateProposalToProcess(MrpLine mrpLine, boolean proposalToProcess) {
    if (!mrpLine.getProposalGenerated()
        && (mrpLine.getMrpLineType().getElementSelect()
                == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL
            || mrpLine.getMrpLineType().getElementSelect()
                == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL)) {
      mrpLine.setProposalToProcess(proposalToProcess);
      mrpLineRepo.save(mrpLine);
    }
  }
}
