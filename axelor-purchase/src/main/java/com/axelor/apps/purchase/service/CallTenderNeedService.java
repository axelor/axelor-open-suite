package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.CallTenderNeed;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface CallTenderNeedService {

  CallTenderNeed createCallTenderNeed(
      Product product, BigDecimal quantity, Unit unit, LocalDate date, int typeSelect);
}
