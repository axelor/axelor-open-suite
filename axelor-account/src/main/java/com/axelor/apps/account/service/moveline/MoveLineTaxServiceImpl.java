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

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.TaxPaymentMoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.TaxPaymentMoveLineService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineTaxServiceImpl implements MoveLineTaxService {
  protected MoveLineRepository moveLineRepository;
  protected TaxPaymentMoveLineService taxPaymentMoveLineService;
  protected AppBaseService appBaseService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveRepository moveRepository;
  protected AccountingSituationRepository accountingSituationRepository;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public MoveLineTaxServiceImpl(
      MoveLineRepository moveLineRepository,
      TaxPaymentMoveLineService taxPaymentMoveLineService,
      AppBaseService appBaseService,
      MoveLineCreateService moveLineCreateService,
      MoveRepository moveRepository,
      AccountingSituationRepository accountingSituationRepository,
      MoveLineToolService moveLineToolService) {
    this.moveLineRepository = moveLineRepository;
    this.taxPaymentMoveLineService = taxPaymentMoveLineService;
    this.appBaseService = appBaseService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveRepository = moveRepository;
    this.accountingSituationRepository = accountingSituationRepository;
    this.moveLineToolService = moveLineToolService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine generateTaxPaymentMoveLineList(
      MoveLine customerMoveLine, Invoice invoice, Reconcile reconcile) throws AxelorException {
    BigDecimal paymentAmount = reconcile.getAmount();
    BigDecimal invoiceTotalAmount = invoice.getCompanyInTaxTotal();
    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {

      TaxLine taxLine = invoiceLineTax.getTaxLine();
      BigDecimal vatRate = taxLine.getValue();
      BigDecimal baseAmount = invoiceLineTax.getCompanyExTaxBase();
      BigDecimal detailPaymentAmount =
          baseAmount
              .multiply(paymentAmount)
              .divide(invoiceTotalAmount, 6, RoundingMode.HALF_UP)
              .setScale(2, RoundingMode.HALF_UP);

      TaxPaymentMoveLine taxPaymentMoveLine =
          new TaxPaymentMoveLine(
              customerMoveLine,
              taxLine,
              reconcile,
              vatRate,
              detailPaymentAmount,
              appBaseService.getTodayDate(reconcile.getCompany()));

      taxPaymentMoveLine.setFiscalPosition(invoice.getFiscalPosition());

      taxPaymentMoveLine = taxPaymentMoveLineService.computeTaxAmount(taxPaymentMoveLine);

      customerMoveLine.addTaxPaymentMoveLineListItem(taxPaymentMoveLine);
    }
    this.computeTaxAmount(customerMoveLine);
    return moveLineRepository.save(customerMoveLine);
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
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine computeTaxAmount(MoveLine moveLine) throws AxelorException {
    moveLine.setTaxAmount(BigDecimal.ZERO);
    if (!ObjectUtils.isEmpty(moveLine.getTaxPaymentMoveLineList())) {
      for (TaxPaymentMoveLine taxPaymentMoveLine : moveLine.getTaxPaymentMoveLineList()) {
        moveLine.setTaxAmount(moveLine.getTaxAmount().add(taxPaymentMoveLine.getTaxAmount()));
      }
    }
    return moveLine;
  }

  @Override
  @Transactional
  public void autoTaxLineGenerate(Move move) throws AxelorException {

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

      if (taxLine != null) {

        String accountType = moveLine.getAccount().getAccountType().getTechnicalTypeSelect();

        if (accountType.equals(AccountTypeRepository.TYPE_DEBT)
            || accountType.equals(AccountTypeRepository.TYPE_CHARGE)
            || accountType.equals(AccountTypeRepository.TYPE_INCOME)
            || accountType.equals(AccountTypeRepository.TYPE_ASSET)) {

          moveLineCreateService.createMoveLineForAutoTax(
              move, map, newMap, moveLine, taxLine, accountType);
        }
      }
    }

    moveLineList.addAll(newMap.values());
    moveRepository.save(move);
  }

  public void checkVatSystemPreconditions(Move move, MoveLine moveline) throws AxelorException {
    if (move.getPartner() == null && moveline.getPartner() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.MOVE_PARTNER_FOR_TAX_NOT_FOUND));
    }
    Partner partner = move.getPartner() != null ? move.getPartner() : moveline.getPartner();
    AccountingSituation accountingSituation =
        accountingSituationRepository.findByCompanyAndPartner(move.getCompany(), partner);
    if (CollectionUtils.isEmpty(partner.getAccountingSituationList())
        || accountingSituation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.ACCOUNTING_SITUATION_NOT_FOUND),
          move.getCompany().getName(),
          partner.getFullName());
    }
    if (accountingSituation.getVatSystemSelect() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.ACCOUNTING_SITUATION_VAT_SYSTEM_NOT_FOUND),
          move.getCompany().getName(),
          partner.getFullName());
    }
    if (move.getCompany().getPartner() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.COMPANY_PARTNER_NOT_FOUND),
          move.getCompany().getName());
    }
    accountingSituation =
        accountingSituationRepository.findByCompanyAndPartner(
            move.getCompany(), move.getCompany().getPartner());
    if (CollectionUtils.isEmpty(move.getCompany().getPartner().getAccountingSituationList())
        || accountingSituation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.COMPANY_PARTNER_ACCOUNTING_SITUATION_NOT_FOUND),
          move.getCompany().getName(),
          partner.getFullName());
    }
    if (accountingSituation.getVatSystemSelect() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.COMPANY_PARTNER_VAT_SYSTEM_NOT_FOUND),
          move.getCompany().getName(),
          partner.getFullName());
    }
    if (moveline.getAccount().getVatSystemSelect() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.ACCOUNT_VAT_SYSTEM_NOT_FOUND),
          moveline.getAccount().getCode());
    }
  }

  @Override
  public Integer getVatSystem(Move move, MoveLine moveline) throws AxelorException {
    checkVatSystemPreconditions(move, moveline);
    Partner partner = move.getPartner() != null ? move.getPartner() : moveline.getPartner();
    AccountingSituation accountingSituation = null;
    if (move.getJournal().getJournalType().getTechnicalTypeSelect()
        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE) {
      accountingSituation =
          accountingSituationRepository.findByCompanyAndPartner(move.getCompany(), partner);
    } else if (move.getJournal().getJournalType().getTechnicalTypeSelect()
        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE) {
      accountingSituation =
          accountingSituationRepository.findByCompanyAndPartner(
              move.getCompany(), move.getCompany().getPartner());
    }
    if (accountingSituation != null) {
      if (accountingSituation.getVatSystemSelect()
          == AccountingSituationRepository.VAT_COMMON_SYSTEM) {
        return moveline.getAccount().getVatSystemSelect();
      } else if (accountingSituation.getVatSystemSelect()
          == AccountingSituationRepository.VAT_DELIVERY) {
        return MoveLineRepository.VAT_COMMON_SYSTEM;
      }
    }
    return MoveLineRepository.VAT_SYSTEM_DEFAULT;
  }

  @Override
  public void checkTaxMoveLines(Move move) throws AxelorException {
    if (CollectionUtils.isEmpty(move.getMoveLineList()) || move.getMoveLineList().size() < 2) {
      return;
    }
    for (MoveLine moveline : move.getMoveLineList()) {
      if (!move.getMoveLineList().stream()
          .filter(
              moveLineToolService.isEqualTaxMoveLine(
                  moveline.getTaxLine(), moveline.getVatSystemSelect(), moveline.getId()))
          .collect(Collectors.<MoveLine>toList())
          .isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(IExceptionMessage.SAME_TAX_MOVE_LINES));
      }
    }
  }
}
