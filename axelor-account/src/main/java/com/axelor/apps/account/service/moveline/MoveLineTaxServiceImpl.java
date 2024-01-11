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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.TaxPaymentMoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.TaxPaymentMoveLineService;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class MoveLineTaxServiceImpl implements MoveLineTaxService {
  protected MoveLineRepository moveLineRepository;
  protected TaxPaymentMoveLineService taxPaymentMoveLineService;
  protected AppBaseService appBaseService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveRepository moveRepository;
  protected TaxAccountToolService taxAccountToolService;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public MoveLineTaxServiceImpl(
      MoveLineRepository moveLineRepository,
      TaxPaymentMoveLineService taxPaymentMoveLineService,
      AppBaseService appBaseService,
      MoveLineCreateService moveLineCreateService,
      MoveRepository moveRepository,
      TaxAccountToolService taxAccountToolService,
      MoveLineToolService moveLineToolService) {
    this.moveLineRepository = moveLineRepository;
    this.taxPaymentMoveLineService = taxPaymentMoveLineService;
    this.appBaseService = appBaseService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveRepository = moveRepository;
    this.taxAccountToolService = taxAccountToolService;
    this.moveLineToolService = moveLineToolService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine generateTaxPaymentMoveLineList(
      MoveLine customerPaymentMoveLine, MoveLine invoiceCustomerMoveLine, Reconcile reconcile)
      throws AxelorException {
    Move invoiceMove = invoiceCustomerMoveLine.getMove();
    BigDecimal paymentAmount = reconcile.getAmount();
    BigDecimal invoiceTotalAmount =
        invoiceCustomerMoveLine.getCredit().add(invoiceCustomerMoveLine.getDebit());
    for (MoveLine invoiceMoveLine : invoiceMove.getMoveLineList()) {
      if (AccountTypeRepository.TYPE_TAX.equals(
          invoiceMoveLine.getAccount().getAccountType().getTechnicalTypeSelect())) {
        customerPaymentMoveLine.addTaxPaymentMoveLineListItem(
            this.generateTaxPaymentMoveLine(
                customerPaymentMoveLine,
                invoiceMove,
                invoiceMoveLine,
                invoiceCustomerMoveLine,
                reconcile,
                paymentAmount,
                invoiceTotalAmount,
                invoiceMoveLine.getVatSystemSelect()));

      } else if (!AccountTypeRepository.TYPE_TAX.equals(
              invoiceMoveLine.getAccount().getAccountType().getTechnicalTypeSelect())
          && invoiceMoveLine.getTaxLine() != null
          && invoiceMoveLine.getTaxLine().getValue().compareTo(BigDecimal.ZERO) == 0) {

        customerPaymentMoveLine.addTaxPaymentMoveLineListItem(
            this.generateTaxPaymentMoveLine(
                customerPaymentMoveLine,
                invoiceMove,
                invoiceMoveLine,
                invoiceCustomerMoveLine,
                reconcile,
                paymentAmount,
                invoiceTotalAmount,
                invoiceMoveLine.getAccount().getVatSystemSelect()));
      }
    }
    this.computeTaxAmount(customerPaymentMoveLine);
    return moveLineRepository.save(customerPaymentMoveLine);
  }

  protected TaxPaymentMoveLine generateTaxPaymentMoveLine(
      MoveLine customerPaymentMoveLine,
      Move invoiceMove,
      MoveLine invoiceMoveLine,
      MoveLine invoiceCustomerMoveLine,
      Reconcile reconcile,
      BigDecimal paymentAmount,
      BigDecimal invoiceTotalAmount,
      int vatSystemSelect)
      throws AxelorException {

    TaxLine taxLine = invoiceMoveLine.getTaxLine();
    BigDecimal vatRate = taxLine != null ? taxLine.getValue() : BigDecimal.ZERO;

    BigDecimal baseAmount = BigDecimal.ZERO;
    if (BigDecimal.ZERO.compareTo(vatRate) != 0) {
      baseAmount =
          (invoiceMoveLine.getCredit().add(invoiceMoveLine.getDebit()))
              .divide(vatRate.divide(new BigDecimal(100)), 2, BigDecimal.ROUND_HALF_UP);
    } else {
      baseAmount = invoiceMoveLine.getCredit().add(invoiceMoveLine.getDebit());
    }

    BigDecimal detailPaymentAmount =
        baseAmount
            .multiply(paymentAmount)
            .divide(invoiceTotalAmount, 6, RoundingMode.HALF_UP)
            .setScale(2, RoundingMode.HALF_UP);

    TaxPaymentMoveLine taxPaymentMoveLine =
        new TaxPaymentMoveLine(
            customerPaymentMoveLine,
            taxLine,
            reconcile,
            vatRate,
            detailPaymentAmount,
            appBaseService.getTodayDate(reconcile.getCompany()));

    taxPaymentMoveLine.setFiscalPosition(invoiceMove.getFiscalPosition());

    taxPaymentMoveLine = taxPaymentMoveLineService.computeTaxAmount(taxPaymentMoveLine);

    taxPaymentMoveLine.setVatSystemSelect(vatSystemSelect);

    taxPaymentMoveLine.setFunctionalOriginSelect(
        invoiceCustomerMoveLine.getMove().getFunctionalOriginSelect());

    return taxPaymentMoveLine;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine reverseTaxPaymentMoveLines(MoveLine customerMoveLine, Reconcile reconcile)
      throws AxelorException {
    List<TaxPaymentMoveLine> reverseTaxPaymentMoveLines = new ArrayList<TaxPaymentMoveLine>();
    for (TaxPaymentMoveLine taxPaymentMoveLine : customerMoveLine.getTaxPaymentMoveLineList()) {
      if (!taxPaymentMoveLine.getIsAlreadyReverse()
          && taxPaymentMoveLine.getReconcile().equals(reconcile)) {
        TaxPaymentMoveLine reverseTaxPaymentMoveLine =
            taxPaymentMoveLineService.getReverseTaxPaymentMoveLine(taxPaymentMoveLine);

        reverseTaxPaymentMoveLines.add(reverseTaxPaymentMoveLine);
      }
    }
    for (TaxPaymentMoveLine reverseTaxPaymentMoveLine : reverseTaxPaymentMoveLines) {
      customerMoveLine.addTaxPaymentMoveLineListItem(reverseTaxPaymentMoveLine);
    }
    this.computeTaxAmount(customerMoveLine);
    return moveLineRepository.save(customerMoveLine);
  }

  @Override
  @Transactional
  public MoveLine computeTaxAmount(MoveLine moveLine) {
    moveLine.setTaxAmount(BigDecimal.ZERO);
    if (!ObjectUtils.isEmpty(moveLine.getTaxPaymentMoveLineList())) {
      for (TaxPaymentMoveLine taxPaymentMoveLine : moveLine.getTaxPaymentMoveLineList()) {
        moveLine.setTaxAmount(moveLine.getTaxAmount().add(taxPaymentMoveLine.getTaxAmount()));
      }
    }
    return moveLine;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void autoTaxLineGenerate(Move move, Account account, boolean percentMoveTemplate)
      throws AxelorException {

    autoTaxLineGenerateNoSave(move, account, percentMoveTemplate);
    moveRepository.save(move);
  }

  @Override
  public void autoTaxLineGenerateNoSave(Move move) throws AxelorException {
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())
        && (move.getStatusSelect().equals(MoveRepository.STATUS_NEW)
            || move.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED)
            || move.getStatusSelect().equals(MoveRepository.STATUS_DAYBOOK))) {
      this.autoTaxLineGenerateNoSave(move, null, false);
    }
  }

  @Override
  public void autoTaxLineGenerateNoSave(Move move, Account account, boolean percentMoveTemplate)
      throws AxelorException {

    List<MoveLine> moveLineList = move.getMoveLineList();

    moveLineList.sort(
        new Comparator<MoveLine>() {
          @Override
          public int compare(MoveLine o1, MoveLine o2) {
            if (o2.getSourceTaxLine() != null) {
              return 0;
            }
            return -1;
          }
        });

    Iterator<MoveLine> moveLineItr = moveLineList.iterator();

    Map<String, MoveLine> map = new HashMap<>();
    Map<String, MoveLine> newMap = new HashMap<>();
    while (moveLineItr.hasNext()) {

      MoveLine moveLine = moveLineItr.next();

      TaxLine taxLine = moveLine.getTaxLine();
      TaxLine sourceTaxLine = moveLine.getSourceTaxLine();
      if (sourceTaxLine != null) {

        String sourceTaxLineKey = moveLine.getAccount().getCode() + sourceTaxLine.getId();

        moveLine.setCredit(BigDecimal.ZERO);
        moveLine.setDebit(BigDecimal.ZERO);
        map.put(sourceTaxLineKey, moveLine);
        moveLineItr.remove();
        continue;
      }
      if (taxLine != null && taxLine.getValue().signum() != 0) {

        String accountType = moveLine.getAccount().getAccountType().getTechnicalTypeSelect();

        if (this.isGenerateMoveLineForAutoTax(accountType)) {

          moveLineCreateService.createMoveLineForAutoTax(
              move, map, newMap, moveLine, taxLine, accountType, account, percentMoveTemplate);
        }
      }
    }

    moveLineList.addAll(newMap.values());
  }

  protected boolean isGenerateMoveLineForAutoTax(String accountType) {
    return accountType.equals(AccountTypeRepository.TYPE_DEBT)
        || accountType.equals(AccountTypeRepository.TYPE_CHARGE)
        || accountType.equals(AccountTypeRepository.TYPE_INCOME)
        || accountType.equals(AccountTypeRepository.TYPE_IMMOBILISATION);
  }

  @Override
  public int getVatSystem(Move move, MoveLine moveline) throws AxelorException {
    Partner partner = move.getPartner() != null ? move.getPartner() : moveline.getPartner();
    return taxAccountToolService.calculateVatSystem(
        partner,
        move.getCompany(),
        moveline.getAccount(),
        (move.getJournal().getJournalType().getTechnicalTypeSelect()
            == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE),
        (move.getJournal().getJournalType().getTechnicalTypeSelect()
            == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE));
  }

  @Override
  public void checkDuplicateTaxMoveLines(Move move) throws AxelorException {
    if (CollectionUtils.isEmpty(move.getMoveLineList()) || move.getMoveLineList().size() < 2) {
      return;
    }
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (this.isMoveLineTaxAccount(moveLine) && this.isDuplicateTaxMoveLine(move, moveLine)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(AccountExceptionMessage.SAME_TAX_MOVE_LINES));
      }
    }
  }

  protected boolean isMoveLineTaxAccount(MoveLine moveLine) {
    return moveLine.getAccount() != null
        && moveLine.getAccount().getAccountType() != null
        && AccountTypeRepository.TYPE_TAX.equals(
            moveLine.getAccount().getAccountType().getTechnicalTypeSelect());
  }

  protected boolean isDuplicateTaxMoveLine(Move move, MoveLine moveLine) {
    return move.getMoveLineList().stream()
        .anyMatch(
            ml ->
                moveLineToolService.isEqualTaxMoveLine(
                    moveLine.getAccount(),
                    moveLine.getTaxLine(),
                    moveLine.getVatSystemSelect(),
                    moveLine.getId(),
                    ml));
  }

  @Override
  public void checkEmptyTaxLines(List<MoveLine> moveLineList) throws AxelorException {
    List<Long> moveLineWithoutTaxList = this.getMoveLinesWithoutTax(moveLineList);

    if (ObjectUtils.notEmpty(moveLineWithoutTaxList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(
              I18n.get(AccountExceptionMessage.MOVE_LINE_TAX_LINE_MISSING),
              moveLineWithoutTaxList));
    }
  }

  protected List<Long> getMoveLinesWithoutTax(List<MoveLine> moveLineList) {
    List<Long> moveLineWithoutTaxList = new ArrayList<>();

    if (CollectionUtils.isEmpty(moveLineList)) {
      return null;
    }

    for (MoveLine moveLine : moveLineList) {
      if (moveLine.getMove() != null
          && this.isMoveLineTaxAccountRequired(
              moveLine, moveLine.getMove().getFunctionalOriginSelect())
          && moveLine.getTaxLine() == null) {
        moveLineWithoutTaxList.add(moveLine.getId());
      }
    }
    return moveLineWithoutTaxList;
  }

  @Override
  public boolean isMoveLineTaxAccountRequired(MoveLine moveLine, int functionalOriginSelect) {
    return this.isMoveLineTaxAccount(moveLine)
        && Lists.newArrayList(
                MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE, MoveRepository.FUNCTIONAL_ORIGIN_SALE)
            .contains(functionalOriginSelect);
  }
}
