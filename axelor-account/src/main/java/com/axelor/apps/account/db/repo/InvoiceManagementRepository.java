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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceManagementRepository extends InvoiceRepository {
  @Override
  public Invoice copy(Invoice entity, boolean deep) {
    try {
      Invoice copy = super.copy(entity, deep);
      InvoiceToolService.resetInvoiceStatusOnCopy(copy);
      return copy;
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public Invoice save(Invoice invoice) {
    try {
      List<InvoicePayment> invoicePayments = invoice.getInvoicePaymentList();
      if (CollectionUtils.isNotEmpty(invoicePayments)) {
        LocalDate latestPaymentDate =
            invoicePayments.stream()
                .filter(
                    invoicePayment ->
                        invoicePayment.getStatusSelect()
                            == InvoicePaymentRepository.STATUS_VALIDATED)
                .map(InvoicePayment::getPaymentDate)
                .max(LocalDate::compareTo)
                .orElse(null);
        invoice.setPaymentDate(latestPaymentDate);
      }
      invoice.setNextDueDate(Beans.get(InvoiceToolService.class).getNextDueDate(invoice));
      invoice = super.save(invoice);

      InvoiceService invoiceService = Beans.get(InvoiceService.class);
      invoiceService.setDraftSequence(invoice);

      return invoice;
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      final String subrogationStatusSelect = "$subrogationStatusSelect";
      if (context.get("_model") != null
          && context.get("_model").toString().equals(SubrogationRelease.class.getName())) {
        if (context.get("id") != null) {
          long id = (long) context.get("id");
          SubrogationRelease subrogationRelease =
              Beans.get(SubrogationReleaseRepository.class).find(id);
          if (subrogationRelease != null && subrogationRelease.getStatusSelect() != null) {
            json.put(subrogationStatusSelect, subrogationRelease.getStatusSelect());
          } else {
            json.put(subrogationStatusSelect, SubrogationReleaseRepository.STATUS_NEW);
          }
        }
      } else {
        json.put(subrogationStatusSelect, SubrogationReleaseRepository.STATUS_NEW);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return super.populate(json, context);
  }

  @Override
  public void remove(Invoice entity) {
    if (!entity.getStatusSelect().equals(InvoiceRepository.STATUS_CANCELED)) {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.INVOICE_CAN_NOT_DELETE),
            entity.getInvoiceId());
      } catch (AxelorException e) {
        throw new PersistenceException(e.getMessage(), e);
      }
    }
    super.remove(entity);
  }
}
