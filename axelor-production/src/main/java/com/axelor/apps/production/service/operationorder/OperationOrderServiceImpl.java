/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationOrderServiceImpl implements OperationOrderService {

  @Inject private MetaFiles metaFiles;

  @Inject protected BarcodeGeneratorService barcodeGeneratorService;

  @Inject protected AppProductionService appProductionService;

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder createOperationOrder(ManufOrder manufOrder, ProdProcessLine prodProcessLine)
      throws AxelorException {

    OperationOrder operationOrder =
        this.createOperationOrder(
            manufOrder,
            prodProcessLine.getPriority(),
            prodProcessLine.getWorkCenter(),
            prodProcessLine.getWorkCenter(),
            prodProcessLine);

    return Beans.get(OperationOrderRepository.class).save(operationOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder createOperationOrder(
      ManufOrder manufOrder,
      int priority,
      WorkCenter workCenter,
      WorkCenter machineWorkCenter,
      ProdProcessLine prodProcessLine)
      throws AxelorException {

    logger.debug(
        "Création d'une opération {} pour l'OF {}", priority, manufOrder.getManufOrderSeq());

    String operationName = prodProcessLine.getName();

    OperationOrder operationOrder =
        new OperationOrder(
            priority,
            this.computeName(manufOrder, priority, operationName),
            operationName,
            manufOrder,
            workCenter,
            machineWorkCenter,
            OperationOrderRepository.STATUS_DRAFT,
            prodProcessLine);

    this._createHumanResourceList(operationOrder, machineWorkCenter);

    return Beans.get(OperationOrderRepository.class).save(operationOrder);
  }

  protected void _createHumanResourceList(OperationOrder operationOrder, WorkCenter workCenter) {

    if (workCenter != null && workCenter.getProdHumanResourceList() != null) {

      for (ProdHumanResource prodHumanResource : workCenter.getProdHumanResourceList()) {

        operationOrder.addProdHumanResourceListItem(this.copyProdHumanResource(prodHumanResource));
      }
    }
  }

  protected ProdHumanResource copyProdHumanResource(ProdHumanResource prodHumanResource) {

    return new ProdHumanResource(prodHumanResource.getProduct(), prodHumanResource.getDuration());
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
  public void createToConsumeProdProductList(OperationOrder operationOrder) {

    BigDecimal manufOrderQty = operationOrder.getManufOrder().getQty();
    BigDecimal bomQty = operationOrder.getManufOrder().getBillOfMaterial().getQty();
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    if (prodProcessLine.getToConsumeProdProductList() != null) {

      for (ProdProduct prodProduct : prodProcessLine.getToConsumeProdProductList()) {

        BigDecimal qty =
            Beans.get(ManufOrderService.class)
                .computeToConsumeProdProductLineQuantity(
                    bomQty, manufOrderQty, prodProduct.getQty());

        operationOrder.addToConsumeProdProductListItem(
            new ProdProduct(prodProduct.getProduct(), qty, prodProduct.getUnit()));
      }
    }
  }

  @Override
  public OperationOrder updateDiffProdProductList(OperationOrder operationOrder)
      throws AxelorException {
    List<ProdProduct> toConsumeList = operationOrder.getToConsumeProdProductList();
    List<StockMoveLine> consumedList = operationOrder.getConsumedStockMoveLineList();
    if (toConsumeList == null || consumedList == null) {
      return operationOrder;
    }
    List<ProdProduct> diffConsumeList =
        createDiffProdProductList(operationOrder, toConsumeList, consumedList);

    operationOrder.clearDiffConsumeProdProductList();
    diffConsumeList.forEach(operationOrder::addDiffConsumeProdProductListItem);
    return operationOrder;
  }

  public List<Map<String, Object>> chargeByMachineHours(
      LocalDateTime fromDateTime, LocalDateTime toDateTime) throws AxelorException {
    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    LocalDateTime itDateTime =
        LocalDateTime.parse(fromDateTime.toString(), DateTimeFormatter.ISO_DATE_TIME);
    OperationOrderRepository operationOrderRepo = Beans.get(OperationOrderRepository.class);
    if (Duration.between(fromDateTime, toDateTime).toDays() > 20) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CHARGE_MACHINE_DAYS));
    }

    List<OperationOrder> operationOrderListTemp =
        operationOrderRepo
            .all()
            .filter(
                "self.plannedStartDateT <= ?2 AND self.plannedEndDateT >= ?1",
                fromDateTime,
                toDateTime)
            .fetch();
    Set<String> machineNameList = new HashSet<String>();
    for (OperationOrder operationOrder : operationOrderListTemp) {
      if (operationOrder.getWorkCenter() != null
          && operationOrder.getWorkCenter().getMachine() != null) {
        if (!machineNameList.contains(operationOrder.getWorkCenter().getMachine().getName())) {
          machineNameList.add(operationOrder.getWorkCenter().getMachine().getName());
        }
      }
    }
    while (!itDateTime.isAfter(toDateTime)) {
      List<OperationOrder> operationOrderList =
          operationOrderRepo
              .all()
              .filter(
                  "self.plannedStartDateT <= ?2 AND self.plannedEndDateT >= ?1",
                  itDateTime,
                  itDateTime.plusHours(1))
              .fetch();
      Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
      for (OperationOrder operationOrder : operationOrderList) {
        if (operationOrder.getWorkCenter() != null
            && operationOrder.getWorkCenter().getMachine() != null) {
          String machine = operationOrder.getWorkCenter().getMachine().getName();
          long numberOfMinutes = 0;
          if (operationOrder.getPlannedStartDateT().isBefore(itDateTime)) {
            numberOfMinutes =
                Duration.between(itDateTime, operationOrder.getPlannedEndDateT()).toMinutes();
          } else if (operationOrder.getPlannedEndDateT().isAfter(itDateTime.plusHours(1))) {
            numberOfMinutes =
                Duration.between(operationOrder.getPlannedStartDateT(), itDateTime.plusHours(1))
                    .toMinutes();
          } else {
            numberOfMinutes =
                Duration.between(
                        operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())
                    .toMinutes();
          }
          if (numberOfMinutes > 60) {
            numberOfMinutes = 60;
          }
          BigDecimal percentage =
              new BigDecimal(numberOfMinutes)
                  .multiply(new BigDecimal(100))
                  .divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
          if (map.containsKey(machine)) {
            map.put(machine, map.get(machine).add(percentage));
          } else {
            map.put(machine, percentage);
          }
        }
      }
      Set<String> keyList = map.keySet();
      for (String key : machineNameList) {
        if (keyList.contains(key)) {
          Map<String, Object> dataMap = new HashMap<String, Object>();
          dataMap.put("dateTime", (Object) itDateTime.format(DATE_TIME_FORMAT));
          dataMap.put("charge", (Object) map.get(key));
          dataMap.put("machine", (Object) key);
          dataList.add(dataMap);
        } else {
          Map<String, Object> dataMap = new HashMap<String, Object>();
          dataMap.put("dateTime", (Object) itDateTime.format(DATE_TIME_FORMAT));
          dataMap.put("charge", (Object) BigDecimal.ZERO);
          dataMap.put("machine", (Object) key);
          dataList.add(dataMap);
        }
      }

      itDateTime = itDateTime.plusHours(1);
    }
    return dataList;
  }

  public List<Map<String, Object>> chargeByMachineDays(
      LocalDateTime fromDateTime, LocalDateTime toDateTime) throws AxelorException {

    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    fromDateTime = fromDateTime.withHour(0).withMinute(0);
    toDateTime = toDateTime.withHour(23).withMinute(59);
    LocalDateTime itDateTime =
        LocalDateTime.parse(fromDateTime.toString(), DateTimeFormatter.ISO_DATE_TIME);
    if (Duration.between(fromDateTime, toDateTime).toDays() > 500) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CHARGE_MACHINE_DAYS));
    }

    List<OperationOrder> operationOrderListTemp =
        Beans.get(OperationOrderRepository.class)
            .all()
            .filter(
                "self.plannedStartDateT <= ?2 AND self.plannedEndDateT >= ?1",
                fromDateTime,
                toDateTime)
            .fetch();
    Set<String> machineNameList = new HashSet<String>();
    for (OperationOrder operationOrder : operationOrderListTemp) {
      if (operationOrder.getWorkCenter() != null
          && operationOrder.getWorkCenter().getMachine() != null) {
        if (!machineNameList.contains(operationOrder.getWorkCenter().getMachine().getName())) {
          machineNameList.add(operationOrder.getWorkCenter().getMachine().getName());
        }
      }
    }
    while (!itDateTime.isAfter(toDateTime)) {
      List<OperationOrder> operationOrderList =
          Beans.get(OperationOrderRepository.class)
              .all()
              .filter(
                  "self.plannedStartDateT <= ?2 AND self.plannedEndDateT >= ?1",
                  itDateTime,
                  itDateTime.plusHours(1))
              .fetch();
      Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
      WeeklyPlanningService weeklyPlanningService = Beans.get(WeeklyPlanningService.class);
      for (OperationOrder operationOrder : operationOrderList) {
        if (operationOrder.getWorkCenter() != null
            && operationOrder.getWorkCenter().getMachine() != null) {
          String machine = operationOrder.getWorkCenter().getMachine().getName();
          long numberOfMinutes = 0;
          if (operationOrder.getPlannedStartDateT().isBefore(itDateTime)) {
            numberOfMinutes =
                Duration.between(itDateTime, operationOrder.getPlannedEndDateT()).toMinutes();
          } else if (operationOrder.getPlannedEndDateT().isAfter(itDateTime.plusHours(1))) {
            numberOfMinutes =
                Duration.between(operationOrder.getPlannedStartDateT(), itDateTime.plusHours(1))
                    .toMinutes();
          } else {
            numberOfMinutes =
                Duration.between(
                        operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())
                    .toMinutes();
          }
          if (numberOfMinutes > 60) {
            numberOfMinutes = 60;
          }
          long numberOfMinutesPerDay = 0;
          if (operationOrder.getWorkCenter().getMachine().getWeeklyPlanning() != null) {
            DayPlanning dayPlanning =
                weeklyPlanningService.findDayPlanning(
                    operationOrder.getWorkCenter().getMachine().getWeeklyPlanning(),
                    LocalDateTime.parse(itDateTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
                        .toLocalDate());
            if (dayPlanning != null) {
              numberOfMinutesPerDay =
                  Duration.between(dayPlanning.getMorningFrom(), dayPlanning.getMorningTo())
                      .toMinutes();
              numberOfMinutesPerDay +=
                  Duration.between(dayPlanning.getAfternoonFrom(), dayPlanning.getAfternoonTo())
                      .toMinutes();
            } else {
              numberOfMinutesPerDay = 0;
            }
          } else {
            numberOfMinutesPerDay = 60 * 8;
          }
          if (numberOfMinutesPerDay != 0) {
            BigDecimal percentage =
                new BigDecimal(numberOfMinutes)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(numberOfMinutesPerDay), 2, RoundingMode.HALF_UP);
            if (map.containsKey(machine)) {
              map.put(machine, map.get(machine).add(percentage));
            } else {
              map.put(machine, percentage);
            }
          }
        }
      }
      Set<String> keyList = map.keySet();
      for (String key : machineNameList) {
        if (keyList.contains(key)) {
          int found = 0;
          for (Map<String, Object> mapIt : dataList) {
            if (mapIt.get("dateTime").equals((Object) itDateTime.format(DATE_FORMAT))
                && mapIt.get("machine").equals((Object) key)) {
              mapIt.put("charge", new BigDecimal(mapIt.get("charge").toString()).add(map.get(key)));
              found = 1;
              break;
            }
          }
          if (found == 0) {
            Map<String, Object> dataMap = new HashMap<String, Object>();

            dataMap.put("dateTime", (Object) itDateTime.format(DATE_FORMAT));
            dataMap.put("charge", (Object) map.get(key));
            dataMap.put("machine", (Object) key);
            dataList.add(dataMap);
          }
        }
      }

      itDateTime = itDateTime.plusHours(1);
    }
    return dataList;
  }

  @Override
  public List<ProdProduct> createDiffProdProductList(
      OperationOrder operationOrder,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> stockMoveLineList)
      throws AxelorException {
    List<ProdProduct> diffConsumeList =
        Beans.get(ManufOrderService.class)
            .createDiffProdProductList(prodProductList, stockMoveLineList);
    diffConsumeList.forEach(
        prodProduct -> prodProduct.setDiffConsumeOperationOrder(operationOrder));
    return diffConsumeList;
  }

  @Override
  public void checkConsumedStockMoveLineList(
      OperationOrder operationOrder, OperationOrder oldOperationOrder) throws AxelorException {
    Beans.get(ManufOrderService.class)
        .checkRealizedStockMoveLineList(
            operationOrder.getConsumedStockMoveLineList(),
            oldOperationOrder.getConsumedStockMoveLineList());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateConsumedStockMoveFromOperationOrder(OperationOrder operationOrder)
      throws AxelorException {
    this.updateDiffProdProductList(operationOrder);
    List<StockMoveLine> consumedStockMoveLineList = operationOrder.getConsumedStockMoveLineList();
    if (consumedStockMoveLineList == null) {
      return;
    }
    Optional<StockMove> stockMoveOpt =
        operationOrder
            .getInStockMoveList()
            .stream()
            .filter(stockMove -> stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED)
            .findFirst();
    StockMove stockMove;
    if (stockMoveOpt.isPresent()) {
      stockMove = stockMoveOpt.get();
    } else {
      stockMove =
          Beans.get(ManufOrderStockMoveService.class)
              ._createToConsumeStockMove(
                  operationOrder.getManufOrder(), operationOrder.getManufOrder().getCompany());
      operationOrder.addInStockMoveListItem(stockMove);
      Beans.get(StockMoveService.class).plan(stockMove);
    }

    Beans.get(ManufOrderService.class)
        .updateStockMoveFromManufOrder(consumedStockMoveLineList, stockMove);
  }

  @Override
  public void createBarcode(OperationOrder operationOrder) {
    try {
      String stringId = operationOrder.getId().toString();
      boolean addPadding = true;
      InputStream inStream =
          barcodeGeneratorService.createBarCode(
              stringId, appProductionService.getAppProduction().getBarcodeTypeConfig(), addPadding);
      if (inStream != null) {
        MetaFile barcodeFile =
            metaFiles.upload(
                inStream, String.format("OppOrderBarcode%d.png", operationOrder.getId()));
        operationOrder.setBarCode(barcodeFile);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AxelorException e) {
      throw new ValidationException(e.getMessage());
    }
  }
}
