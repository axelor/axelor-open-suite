package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.meta.CallMethod;
import java.time.LocalDate;

public interface MoveLineFinancialDiscountService {
  @CallMethod
  LocalDate getFinancialDiscountDeadlineDate(MoveLine moveLine);

  void computeFinancialDiscount(MoveLine moveLine);
}
