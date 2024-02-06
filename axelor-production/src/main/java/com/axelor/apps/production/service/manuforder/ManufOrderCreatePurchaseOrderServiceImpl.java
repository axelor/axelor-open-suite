package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationOutsourceService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ManufOrderCreatePurchaseOrderServiceImpl
    implements ManufOrderCreatePurchaseOrderService {

  protected PurchaseOrderService purchaseOrderService;
  protected StockConfigProductionService stockConfigProductionService;
  protected ManufOrderRepository manufOrderRepository;
  protected ManufacturingOperationOutsourceService manufacturingOperationOutsourceService;
  protected ManufOrderOutsourceService manufOrderOutsourceService;
  protected AppBaseService appBaseService;
  protected AppProductionService appProductionService;

  @Inject
  public ManufOrderCreatePurchaseOrderServiceImpl(
      PurchaseOrderService purchaseOrderService,
      StockConfigProductionService stockConfigProductionService,
      ManufOrderRepository manufOrderRepository,
      ManufacturingOperationOutsourceService manufacturingOperationOutsourceService,
      ManufOrderOutsourceService manufOrderOutsourceService,
      AppBaseService appBaseService,
      AppProductionService appProductionService) {
    this.purchaseOrderService = purchaseOrderService;
    this.stockConfigProductionService = stockConfigProductionService;
    this.manufOrderRepository = manufOrderRepository;
    this.manufacturingOperationOutsourceService = manufacturingOperationOutsourceService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createPurchaseOrders(ManufOrder manufOrder) throws AxelorException {

    List<Partner> outsourcePartners = getOutsourcePartnersForGenerationPO(manufOrder);

    List<PurchaseOrder> generatedPurchaseOrders = new ArrayList<>();
    for (Partner outsourcePartner : outsourcePartners) {
      PurchaseOrder purchaseOrder =
          purchaseOrderService.createPurchaseOrder(
              null,
              manufOrder.getCompany(),
              null,
              null,
              null,
              manufOrder.getManufOrderSeq(),
              null,
              null,
              null,
              outsourcePartner,
              null);

      purchaseOrder.setOutsourcingOrder(true);
      purchaseOrder.setFiscalPosition(outsourcePartner.getFiscalPosition());
      StockConfig stockConfig =
          stockConfigProductionService.getStockConfig(manufOrder.getCompany());
      if (manufOrder.getCompany() != null && manufOrder.getCompany().getStockConfig() != null) {
        purchaseOrder.setStockLocation(
            stockConfigProductionService.getReceiptDefaultStockLocation(stockConfig));
      }
      purchaseOrder.setFromStockLocation(
          stockConfigProductionService.getVirtualOutsourcingStockLocation(stockConfig));

      this.setPurchaseOrderSupplierDetails(purchaseOrder);

      generatedPurchaseOrders.add(purchaseOrder);
      manufOrder.addPurchaseOrderSetItem(purchaseOrder);
    }

    for (PurchaseOrder purchaseOrder : generatedPurchaseOrders) {
      List<ManufacturingOperation> manufacturingOperationGeneratePurchaseOrderList =
          manufOrder.getManufacturingOperationList().stream()
              .filter(
                  oo ->
                      oo.getProdProcessLine().getUseLineInGeneratedPurchaseOrder()
                          && purchaseOrder
                              .getSupplierPartner()
                              .equals(
                                  manufacturingOperationOutsourceService
                                      .getOutsourcePartner(oo)
                                      .orElse(null)))
              .collect(Collectors.toList());

      for (ManufacturingOperation manufacturingOperation :
          manufacturingOperationGeneratePurchaseOrderList) {
        this.createPurchaseOrderLineProduction(manufacturingOperation, purchaseOrder);
      }
      purchaseOrderService.computePurchaseOrder(purchaseOrder);
    }

    manufOrderRepository.save(manufOrder);
  }

  protected PurchaseOrder setPurchaseOrderSupplierDetails(PurchaseOrder purchaseOrder)
      throws AxelorException {
    Partner supplierPartner = purchaseOrder.getSupplierPartner();

    if (supplierPartner != null) {
      purchaseOrder.setCurrency(supplierPartner.getCurrency());
      purchaseOrder.setShipmentMode(supplierPartner.getShipmentMode());
      purchaseOrder.setFreightCarrierMode(supplierPartner.getFreightCarrierMode());
      purchaseOrder.setNotes(supplierPartner.getPurchaseOrderComments());

      if (supplierPartner.getPaymentCondition() != null) {
        purchaseOrder.setPaymentCondition(supplierPartner.getPaymentCondition());
      } else {
        purchaseOrder.setPaymentCondition(
            purchaseOrder.getCompany().getAccountConfig().getDefPaymentCondition());
      }

      if (supplierPartner.getOutPaymentMode() != null) {
        purchaseOrder.setPaymentMode(supplierPartner.getOutPaymentMode());
      } else {
        purchaseOrder.setPaymentMode(
            purchaseOrder.getCompany().getAccountConfig().getOutPaymentMode());
      }

      if (supplierPartner.getContactPartnerSet().size() == 1) {
        purchaseOrder.setContactPartner(supplierPartner.getContactPartnerSet().iterator().next());
      }

      purchaseOrder.setCompanyBankDetails(
          Beans.get(BankDetailsService.class)
              .getDefaultCompanyBankDetails(
                  purchaseOrder.getCompany(),
                  purchaseOrder.getPaymentMode(),
                  purchaseOrder.getSupplierPartner(),
                  null));

      purchaseOrder.setPriceList(
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(
                  purchaseOrder.getSupplierPartner(), PriceListRepository.TYPE_PURCHASE));

      if (Beans.get(AppSupplychainService.class).isApp("supplychain")
          && Beans.get(AppSupplychainService.class).getAppSupplychain().getIntercoFromPurchase()
          && !purchaseOrder.getCreatedByInterco()
          && (Beans.get(CompanyRepository.class)
                  .all()
                  .filter("self.partner = ?", supplierPartner)
                  .fetchOne()
              != null)) {
        purchaseOrder.setInterco(true);
      }
    }

    return purchaseOrder;
  }

  protected void createPurchaseOrderLineProduction(
      ManufacturingOperation manufacturingOperation, PurchaseOrder purchaseOrder)
      throws AxelorException {

    UnitConversionService unitConversionService = Beans.get(UnitConversionService.class);
    PurchaseOrderLineService purchaseOrderLineService = Beans.get(PurchaseOrderLineService.class);
    PurchaseOrderLine purchaseOrderLine;
    BigDecimal quantity;
    Unit startUnit = appBaseService.getAppBase().getUnitHours();

    if (ObjectUtils.isEmpty(startUnit)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(ProductionExceptionMessage.PURCHASE_ORDER_NO_HOURS_UNIT));
    }

    Product product = getHrProduct(manufacturingOperation);
    if (product != null) {
      Unit purchaseUnit = product.getPurchasesUnit();
      Unit stockUnit = product.getUnit();

      Unit endUnit = (purchaseUnit != null) ? purchaseUnit : stockUnit;

      if (endUnit == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(ProductionExceptionMessage.PURCHASE_ORDER_NO_END_UNIT));
      }
      final int COMPUTATION_SCALE = 20;
      quantity =
          unitConversionService.convert(
              startUnit,
              endUnit,
              new BigDecimal(manufacturingOperation.getWorkCenter().getHrDurationPerCycle())
                  .divide(BigDecimal.valueOf(3600), COMPUTATION_SCALE, RoundingMode.HALF_UP),
              appBaseService.getNbDecimalDigitForQty(),
              product);
      // have to force the scale as the conversion service will not round if the start unit and the
      // end unit are equals.
      quantity = quantity.setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);

      purchaseOrderLine =
          purchaseOrderLineService.createPurchaseOrderLine(
              purchaseOrder, product, null, null, quantity, purchaseUnit);

      purchaseOrder.getPurchaseOrderLineList().add(purchaseOrderLine);
    }
  }

  protected Product getHrProduct(ManufacturingOperation manufacturingOperation) {
    boolean isCostPerProcessLine = appProductionService.getIsCostPerProcessLine();

    if (isCostPerProcessLine) {
      ProdProcessLine prodProcessLine = manufacturingOperation.getProdProcessLine();
      if (prodProcessLine != null && prodProcessLine.getHrProduct() != null) {
        return prodProcessLine.getHrProduct();
      }
    } else {
      WorkCenter workCenter = manufacturingOperation.getWorkCenter();
      if (workCenter != null && workCenter.getHrProduct() != null) {
        return workCenter.getHrProduct();
      }
    }

    return null;
  }

  protected List<Partner> getOutsourcePartnersForGenerationPO(ManufOrder manufOrder) {

    if (manufOrder.getOutsourcing()
        && manufOrder.getProdProcess().getGeneratePurchaseOrderOnMoPlanning()
        && manufOrderOutsourceService.getOutsourcePartner(manufOrder).isPresent()) {
      return List.of(manufOrderOutsourceService.getOutsourcePartner(manufOrder).get());
    } else {
      return manufOrder.getManufacturingOperationList().stream()
          .filter(
              oo ->
                  oo.getOutsourcing()
                      && oo.getProdProcessLine().getGeneratePurchaseOrderOnMoPlanning())
          .map(oo -> manufacturingOperationOutsourceService.getOutsourcePartner(oo))
          .map(optPartner -> optPartner.orElse(null))
          .filter(Objects::nonNull)
          .distinct()
          .collect(Collectors.toList());
    }
  }
}
