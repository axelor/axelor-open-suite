package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.rest.dto.ExpenseLinePostRequest;
import com.axelor.apps.hr.rest.dto.ExpenseLineResponse;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.utils.api.ResponseConstructor;
import java.math.BigDecimal;
import javax.ws.rs.core.Response;

public class ExpenseLineResponseComputeServiceImpl implements ExpenseLineResponseComputeService {
  @Override
  public Response computeCreateResponse(
      ExpenseLine expenseLine,
      ExpenseLinePostRequest requestBody,
      ExpenseLineResponse expenseLineResponse) {
    BigDecimal totalTax = requestBody.getTotalTax();
    Product expenseProduct = requestBody.fetchExpenseProduct();
    if (expenseProduct != null
        && expenseProduct.getBlockExpenseTax()
        && totalTax != null
        && totalTax.compareTo(BigDecimal.ZERO) != 0) {
      return ResponseConstructor.build(
          Response.Status.CREATED, I18n.get(ITranslation.SET_TOTAL_TAX_ZERO), expenseLineResponse);
    }
    return ResponseConstructor.buildCreateResponse(expenseLine, expenseLineResponse);
  }
}
