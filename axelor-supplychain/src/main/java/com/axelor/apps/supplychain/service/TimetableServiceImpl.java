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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.PaymentConditionToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.TimetableTemplate;
import com.axelor.apps.supplychain.db.TimetableTemplateLine;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class TimetableServiceImpl implements TimetableService {

  SaleOrderInvoiceService saleOrderInvoiceService;
  TimetableRepository timetableRepository;

  public static final int FETCH_LIMIT = 10;

  @Inject
  public TimetableServiceImpl(
      SaleOrderInvoiceService saleOrderInvoiceService, TimetableRepository timetableRepository) {
    this.saleOrderInvoiceService = saleOrderInvoiceService;
    this.timetableRepository = timetableRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateInvoice(Timetable timetable) throws AxelorException {
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
            I18n.get(SupplychainExceptionMessage.SO_INVOICE_6),
            saleOrder.getSaleOrderSeq());
      }
      List<Long> timetableId = new ArrayList<>();
      timetableId.add(timetable.getId());
      Invoice invoice =
          saleOrderInvoiceService.generateInvoice(
              saleOrder,
              SaleOrderRepository.INVOICE_TIMETABLES,
              BigDecimal.ZERO,
              true,
              null,
              timetableId);

      return invoice;
    }

    if (purchaseOrder != null) {
      if (purchaseOrder.getCurrency() == null) {
        throw new AxelorException(
            timetable,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(SupplychainExceptionMessage.SO_INVOICE_6),
            purchaseOrder.getPurchaseOrderSeq());
      }
      List<Long> timetableId = new ArrayList<>();
      timetableId.add(timetable.getId());
      return Beans.get(PurchaseOrderInvoiceServiceImpl.class)
          .generateInvoiceFromTimetableForPurchaseOrder(purchaseOrder, timetableId);
    }

    return null;
  }

  public List<Timetable> applyTemplate(
      TimetableTemplate template, BigDecimal exTaxTotal, LocalDate computationDate) {
    List<Timetable> timetables = new ArrayList<>();

    for (TimetableTemplateLine templateLine : template.getTimetableTemplateLineList()) {
      Timetable timetable = new Timetable();
      timetable.setEstimatedDate(
          PaymentConditionToolService.getDueDate(
              templateLine.getTypeSelect(),
              templateLine.getPaymentTime(),
              templateLine.getPeriodTypeSelect(),
              templateLine.getDaySelect(),
              computationDate));
      timetable.setPercentage(templateLine.getPercentage());
      timetable.setAmount(
          exTaxTotal.multiply(templateLine.getPercentage()).divide(BigDecimal.valueOf(100)));
      timetables.add(timetable);
    }

    timetables.sort(Comparator.comparing(Timetable::getEstimatedDate));
    return timetables;
  }

  @Override
  public void deleteInvoiceTimeTable(Invoice invoice) {
    JPA.em()
        .createQuery(
            "UPDATE Timetable self SET self.invoice = NULL WHERE self.invoice.id = :invoiceId")
        .setParameter("invoiceId", invoice.getId())
        .executeUpdate();
  }

  @Override
  public BigDecimal computeAmount(
      Timetable timetable,
      List<Timetable> timetableList,
      BigDecimal exTaxTotal,
      Currency currency) {
    BigDecimal percentage = timetable.getPercentage();
    BigDecimal result =
        exTaxTotal
            .multiply(percentage)
            .divide(
                BigDecimal.valueOf(100), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP)
            .setScale(currency.getNumberOfDecimals(), RoundingMode.HALF_UP);

    if (CollectionUtils.isEmpty(timetableList)) {
      return result;
    }
    List<Timetable> computeTimetableList = new ArrayList<>(timetableList);
    computeTimetableList.add(timetable);
    if (computeTimetableList.stream()
            .map(Timetable::getPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .compareTo(new BigDecimal(100))
        == 0) {
      return exTaxTotal.subtract(
          timetableList.stream()
              .map(Timetable::getAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add));
    } else {
      return result;
    }
  }
}
