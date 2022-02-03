package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatementquery.BankStatementQueryFetchService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class BankStatementQueryController {

  public void checkSequenceUnicity(ActionRequest request, ActionResponse response) {
    try {
      BankStatementQuery bankStatementQuery = request.getContext().asType(BankStatementQuery.class);
      int sequence = bankStatementQuery.getSequence();

      BankStatementQuery bsq =
          Beans.get(BankStatementQueryFetchService.class)
              .findBySequenceAndRuleTypeExcludeId(
                  sequence,
                  BankStatementRuleRepository.RULE_TYPE_RECONCILIATION_AUTO,
                  bankStatementQuery.getId());
      if (ObjectUtils.notEmpty(bsq)) {
        response.setError(I18n.get(IExceptionMessage.BANK_STATEMENT_QUERY_SEQUENCE_USED));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
