package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.message.db.Template;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import javax.mail.MessagingException;

public interface CallTenderMailService {

  void sendMails(CallTender callTender) throws ClassNotFoundException, MessagingException;

  @Transactional(rollbackOn = Exception.class)
  void generateOfferMail(List<CallTenderOffer> offer, Template template)
      throws AxelorException, IOException;
}
