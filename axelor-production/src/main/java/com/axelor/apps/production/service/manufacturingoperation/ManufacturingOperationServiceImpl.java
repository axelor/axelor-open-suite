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
package com.axelor.apps.production.service.manufacturingoperation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.MachineTool;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ManufacturingOperationDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManufacturingOperationServiceImpl implements ManufacturingOperationService {
  protected BarcodeGeneratorService barcodeGeneratorService;

  protected AppProductionService appProductionService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ProdProcessLineService prodProcessLineService;
  protected ManufacturingOperationRepository manufacturingOperationRepository;
  protected ManufacturingOperationOutsourceService manufacturingOperationOutsourceService;

  @Inject
  public ManufacturingOperationServiceImpl(
      BarcodeGeneratorService barcodeGeneratorService,
      AppProductionService appProductionService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ProdProcessLineService prodProcessLineService,
      ManufacturingOperationRepository manufacturingOperationRepository,
      ManufacturingOperationOutsourceService manufacturingOperationOutsourceService) {
    this.barcodeGeneratorService = barcodeGeneratorService;
    this.appProductionService = appProductionService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.prodProcessLineService = prodProcessLineService;
    this.manufacturingOperationRepository = manufacturingOperationRepository;
    this.manufacturingOperationOutsourceService = manufacturingOperationOutsourceService;
  }

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Transactional(rollbackOn = {Exception.class})
  public ManufacturingOperation createManufacturingOperation(
      ManufOrder manufOrder, ProdProcessLine prodProcessLine) throws AxelorException {

    if (prodProcessLine.getWorkCenter() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_LINE_MISSING_WORK_CENTER),
          prodProcessLine.getProdProcess() != null
              ? prodProcessLine.getProdProcess().getCode()
              : "null",
          prodProcessLine.getName());
    }
    ManufacturingOperation manufacturingOperation =
        this.createManufacturingOperation(
            manufOrder,
            prodProcessLine.getPriority(),
            prodProcessLine.getWorkCenter(),
            prodProcessLine.getWorkCenter().getMachine(),
            prodProcessLine.getMachineTool(),
            prodProcessLine);

    return Beans.get(ManufacturingOperationRepository.class).save(manufacturingOperation);
  }

  @Transactional
  public ManufacturingOperation createManufacturingOperation(
      ManufOrder manufOrder,
      int priority,
      WorkCenter workCenter,
      Machine machine,
      MachineTool machineTool,
      ProdProcessLine prodProcessLine)
      throws AxelorException {

    logger.debug(
        "Creation of an operation {} for the manufacturing order {}",
        priority,
        manufOrder.getManufOrderSeq());

    String operationName = prodProcessLine.getName();

    ManufacturingOperation manufacturingOperation =
        new ManufacturingOperation(
            priority,
            this.computeName(manufOrder, priority, operationName),
            operationName,
            manufOrder,
            workCenter,
            machine,
            ManufacturingOperationRepository.STATUS_DRAFT,
            prodProcessLine,
            machineTool);

    manufacturingOperation.setOutsourcing(
        manufOrder.getOutsourcing() || prodProcessLine.getOutsourcing());
    manufacturingOperation.setOutsourcingPartner(
        manufacturingOperationOutsourceService
            .getOutsourcePartner(manufacturingOperation)
            .orElse(null));

    return Beans.get(ManufacturingOperationRepository.class).save(manufacturingOperation);
  }

  public String computeName(ManufOrder manufOrder, int priority, String operationName) {

    String name = "";
    if (manufOrder != null) {

      if (manufOrder.getManufOrderSeq() != null) {
        name += manufOrder.getManufOrderSeq();
      } else {
        name += manufOrder.getId();
      }
    }

    name += "-" + priority + "-" + operationName;

    return name;
  }

  @Override
  public void createToConsumeProdProductList(ManufacturingOperation manufacturingOperation)
      throws AxelorException {

    BigDecimal manufOrderQty = manufacturingOperation.getManufOrder().getQty();
    BigDecimal bomQty = manufacturingOperation.getManufOrder().getBillOfMaterial().getQty();
    ProdProcessLine prodProcessLine = manufacturingOperation.getProdProcessLine();

    if (prodProcessLine == null) {

      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(ProductionExceptionMessage.PRODUCTION_PROCESS_IS_EMPTY));
    }
    if (prodProcessLine.getToConsumeProdProductList() != null) {
      for (ProdProduct prodProduct : prodProcessLine.getToConsumeProdProductList()) {

        BigDecimal qty =
            Beans.get(ManufOrderService.class)
                .computeToConsumeProdProductLineQuantity(
                    bomQty, manufOrderQty, prodProduct.getQty());

        manufacturingOperation.addToConsumeProdProductListItem(
            new ProdProduct(prodProduct.getProduct(), qty, prodProduct.getUnit()));
      }
    }
  }

  @Override
  public ManufacturingOperation updateDiffProdProductList(
      ManufacturingOperation manufacturingOperation) throws AxelorException {
    List<ProdProduct> toConsumeList = manufacturingOperation.getToConsumeProdProductList();
    List<StockMoveLine> consumedList = manufacturingOperation.getConsumedStockMoveLineList();
    if (toConsumeList == null || consumedList == null) {
      return manufacturingOperation;
    }
    List<ProdProduct> diffConsumeList =
        createDiffProdProductList(manufacturingOperation, toConsumeList, consumedList);

    manufacturingOperation.clearDiffConsumeProdProductList();
    diffConsumeList.forEach(manufacturingOperation::addDiffConsumeProdProductListItem);
    return manufacturingOperation;
  }

  @Override
  public List<ProdProduct> createDiffProdProductList(
      ManufacturingOperation manufacturingOperation,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> stockMoveLineList)
      throws AxelorException {
    List<ProdProduct> diffConsumeList =
        Beans.get(ManufOrderService.class)
            .createDiffProdProductList(prodProductList, stockMoveLineList);
    diffConsumeList.forEach(
        prodProduct -> prodProduct.setDiffConsumeManufacturingOperation(manufacturingOperation));
    return diffConsumeList;
  }

  @Override
  public void checkConsumedStockMoveLineList(
      ManufacturingOperation manufacturingOperation,
      ManufacturingOperation oldManufacturingOperation)
      throws AxelorException {
    Beans.get(ManufOrderService.class)
        .checkRealizedStockMoveLineList(
            manufacturingOperation.getConsumedStockMoveLineList(),
            oldManufacturingOperation.getConsumedStockMoveLineList());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateConsumedStockMoveFromManufacturingOperation(
      ManufacturingOperation manufacturingOperation) throws AxelorException {
    this.updateDiffProdProductList(manufacturingOperation);
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    Company company = manufOrder.getCompany();
    List<StockMoveLine> consumedStockMoveLineList =
        manufacturingOperation.getConsumedStockMoveLineList();
    StockLocation fromStockLocation =
        manufOrderStockMoveService.getFromStockLocationForConsumedStockMove(manufOrder, company);
    StockLocation virtualStockLocation =
        manufOrderStockMoveService.getVirtualStockLocationForConsumedStockMove(manufOrder, company);
    if (consumedStockMoveLineList == null) {
      return;
    }
    Optional<StockMove> stockMoveOpt =
        manufacturingOperation.getInStockMoveList().stream()
            .filter(stockMove -> stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED)
            .findFirst();
    StockMove stockMove;
    if (stockMoveOpt.isPresent()) {
      stockMove = stockMoveOpt.get();
    } else {
      stockMove =
          manufOrderStockMoveService
              .createAndPlanToConsumeStockMove(manufOrder)
              .map(
                  sm -> {
                    manufacturingOperation.addInStockMoveListItem(sm);
                    return sm;
                  })
              .orElse(null);
    }

    Beans.get(ManufOrderService.class)
        .updateStockMoveFromManufOrder(consumedStockMoveLineList, stockMove);
  }

  @Override
  public void createBarcode(ManufacturingOperation manufacturingOperation) {
    if (manufacturingOperation != null && manufacturingOperation.getId() != null) {
      String serialNbr = manufacturingOperation.getId().toString();
      BarcodeTypeConfig barcodeTypeConfig =
          appProductionService.getAppProduction().getBarcodeTypeConfig();
      boolean addPadding = true;
      MetaFile barcodeFile =
          barcodeGeneratorService.createBarCode(
              manufacturingOperation.getId(),
              "OppOrderBarcode%d.png",
              serialNbr,
              barcodeTypeConfig,
              addPadding);
      if (barcodeFile != null) {
        manufacturingOperation.setBarCode(barcodeFile);
      }
    }
  }

  @Override
  public long computeEntireCycleDuration(
      ManufacturingOperation manufacturingOperation, BigDecimal qty) throws AxelorException {
    ProdProcessLine prodProcessLine = manufacturingOperation.getProdProcessLine();

    return prodProcessLineService.computeEntireCycleDuration(
        manufacturingOperation, prodProcessLine, qty);
  }

  /**
   * Computes the duration of all the {@link ManufacturingOperationDuration} of {@code
   * manufacturingOperation}
   *
   * @param manufacturingOperation An operation order
   * @return Real duration of {@code manufacturingOperation}
   */
  @Override
  public Duration computeRealDuration(ManufacturingOperation manufacturingOperation) {
    Duration totalDuration = Duration.ZERO;

    List<ManufacturingOperationDuration> manufacturingOperationDurations =
        manufacturingOperation.getManufacturingOperationDurationList();
    if (manufacturingOperationDurations != null) {
      for (ManufacturingOperationDuration manufacturingOperationDuration :
          manufacturingOperationDurations) {
        if (manufacturingOperationDuration.getStartingDateTime() != null
            && manufacturingOperationDuration.getStoppingDateTime() != null) {
          totalDuration =
              totalDuration.plus(
                  Duration.between(
                      manufacturingOperationDuration.getStartingDateTime(),
                      manufacturingOperationDuration.getStoppingDateTime()));
        }
      }
    }

    return totalDuration;
  }

  @Override
  public LocalDateTime getNextOperationDate(ManufacturingOperation manufacturingOperation) {
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    ManufacturingOperation nextManufacturingOperation =
        manufacturingOperationRepository
            .all()
            .filter(
                "self.manufOrder = :manufOrder AND self.priority >= :priority AND self.statusSelect BETWEEN :statusPlanned AND :statusStandby AND self.id != :manufacturingOperationId")
            .bind("manufOrder", manufOrder)
            .bind("priority", manufacturingOperation.getPriority())
            .bind("statusPlanned", ManufacturingOperationRepository.STATUS_PLANNED)
            .bind("statusStandby", ManufacturingOperationRepository.STATUS_STANDBY)
            .bind("manufacturingOperationId", manufacturingOperation.getId())
            .order("priority")
            .order("plannedStartDateT")
            .fetchOne();

    LocalDateTime manufOrderPlannedEndDateT = manufOrder.getPlannedEndDateT();
    if (nextManufacturingOperation == null) {
      return manufOrderPlannedEndDateT;
    }

    LocalDateTime plannedStartDateT = nextManufacturingOperation.getPlannedStartDateT();

    if (Objects.equals(
        nextManufacturingOperation.getPriority(), manufacturingOperation.getPriority())) {
      LocalDateTime plannedEndDateT = nextManufacturingOperation.getPlannedEndDateT();
      if (plannedEndDateT != null && plannedEndDateT.isBefore(manufOrderPlannedEndDateT)) {
        boolean isOnSameMachine =
            Objects.equals(
                nextManufacturingOperation.getMachine(), manufacturingOperation.getMachine());
        return isOnSameMachine ? plannedStartDateT : plannedEndDateT;
      }

    } else if (plannedStartDateT != null && plannedStartDateT.isBefore(manufOrderPlannedEndDateT)) {
      return plannedStartDateT;
    }

    return manufOrderPlannedEndDateT;
  }

  @Override
  public LocalDateTime getLastOperationDate(ManufacturingOperation manufacturingOperation) {
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    ManufacturingOperation lastManufacturingOperation =
        manufacturingOperationRepository
            .all()
            .filter(
                "self.manufOrder = :manufOrder AND ((self.priority = :priority AND self.machine = :machine) OR self.priority < :priority) AND self.statusSelect BETWEEN :statusPlanned AND :statusStandby AND self.id != :manufacturingOperationId")
            .bind("manufOrder", manufOrder)
            .bind("priority", manufacturingOperation.getPriority())
            .bind("statusPlanned", ManufacturingOperationRepository.STATUS_PLANNED)
            .bind("statusStandby", ManufacturingOperationRepository.STATUS_STANDBY)
            .bind("machine", manufacturingOperation.getMachine())
            .bind("manufacturingOperationId", manufacturingOperation.getId())
            .order("-priority")
            .order("-plannedEndDateT")
            .fetchOne();

    LocalDateTime manufOrderPlannedStartDateT = manufOrder.getPlannedStartDateT();
    if (lastManufacturingOperation == null) {
      return manufOrderPlannedStartDateT;
    }

    LocalDateTime plannedEndDateT = lastManufacturingOperation.getPlannedEndDateT();

    if (Objects.equals(
        lastManufacturingOperation.getPriority(), manufacturingOperation.getPriority())) {
      LocalDateTime plannedStartDateT = lastManufacturingOperation.getPlannedStartDateT();
      if (plannedStartDateT != null && plannedStartDateT.isAfter(manufOrderPlannedStartDateT)) {
        boolean isOnSameMachine =
            Objects.equals(
                lastManufacturingOperation.getMachine(), manufacturingOperation.getMachine());
        return isOnSameMachine ? plannedEndDateT : plannedStartDateT;
      }

    } else if (plannedEndDateT != null && plannedEndDateT.isAfter(manufOrderPlannedStartDateT)) {
      return plannedEndDateT;
    }

    return manufOrderPlannedStartDateT;
  }

  @Override
  public long getDuration(ManufacturingOperation manufacturingOperation) throws AxelorException {
    if (manufacturingOperation.getWorkCenter() != null) {
      return computeEntireCycleDuration(
          manufacturingOperation, manufacturingOperation.getManufOrder().getQty());
    }
    return 0;
  }

  /**
   * Sort manufacturingOperations list by priority and id.
   *
   * @param manufacturingOperations
   * @return
   */
  @Override
  public List<ManufacturingOperation> getSortedManufacturingOperationList(
      List<ManufacturingOperation> manufacturingOperations) {

    Comparator<ManufacturingOperation> byPriority =
        Comparator.comparing(
            ManufacturingOperation::getPriority, Comparator.nullsFirst(Comparator.naturalOrder()));
    Comparator<ManufacturingOperation> byId =
        Comparator.comparing(
            ManufacturingOperation::getId, Comparator.nullsFirst(Comparator.naturalOrder()));

    return manufacturingOperations.stream()
        .sorted(byPriority.thenComparing(byId))
        .collect(Collectors.toList());
  }

  /**
   * Reverse sort manufacturingOperations list by priority and id.
   *
   * @param manufacturingOperations
   * @return
   */
  @Override
  public List<ManufacturingOperation> getReversedSortedManufacturingOperationList(
      List<ManufacturingOperation> manufacturingOperations) {

    return Lists.reverse(getSortedManufacturingOperationList(manufacturingOperations));
  }
}
