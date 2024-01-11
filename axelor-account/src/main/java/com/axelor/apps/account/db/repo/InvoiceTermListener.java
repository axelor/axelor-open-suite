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

import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.repo.AppAccountRepository;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.PreRemove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceTermListener {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Method to check linked entities, especially the ones which are not mapped by invoiceTerm.
   *
   * @param invoiceTerm
   * @throws AxelorException
   */
  @PreRemove
  protected void checkLinkedEntities(InvoiceTerm invoiceTerm) throws AxelorException {

    LOG.debug("Deleting {}", invoiceTerm);

    boolean allowMultiInvoiceTerm =
        Beans.get(AppAccountRepository.class)
            .all()
            .autoFlush(false)
            .fetchOne()
            .getAllowMultiInvoiceTerms();
    checkDebtRecovery(invoiceTerm, allowMultiInvoiceTerm);
    checkPayVoucherDueElement(invoiceTerm, allowMultiInvoiceTerm);
    checkPayVoucherElementToPay(invoiceTerm, allowMultiInvoiceTerm);
    checkInvoiceTermPayment(invoiceTerm, allowMultiInvoiceTerm);

    LOG.debug("Deleted {}", invoiceTerm);
  }

  protected void checkDebtRecovery(InvoiceTerm invoiceTerm, boolean allowMultiInvoiceTerm)
      throws AxelorException {
    LOG.debug("Checking linked debt recovery");
    List<DebtRecovery> linkedDebtRecoveries =
        Beans.get(DebtRecoveryRepository.class)
            .all()
            .filter(":invoiceTerm member of self.invoiceTermDebtRecoverySet")
            .bind("invoiceTerm", invoiceTerm)
            .autoFlush(false)
            .fetch();

    if (!linkedDebtRecoveries.isEmpty()) {
      if (allowMultiInvoiceTerm) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.INVOICE_TERM_LINKED_TO_DEBT_RECOVERIES),
            invoiceTerm.getName(),
            linkedDebtRecoveries.stream()
                .map(DebtRecovery::getName)
                .collect(Collectors.joining("<br>")));
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MONO_INVOICE_TERM_LINKED_TO_DEBT_RECOVERIES),
            linkedDebtRecoveries.stream()
                .map(DebtRecovery::getName)
                .collect(Collectors.joining("<br>")));
      }
    }
  }

  protected void checkInvoiceTermPayment(InvoiceTerm invoiceTerm, boolean allowMultiInvoiceTerm)
      throws AxelorException {
    LOG.debug("Checking invoice term payment");
    List<InvoiceTermPayment> linkedInvoiceTermPayment =
        Beans.get(InvoiceTermPaymentRepository.class)
            .all()
            .filter("self.invoiceTerm = :invoiceTerm")
            .bind("invoiceTerm", invoiceTerm)
            .autoFlush(false)
            .fetch();

    if (!linkedInvoiceTermPayment.isEmpty()) {

      if (allowMultiInvoiceTerm) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.INVOICE_TERM_LINKED_TO_INVOICE_PAYMENT),
            invoiceTerm.getName(),
            linkedInvoiceTermPayment.stream()
                .map(
                    itp -> {
                      if (itp.getInvoicePayment() != null
                          && itp.getInvoicePayment().getInvoice() != null) {
                        return itp.getInvoicePayment().getInvoice().getInvoiceId();
                      }
                      return null;
                    })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("<br>")));
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MONO_INVOICE_TERM_LINKED_TO_INVOICE_PAYMENT),
            linkedInvoiceTermPayment.stream()
                .map(
                    itp -> {
                      if (itp.getInvoicePayment() != null
                          && itp.getInvoicePayment().getInvoice() != null) {
                        return itp.getInvoicePayment().getInvoice().getInvoiceId();
                      }
                      return null;
                    })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("<br>")));
      }
    }
  }

  protected void checkPayVoucherDueElement(InvoiceTerm invoiceTerm, boolean allowMultiInvoiceTerm)
      throws AxelorException {
    LOG.debug("Checking linked payVoucherDueElement");
    List<PayVoucherDueElement> linkedPayVoucherDueElement =
        Beans.get(PayVoucherDueElementRepository.class)
            .all()
            .filter("self.invoiceTerm = :invoiceTerm")
            .bind("invoiceTerm", invoiceTerm)
            .autoFlush(false)
            .fetch();

    if (!linkedPayVoucherDueElement.isEmpty()) {
      if (allowMultiInvoiceTerm) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.INVOICE_TERM_LINKED_TO_PAYMENT_VOUCHER),
            invoiceTerm.getName(),
            linkedPayVoucherDueElement.stream()
                .map(pvde -> pvde.getPaymentVoucher().getRef())
                .distinct()
                .collect(Collectors.joining("<br>")));
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MONO_INVOICE_TERM_LINKED_TO_PAYMENT_VOUCHER),
            linkedPayVoucherDueElement.stream()
                .map(pvde -> pvde.getPaymentVoucher().getRef())
                .distinct()
                .collect(Collectors.joining("<br>")));
      }
    }
  }

  protected void checkPayVoucherElementToPay(InvoiceTerm invoiceTerm, boolean allowMultiInvoiceTerm)
      throws AxelorException {
    LOG.debug("Checking linked payVoucherElementToPay");
    List<PayVoucherElementToPay> linkedPayVoucherElementToPay =
        Beans.get(PayVoucherElementToPayRepository.class)
            .all()
            .filter("self.invoiceTerm = :invoiceTerm")
            .bind("invoiceTerm", invoiceTerm)
            .autoFlush(false)
            .fetch();

    if (!linkedPayVoucherElementToPay.isEmpty()) {

      if (allowMultiInvoiceTerm) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.INVOICE_TERM_LINKED_TO_PAYMENT_VOUCHER),
            invoiceTerm.getName(),
            linkedPayVoucherElementToPay.stream()
                .map(pvetp -> pvetp.getPaymentVoucher().getRef())
                .distinct()
                .collect(Collectors.joining("<br>")));
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MONO_INVOICE_TERM_LINKED_TO_PAYMENT_VOUCHER),
            linkedPayVoucherElementToPay.stream()
                .map(pvetp -> pvetp.getPaymentVoucher().getRef())
                .distinct()
                .collect(Collectors.joining("<br>")));
      }
    }
  }
}
