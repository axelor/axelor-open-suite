package com.axelor.apps.bankpayment.service.bankstatementquery;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.base.db.Partner;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import java.util.Objects;

public class BankStatementQueryServiceImpl implements BankStatementQueryService {

  public static final String INVOICE_QUERY_FORMULA_NOT_EVALUATED_TO_INVOICE =
      "Invoice's query formula has not been evaluated to a Invoice";
  public static final String PARTNER_QUERY_FORMULA_NOT_EVALUATED_TO_PARTNER =
      "Partner's query formula has not been evaluated to a Partner";

  @Override
  public Object evalQuery(
      BankStatementQuery bankStatementQuery, BankStatementLine bankStatementLine)
      throws AxelorException {
    Objects.requireNonNull(bankStatementQuery);
    Objects.requireNonNull(bankStatementLine);

    switch (bankStatementQuery.getRuleTypeSelect()) {
      case BankStatementRuleRepository.RULE_TYPE_PARTNER_FETCHING:
        return evalPartner(bankStatementQuery, bankStatementLine);
      case BankStatementRuleRepository.RULE_TYPE_INVOICE_FETCHING:
        return evalInvoice(bankStatementQuery, bankStatementLine);
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            "The rule type select is not managed by the function");
    }
  }

  protected Invoice evalInvoice(
      BankStatementQuery bankStatementQuery, BankStatementLine bankStatementLine)
      throws AxelorException {
    Context scriptContext =
        new Context(Mapper.toMap(bankStatementLine), BankStatementLineAFB120.class);
    Object invoice = new GroovyScriptHelper(scriptContext).eval(bankStatementQuery.getQuery());

    if (invoice == null) {
      return null;
    }

    if (!(invoice instanceof Invoice)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(INVOICE_QUERY_FORMULA_NOT_EVALUATED_TO_INVOICE));
    }

    return (Invoice) invoice;
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
          I18n.get(PARTNER_QUERY_FORMULA_NOT_EVALUATED_TO_PARTNER));
    }

    return (Partner) partner;
  }
}
