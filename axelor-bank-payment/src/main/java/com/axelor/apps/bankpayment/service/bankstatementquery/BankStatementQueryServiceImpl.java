package com.axelor.apps.bankpayment.service.bankstatementquery;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.base.db.Partner;
import com.axelor.db.EntityHelper;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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
              IExceptionMessage.BANK_STATEMENT_MOVE_LINE_QUERY_FORMULA_NOT_EVALUATED_TO_MOVE_LINE));
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
              IExceptionMessage.BANK_STATEMENT_PARTNER_QUERY_FORMULA_NOT_EVALUATED_TO_PARTNER));
    }

    return (Partner) partner;
  }
}
