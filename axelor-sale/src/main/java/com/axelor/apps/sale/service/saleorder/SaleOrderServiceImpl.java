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
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.report.IReport;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderServiceImpl implements SaleOrderService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
  @Transactional(rollbackOn = {Exception.class, AxelorException.class})
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
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void validateChanges(SaleOrder saleOrder) throws AxelorException {
    // Nothing to do if we don't have supplychain.
  }

  @Override
  public void sortSaleOrderLineList(SaleOrder saleOrder) {
    if (saleOrder.getSaleOrderLineList() != null) {
      saleOrder.getSaleOrderLineList().sort(Comparator.comparing(SaleOrderLine::getSequence));
    }
  }

  @Transactional
  public void createSaleOrderPartnerAddress(Context context) {
    PartnerAddress PartAddress =
        Beans.get(PartnerAddressRepository.class)
            .find(Long.parseLong((context.get("id").toString())));

    Address address = PartAddress.getAddress();
    address.setFullName(Beans.get(AddressService.class).computeFullName(address));

    SaleOrder saleOrder =
        Beans.get(SaleOrderRepository.class)
            .find(Long.parseLong(context.get("_saleOrderId").toString()));

    Boolean isInvoicing = (Boolean) context.get("isInvoicingAddr");

    if (isInvoicing) {
      saleOrder.setMainInvoicingAddress(address);
      saleOrder.setMainInvoicingAddressStr(
          Beans.get(AddressService.class).computeAddressStr(address));

    } else {
      saleOrder.setDeliveryAddress(address);
      saleOrder.setDeliveryAddressStr(Beans.get(AddressService.class).computeAddressStr(address));
    }

    Beans.get(SaleOrderRepository.class).save(saleOrder);
  }
}
