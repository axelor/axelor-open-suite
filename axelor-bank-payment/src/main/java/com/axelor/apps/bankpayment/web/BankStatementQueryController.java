package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementQueryRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class BankStatementQueryController {

  public void checkSequenceUnicity(ActionRequest request, ActionResponse response) {
    BankStatementQuery bankStatementQuery = request.getContext().asType(BankStatementQuery.class);
    int sequence = bankStatementQuery.getSequence();
    if (ObjectUtils.notEmpty(
        Beans.get(BankPaymentBankStatementQueryRepository.class)
            .findBySequenceAndRuleTypeExcludeId(
                sequence,
                BankStatementRuleRepository.RULE_TYPE_RECONCILIATION_AUTO,
                bankStatementQuery.getId()))) {
      response.setError(I18n.get(IExceptionMessage.BANK_STATEMENT_QUERY_SEQUENCE_USED));
    }
  }
}
