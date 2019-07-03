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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceGeneratorSupplyChain;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TimetableServiceImpl implements TimetableService {

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateInvoice(Timetable timetable) throws AxelorException {
    if (Strings.isNullOrEmpty(timetable.getProductName())) {
      throw new AxelorException(
          timetable,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.TIMETABLE_MISSING_PRODUCT_NAME));
    }
    Invoice invoice = this.createInvoice(timetable);
    Beans.get(InvoiceRepository.class).save(invoice);
    timetable.setInvoice(invoice);
    Beans.get(TimetableRepository.class).save(timetable);
    return invoice;
  }

  @Override
  public Invoice createInvoice(Timetable timetable) throws AxelorException {
    SaleOrder saleOrder = timetable.getSaleOrder();
    PurchaseOrder purchaseOrder = timetable.getPurchaseOrder();
    if (saleOrder != null) {
      if (saleOrder.getCurrency() == null) {
        throw new AxelorException(
            timetable,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.SO_INVOICE_6),
            saleOrder.getSaleOrderSeq());
      }
      InvoiceGenerator invoiceGenerator =
          new InvoiceGeneratorSupplyChain(saleOrder) {

            @Override
            public Invoice generate() throws AxelorException {

              return super.createInvoiceHeader();
            }
          };
      Invoice invoice = invoiceGenerator.generate();
      invoiceGenerator.populate(invoice, this.createInvoiceLine(invoice, timetable));

      return invoice;
    }

    if (purchaseOrder != null) {
      if (purchaseOrder.getCurrency() == null) {
        throw new AxelorException(
            timetable,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.PO_INVOICE_1),
            purchaseOrder.getPurchaseOrderSeq());
      }
      InvoiceGenerator invoiceGenerator =
          new InvoiceGeneratorSupplyChain(purchaseOrder) {

            @Override
            public Invoice generate() throws AxelorException {

              return super.createInvoiceHeader();
            }
          };

      Invoice invoice = invoiceGenerator.generate();
      invoiceGenerator.populate(invoice, this.createInvoiceLine(invoice, timetable));
      return invoice;
    }

    return null;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(Invoice invoice, Timetable timetable)
      throws AxelorException {

    Product product = timetable.getProduct();

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGeneratorSupplyChain(
            invoice,
            product,
            timetable.getProductName(),
            timetable.getAmount(),
            timetable.getAmount(),
            timetable.getAmount(),
            timetable.getComments(),
            timetable.getQty(),
            timetable.getUnit(),
            null,
            1,
            BigDecimal.ZERO,
            PriceListLineRepository.AMOUNT_TYPE_NONE,
            timetable.getAmount().multiply(timetable.getQty()),
            null,
            false,
            this.findFirstSaleOrderLine(timetable),
            this.findFirstPurchaseOrderLine(timetable),
            null) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);
            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  @Override
  public SaleOrderLine findFirstSaleOrderLine(Timetable timetable) {
    SaleOrder saleOrder = timetable.getSaleOrder();
    if (saleOrder != null) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        if (saleOrderLine.getTypeSelect() == null
            || !saleOrderLine.getTypeSelect().equals(SaleOrderLineRepository.TYPE_TITLE)) {
          return saleOrderLine;
        }
      }
    }
    return null;
  }

  @Override
  public PurchaseOrderLine findFirstPurchaseOrderLine(Timetable timetable) {
    PurchaseOrder purchaseOrder = timetable.getPurchaseOrder();
    if (purchaseOrder != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        if (!purchaseOrderLine.getIsTitleLine()) {
          return purchaseOrderLine;
        }
      }
    }
    return null;
  }

  @Override
  public void computeProductInformation(Timetable timetable) throws AxelorException {
    Product product = timetable.getProduct();

    String productName = product.getName();

    Unit unit = product.getSalesUnit();
    if (unit == null) unit = product.getUnit();

    timetable.setProductName(productName);
    timetable.setUnit(unit);
  }
}
