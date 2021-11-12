package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.google.inject.Inject;
import java.util.Objects;

public class PaymentModeControlServiceImpl implements PaymentModeControlService {

  protected MoveRepository moveRepository;

  @Inject
  public PaymentModeControlServiceImpl(MoveRepository moveRepository) {

    this.moveRepository = moveRepository;
  }

  @Override
  public boolean isInMove(PaymentMode paymentMode) {
    Objects.requireNonNull(paymentMode);

    return moveRepository.all().filter("self.paymentMode.id = ?", paymentMode.getId()).count() > 0;
  }
}
