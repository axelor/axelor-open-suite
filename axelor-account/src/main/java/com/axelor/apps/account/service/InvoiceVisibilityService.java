package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;

public interface InvoiceVisibilityService {
  boolean isPfpButtonVisible(Invoice invoice, User user, boolean litigation) throws AxelorException;

  boolean isPaymentButtonVisible(Invoice invoice) throws AxelorException;

  boolean isValidatorUserVisible(Invoice invoice) throws AxelorException;

  boolean isDecisionPfpVisible(Invoice invoice) throws AxelorException;

  boolean isSendNotifyVisible(Invoice invoice) throws AxelorException;

  boolean getManagePfpCondition(Invoice invoice) throws AxelorException;

  boolean getOperationTypePurchaseCondition(Invoice invoice) throws AxelorException;

  boolean getPaymentVouchersStatus(Invoice invoice) throws AxelorException;
}
