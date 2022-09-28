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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineServiceImpl implements MoveLineService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveLineToolService moveLineToolService;
  protected MoveLineRepository moveLineRepository;
  protected InvoiceRepository invoiceRepository;
  protected PaymentService paymentService;
  protected AppBaseService appBaseService;
  protected AppAccountService appAccountService;
  protected AccountConfigService accountConfigService;

  @Inject
  public MoveLineServiceImpl(
      MoveLineRepository moveLineRepository,
      InvoiceRepository invoiceRepository,
      PaymentService paymentService,
      AppBaseService appBaseService,
      MoveLineToolService moveLineToolService,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService) {
    this.moveLineRepository = moveLineRepository;
    this.invoiceRepository = invoiceRepository;
    this.paymentService = paymentService;
    this.appBaseService = appBaseService;
    this.moveLineToolService = moveLineToolService;
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public MoveLine balanceCreditDebit(MoveLine moveLine, Move move) {
    if (move.getMoveLineList() != null) {
      BigDecimal totalCredit =
          move.getMoveLineList().stream()
              .map(it -> it.getCredit())
              .reduce((a, b) -> a.add(b))
              .orElse(BigDecimal.ZERO);
      BigDecimal totalDebit =
          move.getMoveLineList().stream()
              .map(it -> it.getDebit())
              .reduce((a, b) -> a.add(b))
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
  public void reconcileMoveLinesWithCacheManagement(List<MoveLine> moveLineList) {

    List<MoveLine> reconciliableCreditMoveLineList =
        moveLineToolService.getReconciliableCreditMoveLines(moveLineList);
    List<MoveLine> reconciliableDebitMoveLineList =
        moveLineToolService.getReconciliableDebitMoveLines(moveLineList);

    Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap = new HashMap<>();

    populateCredit(moveLineMap, reconciliableCreditMoveLineList);

    populateDebit(moveLineMap, reconciliableDebitMoveLineList);

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
        if (i % 20 == 0) {
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
    List<MoveLine> reconciliableCreditMoveLineList =
        moveLineToolService.getReconciliableCreditMoveLines(moveLineList);
    List<MoveLine> reconciliableDebitMoveLineList =
        moveLineToolService.getReconciliableDebitMoveLines(moveLineList);

    Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap = new HashMap<>();

    populateCredit(moveLineMap, reconciliableCreditMoveLineList);

    populateDebit(moveLineMap, reconciliableDebitMoveLineList);

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

  private void populateCredit(
      Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap,
      List<MoveLine> reconciliableMoveLineList) {
    populateMoveLineMap(moveLineMap, reconciliableMoveLineList, true);
  }

  private void populateDebit(
      Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap,
      List<MoveLine> reconciliableMoveLineList) {
    populateMoveLineMap(moveLineMap, reconciliableMoveLineList, false);
  }

  private void populateMoveLineMap(
      Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap,
      List<MoveLine> reconciliableMoveLineList,
      boolean isCredit) {
    for (MoveLine moveLine : reconciliableMoveLineList) {

      Move move = moveLine.getMove();

      List<Object> keys = new ArrayList<Object>();

      keys.add(move.getCompany());
      keys.add(moveLine.getAccount());
      keys.add(moveLine.getPartner());

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
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine setIsSelectedBankReconciliation(MoveLine moveLine) {
    if (moveLine.getIsSelectedBankReconciliation() != null) {
      moveLine.setIsSelectedBankReconciliation(!moveLine.getIsSelectedBankReconciliation());
    } else {
      moveLine.setIsSelectedBankReconciliation(true);
    }
    return moveLineRepository.save(moveLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine removePostedNbr(MoveLine moveLine, String postedNbr) {
    String posted = moveLine.getPostedNbr();
    List<String> postedNbrs = new ArrayList<String>(Arrays.asList(posted.split(",")));
    postedNbrs.remove(postedNbr);
    posted = String.join(",", postedNbrs);
    moveLine.setPostedNbr(posted);
    return moveLine;
  }

  @Override
  public boolean checkManageAnalytic(Move move) throws AxelorException {
    return move != null
        && move.getCompany() != null
        && appAccountService.getAppAccount().getManageAnalyticAccounting()
        && accountConfigService.getAccountConfig(move.getCompany()).getManageAnalyticAccounting();
  }
}
