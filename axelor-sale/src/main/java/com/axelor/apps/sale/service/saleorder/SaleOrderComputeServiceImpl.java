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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineCreateTaxLineService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderComputeServiceImpl implements SaleOrderComputeService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected SaleOrderLineCreateTaxLineService saleOrderLineCreateTaxLineService;
  protected SaleOrderLineComputeService saleOrderLineComputeService;
  protected SaleOrderLinePackService saleOrderLinePackService;
  protected SubSaleOrderLineComputeService subSaleOrderLineComputeService;
  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderComputeServiceImpl(
      SaleOrderLineCreateTaxLineService saleOrderLineCreateTaxLineService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLinePackService saleOrderLinePackService,
      SubSaleOrderLineComputeService subSaleOrderLineComputeService,
      AppSaleService appSaleService) {
    this.saleOrderLineCreateTaxLineService = saleOrderLineCreateTaxLineService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.saleOrderLinePackService = saleOrderLinePackService;
    this.subSaleOrderLineComputeService = subSaleOrderLineComputeService;
    this.appSaleService = appSaleService;
  }

  @Override
  public SaleOrder _computeSaleOrderLineList(SaleOrder saleOrder) throws AxelorException {

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();

    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return saleOrder;
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      subSaleOrderLineComputeService.computeSumSubLineList(saleOrderLine, saleOrder);
    }

    return saleOrder;
  }

  @Override
  public SaleOrder computeSaleOrder(SaleOrder saleOrder) throws AxelorException {

    this.initSaleOrderLineTaxList(saleOrder);

    this._computeSaleOrderLineList(saleOrder);

    this._populateSaleOrder(saleOrder);

    this._computeSaleOrder(saleOrder);

    return saleOrder;
  }

  /**
   * Peupler un devis.
   *
   * <p>Cette fonction permet de déterminer les tva d'un devis.
   *
   * @param saleOrder
   * @throws AxelorException
   */
  @Override
  public void _populateSaleOrder(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder.getSaleOrderLineList() == null) {
      saleOrder.setSaleOrderLineList(new ArrayList<>());
    }

    if (saleOrder.getSaleOrderLineTaxList() == null) {
      saleOrder.setSaleOrderLineTaxList(new ArrayList<>());
    }

    logger.debug(
        "Populate a sale order => sale order lines : {}",
        new Object[] {saleOrder.getSaleOrderLineList().size()});

    // create Tva lines
    if (saleOrder.getClientPartner() != null) {
      saleOrder
          .getSaleOrderLineTaxList()
          .addAll(
              saleOrderLineCreateTaxLineService.createsSaleOrderLineTax(
                  saleOrder, saleOrder.getSaleOrderLineList()));
    }
  }

  /**
   * Compute the sale order total amounts
   *
   * @param saleOrder
   * @throws AxelorException
   */
  @Override
  public void _computeSaleOrder(SaleOrder saleOrder) throws AxelorException {

    saleOrder.setExTaxTotal(BigDecimal.ZERO);
    saleOrder.setCompanyExTaxTotal(BigDecimal.ZERO);
    saleOrder.setTaxTotal(BigDecimal.ZERO);
    saleOrder.setInTaxTotal(BigDecimal.ZERO);

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {

      // skip title lines in computing total amounts
      if (saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL) {
        continue;
      }
      saleOrder.setExTaxTotal(saleOrder.getExTaxTotal().add(saleOrderLine.getExTaxTotal()));

      // In the company accounting currency
      saleOrder.setCompanyExTaxTotal(
          saleOrder.getCompanyExTaxTotal().add(saleOrderLine.getCompanyExTaxTotal()));
    }

    for (SaleOrderLineTax saleOrderLineVat : saleOrder.getSaleOrderLineTaxList()) {

      // In the sale order currency
      saleOrder.setTaxTotal(saleOrder.getTaxTotal().add(saleOrderLineVat.getTaxTotal()));
    }

    saleOrder.setInTaxTotal(saleOrder.getExTaxTotal().add(saleOrder.getTaxTotal()));
    saleOrder.setAdvanceTotal(computeTotalAdvancePayment(saleOrder));
    logger.debug(
        "Sale order amounts: W.T. = {}, Tax = {}, A.T.I. = {}",
        saleOrder.getExTaxTotal(),
        saleOrder.getTaxTotal(),
        saleOrder.getInTaxTotal());
  }

  protected BigDecimal computeTotalAdvancePayment(SaleOrder saleOrder) {
    List<AdvancePayment> advancePaymentList = saleOrder.getAdvancePaymentList();
    BigDecimal total = BigDecimal.ZERO;
    if (advancePaymentList == null || advancePaymentList.isEmpty()) {
      return total;
    }
    for (AdvancePayment advancePayment : advancePaymentList) {
      total = total.add(advancePayment.getAmount());
    }
    return total;
  }

  /**
   * Permet de réinitialiser la liste des lignes de TVA
   *
   * @param saleOrder Un devis
   */
  @Override
  public void initSaleOrderLineTaxList(SaleOrder saleOrder) {

    if (saleOrder.getSaleOrderLineTaxList() == null) {
      saleOrder.setSaleOrderLineTaxList(new ArrayList<SaleOrderLineTax>());
    } else {
      List<SaleOrderLineTax> saleOrderLineTaxList =
          saleOrderLineCreateTaxLineService.getUpdatedSaleOrderLineTax(saleOrder);
      saleOrder.getSaleOrderLineTaxList().clear();
      saleOrder.getSaleOrderLineTaxList().addAll(saleOrderLineTaxList);
    }
  }

  @Override
  public BigDecimal getTotalSaleOrderPrice(SaleOrder saleOrder) {
    BigDecimal price = BigDecimal.ZERO;
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      price = price.add(saleOrderLine.getQty().multiply(saleOrderLine.getPriceDiscounted()));
    }
    return price;
  }

  @Override
  public void computePackTotal(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();

    if (ObjectUtils.isEmpty(saleOrderLineList)
        || !saleOrderLinePackService.hasEndOfPackTypeLine(saleOrderLineList)) {
      return;
    }
    BigDecimal totalExTaxTotal = BigDecimal.ZERO;
    BigDecimal totalInTaxTotal = BigDecimal.ZERO;
    saleOrderLineList.sort(Comparator.comparing(SaleOrderLine::getSequence));
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {

      switch (saleOrderLine.getTypeSelect()) {
        case SaleOrderLineRepository.TYPE_NORMAL:
          totalExTaxTotal = totalExTaxTotal.add(saleOrderLine.getExTaxTotal());
          totalInTaxTotal = totalInTaxTotal.add(saleOrderLine.getInTaxTotal());
          break;

        case SaleOrderLineRepository.TYPE_TITLE:
          break;

        case SaleOrderLineRepository.TYPE_START_OF_PACK:
          totalExTaxTotal = totalInTaxTotal = BigDecimal.ZERO;
          break;

        case SaleOrderLineRepository.TYPE_END_OF_PACK:
          saleOrderLine.setQty(BigDecimal.ZERO);
          saleOrderLine.setExTaxTotal(
              saleOrderLine.getIsShowTotal() ? totalExTaxTotal : BigDecimal.ZERO);
          saleOrderLine.setInTaxTotal(
              saleOrderLine.getIsShowTotal() ? totalInTaxTotal : BigDecimal.ZERO);
          totalExTaxTotal = totalInTaxTotal = BigDecimal.ZERO;
          break;

        default:
          break;
      }
    }
    saleOrder.setSaleOrderLineList(saleOrderLineList);
  }

  @Override
  public void resetPackTotal(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (ObjectUtils.isEmpty(saleOrderLineList)) {
      return;
    }
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_END_OF_PACK) {
        saleOrderLine.setIsHideUnitAmounts(Boolean.FALSE);
        saleOrderLine.setIsShowTotal(Boolean.FALSE);
        saleOrderLine.setExTaxTotal(BigDecimal.ZERO);
        saleOrderLine.setInTaxTotal(BigDecimal.ZERO);
      }
    }
    saleOrder.setSaleOrderLineList(saleOrderLineList);
  }
}
