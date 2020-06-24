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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.ComplementaryProductSelected;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.report.IReport;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.db.JpaSequence;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;

public class SaleOrderServiceImpl implements SaleOrderService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private SaleOrderLineService saleOrderService;

  @Override
  public String getFileName(SaleOrder saleOrder) {
    String prefixFileName = I18n.get("Sale order");
    if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_DRAFT_QUOTATION
        || saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
      prefixFileName = I18n.get("Sale quotation");
    }
    return prefixFileName
        + " "
        + saleOrder.getSaleOrderSeq()
        + ((Beans.get(AppSaleService.class).getAppSale().getManageSaleOrderVersion()
                && saleOrder.getVersionNumber() > 1)
            ? "-V" + saleOrder.getVersionNumber()
            : "");
  }

  @Override
  public SaleOrder computeEndOfValidityDate(SaleOrder saleOrder) {
    if (saleOrder.getDuration() != null && saleOrder.getCreationDate() != null) {
      saleOrder.setEndOfValidityDate(
          Beans.get(DurationService.class)
              .computeDuration(saleOrder.getDuration(), saleOrder.getCreationDate()));
    }
    return saleOrder;
  }

  @Override
  @Deprecated
  public String getReportLink(
      SaleOrder saleOrder, String name, String language, boolean proforma, String format)
      throws AxelorException {

    return ReportFactory.createReport(IReport.SALES_ORDER, name + "-${date}")
        .addParam("Locale", language)
        .addParam("SaleOrderId", saleOrder.getId())
        .addParam("ProformaInvoice", proforma)
        .addFormat(format)
        .generate()
        .getFileLink();
  }

  @Override
  public void computeAddressStr(SaleOrder saleOrder) {
    AddressService addressService = Beans.get(AddressService.class);
    saleOrder.setMainInvoicingAddressStr(
        addressService.computeAddressStr(saleOrder.getMainInvoicingAddress()));
    saleOrder.setDeliveryAddressStr(
        addressService.computeAddressStr(saleOrder.getDeliveryAddress()));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public boolean enableEditOrder(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_COMPLETED) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.SALES_ORDER_COMPLETED));
    }

    saleOrder.setOrderBeingEdited(true);
    return false;
  }

  @Override
  public void checkModifiedConfirmedOrder(SaleOrder saleOrder, SaleOrder saleOrderView)
      throws AxelorException {
    // Nothing to check if we don't have supplychain.
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validateChanges(SaleOrder saleOrder) throws AxelorException {
    checkUnauthorizedDiscounts(saleOrder);
  }

  @Override
  public void sortSaleOrderLineList(SaleOrder saleOrder) {
    if (saleOrder.getSaleOrderLineList() != null) {
      saleOrder.getSaleOrderLineList().sort(Comparator.comparing(SaleOrderLine::getSequence));
    }
  }

  @Override
  @Transactional
  public SaleOrder addPack(SaleOrder saleOrder, Pack pack, BigDecimal packQty) {
    Integer sequence = 0;

    List<SaleOrderLine> soLines = saleOrder.getSaleOrderLineList();
    if (soLines != null && !soLines.isEmpty()) {
      sequence =
          Collections.max(
              soLines.stream().map(soLine -> soLine.getSequence()).collect(Collectors.toSet()));
    }

    BigDecimal ConversionRate = new BigDecimal(1.00);
    if (pack.getCurrency() != null
        && !pack.getCurrency().getCode().equals(saleOrder.getCurrency().getCode())) {
      try {
        ConversionRate =
            Beans.get(CurrencyConversionService.class)
                .convert(pack.getCurrency(), saleOrder.getCurrency());
      } catch (MalformedURLException | JSONException | AxelorException e) {
        TraceBackService.trace(e);
      }
    }

    SaleOrderLine soLine;
    for (PackLine packLine : pack.getComponents()) {
      soLine =
          saleOrderService.createSaleOrderLine(
              packLine, saleOrder, packQty, ConversionRate, ++sequence);
      if (soLine != null) {
        soLine.setSaleOrder(saleOrder);
        soLines.add(soLine);
      }
    }

    if (soLines != null && !soLines.isEmpty()) {
      try {
        saleOrder = Beans.get(SaleOrderComputeService.class).computeSaleOrder(saleOrder);
        Beans.get(SaleOrderMarginService.class).computeMarginSaleOrder(saleOrder);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }

      Beans.get(SaleOrderRepository.class).save(saleOrder);
    }
    return saleOrder;
  }

  @Override
  public List<SaleOrderLine> handleComplementaryProducts(SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList == null) {
      saleOrderLineList = new ArrayList<SaleOrderLine>();
    }

    SaleOrderLine originSoLine = null;
    for (SaleOrderLine soLine : saleOrderLineList) {
      if (soLine.getIsComplementaryProductsUnhandledYet()) {
        originSoLine = soLine;
        if (originSoLine.getManualId() == null || originSoLine.getManualId().equals("")) {
          this.setNewManualId(originSoLine);
        }
        break;
      }
    }

    if (originSoLine != null
        && originSoLine.getProduct() != null
        && originSoLine.getSelectedComplementaryProductList() != null) {
      SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
      AppBaseService appBaseService = Beans.get(AppBaseService.class);
      for (ComplementaryProductSelected compProductSelected :
          originSoLine.getSelectedComplementaryProductList()) {
        // Search if there is already a line for this product to modify or remove
        SaleOrderLine newSoLine = null;
        if (saleOrderLineList != null) {
          for (SaleOrderLine soLine : saleOrderLineList) {
            if (originSoLine.getManualId().equals(soLine.getParentId())) {
              if (soLine.getProduct() == compProductSelected.getProduct()) {
                // Edit line if it already exists instead of recreating, otherwise remove if already
                // exists and is no longer selected
                if (compProductSelected.getIsSelected()) {
                  newSoLine = soLine;
                } else {
                  saleOrderLineList.remove(soLine);
                }
                break;
              }
            }
          }
        }

        if (newSoLine == null) {
          if (compProductSelected.getIsSelected()) {
            newSoLine = new SaleOrderLine();
            newSoLine.setProduct(compProductSelected.getProduct());
            newSoLine.setSaleOrder(saleOrder);
            newSoLine.setQty(
                originSoLine
                    .getQty()
                    .multiply(compProductSelected.getQty())
                    .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN));

            saleOrderLineService.computeProductInformation(newSoLine, newSoLine.getSaleOrder());
            saleOrderLineService.computeValues(newSoLine.getSaleOrder(), newSoLine);

            newSoLine.setParentId(originSoLine.getManualId());

            saleOrderLineList.add(newSoLine);
          }
        } else {
          newSoLine.setQty(
              originSoLine
                  .getQty()
                  .multiply(compProductSelected.getQty())
                  .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN));

          saleOrderLineService.computeProductInformation(newSoLine, newSoLine.getSaleOrder());
          saleOrderLineService.computeValues(newSoLine.getSaleOrder(), newSoLine);
        }
      }
      originSoLine.setIsComplementaryProductsUnhandledYet(false);
    }

    return saleOrderLineList;
  }

  @Override
  public void checkUnauthorizedDiscounts(SaleOrder saleOrder) throws AxelorException {
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList != null) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        BigDecimal maxDiscount = saleOrderLineService.computeMaxDiscount(saleOrder, saleOrderLine);
        if (maxDiscount != null
            // do not block if the discount amount if from a derogation
            && !saleOrderLine.getAllowDiscountDerogation()
            && saleOrderLineService.isSaleOrderLineDiscountGreaterThanMaxDiscount(
                saleOrderLine, maxDiscount)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(IExceptionMessage.SALE_ORDER_DISCOUNT_TOO_HIGH));
        }
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void setNewManualId(SaleOrderLine saleOrderLine) {
    saleOrderLine.setManualId(JpaSequence.nextValue("sale.order.line.idSeq"));
  }
}
