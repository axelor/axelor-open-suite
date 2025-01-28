package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.db.mapper.Mapper;
import com.axelor.rpc.Context;
import com.axelor.rpc.JsonContext;
import com.axelor.utils.helpers.json.JsonHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfiguratorSaleOrderDuplicateServiceImpl
    implements ConfiguratorSaleOrderDuplicateService {

  protected final ConfiguratorRepository configuratorRepository;
  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final SaleOrderComputeService saleOrderComputeService;
  protected final SaleOrderRepository saleOrderRepository;
  protected final ConfiguratorService configuratorService;
  protected final ConfiguratorSaleOrderLineService configuratorSaleOrderLineService;
  protected final ConfiguratorCheckService configuratorCheckService;

  @Inject
  public ConfiguratorSaleOrderDuplicateServiceImpl(
      ConfiguratorRepository configuratorRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderRepository saleOrderRepository,
      ConfiguratorService configuratorService,
      ConfiguratorSaleOrderLineService configuratorSaleOrderLineService,
      ConfiguratorCheckService configuratorCheckService) {
    this.configuratorRepository = configuratorRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderRepository = saleOrderRepository;
    this.configuratorService = configuratorService;
    this.configuratorSaleOrderLineService = configuratorSaleOrderLineService;
    this.configuratorCheckService = configuratorCheckService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void duplicateSaleOrderLine(SaleOrderLine saleOrderLine)
      throws AxelorException, JsonProcessingException {

    var saleOrder = saleOrderLine.getSaleOrder();

    duplicateLineWithoutCompute(saleOrderLine);
    computeSaleOrder(saleOrder);
  }

  @Transactional(rollbackOn = Exception.class)
  protected SaleOrderLine duplicateLineWithoutCompute(SaleOrderLine saleOrderLine)
      throws AxelorException, JsonProcessingException {
    Objects.requireNonNull(saleOrderLine);
    var configurator = saleOrderLine.getConfigurator();
    var context = new Context(Configurator.class);

    var saleOrder = saleOrderLine.getSaleOrder();
    var duplicatedConfigurator = configuratorRepository.copy(configurator, false);
    var jsonAttributes =
        new JsonContext(
            context,
            Mapper.of(Configurator.class).getProperty("attributes"),
            duplicatedConfigurator.getAttributes());
    var jsonIndicators =
        new JsonContext(
            context,
            Mapper.of(Configurator.class).getProperty("indicators"),
            duplicatedConfigurator.getIndicators());

    return duplicateLine(
        saleOrderLine, duplicatedConfigurator, jsonAttributes, jsonIndicators, saleOrder);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void simpleDuplicate(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    var copiedSaleOrderLine = saleOrderLineRepository.copy(saleOrderLine, false);
    saleOrder.addSaleOrderLineListItem(copiedSaleOrderLine);
    computeSaleOrder(saleOrder);
  }

  protected SaleOrderLine duplicateLine(
      SaleOrderLine saleOrderLine,
      Configurator duplicatedConfigurator,
      JsonContext jsonAttributes,
      JsonContext jsonIndicators,
      SaleOrder saleOrder)
      throws AxelorException, JsonProcessingException {
    configuratorService.updateIndicators(
        duplicatedConfigurator, jsonAttributes, jsonIndicators, saleOrder.getId());
    duplicatedConfigurator.setIndicators(JsonHelper.toJson(jsonIndicators));
    SaleOrderLine duplicatedSaleOrderLine = null;
    if (duplicatedConfigurator.getConfiguratorCreator().getGenerateProduct()) {

      configuratorService.generateProduct(
          duplicatedConfigurator, jsonAttributes, jsonIndicators, saleOrder.getId());
      duplicatedSaleOrderLine =
          configuratorSaleOrderLineService.generateSaleOrderLine(
              duplicatedConfigurator, duplicatedConfigurator.getProduct(), saleOrderLine);

    } else {

      configuratorCheckService.checkLinkedSaleOrderLine(duplicatedConfigurator);
      duplicatedSaleOrderLine =
          configuratorService.generateSaleOrderLine(
              duplicatedConfigurator, saleOrder, jsonAttributes, jsonIndicators);
    }

    configuratorRepository.save(duplicatedConfigurator);
    return duplicatedSaleOrderLine;
  }

  @Override
  public SaleOrder duplicateSaleOrder(SaleOrder saleOrder) throws AxelorException {

      //Duplicate sale order line list first of sale order.
      //Remove sale order line duplication for sale order and save current saleOrder

    //Error occuring at this point will be throw
    //Since we can't have a general transaction because of how error management work in line duplication
    //We will remove duplicate sale order manually
    SaleOrder duplicatedSaleOrder = null;
    try {
      var duplicatedSaleOrderLines = duplicateLinesOnly(saleOrder);

      //Finding saleOrder in case of error in duplicateLines;
         duplicatedSaleOrder = duplicateSaleOrder(saleOrderRepository.find(saleOrder.getId()), duplicatedSaleOrderLines);
         return duplicatedSaleOrder;
      } catch (Exception e) {
        removeSaleOrder(duplicatedSaleOrder);
        throw e;
      }

  }


  @Transactional(rollbackOn = Exception.class)
  protected void removeSaleOrder(SaleOrder saleOrder) {
    if (saleOrder != null) {
      saleOrderRepository.remove(saleOrder);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected SaleOrder duplicateSaleOrder(SaleOrder saleOrder, List<SaleOrderLine> duplicatedSaleOrderLines) throws AxelorException {
    //Duplicate sale order and add all new lines.
    //Compute sale order.
    //Save sale order and return duplicateSaleOrder
    var duplicatedSaleOrder = saleOrderRepository.copy(saleOrder, true);
    clearList(duplicatedSaleOrder);
    for (var duplicatedSaleOrderLine : duplicatedSaleOrderLines) {
      duplicatedSaleOrderLine.setSaleOrder(duplicatedSaleOrder);
      duplicatedSaleOrder.getSaleOrderLineList().add(saleOrderLineRepository.save(duplicatedSaleOrderLine));
    }

    duplicatedSaleOrder = computeSaleOrder(duplicatedSaleOrder);
    return duplicatedSaleOrder;
  }

  protected void clearList(SaleOrder duplicatedSaleOrder) {
    var saleOrderLineListToRemove = new ArrayList<>(duplicatedSaleOrder.getSaleOrderLineList());
    for (SaleOrderLine saleOrderLine : saleOrderLineListToRemove) {
      duplicatedSaleOrder.removeSaleOrderLineListItem(saleOrderLine);
    }
  }

  protected List<SaleOrderLine> duplicateLinesOnly(SaleOrder saleOrder) throws AxelorException {
    var finalSaleOrderLineList= duplicateSaleOrderLineList(saleOrder, true);
    saleOrder = saleOrderRepository.find(saleOrder.getId());
    for (SaleOrderLine saleOrderLine : finalSaleOrderLineList) {
      saleOrder.removeSaleOrderLineListItem(saleOrderLine);
    }

    computeSaleOrder(saleOrder);

    return finalSaleOrderLineList;
  }

  @Transactional(rollbackOn = Exception.class)
  protected SaleOrder computeSaleOrder(SaleOrder saleOrder) throws AxelorException {
    saleOrderComputeService.computeSaleOrder(saleOrder);
    return saleOrderRepository.save(saleOrder);
  }


  @Override
  public List<SaleOrderLine> duplicateSaleOrderLineList(SaleOrder saleOrder, boolean deep) {

    if (saleOrder.getSaleOrderLineList() == null || saleOrder.getSaleOrderLineList().isEmpty()) {
      return new ArrayList<>();
    }

    //Not using directly sale order list because it will change at every duplicate
    var saleOrderLineList = new ArrayList<>(saleOrder.getSaleOrderLineList());

    List<SaleOrderLine> finalSaleOrderLineList = new ArrayList<>();

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      if (saleOrderLine.getConfigurator() != null) {
        addToFinalList(saleOrderLineRepository.find(saleOrderLine.getId()), finalSaleOrderLineList, deep);

      } else {
        finalSaleOrderLineList.add(saleOrderLineRepository.copy(saleOrderLineRepository.find(saleOrderLine.getId()), deep));
      }
    }

    return finalSaleOrderLineList;
  }

  protected List<SaleOrderLine> refreshList(List<SaleOrderLine> finalSaleOrderLineList) {
    List<SaleOrderLine> refreshedList = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : finalSaleOrderLineList) {
      if (saleOrderLine.getId() != null) {
        refreshedList.add(saleOrderLineRepository.find(saleOrderLine.getId()));
      } else {
        refreshedList.add(saleOrderLine);
      }
    }
    return refreshedList;
  }


  protected void addToFinalList(SaleOrderLine saleOrderLine, List<SaleOrderLine> finalSaleOrderLineList, boolean deep) {
    try {
      finalSaleOrderLineList.add(duplicateLineWithoutCompute(saleOrderLine));
    } catch (Exception e) {
      //Need to find the saleOrderLine because of error.
      finalSaleOrderLineList.add(saleOrderLineRepository.copy(saleOrderLineRepository.find(saleOrderLine.getId()), deep));
      TraceBackService.trace(e);
    }
  }
}
