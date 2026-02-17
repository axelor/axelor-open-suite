/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.repo.BillOfMaterialLineRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.repo.AppSaleRepository;
import jakarta.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderLineBomServiceImpl implements SaleOrderLineBomService {

  protected final SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService;
  protected final AppSaleService appSaleService;
  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final BillOfMaterialLineRepository billOfMaterialLineRepository;
  protected final BillOfMaterialLineService billOfMaterialLineService;
  protected final SaleOrderLineDetailsBomService saleOrderLineDetailsBomService;
  protected final SaleOrderLineDetailsProdProcessService saleOrderLineDetailsProdProcessService;
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public SaleOrderLineBomServiceImpl(
      SaleOrderLineBomLineMappingService saleOrderLineBomLineMappingService,
      AppSaleService appSaleService,
      BillOfMaterialRepository billOfMaterialRepository,
      BillOfMaterialLineRepository billOfMaterialLineRepository,
      BillOfMaterialLineService billOfMaterialLineService,
      SaleOrderLineDetailsBomService saleOrderLineDetailsBomService,
      SaleOrderLineDetailsProdProcessService saleOrderLineDetailsProdProcessService) {
    this.saleOrderLineBomLineMappingService = saleOrderLineBomLineMappingService;
    this.appSaleService = appSaleService;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.billOfMaterialLineRepository = billOfMaterialLineRepository;
    this.billOfMaterialLineService = billOfMaterialLineService;
    this.saleOrderLineDetailsBomService = saleOrderLineDetailsBomService;
    this.saleOrderLineDetailsProdProcessService = saleOrderLineDetailsProdProcessService;
  }

  @Override
  public void checkProdProcessRequired(BillOfMaterial billOfMaterial, Product product)
      throws AxelorException {
    if (billOfMaterial == null || billOfMaterial.getProdProcess() != null) {
      return;
    }

    String productName =
        Optional.ofNullable(product)
            .map(Product::getFullName)
            .orElseGet(() -> Optional.ofNullable(product).map(Product::getName).orElse(""));

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(ProductionExceptionMessage.SALE_ORDER_LINE_PROD_PROCESS_REQUIRED),
        productName);
  }

  @Override
  public List<SaleOrderLine> createSaleOrderLinesFromBom(
      BillOfMaterial billOfMaterial, SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(billOfMaterial);

    var saleOrderLinesList = new ArrayList<SaleOrderLine>();

    if (appSaleService.getAppSale().getListDisplayTypeSelect()
        != AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI) {
      return saleOrderLinesList;
    }

    for (BillOfMaterialLine billOfMaterialLine : billOfMaterial.getBillOfMaterialLineList()) {
      var saleOrderLine =
          saleOrderLineBomLineMappingService.mapToSaleOrderLine(billOfMaterialLine, saleOrder);
      if (saleOrderLine != null) {
        BillOfMaterial lineBom = saleOrderLine.getBillOfMaterial();
        if (saleOrderLine.getIsToProduce()) {
          checkProdProcessRequired(lineBom, saleOrderLine.getProduct());
          saleOrderLineDetailsBomService
              .createSaleOrderLineDetailsFromBom(lineBom, saleOrder, saleOrderLine)
              .stream()
              .filter(Objects::nonNull)
              .forEach(saleOrderLine::addSaleOrderLineDetailsListItem);
          saleOrderLineDetailsProdProcessService
              .createSaleOrderLineDetailsFromProdProcess(
                  lineBom.getProdProcess(), saleOrder, saleOrderLine)
              .stream()
              .filter(Objects::nonNull)
              .filter(line -> line.getTypeSelect() == SaleOrderLineDetailsRepository.TYPE_OPERATION)
              .forEach(saleOrderLine::addSaleOrderLineDetailsListItem);
        }
        saleOrderLinesList.add(saleOrderLine);
      }
    }

    return saleOrderLinesList.stream()
        .sorted(Comparator.comparingInt(SaleOrderLine::getSequence))
        .collect(Collectors.toList());
  }
}
