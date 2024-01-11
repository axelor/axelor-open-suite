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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.batch.BatchAccountingCutOff;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class MoveLineServiceImpl implements MoveLineService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected int jpaLimit = 20;

  protected MoveLineToolService moveLineToolService;
  protected MoveLineRepository moveLineRepository;
  protected InvoiceRepository invoiceRepository;
  protected PaymentService paymentService;
  protected AppAccountService appAccountService;
  protected AccountConfigService accountConfigService;
  protected InvoiceTermService invoiceTermService;
  protected MoveLineControlService moveLineControlService;
  protected MoveLineTaxService moveLineTaxService;

  @Inject
  public MoveLineServiceImpl(
      MoveLineRepository moveLineRepository,
      InvoiceRepository invoiceRepository,
      PaymentService paymentService,
      MoveLineToolService moveLineToolService,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      InvoiceTermService invoiceTermService,
      MoveLineControlService moveLineControlService,
      MoveLineTaxService moveLineTaxService) {
    this.moveLineRepository = moveLineRepository;
    this.invoiceRepository = invoiceRepository;
    this.paymentService = paymentService;
    this.moveLineToolService = moveLineToolService;
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
    this.invoiceTermService = invoiceTermService;
    this.moveLineControlService = moveLineControlService;
    this.moveLineTaxService = moveLineTaxService;
  }

  @Override
  public MoveLine balanceCreditDebit(MoveLine moveLine, Move move) {
    if (move.getMoveLineList() != null) {
      BigDecimal totalCredit =
          move.getMoveLineList().stream()
              .map(MoveLine::getCredit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);

      BigDecimal totalDebit =
          move.getMoveLineList().stream()
              .map(MoveLine::getDebit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);

      if (totalCredit.compareTo(totalDebit) < 0) {
        moveLine.setCredit(totalDebit.subtract(totalCredit));
      } else if (totalCredit.compareTo(totalDebit) > 0) {
        moveLine.setDebit(totalCredit.subtract(totalDebit));
      }
    }

    return moveLine;
  }

  // TODO: Refactoriser cette methode dans un service Invoice
  /**
   * Procédure permettant d'impacter la case à cocher "Passage à l'huissier" sur la facture liée à
   * l'écriture
   *
   * @param moveLine Une ligne d'écriture
   */
  @Override
  @Transactional
  public void usherProcess(MoveLine moveLine) {

    Invoice invoice = moveLine.getMove().getInvoice();
    if (invoice != null) {
      if (moveLine.getUsherPassageOk()) {
        invoice.setUsherPassageOk(true);
      } else {
        invoice.setUsherPassageOk(false);
      }
      invoiceRepository.save(invoice);
    }
  }

  /**
   * Method used to reconcile the move line list passed as a parameter
   *
   * @param moveLineList
   */
  @Override
  public void reconcileMoveLinesWithCacheManagement(List<MoveLine> moveLineList)
      throws AxelorException {

    if (moveLineList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.MOVE_LINE_RECONCILE_LINE_NO_SELECTED));
    }

    moveLineTaxService.checkEmptyTaxLines(moveLineList);

    Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap =
        getPopulatedReconcilableMoveLineMap(moveLineList);

    Comparator<MoveLine> byDate = Comparator.comparing(MoveLine::getDate);

    int i = 0;

    for (Pair<List<MoveLine>, List<MoveLine>> moveLineLists : moveLineMap.values()) {
      try {
        moveLineLists = this.findMoveLineLists(moveLineLists);
        this.useExcessPaymentOnMoveLinesDontThrow(byDate, paymentService, moveLineLists);
      } catch (Exception e) {
        TraceBackService.trace(e);
        log.debug(e.getMessage());
      } finally {
        i++;
        if (i % jpaLimit == 0) {
          JPA.clear();
        }
      }
    }
  }

  protected Pair<List<MoveLine>, List<MoveLine>> findMoveLineLists(
      Pair<List<MoveLine>, List<MoveLine>> moveLineLists) {
    List<MoveLine> fetchedDebitMoveLineList =
        moveLineLists.getLeft().stream()
            .map(moveLine -> moveLineRepository.find(moveLine.getId()))
            .collect(Collectors.toList());
    List<MoveLine> fetchedCreditMoveLineList =
        moveLineLists.getRight().stream()
            .map(moveLine -> moveLineRepository.find(moveLine.getId()))
            .collect(Collectors.toList());
    return Pair.of(fetchedDebitMoveLineList, fetchedCreditMoveLineList);
  }

  @Override
  @Transactional
  public void reconcileMoveLines(List<MoveLine> moveLineList) {

    Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap =
        getPopulatedReconcilableMoveLineMap(moveLineList);

    Comparator<MoveLine> byDate = Comparator.comparing(MoveLine::getDate);

    for (Pair<List<MoveLine>, List<MoveLine>> moveLineLists : moveLineMap.values()) {
      List<MoveLine> companyPartnerCreditMoveLineList = moveLineLists.getLeft();
      List<MoveLine> companyPartnerDebitMoveLineList = moveLineLists.getRight();
      companyPartnerCreditMoveLineList.sort(byDate);
      companyPartnerDebitMoveLineList.sort(byDate);
      paymentService.useExcessPaymentOnMoveLinesDontThrow(
          companyPartnerDebitMoveLineList, companyPartnerCreditMoveLineList);
    }
  }

  protected Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>>
      getPopulatedReconcilableMoveLineMap(List<MoveLine> moveLineList) {

    Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap = new HashMap<>();
    populateCredit(moveLineMap, moveLineToolService.getReconciliableCreditMoveLines(moveLineList));
    populateDebit(moveLineMap, moveLineToolService.getReconciliableDebitMoveLines(moveLineList));
    return moveLineMap;
  }

  @Transactional
  protected void useExcessPaymentOnMoveLinesDontThrow(
      Comparator<MoveLine> byDate,
      PaymentService paymentService,
      Pair<List<MoveLine>, List<MoveLine>> moveLineLists) {
    List<MoveLine> companyPartnerCreditMoveLineList = moveLineLists.getLeft();
    List<MoveLine> companyPartnerDebitMoveLineList = moveLineLists.getRight();
    companyPartnerCreditMoveLineList.sort(byDate);
    companyPartnerDebitMoveLineList.sort(byDate);
    paymentService.useExcessPaymentOnMoveLinesDontThrow(
        companyPartnerDebitMoveLineList, companyPartnerCreditMoveLineList);
  }

  protected void populateCredit(
      Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap,
      List<MoveLine> reconciliableMoveLineList) {
    populateMoveLineMap(moveLineMap, reconciliableMoveLineList, true);
  }

  protected void populateDebit(
      Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap,
      List<MoveLine> reconciliableMoveLineList) {
    populateMoveLineMap(moveLineMap, reconciliableMoveLineList, false);
  }

  protected void populateMoveLineMap(
      Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap,
      List<MoveLine> reconciliableMoveLineList,
      boolean isCredit) {
    for (MoveLine moveLine : reconciliableMoveLineList) {

      Move move = moveLine.getMove();

      List<Object> keys = new ArrayList<Object>();

      keys.add(move.getCompany());
      keys.add(moveLine.getAccount());
      if (moveLine.getAccount().getUseForPartnerBalance()) {
        keys.add(moveLine.getPartner());
      }

      Pair<List<MoveLine>, List<MoveLine>> moveLineLists = moveLineMap.get(keys);

      if (moveLineLists == null) {
        moveLineLists = Pair.of(new ArrayList<>(), new ArrayList<>());
        moveLineMap.put(keys, moveLineLists);
      }

      List<MoveLine> moveLineList = isCredit ? moveLineLists.getLeft() : moveLineLists.getRight();
      moveLineList.add(moveLine);
    }
  }

  @Override
  @Transactional
  public void setIsSelectedBankReconciliation(MoveLine moveLine) {
    moveLine = moveLineRepository.find(moveLine.getId());

    if (moveLine.getIsSelectedBankReconciliation() != null) {
      moveLine.setIsSelectedBankReconciliation(!moveLine.getIsSelectedBankReconciliation());
    } else {
      moveLine.setIsSelectedBankReconciliation(true);
    }
  }

  @Override
  @Transactional
  public MoveLine removePostedNbr(MoveLine moveLine, String postedNbr) {
    String posted = moveLine.getPostedNbr();
    List<String> postedNbrs = new ArrayList<String>(Arrays.asList(posted.split(",")));
    postedNbrs.remove(postedNbr);
    posted = String.join(",", postedNbrs);
    moveLine.setPostedNbr(posted);
    return moveLine;
  }

  @Override
  public boolean checkManageCutOffDates(MoveLine moveLine) {
    return moveLine.getAccount() != null && moveLine.getAccount().getManageCutOffPeriod();
  }

  @Override
  public void applyCutOffDates(
      MoveLine moveLine, Move move, LocalDate cutOffStartDate, LocalDate cutOffEndDate) {
    if (cutOffStartDate != null && cutOffEndDate != null) {
      moveLine.setCutOffStartDate(cutOffStartDate);
      moveLine.setCutOffEndDate(cutOffEndDate);
    }
  }

  @Override
  public BigDecimal getCutOffProrataAmount(MoveLine moveLine, LocalDate moveDate) {
    if (moveDate == null
        || moveLine.getCutOffStartDate() == null
        || moveLine.getCutOffEndDate() == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal daysProrata =
        BigDecimal.valueOf(ChronoUnit.DAYS.between(moveDate, moveLine.getCutOffEndDate()));
    BigDecimal daysTotal =
        BigDecimal.valueOf(
            ChronoUnit.DAYS.between(moveLine.getCutOffStartDate(), moveLine.getCutOffEndDate()));
    if (daysTotal.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal prorata = daysProrata.divide(daysTotal, 10, RoundingMode.HALF_UP);

    return prorata.multiply(moveLine.getCurrencyAmount()).setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public boolean checkManageAnalytic(Move move) throws AxelorException {
    return move != null
        && move.getCompany() != null
        && appAccountService.getAppAccount().getManageAnalyticAccounting()
        && accountConfigService.getAccountConfig(move.getCompany()).getManageAnalyticAccounting();
  }

  @Override
  public LocalDate getFinancialDiscountDeadlineDate(MoveLine moveLine) {
    if (moveLine == null) {
      return null;
    }

    int discountDelay =
        Optional.of(moveLine)
            .map(MoveLine::getFinancialDiscount)
            .map(FinancialDiscount::getDiscountDelay)
            .orElse(0);

    LocalDate deadlineDate = moveLine.getDueDate().minusDays(discountDelay);

    return deadlineDate.isBefore(moveLine.getDate()) ? moveLine.getDate() : deadlineDate;
  }

  @Override
  public void computeFinancialDiscount(MoveLine moveLine) {
    if (!appAccountService.getAppAccount().getManageFinancialDiscount()) {
      return;
    }

    if (moveLine.getAccount() != null
        && moveLine.getAccount().getUseForPartnerBalance()
        && moveLine.getFinancialDiscount() != null) {
      FinancialDiscount financialDiscount = moveLine.getFinancialDiscount();
      BigDecimal amount = moveLine.getCredit().max(moveLine.getDebit());

      moveLine.setFinancialDiscountRate(financialDiscount.getDiscountRate());
      moveLine.setFinancialDiscountTotalAmount(
          amount.multiply(
              financialDiscount
                  .getDiscountRate()
                  .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));
      moveLine.setRemainingAmountAfterFinDiscount(
          amount.subtract(moveLine.getFinancialDiscountTotalAmount()));
    } else {
      moveLine.setFinancialDiscount(null);
      moveLine.setFinancialDiscountRate(BigDecimal.ZERO);
      moveLine.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
      moveLine.setRemainingAmountAfterFinDiscount(BigDecimal.ZERO);
    }

    this.computeInvoiceTermsFinancialDiscount(moveLine);
  }

  @Override
  public void computeInvoiceTermsFinancialDiscount(MoveLine moveLine) {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      moveLine.getInvoiceTermList().stream()
          .filter(it -> !it.getIsPaid() && it.getAmountRemaining().compareTo(it.getAmount()) == 0)
          .forEach(
              it ->
                  invoiceTermService.computeFinancialDiscount(
                      it,
                      moveLine.getCredit().max(moveLine.getDebit()),
                      moveLine.getFinancialDiscount(),
                      moveLine.getFinancialDiscountTotalAmount(),
                      moveLine.getRemainingAmountAfterFinDiscount()));
    }
  }

  public Batch validateCutOffBatch(List<Long> recordIdList, Long batchId) {
    BatchAccountingCutOff batchAccountingCutOff = Beans.get(BatchAccountingCutOff.class);

    batchAccountingCutOff.recordIdList = recordIdList;
    batchAccountingCutOff.run(Beans.get(AccountingBatchRepository.class).find(batchId));

    return batchAccountingCutOff.getBatch();
  }

  public void updatePartner(List<MoveLine> moveLineList, Partner partner, Partner previousPartner) {
    moveLineList.stream()
        .filter(it -> Objects.equals(it.getPartner(), previousPartner))
        .forEach(it -> it.setPartner(partner));
  }

  @Override
  public List<MoveLine> getReconcilableMoveLines(List<Integer> moveLineIds) {
    if (moveLineIds == null) {
      return Collections.emptyList();
    }
    return getMoveLinesFromIds(moveLineIds).stream()
        .filter(moveLineControlService::canReconcile)
        .collect(Collectors.toList());
  }

  protected List<MoveLine> getMoveLinesFromIds(List<Integer> moveLineIds) {
    if (moveLineIds == null) {
      return Collections.emptyList();
    }
    List<MoveLine> moveLineList = new ArrayList<>();
    for (Integer id : moveLineIds) {
      MoveLine moveLine = moveLineRepository.find(id.longValue());
      moveLineList.add(moveLine);
    }
    return moveLineList;
  }
}
