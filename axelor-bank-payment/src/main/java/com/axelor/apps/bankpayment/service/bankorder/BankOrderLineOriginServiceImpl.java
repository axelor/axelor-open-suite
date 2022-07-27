/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLineOrigin;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineOriginRepository;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BankOrderLineOriginServiceImpl implements BankOrderLineOriginService {

  protected BankOrderLineOriginRepository bankOrderLineOriginRepository;

  @Inject private InvoiceTermRepository invoiceTermRepo;

  @Inject
  public BankOrderLineOriginServiceImpl(
      BankOrderLineOriginRepository bankOrderLineOriginRepository) {
    this.bankOrderLineOriginRepository = bankOrderLineOriginRepository;
  }

  public BankOrderLineOrigin createBankOrderLineOrigin(Model model) {

    Class<?> klass = EntityHelper.getEntityClass(model);

    return this.createBankOrderLineOrigin(
        klass.getCanonicalName(),
        model.getId(),
        computeRelatedToSelectName(model),
        computeRelatedToSelectDate(model),
        computeRelatedToSelectDueDate(model));
  }

  protected String computeRelatedToSelectName(Model model) {

    if (model instanceof Invoice) {
      Invoice invoice = ((Invoice) model);
      if (invoice.getMove() != null) {
        return invoice.getMove().getOrigin() != null && !"".equals(invoice.getMove().getOrigin())
            ? invoice.getMove().getOrigin()
            : invoice.getMove().getReference();
      }
      return ((Invoice) model).getInvoiceId();

    } else if (model instanceof PaymentScheduleLine) {

      return ((PaymentScheduleLine) model).getName();

    } else if (model instanceof InvoiceTerm) {
      InvoiceTerm invoiceTerm = ((InvoiceTerm) model);
      if (invoiceTerm.getMoveLine() != null && invoiceTerm.getMoveLine().getMove() != null) {
        return (invoiceTerm.getMoveLine().getOrigin() != null)
                && !("".equals(invoiceTerm.getMoveLine().getOrigin()))
            ? invoiceTerm.getMoveLine().getOrigin()
            : invoiceTerm.getMoveLine().getMove().getReference();
      }
      return invoiceTerm.getName();
    } else if (model instanceof Reimbursement) {

      return ((Reimbursement) model).getRef();
    }
    return null;
  }

  protected LocalDate computeRelatedToSelectDate(Model model) {
    if (model instanceof Invoice) {
      Invoice invoice = ((Invoice) model);
      if (invoice.getMove() != null) {
        return invoice.getMove().getOriginDate() != null
            ? invoice.getMove().getOriginDate()
            : invoice.getMove().getDate();
      }
      return ((Invoice) model).getInvoiceDate();

    } else if (model instanceof PaymentScheduleLine) {

      return ((PaymentScheduleLine) model).getScheduleDate();

    } else if (model instanceof InvoiceTerm) {
      InvoiceTerm invoiceTerm = ((InvoiceTerm) model);
      if (invoiceTerm.getMoveLine() != null) {
        return invoiceTerm.getMoveLine().getOriginDate() != null
            ? invoiceTerm.getMoveLine().getOriginDate()
            : invoiceTerm.getMoveLine().getDate();
      }
      return invoiceTerm.getOriginDate();
    } else if (model instanceof Reimbursement) {

      return null;
    }
    return null;
  }

  protected LocalDate computeRelatedToSelectDueDate(Model model) {
    if (model instanceof Invoice) {

      return ((Invoice) model).getDueDate();

    } else if (model instanceof PaymentScheduleLine) {

      return ((PaymentScheduleLine) model).getScheduleDate();

    } else if (model instanceof InvoiceTerm) {
      return ((InvoiceTerm) model).getDueDate();
    } else if (model instanceof Reimbursement) {

      return null;
    }
    return null;
  }

  protected BankOrderLineOrigin createBankOrderLineOrigin(
      String relatedToSelect,
      Long relatedToSelectId,
      String relatedToSelectName,
      LocalDate relatedToSelectDate,
      LocalDate relatedToSelectDueDate) {
    BankOrderLineOrigin bankOrderLineOrigin = new BankOrderLineOrigin();

    bankOrderLineOrigin.setRelatedToSelect(relatedToSelect);
    bankOrderLineOrigin.setRelatedToSelectId(relatedToSelectId);
    bankOrderLineOrigin.setRelatedToSelectName(relatedToSelectName);
    bankOrderLineOrigin.setRelatedToSelectDate(relatedToSelectDate);
    bankOrderLineOrigin.setRelatedToSelectDueDate(relatedToSelectDueDate);

    return bankOrderLineOrigin;
  }

  public boolean existBankOrderLineOrigin(BankOrder bankOrder, Model model) {

    Class<?> klass = EntityHelper.getEntityClass(model);

    Long count =
        bankOrderLineOriginRepository
            .all()
            .filter(
                "self.relatedToSelect = ?1 AND self.relatedToSelectId = ?2",
                klass.getCanonicalName(),
                model.getId())
            .count();

    if (count != null && count > 0) {
      return true;
    }
    return false;
  }

  @Override
  public Map<String, Object> getRelatedDataMap(BankOrderLineOrigin bankOrderLineOrigin) {
    Map<String, Object> relatedDataMap = new HashMap<>();

    if (!BankOrderLineOriginRepository.RELATED_TO_INVOICE_TERM.equals(
        bankOrderLineOrigin.getRelatedToSelect())) {
      return relatedDataMap;
    }

    InvoiceTerm invoiceTerm = invoiceTermRepo.find(bankOrderLineOrigin.getRelatedToSelectId());
    if (invoiceTerm == null) {
      return relatedDataMap;
    }

    if (invoiceTerm.getInvoice() != null) {
      relatedDataMap.put("relatedModel", BankOrderLineOriginRepository.RELATED_TO_INVOICE);
      relatedDataMap.put("relatedId", invoiceTerm.getInvoice().getId());
    } else if (invoiceTerm.getMoveLine() != null) {
      relatedDataMap.put("relatedModel", Move.class.getCanonicalName());
      relatedDataMap.put("relatedId", invoiceTerm.getMoveLine().getMove().getId());
    }

    return relatedDataMap;
  }
}
