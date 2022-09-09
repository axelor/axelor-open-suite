package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentSession;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import wslite.json.JSONException;

public interface PaymentSessionEmailService {
  public int sendEmails(PaymentSession paymentSession)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, JSONException;
}
