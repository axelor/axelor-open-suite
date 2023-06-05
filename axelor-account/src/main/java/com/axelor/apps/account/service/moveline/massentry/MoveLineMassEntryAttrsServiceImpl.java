package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;

public class MoveLineMassEntryAttrsServiceImpl implements MoveLineMassEntryAttrsService {

  protected AppAccountService appAccountService;

  @Inject
  public MoveLineMassEntryAttrsServiceImpl(AppAccountService appAccountService) {
    this.appAccountService = appAccountService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addCutOffReadOnly(Account account, Map<String, Map<String, Object>> attrsMap) {
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
        "credit",
        "focus",
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
      Account account, Map<String, Map<String, Object>> attrsMap) {
    if (appAccountService.getAppAccount().getActivatePassedForPayment()
        && account != null
        && account.getUseForPartnerBalance()) {
      this.addAttr("movePfpValidatorUser", "required", true, attrsMap);
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
        Beans.get(InvoiceTermService.class).getPfpValidatorUserDomain(partner, company),
        attrsMap);
  }
}
