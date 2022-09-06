package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface PaymentSessionValidateService {
  public int validateInvoiceTerms(PaymentSession paymentSession);

  public int processPaymentSession(PaymentSession paymentSession) throws AxelorException;

  public StringBuilder generateFlashMessage(PaymentSession paymentSession, int moveCount);

  public List<Partner> getPartnersWithNegativeAmount(PaymentSession paymentSession)
      throws AxelorException;

  public void reconciledInvoiceTermMoves(PaymentSession paymentSession) throws AxelorException;

  public boolean checkIsHoldBackWithRefund(PaymentSession paymentSession) throws AxelorException;

  boolean isEmpty(PaymentSession paymentSession);

  public List<InvoiceTerm> getInvoiceTermsWithInActiveBankDetails(PaymentSession paymentSession);
}
