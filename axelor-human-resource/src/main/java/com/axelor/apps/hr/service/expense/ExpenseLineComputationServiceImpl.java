package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.ExpenseLine;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExpenseLineComputationServiceImpl implements ExpenseLineComputationService {

  @Override
  public ExpenseLine getTotalTaxFromProductAndTotalAmount(ExpenseLine expenseLine) {
    AccountManagement accountManagement = null;
    Tax tax = null;
    Product product = expenseLine.getExpenseProduct();
    if (product != null) {
      accountManagement =
          product.getAccountManagementList().stream()
              .filter(it -> (it.getPurchaseTax() != null))
              .findFirst()
              .orElse(null);
      if (accountManagement == null) {
        ProductFamily productFamily = expenseLine.getExpenseProduct().getProductFamily();
        if (productFamily != null) {
          accountManagement =
              productFamily.getAccountManagementList().stream()
                  .filter(it -> (it.getPurchaseTax() != null))
                  .findFirst()
                  .orElse(null);
        }
        if (accountManagement == null && expenseLine.getExpense().getCompany() != null) {
          tax =
              expenseLine
                  .getExpense()
                  .getCompany()
                  .getAccountConfig()
                  .getExpenseTaxAccount()
                  .getDefaultTax();
        }
      }
    }
    if (tax == null && accountManagement != null) {
      tax = accountManagement.getPurchaseTax();
    }
    extractTotalTax(expenseLine, tax);
    return expenseLine;
  }

  protected void extractTotalTax(ExpenseLine expenseLine, Tax tax) {
    if (tax != null) {
      BigDecimal value =
          expenseLine
              .getTotalAmount()
              .subtract(
                  expenseLine
                      .getTotalAmount()
                      .divide(
                          BigDecimal.ONE.add(
                              tax.getActiveTaxLine()
                                  .getValue()
                                  .divide(
                                      BigDecimal.valueOf(100),
                                      AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                                      RoundingMode.DOWN)),
                          AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                          RoundingMode.HALF_DOWN));
      expenseLine.setTotalTax(value);
    } else {
      expenseLine.setTotalTax(BigDecimal.ZERO);
    }
  }
}
