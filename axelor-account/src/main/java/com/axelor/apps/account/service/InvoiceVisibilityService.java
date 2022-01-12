package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.auth.db.User;

public interface InvoiceVisibilityService {
  boolean isPfpButtonVisible(Invoice invoice, User user, boolean litigation);

  boolean isPaymentButtonVisible(Invoice invoice);

  boolean isValidatorUserVisible(Invoice invoice);

  boolean isDecisionPfpVisible(Invoice invoice);

  boolean isSendNotifyVisible(Invoice invoice);
}
