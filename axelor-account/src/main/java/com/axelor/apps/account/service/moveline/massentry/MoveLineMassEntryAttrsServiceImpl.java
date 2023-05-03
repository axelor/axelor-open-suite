package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

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
}
