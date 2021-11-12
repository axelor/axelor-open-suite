package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentMode;

public interface PaymentModeControlService {

  /**
   * This method checks if paymentMode is linked to a Move.
   *
   * @param paymentMode
   * @return true if paymentMode is linked to Move, else false
   */
  boolean isInMove(PaymentMode paymentMode);
}
