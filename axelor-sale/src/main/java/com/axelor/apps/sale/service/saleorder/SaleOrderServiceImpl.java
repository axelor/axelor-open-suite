/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.report.IReport;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.MalformedURLException;
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

    return I18n.get("Sale order")
        + " "
        + saleOrder.getSaleOrderSeq()
        + ((saleOrder.getVersionNumber() > 1) ? "-V" + saleOrder.getVersionNumber() : "");
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
    // Nothing to do if we don't have supplychain.
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
}
