/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveCounterPartService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class MassEntryServiceImpl implements MassEntryService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MassEntryToolService massEntryToolService;
  protected MoveToolService moveToolService;
  protected MoveLineTaxService moveLineTaxService;
  protected MoveCounterPartService moveCounterPartService;
  protected MassEntryVerificationService massEntryVerificationService;
  protected MoveLoadDefaultConfigService moveLoadDefaultConfigService;

  @Inject
  public MassEntryServiceImpl(
      MassEntryToolService massEntryToolService,
      MoveToolService moveToolService,
      MoveLineTaxService moveLineTaxService,
      MoveCounterPartService moveCounterPartService,
      MassEntryVerificationService massEntryVerificationService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService) {
    this.massEntryToolService = massEntryToolService;
    this.moveToolService = moveToolService;
    this.moveLineTaxService = moveLineTaxService;
    this.moveCounterPartService = moveCounterPartService;
    this.massEntryVerificationService = massEntryVerificationService;
    this.moveLoadDefaultConfigService = moveLoadDefaultConfigService;
  }

  public void fillMoveLineListWithMoveLineMassEntryList(Move move, Integer temporaryMoveNumber) {
    List<MoveLine> moveLineList = new ArrayList<>();
    boolean firstLine = true;

    for (MoveLineMassEntry moveLineMassEntry : move.getMoveLineMassEntryList()) {
      if (Objects.equals(moveLineMassEntry.getTemporaryMoveNumber(), temporaryMoveNumber)
          && moveLineMassEntry.getInputAction() == 1) {
        if (firstLine) {
          move.setPaymentMode(moveLineMassEntry.getMovePaymentMode());
          move.setPaymentCondition(moveLineMassEntry.getMovePaymentCondition());
          move.setDate(moveLineMassEntry.getDate());
          move.setDescription(moveLineMassEntry.getMoveDescription());
          move.setPartnerBankDetails(moveLineMassEntry.getMovePartnerBankDetails());

          // TODO Need to be seen, to enable multiPartners in a Move and remove this line
          move.setPartner(moveLineMassEntry.getPartner());
          firstLine = false;
        }
        moveLineMassEntry.setMove(move);
        moveLineList.add(moveLineMassEntry);
      }
    }
    move.setMoveLineList(moveLineList);
    massEntryToolService.clearMoveLineMassEntryListAndAddNewLines(move, temporaryMoveNumber);
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

  public MoveLineMassEntry getFirstMoveLineMassEntryInformations(
      List<MoveLineMassEntry> moveLineMassEntryList, MoveLineMassEntry moveLineMassEntry) {
    for (MoveLineMassEntry moveLine : moveLineMassEntryList) {
      if (moveLine.getTemporaryMoveNumber().equals(moveLineMassEntry.getTemporaryMoveNumber())) {
        moveLineMassEntry.setPartner(moveLine.getPartner());
        moveLineMassEntry.setPartnerId(moveLine.getPartnerId());
        moveLineMassEntry.setPartnerSeq(moveLine.getPartnerSeq());
        moveLineMassEntry.setPartnerFullName(moveLine.getPartnerFullName());
        moveLineMassEntry.setDate(moveLine.getDate());
        moveLineMassEntry.setDueDate(moveLine.getDueDate());
        moveLineMassEntry.setOriginDate(moveLine.getOriginDate());
        moveLineMassEntry.setOrigin(moveLine.getOrigin());
        moveLineMassEntry.setMoveStatusSelect(moveLine.getMoveStatusSelect());
        moveLineMassEntry.setInterbankCodeLine(moveLine.getInterbankCodeLine());
        moveLineMassEntry.setMoveDescription(moveLine.getMoveDescription());
        moveLineMassEntry.setDescription(moveLine.getMoveDescription());
        moveLineMassEntry.setExportedDirectDebitOk(moveLine.getExportedDirectDebitOk());
        moveLineMassEntry.setMovePaymentCondition(moveLine.getMovePaymentCondition());
        moveLineMassEntry.setMovePaymentMode(moveLine.getMovePaymentMode());
        moveLineMassEntry.setMovePartnerBankDetails(moveLine.getMovePartnerBankDetails());
        break;
      }
    }
    return moveLineMassEntry;
  }

  public void resetMoveLineMassEntry(MoveLineMassEntry moveLineMassEntry) {
    moveLineMassEntry.setOrigin(null);
    moveLineMassEntry.setOriginDate(null);
    moveLineMassEntry.setPartner(null);
    moveLineMassEntry.setPartnerId(null);
    moveLineMassEntry.setPartnerSeq(null);
    moveLineMassEntry.setPartnerFullName(null);
    moveLineMassEntry.setMoveDescription(null);
    moveLineMassEntry.setMovePaymentCondition(null);
    moveLineMassEntry.setMovePaymentMode(null);
    moveLineMassEntry.setMovePartnerBankDetails(null);
    moveLineMassEntry.setAccount(null);
    moveLineMassEntry.setTaxLine(null);
    moveLineMassEntry.setDescription(null);
    moveLineMassEntry.setDebit(BigDecimal.ZERO);
    moveLineMassEntry.setCredit(BigDecimal.ZERO);
    moveLineMassEntry.setCurrencyRate(BigDecimal.ONE);
    moveLineMassEntry.setCurrencyAmount(BigDecimal.ZERO);
    moveLineMassEntry.setMoveStatusSelect(null);
    moveLineMassEntry.setVatSystemSelect(0);
  }

  public void verifyFieldsChangeOnMoveLineMassEntry(Move move) throws AxelorException {
    List<MoveLineMassEntry> moveLineMassEntryList =
        massEntryToolService.getEditedMoveLineMassEntry(move.getMoveLineMassEntryList());

    if (ObjectUtils.notEmpty(moveLineMassEntryList)) {
      for (MoveLineMassEntry moveLineEdited : moveLineMassEntryList) {
        for (MoveLineMassEntry moveLine : move.getMoveLineMassEntryList()) {
          if (Objects.equals(
              moveLine.getTemporaryMoveNumber(), moveLineEdited.getTemporaryMoveNumber())) {
            move.setDate(moveLineEdited.getDate());
            massEntryVerificationService.checkAndReplaceDateInMoveLineMassEntry(
                moveLine, moveLineEdited.getDate(), move);
            massEntryVerificationService.checkAndReplaceOriginDateInMoveLineMassEntry(
                moveLine, moveLineEdited.getOriginDate());
            massEntryVerificationService.checkAndReplaceOriginInMoveLineMassEntry(
                moveLine, moveLineEdited.getOrigin() != null ? moveLineEdited.getOrigin() : "");
            massEntryVerificationService.checkAndReplaceMoveDescriptionInMoveLineMassEntry(
                moveLine,
                moveLineEdited.getMoveDescription() != null
                    ? moveLineEdited.getMoveDescription()
                    : "");
            if (moveLineEdited.getAccount() != null
                && !moveLineEdited.getAccount().getHasInvoiceTerm()
                && moveLineEdited.getMovePaymentMode() != null) {
              massEntryVerificationService.checkAndReplaceMovePaymentModeInMoveLineMassEntry(
                  moveLine, moveLineEdited.getMovePaymentMode());
            }
            massEntryVerificationService.checkAndReplaceCurrencyRateModeInMoveLineMassEntry(
                moveLine, moveLineEdited.getCurrencyRate());

            // TODO add other verification method

            moveLine.setIsEdited(false);
          }
        }
      }
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
}
