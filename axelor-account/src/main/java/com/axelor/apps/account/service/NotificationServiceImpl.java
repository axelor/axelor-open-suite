/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Notification;
import com.axelor.apps.account.db.NotificationItem;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.NotificationRepository;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javax.persistence.TypedQuery;

public class NotificationServiceImpl implements NotificationService {

  protected MoveValidateService moveValidateService;
  protected MoveCreateService moveCreateService;
  protected ReconcileService reconcileService;
  protected AccountConfigService accountConfigService;
  protected SubrogationReleaseService subrogationReleaseService;
  protected MoveRepository moveRepository;
  protected MoveLineCreateService moveLineCreateService;

  @Inject
  public NotificationServiceImpl(
      MoveValidateService moveValidateService,
      MoveCreateService moveCreateService,
      ReconcileService reconcileService,
      AccountConfigService accountConfigService,
      SubrogationReleaseService subrogationReleaseService,
      MoveRepository moveRepository,
      MoveLineCreateService moveLineCreateService) {
    this.moveValidateService = moveValidateService;
    this.moveCreateService = moveCreateService;
    this.reconcileService = reconcileService;
    this.accountConfigService = accountConfigService;
    this.subrogationReleaseService = subrogationReleaseService;
    this.moveRepository = moveRepository;
    this.moveLineCreateService = moveLineCreateService;
  }

  @Override
  public void populateNotificationItemList(Notification notification) {
    notification.clearNotificationItemList();

    Comparator<Invoice> byInvoiceDate =
        (i1, i2) -> i1.getInvoiceDate().compareTo(i2.getInvoiceDate());
    Comparator<Invoice> byDueDate = (i1, i2) -> i1.getDueDate().compareTo(i2.getDueDate());
    Comparator<Invoice> byInvoiceId = (i1, i2) -> i1.getInvoiceId().compareTo(i2.getInvoiceId());

    List<Invoice> invoiceList = new ArrayList<Invoice>();
    if (notification.getSubrogationRelease() != null) {
      invoiceList =
          notification.getSubrogationRelease().getInvoiceSet().stream()
              .sorted(byInvoiceDate.thenComparing(byDueDate).thenComparing(byInvoiceId))
              .collect(Collectors.toList());
    }
    for (Invoice invoice : invoiceList) {
      if (invoice.getAmountRemaining().signum() > 0) {
        notification.addNotificationItemListItem(createNotificationItem(invoice));
      }
    }
  }

  protected NotificationItem createNotificationItem(Invoice invoice) {
    if (invoice.getOperationTypeSelect().equals(InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND)) {
      return new NotificationItem(invoice, invoice.getAmountRemaining().negate());
    } else {
      return new NotificationItem(invoice, invoice.getAmountRemaining());
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(Notification notification) throws AxelorException {

    for (NotificationItem notificationItem : notification.getNotificationItemList()) {
      this.createPaymentMove(notificationItem);
    }

    notification.setStatusSelect(NotificationRepository.STATUS_VALIDATED);
  }

  protected Journal getJournal(AccountConfig accountConfig) throws AxelorException {
    return accountConfigService.getAutoMiscOpeJournal(accountConfig);
  }

  protected Account getAccount(AccountConfig accountConfig, NotificationItem notificationItem)
      throws AxelorException {
    if (notificationItem.getTypeSelect()
        == NotificationRepository.TYPE_PAYMENT_TO_THE_FACTORE_AFTER_FACTORE_RETURN) {
      return accountConfigService.getFactorDebitAccount(accountConfig);
    } else {
      return accountConfigService.getFactorCreditAccount(accountConfig);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Move createPaymentMove(NotificationItem notificationItem) throws AxelorException {

    Notification notification = notificationItem.getNotification();
    Invoice invoice = notificationItem.getInvoice();
    Company company = invoice.getCompany();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Journal journal = getJournal(accountConfig);

    SubrogationRelease subrogationRelease = getSubrogationRelease(notificationItem);

    String origin = computeOrigin(subrogationRelease, invoice);

    BigDecimal amountPaid = notificationItem.getAmountPaid();

    if (amountPaid.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }

    boolean isInvoice = amountPaid.signum() > 0;

    Move paymentMove =
        moveCreateService.createMove(
            journal,
            company,
            company.getCurrency(),
            invoice.getPartner(),
            notification.getPaymentDate(),
            notification.getPaymentDate(),
            null,
            invoice.getFiscalPosition(),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            origin,
            invoice.getInvoiceId(),
            invoice.getCompanyBankDetails());
    MoveLine partnerMoveLine, notificationMoveLine;

    Account account = getAccount(accountConfig, notificationItem);

    notificationMoveLine =
        moveLineCreateService.createMoveLine(
            paymentMove,
            invoice.getPartner(),
            account,
            amountPaid.abs(),
            isInvoice,
            notification.getPaymentDate(),
            null,
            1,
            origin,
            invoice.getInvoiceId());

    partnerMoveLine =
        moveLineCreateService.createMoveLine(
            paymentMove,
            invoice.getPartner(),
            invoice.getPartnerAccount(),
            amountPaid.abs(),
            !isInvoice,
            notification.getPaymentDate(),
            null,
            2,
            origin,
            invoice.getInvoiceId());

    paymentMove.addMoveLineListItem(notificationMoveLine);
    paymentMove.addMoveLineListItem(partnerMoveLine);
    paymentMove = moveRepository.save(paymentMove);

    moveValidateService.accounting(paymentMove);

    MoveLine invoiceMoveLine = findInvoiceAccountMoveLine(invoice);
    MoveLine subrogationReleaseMoveLine =
        findSubrogationReleaseAccountMoveLine(invoice, account, isInvoice);

    if (invoiceMoveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) == 1) {
      if (amountPaid.signum() > 0) {
        reconcileService.reconcile(invoiceMoveLine, partnerMoveLine, true, true);
      } else {
        reconcileService.reconcile(partnerMoveLine, invoiceMoveLine, true, true);
      }
      if (subrogationReleaseMoveLine != null
          && notificationItem.getTypeSelect()
              == NotificationRepository.TYPE_PAYMENT_TO_THE_FACTORE) {
        if (amountPaid.signum() > 0) {
          reconcileService.reconcile(notificationMoveLine, subrogationReleaseMoveLine, true, false);
        } else {
          reconcileService.reconcile(subrogationReleaseMoveLine, notificationMoveLine, true, false);
        }
      }
    }

    notificationItem.setMove(paymentMove);

    if (subrogationRelease != null) {
      subrogationReleaseService.clear(subrogationRelease);
    }

    return paymentMove;
  }

  protected String computeOrigin(SubrogationRelease subrogationRelease, Invoice invoice) {

    return subrogationRelease != null
        ? subrogationRelease.getSequenceNumber()
        : I18n.get("Payment notification") + " " + invoice.getInvoiceId();
  }

  protected SubrogationRelease getSubrogationRelease(NotificationItem notificationItem) {

    Invoice invoice = notificationItem.getInvoice();

    TypedQuery<SubrogationRelease> query =
        JPA.em()
            .createQuery(
                "SELECT self FROM SubrogationRelease self JOIN self.invoiceSet invoices WHERE self.statusSelect = :statusSelect AND invoices.id IN (:invoiceId)",
                SubrogationRelease.class);
    query.setParameter("statusSelect", SubrogationReleaseRepository.STATUS_ACCOUNTED);
    query.setParameter("invoiceId", invoice.getId());

    List<SubrogationRelease> subrogationReleaseResultList = query.getResultList();

    if (subrogationReleaseResultList != null && !subrogationReleaseResultList.isEmpty()) {
      return subrogationReleaseResultList.get(0);
    }

    return null;
  }

  protected MoveLine findInvoiceAccountMoveLine(Invoice invoice) {
    for (MoveLine moveLine : invoice.getMove().getMoveLineList()) {
      if (moveLine.getAccount().equals(invoice.getPartnerAccount())) {
        return moveLine;
      }
    }
    throw new NoSuchElementException();
  }

  protected MoveLine findSubrogationReleaseAccountMoveLine(
      Invoice invoice, Account account, boolean isInvoice) throws AxelorException {
    if (invoice.getSubrogationReleaseMove() != null) {
      for (MoveLine moveLine : invoice.getSubrogationReleaseMove().getMoveLineList()) {
        if (moveLine.getAccount().equals(account)
            && ((!isInvoice && moveLine.getDebit().signum() > 0)
                || (isInvoice && moveLine.getCredit().signum() > 0))) {
          return moveLine;
        }
      }
    }
    return null;
  }

  @Override
  public List<Long> getMoveLines(Notification notification) {
    List<Long> moveLineIdList = new ArrayList<Long>();
    for (NotificationItem notificationItem : notification.getNotificationItemList()) {
      if (notificationItem.getMove() == null) {
        continue;
      }
      for (MoveLine moveLine : notificationItem.getMove().getMoveLineList()) {
        moveLineIdList.add(moveLine.getId());
      }
    }
    return moveLineIdList;
  }
}
