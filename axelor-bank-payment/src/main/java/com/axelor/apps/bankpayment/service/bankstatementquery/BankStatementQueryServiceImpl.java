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
package com.axelor.apps.bankpayment.service.bankstatementquery;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.db.EntityHelper;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import java.util.Objects;

public class BankStatementQueryServiceImpl implements BankStatementQueryService {

  @Override
  public Object evalQuery(
      BankStatementQuery bankStatementQuery, BankStatementLine bankStatementLine, Move move)
      throws AxelorException {
    Objects.requireNonNull(bankStatementQuery);
    Objects.requireNonNull(bankStatementLine);

    switch (bankStatementQuery.getRuleTypeSelect()) {
      case BankStatementRuleRepository.RULE_TYPE_PARTNER_FETCHING:
        return evalPartner(bankStatementQuery, bankStatementLine);
      case BankStatementRuleRepository.RULE_TYPE_MOVE_LINE_FETCHING:
        return evalMoveLine(bankStatementQuery, bankStatementLine, move);
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            "The rule type select is not managed by the function");
    }
  }

  protected MoveLine evalMoveLine(
      BankStatementQuery bankStatementQuery, BankStatementLine bankStatementLine, Move move)
      throws AxelorException {
    Objects.requireNonNull(move);
    Context scriptContext =
        new Context(Mapper.toMap(bankStatementLine), BankStatementLineAFB120.class);
    scriptContext.put("generatedMove", EntityHelper.getEntity(move));
    Object moveLine = new GroovyScriptHelper(scriptContext).eval(bankStatementQuery.getQuery());

    if (moveLine == null) {
      return null;
    }

    if (!(moveLine instanceof MoveLine)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              BankPaymentExceptionMessage
                  .BANK_STATEMENT_MOVE_LINE_QUERY_FORMULA_NOT_EVALUATED_TO_MOVE_LINE));
    }

    return (MoveLine) moveLine;
  }

  protected Partner evalPartner(
      BankStatementQuery partnerFetchQuery, BankStatementLine bankStatementLine)
      throws AxelorException {
    Context scriptContext =
        new Context(Mapper.toMap(bankStatementLine), BankStatementLineAFB120.class);
    Object partner = new GroovyScriptHelper(scriptContext).eval(partnerFetchQuery.getQuery());

    if (partner == null) {
      return null;
    }

    if (!(partner instanceof Partner)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              BankPaymentExceptionMessage
                  .BANK_STATEMENT_PARTNER_QUERY_FORMULA_NOT_EVALUATED_TO_PARTNER));
    }

    return (Partner) partner;
  }
}
