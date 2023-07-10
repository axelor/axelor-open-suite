package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.moveline.MoveLineAttrsService;
import com.axelor.apps.account.service.moveline.MoveLineCheckService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineDefaultService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineCheckBankPaymentService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineGroupBankPaymentServiceImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLineRecordBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class MoveLineGroupBudgetServiceImpl extends MoveLineGroupBankPaymentServiceImpl {

  protected BudgetToolsService budgetToolsService;

  @Inject
  public MoveLineGroupBudgetServiceImpl(
      MoveLineService moveLineService,
      MoveLineDefaultService moveLineDefaultService,
      MoveLineRecordService moveLineRecordService,
      MoveLineAttrsService moveLineAttrsService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineCheckService moveLineCheckService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveLineToolService moveLineToolService,
      MoveToolService moveToolService,
      AnalyticLineService analyticLineService,
      MoveAttrsService moveAttrsService,
      MoveLineCheckBankPaymentService moveLineCheckBankPaymentService,
      MoveLineRecordBankPaymentService moveLineRecordBankPaymentService,
      BudgetToolsService budgetToolsService) {
    super(
        moveLineService,
        moveLineDefaultService,
        moveLineRecordService,
        moveLineAttrsService,
        moveLineComputeAnalyticService,
        moveLineCheckService,
        moveLineInvoiceTermService,
        moveLineToolService,
        moveToolService,
        analyticLineService,
        moveAttrsService,
        moveLineCheckBankPaymentService,
        moveLineRecordBankPaymentService);
    this.budgetToolsService = budgetToolsService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadMoveAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = super.getOnLoadMoveAttrsMap(moveLine, move);
    if (move != null) {
      boolean condition = budgetToolsService.checkBudgetKeyAndRoleForMove(move);
      this.addAttr("budgetDistributionList", "readonly", condition, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = super.getOnLoadAttrsMap(moveLine, move);
    if (move != null) {
      boolean condition = budgetToolsService.checkBudgetKeyAndRoleForMove(move);
      this.addAttr("budgetDistributionList", "readonly", condition, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException {

    Map<String, Map<String, Object>> attrsMap = super.getOnNewAttrsMap(moveLine, move);

    if (move != null) {
      boolean condition = budgetToolsService.checkBudgetKeyAndRoleForMove(move);
      this.addAttr("budgetDistributionList", "readonly", condition, attrsMap);
    }

    return attrsMap;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }
}
