package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.meta.CallMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public interface MoveLineFinancialDiscountService {
  @CallMethod
  LocalDate getFinancialDiscountDeadlineDate(MoveLine moveLine);

  void computeFinancialDiscount(MoveLine moveLine);

  boolean isFinancialDiscountLine(MoveLine moveLine, Company company) throws AxelorException;

  int createFinancialDiscountMoveLine(
      Invoice invoice,
      Move move,
      InvoicePayment invoicePayment,
      String origin,
      int counter,
      boolean isDebit,
      boolean financialDiscountVat)
      throws AxelorException;

  int createFinancialDiscountMoveLine(
      Move move,
      Partner partner,
      Map<Tax, Pair<BigDecimal, BigDecimal>> taxMap,
      Map<Tax, Integer> vatSystemTaxMap,
      Map<Tax, Account> accountTaxMap,
      Account financialDiscountAccount,
      String origin,
      String description,
      BigDecimal financialDiscountAmount,
      BigDecimal financialDiscountTaxAmount,
      LocalDate paymentDate,
      int counter,
      boolean isDebit,
      boolean financialDiscountVat)
      throws AxelorException;

  Map<Tax, Account> getAccountTaxMap(Move move);

  Map<Tax, Integer> getVatSystemTaxMap(Move move);

  Map<Tax, Pair<BigDecimal, BigDecimal>> getFinancialDiscountTaxMap(MoveLine moveLine);
}
