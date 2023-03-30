package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.account.service.move.massentry.MassEntryToolService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineMassEntryServiceImpl implements MoveLineMassEntryService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveLineTaxService moveLineTaxService;
  protected MoveCounterPartService moveCounterPartService;
  protected MassEntryToolService massEntryToolService;
  protected MoveLoadDefaultConfigService moveLoadDefaultConfigService;
  protected CurrencyService currencyService;
  protected MoveLineMassEntryToolService moveLineMassEntryToolService;
  protected AccountingSituationService accountingSituationService;

  @Inject
  public MoveLineMassEntryServiceImpl(
      MoveLineTaxService moveLineTaxService,
      MoveCounterPartService moveCounterPartService,
      MassEntryToolService massEntryToolService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService,
      CurrencyService currencyService,
      MoveLineMassEntryToolService moveLineMassEntryToolService,
      AccountingSituationService accountingSituationService) {
    this.moveLineTaxService = moveLineTaxService;
    this.moveCounterPartService = moveCounterPartService;
    this.massEntryToolService = massEntryToolService;
    this.moveLoadDefaultConfigService = moveLoadDefaultConfigService;
    this.currencyService = currencyService;
    this.moveLineMassEntryToolService = moveLineMassEntryToolService;
    this.accountingSituationService = accountingSituationService;
  }

  @Override
  public void generateTaxLineAndCounterpart(
      Move parentMove, Move childMove, LocalDate dueDate, Integer temporaryMoveNumber)
      throws AxelorException {
    if (ObjectUtils.notEmpty(childMove.getMoveLineList())) {
      if (childMove.getStatusSelect().equals(MoveRepository.STATUS_NEW)
          || childMove.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED)) {
        moveLineTaxService.autoTaxLineGenerate(childMove);
        moveCounterPartService.generateCounterpartMoveLine(childMove, dueDate);
      }
      massEntryToolService.clearMoveLineMassEntryListAndAddNewLines(
          parentMove, childMove, temporaryMoveNumber);
    }
  }

  @Override
  public void loadAccountInformation(Move move, MoveLineMassEntry moveLine) throws AxelorException {
    Account accountingAccount =
        moveLoadDefaultConfigService.getAccountingAccountFromAccountConfig(move);

    if (accountingAccount != null) {
      moveLine.setAccount(accountingAccount);

      AnalyticDistributionTemplate analyticDistributionTemplate =
          accountingAccount.getAnalyticDistributionTemplate();
      if (accountingAccount.getAnalyticDistributionAuthorized()
          && analyticDistributionTemplate != null) {
        moveLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);
      }
    }
    moveLine.setTaxLine(moveLoadDefaultConfigService.getTaxLine(move, moveLine, accountingAccount));
  }

  @Override
  public Map<String, Map<String, Object>> setAttrsInputActionOnChange(
      boolean isCounterPartLine, Account account) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();
    Map<String, Object> readonlyMap = new HashMap<>();
    Map<String, Object> requiredMap = new HashMap<>();
    Map<String, Object> taxLineMap = new HashMap<>();

    readonlyMap.put("readonly", isCounterPartLine);
    requiredMap.put("readonly", isCounterPartLine);
    requiredMap.put("required", !isCounterPartLine);
    taxLineMap.put(
        "readonly",
        isCounterPartLine && account != null && !account.getIsTaxAuthorizedOnMoveLine());

    attrsMap.put("date", readonlyMap);
    attrsMap.put("originDate", readonlyMap);
    attrsMap.put("origin", readonlyMap);
    attrsMap.put("moveDescription", readonlyMap);
    attrsMap.put("movePaymentCondition", readonlyMap);
    attrsMap.put("movePaymentMode", readonlyMap);
    attrsMap.put("account", requiredMap);
    attrsMap.put("taxLine", taxLineMap);
    attrsMap.put("partner", readonlyMap);
    attrsMap.put("description", readonlyMap);
    attrsMap.put("debit", readonlyMap);
    attrsMap.put("credit", readonlyMap);
    attrsMap.put("currencyRate", readonlyMap);
    attrsMap.put("currencyAmount", readonlyMap);
    attrsMap.put("cutOffStartDate", readonlyMap);
    attrsMap.put("cutOffEndDate", readonlyMap);
    attrsMap.put("pfpValidatorUser", readonlyMap);
    attrsMap.put("movePartnerBankDetails", readonlyMap);
    attrsMap.put("vatSystemSelect", readonlyMap);
    attrsMap.put("moveStatusSelect", readonlyMap);

    return attrsMap;
  }

  @Override
  public BigDecimal computeCurrentRate(
      BigDecimal currencyRate,
      List<MoveLineMassEntry> moveLineList,
      Currency currency,
      Currency companyCurrency,
      Integer temporaryMoveNumber,
      LocalDate originDate)
      throws AxelorException {
    if (currency != null && companyCurrency != null && !currency.equals(companyCurrency)) {
      if (moveLineList.size() == 0) {
        if (originDate != null) {
          currencyRate =
              currencyService.getCurrencyConversionRate(currency, companyCurrency, originDate);
        } else {
          currencyRate = currencyService.getCurrencyConversionRate(currency, companyCurrency);
        }
      } else {
        if (moveLineList.stream()
            .anyMatch(it -> Objects.equals(it.getTemporaryMoveNumber(), temporaryMoveNumber))) {
          currencyRate =
              moveLineList.stream()
                  .filter(it -> Objects.equals(it.getTemporaryMoveNumber(), temporaryMoveNumber))
                  .findFirst()
                  .get()
                  .getCurrencyRate();
        }
      }
    }
    return currencyRate;
  }

  @Override
  public void setPartnerAndRelatedFields(Move move, MoveLineMassEntry moveLine)
      throws AxelorException {
    if (move != null && move.getJournal() != null) {
      moveLineMassEntryToolService.setPaymentModeOnMoveLineMassEntry(
          moveLine, move.getJournal().getJournalType().getTechnicalTypeSelect());

      move.setPartner(moveLine.getPartner());
      move.setPaymentMode(moveLine.getMovePaymentMode());

      moveLine.setMovePaymentCondition(null);
      if (move.getJournal().getJournalType().getTechnicalTypeSelect()
          != JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY) {
        moveLine.setMovePaymentCondition(moveLine.getPartner().getPaymentCondition());
      }

      this.loadAccountInformation(move, moveLine);

      AccountingSituation accountingSituation =
          accountingSituationService.getAccountingSituation(
              moveLine.getPartner(), move.getCompany());
      moveLine.setVatSystemSelect(null);
      if (accountingSituation != null) {
        moveLine.setVatSystemSelect(accountingSituation.getVatSystemSelect());
      }
    }

    moveLine.setMovePartnerBankDetails(
        moveLine.getPartner().getBankDetailsList().stream()
            .filter(it -> it.getIsDefault() && it.getActive())
            .findFirst()
            .orElse(null));
    moveLine.setCurrencyCode(
        moveLine.getPartner().getCurrency() != null
            ? moveLine.getPartner().getCurrency().getCodeISO()
            : null);

    // TODO display or update pfp validator on line when partner != null and account != null and
    // account.useForPartnerBalance == true
    // obviously just set it when partner.pfpValidator != null
    // Same process when we update the account
  }
}
