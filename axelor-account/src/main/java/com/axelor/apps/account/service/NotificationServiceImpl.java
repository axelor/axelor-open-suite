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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Notification;
import com.axelor.apps.account.db.NotificationItem;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.NotificationRepository;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class NotificationServiceImpl implements NotificationService {

  @Override
  public void populateNotificationItemList(Notification notification) {
    notification.clearNotificationItemList();

    Comparator<Invoice> byInvoiceDate =
        (i1, i2) -> i1.getInvoiceDate().compareTo(i2.getInvoiceDate());
    Comparator<Invoice> byDueDate = (i1, i2) -> i1.getDueDate().compareTo(i2.getDueDate());
    Comparator<Invoice> byInvoiceId = (i1, i2) -> i1.getInvoiceId().compareTo(i2.getInvoiceId());

    List<Invoice> invoiceList =
        notification
            .getSubrogationRelease()
            .getInvoiceSet()
            .stream()
            .sorted(byInvoiceDate.thenComparing(byDueDate).thenComparing(byInvoiceId))
            .collect(Collectors.toList());

    for (Invoice invoice : invoiceList) {
      if (invoice.getAmountRemaining().signum() > 0) {
        notification.addNotificationItemListItem(createNotificationItem(invoice));
      }
    }
  }

  private NotificationItem createNotificationItem(Invoice invoice) {
    return new NotificationItem(invoice, invoice.getAmountRemaining());
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void validate(Notification notification) throws AxelorException {
    MoveService moveService = Beans.get(MoveService.class);
    InvoicePaymentCreateService invoicePaymentCreateService =
        Beans.get(InvoicePaymentCreateService.class);
    ReconcileService reconcileService = Beans.get(ReconcileService.class);
    AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);

    SubrogationRelease subrogationRelease = notification.getSubrogationRelease();
    Company company = subrogationRelease.getCompany();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Journal journal = accountConfigService.getAutoMiscOpeJournal(accountConfig);
    boolean allCleared = true;

    for (NotificationItem notificationItem : notification.getNotificationItemList()) {
      Invoice invoice = notificationItem.getInvoice();
      BigDecimal amountRemaining = invoice.getAmountRemaining();

      if (amountRemaining.signum() > 0) {
        BigDecimal amountPaid = notificationItem.getAmountPaid();

        if (amountRemaining.compareTo(amountPaid) > 0) {
          allCleared = false;
        }

        Move paymentMove =
            moveService
                .getMoveCreateService()
                .createMove(
                    journal,
                    company,
                    company.getCurrency(),
                    invoice.getPartner(),
                    notification.getPaymentDate(),
                    null,
                    MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);
        MoveLine creditMoveLine, debitMoveLine;
        boolean isOutPayment = InvoiceToolService.isOutPayment(invoice);

        if (!isOutPayment) {
          creditMoveLine =
              moveService
                  .getMoveLineService()
                  .createMoveLine(
                      paymentMove,
                      invoice.getPartner(),
                      invoice.getPartnerAccount(),
                      amountPaid,
                      false,
                      notification.getPaymentDate(),
                      null,
                      1,
                      subrogationRelease.getSequenceNumber(),
                      invoice.getInvoiceId());
          debitMoveLine =
              moveService
                  .getMoveLineService()
                  .createMoveLine(
                      paymentMove,
                      invoice.getPartner(),
                      accountConfig.getFactorCreditAccount(),
                      amountPaid,
                      true,
                      notification.getPaymentDate(),
                      null,
                      2,
                      subrogationRelease.getSequenceNumber(),
                      invoice.getInvoiceId());
        } else {
          creditMoveLine =
              moveService
                  .getMoveLineService()
                  .createMoveLine(
                      paymentMove,
                      invoice.getPartner(),
                      accountConfig.getFactorDebitAccount(),
                      amountPaid,
                      false,
                      notification.getPaymentDate(),
                      null,
                      1,
                      subrogationRelease.getSequenceNumber(),
                      invoice.getInvoiceId());
          debitMoveLine =
              moveService
                  .getMoveLineService()
                  .createMoveLine(
                      paymentMove,
                      invoice.getPartner(),
                      invoice.getPartnerAccount(),
                      amountPaid,
                      true,
                      notification.getPaymentDate(),
                      null,
                      2,
                      subrogationRelease.getSequenceNumber(),
                      invoice.getInvoiceId());
        }

        paymentMove.addMoveLineListItem(debitMoveLine);
        paymentMove.addMoveLineListItem(creditMoveLine);
        paymentMove = Beans.get(MoveRepository.class).save(paymentMove);

        moveService.getMoveValidateService().validate(paymentMove);

        if (!isOutPayment) {
          reconcileService.reconcile(
              findInvoiceAccountMoveLine(invoice), creditMoveLine, true, true);
        } else {
          reconcileService.reconcile(
              debitMoveLine, findInvoiceAccountMoveLine(invoice), true, true);
        }
      }
    }

    if (allCleared) {
      subrogationRelease.setStatusSelect(SubrogationReleaseRepository.STATUS_CLEARED);
    }

    notification.setStatusSelect(NotificationRepository.STATUS_VALIDATED);
  }

  private MoveLine findInvoiceAccountMoveLine(Invoice invoice) {
    for (MoveLine moveLine : invoice.getMove().getMoveLineList()) {
      if (moveLine.getAccount().equals(invoice.getPartnerAccount())) {
        return moveLine;
      }
    }
    throw new NoSuchElementException();
  }
}
