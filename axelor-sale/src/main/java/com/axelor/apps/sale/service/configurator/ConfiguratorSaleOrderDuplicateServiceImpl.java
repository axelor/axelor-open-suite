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
package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
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
    if (saleOrderLine.getConfigurator() == null) {
      // Copy
      var copy = saleOrderLineRepository.save(saleOrderLineRepository.copy(saleOrderLine, false));
      saleOrder.addSaleOrderLineListItem(copy);
    } else {
      duplicateLineWithoutCompute(saleOrderLine);
    }

    computeSaleOrder(saleOrder);
  }

  @Transactional(rollbackOn = Exception.class)
  protected SaleOrderLine duplicateLineWithoutCompute(SaleOrderLine saleOrderLine)
      throws AxelorException, JsonProcessingException {
    Objects.requireNonNull(saleOrderLine);
    var configurator = saleOrderLine.getConfigurator();

    if (configuratorCheckService.isConfiguratorVersionDifferent(configurator)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.CONFIGURATOR_VERSION_IS_DIFFERENT));
    }

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

    // Duplicate and save first.
    SaleOrder duplicatedSaleOrder = copyAndSave(saleOrder);
    try {
      duplicatedSaleOrder = duplicateLinesOnly(duplicatedSaleOrder);

      return duplicatedSaleOrder;
    } catch (Exception e) {
      removeSaleOrder(duplicatedSaleOrder);
      throw e;
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected SaleOrder copyAndSave(SaleOrder saleOrder) {
    SaleOrder duplicatedSaleOrder = saleOrderRepository.copy(saleOrder, true);
    duplicatedSaleOrder = saleOrderRepository.save(duplicatedSaleOrder);
    return duplicatedSaleOrder;
  }

  @Transactional(rollbackOn = Exception.class)
  protected void removeSaleOrder(SaleOrder saleOrder) {
    if (saleOrder.getId() != null) {
      saleOrderRepository.remove(saleOrder);
    }
  }

  protected SaleOrder duplicateLinesOnly(SaleOrder saleOrder) throws AxelorException {
    duplicateSaleOrderLineList(saleOrder);
    saleOrder = saleOrderRepository.find(saleOrder.getId());

    return computeSaleOrder(saleOrder);
  }

  @Transactional(rollbackOn = Exception.class)
  protected SaleOrder computeSaleOrder(SaleOrder saleOrder) throws AxelorException {
    saleOrderComputeService.computeSaleOrder(saleOrder);
    return saleOrderRepository.save(saleOrder);
  }

  protected void duplicateSaleOrderLineList(SaleOrder saleOrder) {

    if (saleOrder.getSaleOrderLineList() == null || saleOrder.getSaleOrderLineList().isEmpty()) {
      return;
    }

    // Duplicate only lines with configurator
    var toRemoveLines =
        saleOrder.getSaleOrderLineList().stream()
            .filter(sol -> sol.getConfigurator() != null)
            .collect(Collectors.toCollection(ArrayList::new));
    List<SaleOrderLine> toRemoveFromRemoveList = new ArrayList<>();

    for (SaleOrderLine saleOrderLine : toRemoveLines) {
      if (saleOrderLine.getConfigurator() != null) {
        duplicateToSaleOrder(saleOrderLine, toRemoveFromRemoveList);
      }
    }
    for (SaleOrderLine saleOrderLine : toRemoveFromRemoveList) {
      toRemoveLines.remove(saleOrderLine);
    }
    manageLines(saleOrder, toRemoveLines);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void manageLines(SaleOrder saleOrder, List<SaleOrderLine> toRemoveLines) {
    saleOrder = saleOrderRepository.find(saleOrder.getId());
    for (SaleOrderLine saleOrderLine : toRemoveLines) {
      saleOrder.removeSaleOrderLineListItem(saleOrderLine);
      saleOrderLineRepository.remove(saleOrderLine);
    }
    saleOrderRepository.save(saleOrder);
  }

  protected void duplicateToSaleOrder(
      SaleOrderLine saleOrderLine, List<SaleOrderLine> toRemoveFromRemove) {
    try {
      duplicateLineWithoutCompute(saleOrderLineRepository.find(saleOrderLine.getId()));
    } catch (Exception e) {
      // We need to not remove it
      updateConfiguratorSolOnError(saleOrderLineRepository.find(saleOrderLine.getId()));
      toRemoveFromRemove.add(saleOrderLine);
      TraceBackService.trace(e);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void updateConfiguratorSolOnError(SaleOrderLine saleOrderLine) {
    saleOrderLine.setConfigurator(null);
    saleOrderLineRepository.save(saleOrderLine);
  }
}
