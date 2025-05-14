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
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.enums.ProductionStatusSelect;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineDummySupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineDummyProductionServiceImpl
    extends SaleOrderLineDummySupplychainServiceImpl {

  protected static final String DUMMY_PRODUCTION_STATUS = "$productionStatus";

  @Inject
  public SaleOrderLineDummyProductionServiceImpl(
      AppBaseService appBaseService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      ProductMultipleQtyService productMultipleQtyService,
      AppSaleService appSaleService,
      StockMoveLineRepository stockMoveLineRepository,
      AppSupplychainService appSupplychainService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain) {
    super(
        appBaseService,
        saleOrderLineDiscountService,
        productMultipleQtyService,
        appSaleService,
        stockMoveLineRepository,
        appSupplychainService,
        saleOrderLineServiceSupplyChain);
  }

  @Override
  public Map<String, Object> getOnLoadDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> dummyFields = super.getOnLoadDummies(saleOrderLine, saleOrder);
    initProductionInformation(saleOrderLine)
        .ifPresent(s -> dummyFields.put(DUMMY_PRODUCTION_STATUS, s.getValue()));
    return dummyFields;
  }

  protected Optional<ProductionStatusSelect> initProductionInformation(
      SaleOrderLine saleOrderLine) {

    List<ManufOrder> manufOrderList = saleOrderLine.getManufOrderList();
    if (CollectionUtils.isEmpty(manufOrderList)) {
      return Optional.empty();
    }
    List<Integer> statusSelectList =
        manufOrderList.stream().map(ManufOrder::getStatusSelect).collect(Collectors.toList());
    if (statusSelectList.stream()
        .allMatch(status -> status == ManufOrderRepository.STATUS_CANCELED)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_CANCELED);
    }

    if (statusSelectList.stream().allMatch(status -> status == ManufOrderRepository.STATUS_DRAFT)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_DRAFT);
    }

    if (statusSelectList.stream()
        .allMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_STANDBY);
    }

    if (statusSelectList.stream()
        .allMatch(
            status ->
                status == ManufOrderRepository.STATUS_FINISHED
                    || status == ManufOrderRepository.STATUS_CANCELED
                    || status == ManufOrderRepository.STATUS_MERGED)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_FINISHED);
    }

    if (statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_IN_PROGRESS)
        || statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_FINISHED)
        || statusSelectList.stream()
            .anyMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_IN_PROGRESS);
    }

    if (statusSelectList.stream()
        .anyMatch(status -> status == ManufOrderRepository.STATUS_PLANNED)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_PLANNED);
    }

    if (statusSelectList.stream()
        .anyMatch(status -> status == ManufOrderRepository.STATUS_STANDBY)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_STANDBY);
    }

    if (statusSelectList.stream().anyMatch(status -> status == ManufOrderRepository.STATUS_DRAFT)) {
      return Optional.of(ProductionStatusSelect.PRODUCTION_STATUS_DRAFT);
    }

    return Optional.empty();
  }
}
