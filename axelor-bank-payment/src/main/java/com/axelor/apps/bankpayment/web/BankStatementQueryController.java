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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatementquery.BankStatementQueryFetchService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
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
        response.setError(I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_QUERY_SEQUENCE_USED));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
