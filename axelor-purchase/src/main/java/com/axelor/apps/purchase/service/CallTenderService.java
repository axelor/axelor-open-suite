package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.CallTender;

import java.io.IOException;

public interface CallTenderService {

  void generateCallTenderOffers(CallTender callTender);

  void sendCallTenderOffers(CallTender callTender) throws AxelorException, IOException;
}
