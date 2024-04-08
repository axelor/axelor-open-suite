package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ManufOrderMergingServiceImpl implements ManufOrderMergingService {

  protected final ManufOrderRepository manufOrderRepo;
  protected final AppProductionService appProductionService;
  protected final AppBaseService appBaseService;
  protected final ManufOrderPlanService manufOrderPlanService;
  protected final ManufOrderOperationOrderService manufOrderOperationOrderService;

  @Inject
  public ManufOrderMergingServiceImpl(
      ManufOrderRepository manufOrderRepo,
      AppProductionService appProductionService,
      AppBaseService appBaseService,
      ManufOrderPlanService manufOrderPlanService,
      ManufOrderOperationOrderService manufOrderOperationOrderService) {
    this.manufOrderRepo = manufOrderRepo;
    this.appProductionService = appProductionService;
    this.appBaseService = appBaseService;
    this.manufOrderPlanService = manufOrderPlanService;
    this.manufOrderOperationOrderService = manufOrderOperationOrderService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void merge(List<Long> ids) throws AxelorException {
    if (!canMerge(ids)) {
      throw new AxelorException(
          ManufOrder.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.MANUF_ORDER_NO_GENERATION));
    }
    List<ManufOrder> manufOrderList =
        manufOrderRepo.all().filter("self.id in (" + Joiner.on(",").join(ids) + ")").fetch();

    /* Init all the necessary values to create the new Manuf Order */
    Product product = manufOrderList.get(0).getProduct();
    StockLocation stockLocation = manufOrderList.get(0).getWorkshopStockLocation();
    Company company = manufOrderList.get(0).getCompany();
    BillOfMaterial billOfMaterial =
        manufOrderList.stream()
            .filter(x -> x.getBillOfMaterial().getVersionNumber() == 1)
            .findFirst()
            .get()
            .getBillOfMaterial();
    int priority = manufOrderList.stream().mapToInt(ManufOrder::getPrioritySelect).max().orElse(2);
    Unit unit = billOfMaterial.getUnit();
    BigDecimal qty = BigDecimal.ZERO;
    String note = "";

    ManufOrder mergedManufOrder = new ManufOrder();

    mergedManufOrder.setMoCommentFromSaleOrder("");
    mergedManufOrder.setMoCommentFromSaleOrderLine("");

    for (ManufOrder manufOrder : manufOrderList) {
      manufOrder.setStatusSelect(ManufOrderRepository.STATUS_MERGED);

      manufOrder.setManufOrderMergeResult(mergedManufOrder);
      for (ProductionOrder productionOrder : manufOrder.getProductionOrderSet()) {
        mergedManufOrder.addProductionOrderSetItem(productionOrder);
      }
      for (SaleOrder saleOrder : manufOrder.getSaleOrderSet()) {
        mergedManufOrder.addSaleOrderSetItem(saleOrder);
      }
      /*
       * If unit are the same, then add the qty If not, convert the unit and get the
       * converted qty
       */
      if (manufOrder.getUnit() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(ProductionExceptionMessage.MANUF_ORDER_MERGE_MISSING_UNIT));
      }
      if (manufOrder.getUnit().equals(unit)) {
        qty = qty.add(manufOrder.getQty());
      } else {
        BigDecimal qtyConverted =
            Beans.get(UnitConversionService.class)
                .convert(
                    manufOrder.getUnit(),
                    unit,
                    manufOrder.getQty(),
                    appBaseService.getNbDecimalDigitForQty(),
                    null);
        qty = qty.add(qtyConverted);
      }
      if (manufOrder.getNote() != null && !manufOrder.getNote().equals("")) {
        note += manufOrder.getManufOrderSeq() + " : " + manufOrder.getNote() + "\n";
      }

      if (!Strings.isNullOrEmpty(manufOrder.getMoCommentFromSaleOrder())) {
        mergedManufOrder.setMoCommentFromSaleOrder(
            mergedManufOrder
                .getMoCommentFromSaleOrder()
                .concat(System.lineSeparator())
                .concat(manufOrder.getMoCommentFromSaleOrder()));
      }

      if (!Strings.isNullOrEmpty(manufOrder.getMoCommentFromSaleOrderLine())) {
        mergedManufOrder.setMoCommentFromSaleOrderLine(
            mergedManufOrder
                .getMoCommentFromSaleOrderLine()
                .concat(System.lineSeparator())
                .concat(manufOrder.getMoCommentFromSaleOrderLine()));
      }
    }

    Optional<LocalDateTime> minDate =
        manufOrderList.stream()
            .filter(mo -> mo.getPlannedStartDateT() != null)
            .map(ManufOrder::getPlannedStartDateT)
            .min(LocalDateTime::compareTo);

    minDate.ifPresent(mergedManufOrder::setPlannedStartDateT);

    /* Update the created manuf order */
    mergedManufOrder.setStatusSelect(ManufOrderRepository.STATUS_DRAFT);
    mergedManufOrder.setProduct(product);
    mergedManufOrder.setUnit(unit);
    mergedManufOrder.setWorkshopStockLocation(stockLocation);
    mergedManufOrder.setQty(qty);
    mergedManufOrder.setBillOfMaterial(billOfMaterial);
    mergedManufOrder.setCompany(company);
    mergedManufOrder.setPrioritySelect(priority);
    mergedManufOrder.setProdProcess(billOfMaterial.getProdProcess());
    mergedManufOrder.setNote(note);

    /*
     * Check the config to see if you directly plan the created manuf order or just
     * prefill the operations
     */
    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getIsManufOrderPlannedAfterMerge()) {
      manufOrderPlanService.plan(mergedManufOrder);
    } else {
      manufOrderOperationOrderService.preFillOperations(mergedManufOrder);
    }

    manufOrderRepo.save(mergedManufOrder);
  }

  @Override
  public boolean canMerge(List<Long> ids) {
    List<ManufOrder> manufOrderList =
        manufOrderRepo.all().filter("self.id in (" + Joiner.on(",").join(ids) + ")").fetch();

    // I check if all the status of the manuf order in the list are Draft or
    // Planned. If not i can return false
    boolean allStatusDraftOrPlanned =
        manufOrderList.stream()
            .allMatch(
                x ->
                    x.getStatusSelect().equals(ManufOrderRepository.STATUS_DRAFT)
                        || x.getStatusSelect().equals(ManufOrderRepository.STATUS_PLANNED));
    if (!allStatusDraftOrPlanned) {
      return false;
    }
    // I check if all the products are the same. If not i return false
    Product product = manufOrderList.get(0).getProduct();
    boolean allSameProducts = manufOrderList.stream().allMatch(x -> x.getProduct().equals(product));
    if (!allSameProducts) {
      return false;
    }

    // Workshop management must be enabled to do the checking
    if (appProductionService.getAppProduction().getManageWorkshop()) {
      // Check if one of the workShopStockLocation is null
      boolean oneWorkShopIsNull =
          manufOrderList.stream().anyMatch(x -> x.getWorkshopStockLocation() == null);
      if (oneWorkShopIsNull) {
        return false;
      }

      // I check if all the stockLocation are the same. If not i return false
      StockLocation stockLocation = manufOrderList.get(0).getWorkshopStockLocation();
      boolean allSameLocation =
          manufOrderList.stream()
              .allMatch(
                  x ->
                      x.getWorkshopStockLocation() != null
                          && x.getWorkshopStockLocation().equals(stockLocation));
      if (!allSameLocation) {
        return false;
      }
    }

    // Check if one of the billOfMaterial is null
    boolean oneBillOfMaterialIsNull =
        manufOrderList.stream().anyMatch(x -> x.getBillOfMaterial() == null);
    if (oneBillOfMaterialIsNull) {
      return false;
    }

    // Check if one of the billOfMaterial has his version equal to 1
    boolean oneBillOfMaterialWithFirstVersion =
        manufOrderList.stream().anyMatch(x -> x.getBillOfMaterial().getVersionNumber() == 1);
    if (!oneBillOfMaterialWithFirstVersion) {
      return false;
    }

    // I check if all the billOfMaterial are the same. If not i will check
    // if all version are compatible, and if not i can return false
    BillOfMaterial billOfMaterial =
        manufOrderList.stream()
            .filter(x -> x.getBillOfMaterial().getVersionNumber() == 1)
            .findFirst()
            .get()
            .getBillOfMaterial();
    boolean allSameOrCompatibleBillOfMaterial =
        manufOrderList.stream()
            .allMatch(
                x ->
                    x.getBillOfMaterial().equals(billOfMaterial)
                        || billOfMaterial.equals(
                            x.getBillOfMaterial().getOriginalBillOfMaterial()));
    if (!allSameOrCompatibleBillOfMaterial) {
      return false;
    }

    return true;
  }
}
