package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
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

  @Inject
  public MoveLineMassEntryServiceImpl(
      MoveLineTaxService moveLineTaxService,
      MoveCounterPartService moveCounterPartService,
      MassEntryToolService massEntryToolService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService,
      CurrencyService currencyService,
      MoveLineMassEntryToolService moveLineMassEntryToolService) {
    this.moveLineTaxService = moveLineTaxService;
    this.moveCounterPartService = moveCounterPartService;
    this.massEntryToolService = massEntryToolService;
    this.moveLoadDefaultConfigService = moveLoadDefaultConfigService;
    this.currencyService = currencyService;
    this.moveLineMassEntryToolService = moveLineMassEntryToolService;
  }

  public void generateTaxLineAndCounterpart(
      Move move, LocalDate dueDate, Integer temporaryMoveNumber) throws AxelorException {
    if (ObjectUtils.notEmpty(move.getMoveLineList())) {
      if (move.getStatusSelect().equals(MoveRepository.STATUS_NEW)
          || move.getStatusSelect().equals(MoveRepository.STATUS_SIMULATED)) {
        moveLineTaxService.autoTaxLineGenerate(move);
        moveCounterPartService.generateCounterpartMoveLine(move, dueDate);
      }
      massEntryToolService.clearMoveLineMassEntryListAndAddNewLines(move, temporaryMoveNumber);
    }
  }

  public void loadAccountInformation(Move move, MoveLineMassEntry moveLineMassEntry)
      throws AxelorException {
    Account accountingAccount =
        moveLoadDefaultConfigService.getAccountingAccountFromAccountConfig(move);

    if (accountingAccount != null) {
      moveLineMassEntry.setAccount(accountingAccount);

      AnalyticDistributionTemplate analyticDistributionTemplate =
          accountingAccount.getAnalyticDistributionTemplate();
      if (accountingAccount.getAnalyticDistributionAuthorized()
          && analyticDistributionTemplate != null) {
        moveLineMassEntry.setAnalyticDistributionTemplate(analyticDistributionTemplate);
      }
    }
    moveLineMassEntry.setTaxLine(
        moveLoadDefaultConfigService.getTaxLine(move, moveLineMassEntry, accountingAccount));
  }

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

  public BigDecimal computeCurrentRate(
      BigDecimal currencyRate, Move move, Integer temporaryMoveNumber, LocalDate originDate)
      throws AxelorException {
    Currency currency = move.getCurrency();
    Currency companyCurrency = move.getCompanyCurrency();

    if (currency != null && companyCurrency != null && !currency.equals(companyCurrency)) {
      if (move.getMoveLineMassEntryList().size() == 0) {
        if (originDate != null) {
          currencyRate =
              currencyService.getCurrencyConversionRate(currency, companyCurrency, originDate);
        } else {
          currencyRate = currencyService.getCurrencyConversionRate(currency, companyCurrency);
        }
      } else {
        if (move.getMoveLineMassEntryList().stream()
            .anyMatch(
                moveLineMassEntry1 ->
                    Objects.equals(
                        moveLineMassEntry1.getTemporaryMoveNumber(), temporaryMoveNumber))) {
          currencyRate =
              move.getMoveLineMassEntryList().stream()
                  .filter(
                      moveLineMassEntry1 ->
                          Objects.equals(
                              moveLineMassEntry1.getTemporaryMoveNumber(), temporaryMoveNumber))
                  .findFirst()
                  .get()
                  .getCurrencyRate();
        }
      }
    }
    return currencyRate;
  }

  public void setPartnerAndBankDetails(Move move, MoveLineMassEntry moveLineMassEntry)
      throws AxelorException {
    if (move != null && move.getJournal() != null) {
      moveLineMassEntryToolService.setPaymentModeOnMoveLineMassEntry(
          moveLineMassEntry, move.getJournal().getJournalType().getTechnicalTypeSelect());

      move.setPartner(moveLineMassEntry.getPartner());
      move.setPaymentMode(moveLineMassEntry.getMovePaymentMode());

      moveLineMassEntry.setMovePaymentCondition(null);
      if (move.getJournal().getJournalType().getTechnicalTypeSelect()
          != JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY) {
        moveLineMassEntry.setMovePaymentCondition(
            moveLineMassEntry.getPartner().getPaymentCondition());
      }

      this.loadAccountInformation(move, moveLineMassEntry);
    }

    moveLineMassEntry.setMovePartnerBankDetails(
        moveLineMassEntry.getPartner().getBankDetailsList().stream()
                .anyMatch(it -> it.getIsDefault() && it.getActive())
            ? moveLineMassEntry.getPartner().getBankDetailsList().stream()
                .filter(it -> it.getIsDefault() && it.getActive())
                .findFirst()
                .get()
            : null);
    moveLineMassEntry.setCurrencyCode(
        moveLineMassEntry.getPartner().getCurrency() != null
            ? moveLineMassEntry.getPartner().getCurrency().getCodeISO()
            : null);
  }
}
