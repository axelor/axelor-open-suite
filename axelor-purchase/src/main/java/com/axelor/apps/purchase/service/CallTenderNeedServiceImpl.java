package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.repo.CallTenderNeedRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class CallTenderNeedServiceImpl implements CallTenderNeedService {

  protected final CallTenderNeedRepository callTenderNeedRepository;

  @Inject
  public CallTenderNeedServiceImpl(CallTenderNeedRepository callTenderNeedRepository) {
    this.callTenderNeedRepository = callTenderNeedRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public CallTenderNeed createCallTenderNeed(
      Product product, BigDecimal quantity, Unit unit, LocalDate date, int typeSelect) {
    Objects.requireNonNull(product);

    var callTenderNeed = new CallTenderNeed();

    callTenderNeed.setProduct(product);
    callTenderNeed.setUnit(unit);
    callTenderNeed.setRequestedQty(quantity);
    callTenderNeed.setRequestedDate(date);
    callTenderNeed.setTypeSelect(typeSelect);

    return callTenderNeedRepository.save(callTenderNeed);
  }
}
