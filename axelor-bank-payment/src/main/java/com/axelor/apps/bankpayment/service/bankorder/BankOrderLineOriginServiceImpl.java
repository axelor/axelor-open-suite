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
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLineOrigin;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineOriginRepository;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BankOrderLineOriginServiceImpl implements BankOrderLineOriginService {

  protected BankOrderLineOriginRepository bankOrderLineOriginRepository;

  protected InvoiceTermRepository invoiceTermRepo;

  protected InvoiceRepository invoiceRepository;

  protected DMSFileRepository dmsFileRepository;

  protected MetaFiles metaFiles;

  protected final String RELATED_MODEL_KEY = "relatedModel";

  protected final String RELATED_ID_KEY = "relatedId";

  @Inject
  public BankOrderLineOriginServiceImpl(
      BankOrderLineOriginRepository bankOrderLineOriginRepository,
      InvoiceTermRepository invoiceTermRepo,
      InvoiceRepository invoiceRepository,
      DMSFileRepository dmsFileRepository,
      MetaFiles metaFiles) {
    this.bankOrderLineOriginRepository = bankOrderLineOriginRepository;
    this.invoiceTermRepo = invoiceTermRepo;
    this.invoiceRepository = invoiceRepository;
    this.dmsFileRepository = dmsFileRepository;
    this.metaFiles = metaFiles;
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

    } else if (model instanceof PaymentVoucher) {

      return ((PaymentVoucher) model).getRef();
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

    } else if (model instanceof PaymentVoucher) {

      return ((PaymentVoucher) model).getPaymentDate();
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

    if (klass.equals(Invoice.class)) {
      Invoice invoice = (Invoice) model;
      count +=
          bankOrderLineOriginRepository
              .all()
              .filter(
                  "self.relatedToSelect = ?1 AND self.relatedToSelectId in (?2)",
                  BankOrderLineOriginRepository.RELATED_TO_INVOICE_TERM,
                  invoice.getInvoiceTermList().stream()
                      .map(InvoiceTerm::getId)
                      .collect(Collectors.toList()))
              .count();
    }

    if (count != null && count > 0) {
      return true;
    }
    return false;
  }

  @Override
  public Map<String, Object> getRelatedDataMap(BankOrderLineOrigin bankOrderLineOrigin) {
    Map<String, Object> relatedDataMap = new HashMap<>();
    String relatedTo = bankOrderLineOrigin.getRelatedToSelect();
    List<String> authorizedType = new ArrayList<>();
    authorizedType.add(BankOrderLineOriginRepository.RELATED_TO_INVOICE_TERM);
    authorizedType.add(BankOrderLineOriginRepository.RELATED_TO_INVOICE);

    if (!authorizedType.contains(relatedTo)) {
      return relatedDataMap;
    }

    if (BankOrderLineOriginRepository.RELATED_TO_INVOICE_TERM.equals(relatedTo)) {
      relatedDataMap = getInvoiceTermDataMap(bankOrderLineOrigin, relatedDataMap);
    }

    if (BankOrderLineOriginRepository.RELATED_TO_INVOICE.equals(relatedTo)) {
      relatedDataMap = getInvoiceDataMap(bankOrderLineOrigin, relatedDataMap);
    }

    return relatedDataMap;
  }

  protected Map<String, Object> getInvoiceDataMap(
      BankOrderLineOrigin bankOrderLineOrigin, Map<String, Object> relatedDataMap) {
    Invoice invoice = invoiceRepository.find(bankOrderLineOrigin.getRelatedToSelectId());
    if (invoice == null) {
      return relatedDataMap;
    }
    setRelatedData(
        relatedDataMap, BankOrderLineOriginRepository.RELATED_TO_INVOICE, invoice.getId());
    return relatedDataMap;
  }

  protected Map<String, Object> getInvoiceTermDataMap(
      BankOrderLineOrigin bankOrderLineOrigin, Map<String, Object> relatedDataMap) {
    InvoiceTerm invoiceTerm = invoiceTermRepo.find(bankOrderLineOrigin.getRelatedToSelectId());
    if (invoiceTerm == null) {
      return relatedDataMap;
    }

    if (invoiceTerm.getInvoice() != null) {
      setRelatedData(
          relatedDataMap,
          BankOrderLineOriginRepository.RELATED_TO_INVOICE,
          invoiceTerm.getInvoice().getId());
    } else if (invoiceTerm.getMoveLine() != null) {
      setRelatedData(
          relatedDataMap,
          Move.class.getCanonicalName(),
          invoiceTerm.getMoveLine().getMove().getId());
    }
    return relatedDataMap;
  }

  protected Map<String, Object> setRelatedData(
      Map<String, Object> relatedDataMap, String relatedModel, Long relatedId) {
    relatedDataMap.put(RELATED_MODEL_KEY, relatedModel);
    relatedDataMap.put(RELATED_ID_KEY, relatedId);
    return relatedDataMap;
  }

  public boolean dmsFilePresent(BankOrderLineOrigin bankOrderLineOrigin) {
    Map<String, Object> relatedDataMap = getRelatedDataMap(bankOrderLineOrigin);
    boolean dmsFileExists =
        dmsFileRepository
                .all()
                .filter(
                    "self.relatedModel = :relatedModel AND self.relatedId = :relatedId AND self.isDirectory = false")
                .bind("relatedModel", relatedDataMap.get(RELATED_MODEL_KEY))
                .bind("relatedId", relatedDataMap.get(RELATED_ID_KEY))
                .fetchOne()
            != null;
    if (dmsFileExists) {
      return true;
    }
    Invoice invoice = getRelatedInvoice(relatedDataMap);
    return invoice != null && invoice.getPrintedPDF() != null;
  }

  @Override
  @Transactional
  public void ensurePrintedPdfAttached(BankOrderLineOrigin bankOrderLineOrigin) {
    Map<String, Object> relatedDataMap = getRelatedDataMap(bankOrderLineOrigin);
    Invoice invoice = getRelatedInvoice(relatedDataMap);
    if (invoice == null) {
      return;
    }
    MetaFile printedPdf = invoice.getPrintedPDF();
    if (printedPdf == null) {
      return;
    }
    boolean alreadyAttached =
        dmsFileRepository
                .all()
                .filter(
                    "self.relatedModel = :relatedModel AND self.relatedId = :relatedId AND self.metaFile = :metaFile AND self.isDirectory = false")
                .bind("relatedModel", relatedDataMap.get(RELATED_MODEL_KEY))
                .bind("relatedId", relatedDataMap.get(RELATED_ID_KEY))
                .bind("metaFile", printedPdf)
                .fetchOne()
            != null;
    if (alreadyAttached) {
      return;
    }
    metaFiles.attach(printedPdf, printedPdf.getFileName(), invoice);
  }

  protected Invoice getRelatedInvoice(Map<String, Object> relatedDataMap) {
    if (!BankOrderLineOriginRepository.RELATED_TO_INVOICE.equals(
        relatedDataMap.get(RELATED_MODEL_KEY))) {
      return null;
    }
    return invoiceRepository.find((Long) relatedDataMap.get(RELATED_ID_KEY));
  }
}
