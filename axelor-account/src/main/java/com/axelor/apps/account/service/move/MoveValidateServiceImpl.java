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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticJournalRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.PeriodServiceAccount;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection.Option;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class MoveValidateServiceImpl implements MoveValidateService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected int jpaLimit = 20;
  protected MoveLineControlService moveLineControlService;
  protected MoveLineToolService moveLineToolService;
  protected AccountConfigService accountConfigService;
  protected MoveSequenceService moveSequenceService;
  protected MoveCustAccountService moveCustAccountService;
  protected MoveToolService moveToolService;
  protected MoveInvoiceTermService moveInvoiceTermService;
  protected MoveRepository moveRepository;
  protected AccountRepository accountRepository;
  protected PartnerRepository partnerRepository;
  protected AppBaseService appBaseService;
  protected AppAccountService appAccountService;
  protected FixedAssetGenerationService fixedAssetGenerationService;
  protected MoveLineTaxService moveLineTaxService;
  protected PeriodServiceAccount periodServiceAccount;
  protected MoveControlService moveControlService;
  protected CompanyConfigService companyConfigService;

  @Inject
  public MoveValidateServiceImpl(
      MoveLineControlService moveLineControlService,
      MoveLineToolService moveLineToolService,
      AccountConfigService accountConfigService,
      MoveSequenceService moveSequenceService,
      MoveCustAccountService moveCustAccountService,
      MoveToolService moveToolService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveRepository moveRepository,
      AccountRepository accountRepository,
      PartnerRepository partnerRepository,
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      FixedAssetGenerationService fixedAssetGenerationService,
      MoveLineTaxService moveLineTaxService,
      PeriodServiceAccount periodServiceAccount,
      MoveControlService moveControlService,
      CompanyConfigService companyConfigService) {
    this.moveLineControlService = moveLineControlService;
    this.moveLineToolService = moveLineToolService;
    this.accountConfigService = accountConfigService;
    this.moveSequenceService = moveSequenceService;
    this.moveCustAccountService = moveCustAccountService;
    this.moveToolService = moveToolService;
    this.moveInvoiceTermService = moveInvoiceTermService;
    this.moveRepository = moveRepository;
    this.accountRepository = accountRepository;
    this.partnerRepository = partnerRepository;
    this.appBaseService = appBaseService;
    this.appAccountService = appAccountService;
    this.fixedAssetGenerationService = fixedAssetGenerationService;
    this.moveLineTaxService = moveLineTaxService;
    this.periodServiceAccount = periodServiceAccount;
    this.moveControlService = moveControlService;
    this.companyConfigService = companyConfigService;
  }

  /**
   * In move lines, fill the dates field and the partner if they are missing, and fill the counter.
   *
   * @param move
   */
  @Override
  public void completeMoveLines(Move move) {
    LocalDate date = move.getDate();
    Partner partner = move.getPartner();

    int counter = 1;
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getDate() == null) {
        moveLine.setDate(date);
      }

      if (moveLine.getAccount() != null
          && moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getDueDate() == null) {
        moveLine.setDueDate(date);
      }

      if (moveLine.getOriginDate() == null) {
        if (ObjectUtils.notEmpty(move.getOriginDate())) {
          moveLine.setOriginDate(move.getOriginDate());
        } else {
          moveLine.setOriginDate(date);
        }
      }
      moveLine.setCounter(counter);
      counter++;
    }
  }

  @Override
  public void checkPreconditions(Move move) throws AxelorException {
    checkPeriodPreconditions(move);
    checkConsistencyPreconditions(move);
  }

  @Override
  public void checkConsistencyPreconditions(Move move) throws AxelorException {
    Journal journal = move.getJournal();
    Company company = move.getCompany();

    if (company == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_3),
          move.getReference());
    }

    if (journal == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_2),
          move.getReference());
    }

    if (move.getMoveLineList() == null || move.getMoveLineList().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.MOVE_8),
          move.getReference());
    }

    if (move.getCurrency() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_12),
          move.getReference());
    }

    if (appAccountService.getAppAccount().getManageCutOffPeriod()
        && move.getFunctionalOriginSelect() != MoveRepository.FUNCTIONAL_ORIGIN_CUT_OFF
        && !moveToolService.checkMoveLinesCutOffDates(move)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.MOVE_MISSING_CUT_OFF_DATE));
    }

    if (move.getPaymentCondition() != null
        && CollectionUtils.isNotEmpty(move.getPaymentCondition().getPaymentConditionLineList())
        && move.getPaymentCondition().getPaymentConditionLineList().size() > 1
        && !appAccountService.getAppAccount().getAllowMultiInvoiceTerms()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_MULTIPLE_LINES_NO_MULTI));
    }

    moveControlService.checkSameCompany(move);

    if (move.getMoveLineList().stream()
        .allMatch(
            moveLine ->
                moveLine.getDebit().add(moveLine.getCredit()).compareTo(BigDecimal.ZERO) == 0)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.MOVE_8),
          move.getReference());
    }

    checkInactiveAnalyticJournal(move);
    checkInactiveAccount(move);
    checkInactiveAnalyticAccount(move);
    checkInactiveJournal(move);
    checkFunctionalOriginSelect(move);

    validateVatSystem(move);

    Integer functionalOriginSelect = move.getFunctionalOriginSelect();
    if (functionalOriginSelect != MoveRepository.FUNCTIONAL_ORIGIN_CLOSURE
        && functionalOriginSelect != MoveRepository.FUNCTIONAL_ORIGIN_OPENING) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        Account account = moveLine.getAccount();
        if (account.getIsTaxAuthorizedOnMoveLine()
            && account.getIsTaxRequiredOnMoveLine()
            && moveLine.getTaxLine() == null
            && functionalOriginSelect != MoveRepository.FUNCTIONAL_ORIGIN_CUT_OFF) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(AccountExceptionMessage.MOVE_9),
              account.getCode(),
              account.getName(),
              moveLine.getName());
        }

        if (moveLine.getAnalyticDistributionTemplate() == null
            && ObjectUtils.isEmpty(moveLine.getAnalyticMoveLineList())
            && account.getAnalyticDistributionAuthorized()
            && account.getAnalyticDistributionRequiredOnMoveLines()) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(AccountExceptionMessage.MOVE_10),
              account.getName(),
              moveLine.getName());
        }

        if (account != null
            && !account.getAnalyticDistributionAuthorized()
            && (moveLine.getAnalyticDistributionTemplate() != null
                || (moveLine.getAnalyticMoveLineList() != null
                    && !moveLine.getAnalyticMoveLineList().isEmpty()))) {
          throw new AxelorException(
              move,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MOVE_11),
              moveLine.getName());
        }

        moveLineControlService.validateMoveLine(moveLine);
        moveLineControlService.checkAccountCompany(moveLine);
        moveLineControlService.checkJournalCompany(moveLine);

        moveLineToolService.checkDateInPeriod(move, moveLine);
      }

      moveLineTaxService.checkDuplicateTaxMoveLines(move);
      moveLineTaxService.checkEmptyTaxLines(move.getMoveLineList());
      this.validateWellBalancedMove(move);
      this.checkMoveLineInvoiceTermBalance(move);
      this.checkMoveLineDescription(move);

      if (move.getJournal() != null
          && move.getPartner() != null
          && move.getJournal().getHasDuplicateDetectionOnOrigin()) {
        List<Move> moveList = moveToolService.getMovesWithDuplicatedOrigin(move);
        if (ObjectUtils.notEmpty(moveList)) {
          throw new AxelorException(
              move,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MOVE_DUPLICATE_ORIGIN_BLOCKING_MESSAGE),
              moveList.stream().map(Move::getReference).collect(Collectors.joining(",")),
              move.getPartner().getFullName(),
              move.getPeriod().getYear().getName());
        }
      }
    }
  }

  @Override
  public void checkPeriodPreconditions(Move move) throws AxelorException {
    if (move.getPeriod() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_4),
          move.getReference());
    }
    if (!CollectionUtils.isEmpty(move.getPeriod().getClosedJournalSet())
        && move.getPeriod().getClosedJournalSet().contains(move.getJournal())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(AccountExceptionMessage.MOVE_13),
              move.getJournal().getCode(),
              move.getPeriod().getCode()));
    }

    checkClosurePeriod(move);
  }

  protected void checkMoveLineInvoiceTermBalance(Move move) throws AxelorException {

    log.debug(
        "Well-balanced move line invoice terms validation on account move {}", move.getReference());

    for (MoveLine moveLine : move.getMoveLineList()) {
      if (CollectionUtils.isEmpty(moveLine.getInvoiceTermList())
          || !moveLine.getAccount().getUseForPartnerBalance()) {
        return;
      }
      BigDecimal totalMoveLineInvoiceTerm =
          moveLine.getInvoiceTermList().stream()
              .map(InvoiceTerm::getCompanyAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      if (totalMoveLineInvoiceTerm.compareTo(moveLine.getDebit().max(moveLine.getCredit())) != 0) {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MOVE_LINE_INVOICE_TERM_SUM_COMPANY_AMOUNT),
            moveLine.getName());
      }
    }
  }

  protected void checkFunctionalOriginSelect(Move move) throws AxelorException {
    Integer functionalOriginSelect = move.getFunctionalOriginSelect();
    if (functionalOriginSelect == null || functionalOriginSelect == 0) {
      return;
    }
    Journal journal = move.getJournal();
    String authorizedFunctionalOriginSelect = journal.getAuthorizedFunctionalOriginSelect();
    if (authorizedFunctionalOriginSelect != null
        && !(Splitter.on(",")
            .trimResults()
            .splitToList(authorizedFunctionalOriginSelect)
            .contains(functionalOriginSelect.toString()))) {

      Option selectionItem =
          MetaStore.getSelectionItem(
              "iaccount.move.functional.origin.select", functionalOriginSelect.toString());
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_14),
          selectionItem.getLocalizedTitle(),
          move.getReference(),
          journal.getName(),
          journal.getCode());
    }
  }

  protected void checkClosurePeriod(Move move) throws AxelorException {

    if (!periodServiceAccount.isAuthorizedToAccountOnPeriod(move, AuthUtils.getUser())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_PERIOD_IS_CLOSED));
    }
  }

  /**
   * Comptabiliser une écriture comptable.
   *
   * @param move
   * @throws AxelorException
   */
  @Override
  public void accounting(Move move) throws AxelorException {

    this.accounting(move, true);
  }

  /**
   * Comptabiliser une écriture comptable.
   *
   * @param move
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void accounting(Move move, boolean updateCustomerAccount) throws AxelorException {

    log.debug("Accounting of the move {}", move.getReference());

    this.checkPreconditions(move);

    log.debug("Precondition check of move {} OK", move.getReference());
    boolean dayBookMode =
        accountConfigService.getAccountConfig(move.getCompany()).getAccountingDaybook()
            && move.getJournal().getAllowAccountingDaybook();

    if (move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_CLOSED
        && !move.getAutoYearClosureMove()) {
      if (dayBookMode
          && (move.getStatusSelect() == MoveRepository.STATUS_NEW
              || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_DAYBOOK_FISCAL_PERIOD_CLOSED));
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_ACCOUNTING_FISCAL_PERIOD_CLOSED));
      }
    }

    if (!dayBookMode || move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK) {
      moveSequenceService.setSequence(move);
    }

    if (move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_ADJUSTING) {
      move.setAdjustingMove(true);
    }

    moveInvoiceTermService.generateInvoiceTerms(move);
    moveInvoiceTermService.updateMoveLineDueDates(move);

    this.completeMoveLines(move);
    this.setMoveLineAccountingDate(move, dayBookMode);
    this.freezeFieldsOnMoveLines(move);
    this.updateValidateStatus(move, dayBookMode);

    if (move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED) {
      this.generateFixedAssetMoveLine(move);
    }

    moveRepository.save(move);

    if (updateCustomerAccount) {
      moveCustAccountService.updateCustomerAccount(move);
    }
  }

  protected void setMoveLineAccountingDate(Move move, boolean daybook) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK || !daybook) {
        moveLine.setAccountingDate(appBaseService.getTodayDate(move.getCompany()));
      }
    }
  }

  /**
   * This method may generate fixed asset for each moveLine of move. It will generate if
   * moveLine.fixedAssetCategory != null AND moveLine.account.accountType.technicalTypeSelect =
   * 'immobilisation'
   *
   * @param move
   * @throws AxelorException
   * @throws NullPointerException if move is null or if a line does not have an account
   */
  @Override
  public void generateFixedAssetMoveLine(Move move) throws AxelorException {
    log.debug("Starting generation of fixed assets for move " + move);
    Objects.requireNonNull(move);

    List<MoveLine> moveLineList = move.getMoveLineList();
    if (moveLineList != null) {
      for (MoveLine line : moveLineList) {
        if (line.getFixedAssetCategory() != null
            && line.getAccount()
                .getAccountType()
                .getTechnicalTypeSelect()
                .equals(AccountTypeRepository.TYPE_IMMOBILISATION)) {
          fixedAssetGenerationService.generateAndSaveFixedAsset(move, line);
        }
      }
    }
  }

  /**
   * Procédure permettant de vérifier qu'une écriture est équilibré, et la validé si c'est le cas
   *
   * @param move Une écriture
   * @throws AxelorException
   */
  @Override
  public void validateWellBalancedMove(Move move) throws AxelorException {

    log.debug("Well-balanced validation on account move {}", move.getReference());

    if (move.getMoveLineList() != null) {

      BigDecimal totalDebit = BigDecimal.ZERO;
      BigDecimal totalCredit = BigDecimal.ZERO;

      for (MoveLine moveLine : move.getMoveLineList()) {

        if (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0
            && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
          throw new AxelorException(
              move,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(AccountExceptionMessage.MOVE_6),
              moveLine.getName());
        }

        totalDebit = totalDebit.add(moveLine.getDebit());
        totalCredit = totalCredit.add(moveLine.getCredit());
      }

      if (totalDebit.compareTo(totalCredit) != 0) {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MOVE_7),
            move.getReference(),
            totalDebit,
            totalCredit);
      }
    }
  }

  @Override
  public void updateValidateStatus(Move move, boolean daybook) throws AxelorException {
    if (move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK || !daybook) {
      move.setStatusSelect(MoveRepository.STATUS_ACCOUNTED);
      move.setAccountingDate(appBaseService.getTodayDate(move.getCompany()));
    } else {
      move.setStatusSelect(MoveRepository.STATUS_DAYBOOK);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateInDayBookMode(Move move) throws AxelorException {

    this.checkPreconditions(move);

    Set<Partner> partnerSet = new HashSet<>();

    partnerSet.addAll(this.getPartnerOfMoveBeforeUpdate(move));
    partnerSet.addAll(moveCustAccountService.getPartnerOfMove(move));

    List<Partner> partnerList = new ArrayList<>();
    partnerList.addAll(partnerSet);

    this.freezeFieldsOnMoveLines(move);
    moveRepository.save(move);

    moveCustAccountService.updateCustomerAccount(partnerList, move.getCompany());
  }

  /**
   * Get the distinct partners of an account move that impact the partner balances
   *
   * @param move
   * @return A list of partner
   */
  @Override
  public List<Partner> getPartnerOfMoveBeforeUpdate(Move move) {
    List<Partner> partnerList = new ArrayList<Partner>();
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccountId() != null) {
        Account account = accountRepository.find(moveLine.getAccountId());
        if (account != null
            && account.getUseForPartnerBalance()
            && moveLine.getPartnerId() != null) {
          Partner partner = partnerRepository.find(moveLine.getPartnerId());
          if (partner != null && !partnerList.contains(partner)) {
            partnerList.add(partner);
          }
        }
      }
    }
    return partnerList;
  }

  /**
   * Method that freeze the account and partner fields on move lines
   *
   * @param move
   */
  @Override
  public void freezeFieldsOnMoveLines(Move move) throws AxelorException {
    for (MoveLine moveLine : move.getMoveLineList()) {

      Account account = moveLine.getAccount();

      moveLine.setAccountId(account.getId());
      moveLine.setAccountCode(account.getCode());
      moveLine.setAccountName(account.getName());

      Partner partner = moveLine.getPartner();

      if (partner != null) {
        moveLine.setPartnerId(partner.getId());
        moveLine.setPartnerFullName(partner.getSimpleFullName());
        moveLine.setPartnerSeq(partner.getPartnerSeq());
      }
      if (moveLine.getTaxLine() != null) {
        moveLine.setTaxRate(moveLine.getTaxLine().getValue());
        moveLine.setTaxCode(moveLine.getTaxLine().getTax().getCode());
      }

      setMoveLineFixedInformation(move, moveLine);
    }
  }

  protected void setMoveLineFixedInformation(Move move, MoveLine moveLine) throws AxelorException {
    Company company = move.getCompany();
    Journal journal = move.getJournal();
    moveLine.setCompanyCode(company.getCode());
    moveLine.setCompanyName(company.getName());
    moveLine.setJournalCode(journal.getCode());
    moveLine.setJournalName(journal.getName());
    moveLine.setFiscalYearCode(move.getPeriod().getYear().getCode());
    moveLine.setCurrencyCode(move.getCurrencyCode());
    moveLine.setCompanyCurrencyCode(companyConfigService.getCompanyCurrency(company).getCode());
    moveLine.setAdjustingMove(move.getAdjustingMove());
  }

  @Override
  public String accountingMultiple(List<Integer> moveIds) {
    String errors = "";
    if (moveIds == null) {
      return errors;
    }
    User user = AuthUtils.getUser();
    int i = 0;
    for (Integer moveId : moveIds) {
      Move move = moveRepository.find(moveId.longValue());
      try {
        if (!periodServiceAccount.isAuthorizedToAccountOnPeriod(move, user)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              String.format(
                  I18n.get(AccountExceptionMessage.ACCOUNT_PERIOD_TEMPORARILY_CLOSED),
                  move.getReference()));
        }
        if (move.getStatusSelect() != MoveRepository.STATUS_ACCOUNTED
            && move.getStatusSelect() != MoveRepository.STATUS_CANCELED) {
          accounting(move);
        }
      } catch (Exception e) {
        TraceBackService.trace(e);
        if (errors.length() > 0) {
          errors = errors.concat(", ");
        }
        errors = errors.concat(move.getReference());
      } finally {
        if (++i % jpaLimit == 0) {
          JPA.clear();
        }
      }
    }

    return errors;
  }

  public void accountingMultiple(Query<Move> moveListQuery) throws AxelorException {
    Move move;

    while (!((move = moveListQuery.fetchOne()) == null)) {
      accounting(move);
      JPA.clear();
    }
  }

  protected void checkInactiveAnalyticAccount(Move move) throws AxelorException {
    if (move != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      List<String> inactiveList =
          move.getMoveLineList().stream()
              .map(MoveLine::getAnalyticMoveLineList)
              .filter(Objects::nonNull)
              .flatMap(Collection::stream)
              .map(AnalyticMoveLine::getAnalyticAccount)
              .filter(
                  analyticAccount ->
                      analyticAccount.getStatusSelect() != null
                          && analyticAccount.getStatusSelect()
                              != AnalyticAccountRepository.STATUS_ACTIVE)
              .distinct()
              .map(AnalyticAccount::getCode)
              .collect(Collectors.toList());
      if (inactiveList.size() == 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.INACTIVE_ANALYTIC_ACCOUNT_FOUND),
            inactiveList.get(0));
      } else if (inactiveList.size() > 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.INACTIVE_ANALYTIC_ACCOUNTS_FOUND),
            inactiveList.stream().collect(Collectors.joining(", ")));
      }
    }
  }

  protected void checkInactiveAnalyticJournal(Move move) throws AxelorException {
    if (move != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      List<String> inactiveList =
          move.getMoveLineList().stream()
              .map(MoveLine::getAnalyticMoveLineList)
              .filter(Objects::nonNull)
              .flatMap(Collection::stream)
              .map(AnalyticMoveLine::getAnalyticJournal)
              .filter(
                  analyticJournal ->
                      analyticJournal.getStatusSelect() != null
                          && analyticJournal.getStatusSelect()
                              != AnalyticJournalRepository.STATUS_ACTIVE)
              .distinct()
              .map(AnalyticJournal::getName)
              .collect(Collectors.toList());
      if (inactiveList.size() == 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.INACTIVE_ANALYTIC_JOURNAL_FOUND),
            inactiveList.get(0));
      } else if (inactiveList.size() > 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.INACTIVE_ANALYTIC_JOURNALS_FOUND),
            inactiveList.stream().collect(Collectors.joining(", ")));
      }
    }
  }

  protected void checkInactiveAccount(Move move) throws AxelorException {
    if (move != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      List<String> inactiveList =
          move.getMoveLineList().stream()
              .map(MoveLine::getAccount)
              .filter(Objects::nonNull)
              .filter(
                  account ->
                      account.getStatusSelect() != null
                          && account.getStatusSelect() != AccountRepository.STATUS_ACTIVE)
              .distinct()
              .map(Account::getCode)
              .collect(Collectors.toList());
      if (inactiveList.size() == 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.INACTIVE_ACCOUNT_FOUND),
            inactiveList.get(0));
      } else if (inactiveList.size() > 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.INACTIVE_ACCOUNTS_FOUND),
            inactiveList.stream().collect(Collectors.joining(", ")));
      }
    }
  }

  protected void checkInactiveJournal(Move move) throws AxelorException {
    if (move.getJournal() != null
        && move.getJournal().getStatusSelect() != JournalRepository.STATUS_ACTIVE) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.INACTIVE_JOURNAL_FOUND),
          move.getJournal().getName());
    }
  }

  protected void validateVatSystem(Move move) throws AxelorException {
    if (!CollectionUtils.isEmpty(move.getMoveLineList())) {
      if ((move.getJournal().getJournalType() != null
              && (move.getJournal().getJournalType().getTechnicalTypeSelect()
                      == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE
                  || move.getJournal().getJournalType().getTechnicalTypeSelect()
                      == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE))
          && isConfiguredVatSystem(move)
          && isConfigurationIssueOnVatSystem(move)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.TAX_MOVELINE_VAT_SYSTEM_DEFAULT),
            move.getReference());
      }
    }
  }

  protected boolean isConfiguredVatSystem(Move move) {
    for (MoveLine moveline : move.getMoveLineList()) {
      if (moveline.getTaxLine() != null
          && moveline.getAccount() != null
          && moveline.getAccount().getAccountType() != null
          && !AccountTypeRepository.TYPE_TAX.equals(
              moveline.getAccount().getAccountType().getTechnicalTypeSelect())
          && moveline.getAccount().getIsTaxAuthorizedOnMoveLine()
          && moveline.getAccount().getVatSystemSelect() != null
          && moveline.getAccount().getVatSystemSelect() != AccountRepository.VAT_SYSTEM_DEFAULT) {
        return true;
      }
    }
    return false;
  }

  protected boolean isConfigurationIssueOnVatSystem(Move move) {
    for (MoveLine moveline : move.getMoveLineList()) {
      if (moveline.getAccount() != null
          && moveline.getAccount().getAccountType() != null
          && AccountTypeRepository.TYPE_TAX.equals(
              moveline.getAccount().getAccountType().getTechnicalTypeSelect())
          && moveline.getVatSystemSelect() == MoveLineRepository.VAT_SYSTEM_DEFAULT) {
        return true;
      }
    }
    return false;
  }

  public void checkMoveLinesPartner(Move move) throws AxelorException {
    if (CollectionUtils.isEmpty(move.getMoveLineList())) {
      return;
    }
    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLineControlService.checkPartner(moveLine);
    }
  }

  protected void checkMoveLineDescription(Move move) throws AxelorException {
    if (ObjectUtils.notEmpty(move.getMoveLineList())
        && accountConfigService.getAccountConfig(move.getCompany()).getIsDescriptionRequired()
        && move.getMoveLineList().stream()
            .map(MoveLine::getDescription)
            .anyMatch(ObjectUtils::isEmpty)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_LINE_DESCRIPTION_MISSING));
    }
  }
}
