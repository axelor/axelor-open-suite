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
package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;

public class MoveLineMassEntryAttrsServiceImpl implements MoveLineMassEntryAttrsService {

  protected AppAccountService appAccountService;
  protected InvoiceTermService invoiceTermService;
  protected PfpService pfpService;

  @Inject
  public MoveLineMassEntryAttrsServiceImpl(
      AppAccountService appAccountService,
      InvoiceTermService invoiceTermService,
      PfpService pfpService) {
    this.appAccountService = appAccountService;
    this.invoiceTermService = invoiceTermService;
    this.pfpService = pfpService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addCutOffReadonly(Account account, Map<String, Map<String, Object>> attrsMap) {
    if (account != null) {
      this.addAttr("cutOffStartDate", "readonly", !account.getManageCutOffPeriod(), attrsMap);
      this.addAttr("cutOffEndDate", "readonly", !account.getManageCutOffPeriod(), attrsMap);
    }
  }

  @Override
  public void addMovePaymentModeReadOnly(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "movePaymentMode",
        "readonly",
        !appAccountService.getAppAccount().getAllowMultiInvoiceTerms(),
        attrsMap);
  }

  @Override
  public void addInputActionSelectionIn(Move move, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "inputAction",
        "selection-in",
        move != null
                && (move.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_SALE
                    || move.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE
                    || move.getFunctionalOriginSelect()
                        == MoveRepository.FUNCTIONAL_ORIGIN_FIXED_ASSET)
            ? new int[] {1, 2, 3}
            : new int[] {1, 3},
        attrsMap);
    this.addAttr(
        "inputAction",
        "readonly",
        move != null && ObjectUtils.isEmpty(move.getMoveLineMassEntryList()),
        attrsMap);
  }

  @Override
  public void addDebitCreditFocus(
      Account account, boolean isOtherCurrency, Map<String, Map<String, Object>> attrsMap) {
    if (!isOtherCurrency) {
      this.addAttr("currencyAmount", "focus", true, attrsMap);
      if (account != null
          && (account.getCommonPosition() == 2 || account.getCommonPosition() == 0)) {
        this.addAttr("debit", "focus", true, attrsMap);
      } else if (account != null && account.getCommonPosition() == 1) {
        this.addAttr("credit", "focus", true, attrsMap);
      }
    }
  }

  @Override
  public void addPartnerBankDetailsReadOnly(
      MoveLineMassEntry moveLine, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "movePartnerBankDetails",
        "readonly",
        moveLine != null
            && moveLine.getMovePaymentMode() != null
            && moveLine.getMovePaymentMode().getTypeSelect() != null
            && moveLine.getMovePaymentMode().getTypeSelect() != PaymentModeRepository.TYPE_DD
            && moveLine.getMovePaymentMode().getTypeSelect() != PaymentModeRepository.TYPE_IPO
            && moveLine.getMovePaymentMode().getTypeSelect() != PaymentModeRepository.TYPE_TRANSFER
            && moveLine.getMovePaymentMode().getTypeSelect()
                != PaymentModeRepository.TYPE_EXCHANGES,
        attrsMap);
  }

  @Override
  public void addMovePfpValidatorUserReadOnly(
      MoveLineMassEntry moveLine, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "movePfpValidatorUser",
        "readonly",
        ObjectUtils.isEmpty(moveLine.getMovePfpValidatorUser()),
        attrsMap);
  }

  @Override
  public void addMovePfpValidatorUserRequired(
      Account account, Journal journal, Company company, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    if (pfpService.isManagePassedForPayment(company)
        && account != null
        && account.getUseForPartnerBalance()
        && journal != null
        && journal.getJournalType() != null) {
      this.addAttr(
          "movePfpValidatorUser",
          "required",
          journal.getJournalType().getTechnicalTypeSelect()
              == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE,
          attrsMap);
    }
  }

  @Override
  public void addTemporaryMoveNumberFocus(Move move, Map<String, Map<String, Object>> attrsMap) {
    if (ObjectUtils.notEmpty(move.getMoveLineMassEntryList())
        && move.getMoveLineMassEntryList().size() > 0) {
      this.addAttr("temporaryMoveNumber", "focus", true, attrsMap);
    }
  }

  @Override
  public void addMovePaymentConditionRequired(
      JournalType journalType, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "movePaymentCondition",
        "required",
        journalType != null
            && journalType.getTechnicalTypeSelect() != null
            && journalType.getTechnicalTypeSelect()
                < JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER,
        attrsMap);
  }

  @Override
  public void addOriginRequired(
      MoveLineMassEntry moveLine, Journal journal, Map<String, Map<String, Object>> attrsMap) {
    int[] technicalTypeSelectArray = {
      JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE,
      JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE,
      JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE
    };

    this.addAttr(
        "origin",
        "required",
        moveLine.getOriginDate() != null
            && journal != null
            && journal.getHasRequiredOrigin()
            && ArrayUtils.contains(
                technicalTypeSelectArray, journal.getJournalType().getTechnicalTypeSelect()),
        attrsMap);
  }

  @Override
  public void addPfpValidatorUserDomain(
      Partner partner, Company company, Map<String, Map<String, Object>> attrsMap) {

    this.addAttr(
        "movePfpValidatorUser",
        "domain",
        invoiceTermService.getPfpValidatorUserDomain(partner, company),
        attrsMap);
  }

  @Override
  public void addReadonly(
      boolean isCounterPartLine, Account account, Map<String, Map<String, Object>> attrsMap) {
    ArrayList<String> fieldsList =
        new ArrayList(
            Arrays.asList(
                "date",
                "originDate",
                "origin",
                "moveDescription",
                "movePaymentMode",
                "movePaymentCondition",
                "partner",
                "description",
                "currencyRate",
                "currencyAmount",
                "cutOffStartDate",
                "cutOffEndDate",
                "pfpValidatorUser",
                "movePartnerBankDetails",
                "vatSystemSelect",
                "moveStatusSelect",
                "account",
                "analyticMoveLineList"));

    for (String field : fieldsList) {
      this.addAttr(field, "readonly", isCounterPartLine, attrsMap);
    }

    this.addAttr(
        "taxLine",
        "readonly",
        isCounterPartLine && account != null && !account.getIsTaxAuthorizedOnMoveLine(),
        attrsMap);
  }

  @Override
  public void addRequired(boolean isCounterPartLine, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("account", "required", !isCounterPartLine, attrsMap);
  }

  @Override
  public void addInputActionReadonly(boolean readonly, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("inputAction", "required", readonly, attrsMap);
  }

  @Override
  public void addTemporaryMoveNumberFocus(Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("temporaryMoveNumber", "focus", true, attrsMap);
  }
}
