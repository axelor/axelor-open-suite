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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.ProdProcessOutsourceService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ManufOrderOutsourceServiceImpl implements ManufOrderOutsourceService {

  protected ProdProcessOutsourceService prodProcessOutsourceService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;

  protected StockMoveService stockMoveService;
  protected AppBaseService appBaseService;
  protected ManufOrderRepository manufOrderRepository;

  @Inject
  public ManufOrderOutsourceServiceImpl(
      ProdProcessOutsourceService prodProcessOutsourceService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      StockMoveService stockMoveService,
      AppBaseService appBaseService,
      ManufOrderRepository manufOrderRepository) {
    this.prodProcessOutsourceService = prodProcessOutsourceService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.stockMoveService = stockMoveService;
    this.appBaseService = appBaseService;
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  public Optional<Partner> getOutsourcePartner(ManufOrder manufOrder) {
    Objects.requireNonNull(manufOrder);
    Objects.requireNonNull(manufOrder.getProdProcess());

    if (manufOrder.getOutsourcing() && manufOrder.getOutsourcingPartner() != null) {
      return Optional.of(manufOrder.getOutsourcingPartner());
    } else if (manufOrder.getOutsourcing() && manufOrder.getOutsourcingPartner() == null) {
      return prodProcessOutsourceService.getOutsourcePartner(manufOrder.getProdProcess());
    } else {
      return Optional.empty();
    }
  }

  @Override
  public boolean isOutsource(ManufOrder manufOrder) {
    return manufOrder.getOutsourcing();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validateOutsourceDeclaration(
      ManufOrder manufOrder, Partner outsourcePartner, List<ProdProduct> prodProductList)
      throws AxelorException {
    Company company = manufOrder.getCompany();

    // Get stock locations dest and source
    // From production to Outsource stock location
    StockLocation fromStockLocation =
        manufOrderStockMoveService._getVirtualProductionStockLocation(manufOrder, company);
    StockLocation virtualStockLocation =
        manufOrderStockMoveService._getVirtualOutsourcingStockLocation(manufOrder, company);

    if (company != null && prodProductList != null) {
      StockMove stockMove =
          createStockMove(
              manufOrder,
              outsourcePartner,
              fromStockLocation,
              virtualStockLocation,
              prodProductList);
      stockMoveService.plan(stockMove);
      manufOrder.addOutsourcingStockMoveListItem(stockMove);
      manufOrderRepository.save(manufOrder);
    }
  }

  protected StockMove createStockMove(
      ManufOrder manufOrder,
      Partner outsourcePartner,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      List<ProdProduct> prodProductList)
      throws AxelorException {

    StockMove stockMove =
        stockMoveService.createStockMove(
            fromStockLocation.getAddress(),
            toStockLocation.getAddress(),
            manufOrder.getCompany(),
            outsourcePartner,
            fromStockLocation,
            toStockLocation,
            null,
            appBaseService.getTodayDate(manufOrder.getCompany()),
            null,
            null,
            null,
            null,
            null,
            null,
            StockMoveRepository.TYPE_OUTGOING);

    stockMove.setInvoicedPartner(manufOrder.getClientPartner());
    stockMove.setFromAddressStr(
        fromStockLocation.getAddress() != null
            ? fromStockLocation.getAddress().getFullName()
            : null);
    stockMove.setToAddressStr(
        toStockLocation.getAddress() != null ? toStockLocation.getAddress().getFullName() : null);

    // Generation stockMoveLine
    for (ProdProduct prodProduct : prodProductList) {
      manufOrderStockMoveService._createStockMoveLine(
          prodProduct,
          stockMove,
          StockMoveLineService.TYPE_OUT_PRODUCTIONS,
          fromStockLocation,
          toStockLocation);
    }

    return stockMove;
  }
}
