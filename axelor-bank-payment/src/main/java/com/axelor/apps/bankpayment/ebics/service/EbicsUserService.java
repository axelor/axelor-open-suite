/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.ebics.service;

import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsRequestLog;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.EbicsRequestLogRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.ebics.client.EbicsRootElement;
import com.axelor.ebics.exception.EbicsLibException;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import org.jdom.JDOMException;

public class EbicsUserService {

  @Inject private EbicsService ebicsService;

  @Inject private EbicsRequestLogRepository requestLogRepo;

  @Inject private EbicsUserRepository ebicsUserRepo;

  @Transactional
  public void logRequest(
      long ebicsUserId, String requestType, String responseCode, EbicsRootElement[] rootElements) {

    EbicsRequestLog requestLog = new EbicsRequestLog();
    requestLog.setEbicsUser(ebicsUserRepo.find(ebicsUserId));
    LocalDateTime time = LocalDateTime.now();
    requestLog.setRequestTime(time);
    requestLog.setRequestType(requestType);
    requestLog.setResponseCode(responseCode);

    try {
      trace(requestLog, rootElements);
    } catch (Exception e) {
      e.printStackTrace();
    }

    requestLogRepo.save(requestLog);
  }

  @Transactional(rollbackOn = {Exception.class})
  public String getNextOrderId(EbicsUser user) throws AxelorException {

    String orderId = user.getNextOrderId();

    if (orderId == null) {
      EbicsPartner partner = user.getEbicsPartner();
      EbicsUser otherUser =
          ebicsUserRepo
              .all()
              .filter(
                  "self.ebicsPartner = ?1 and self.id != ?2 and self.nextOrderId != null",
                  partner,
                  user.getId())
              .order("-nextOrderId")
              .fetchOne();

      char firstLetter = 'A';
      if (otherUser != null) {
        String otherOrderId = otherUser.getNextOrderId();
        firstLetter = otherOrderId.charAt(0);
        firstLetter++;
      }

      orderId = String.valueOf(firstLetter) + "000";
      user.setNextOrderId(orderId);
      ebicsUserRepo.save(user);
    } else {
      orderId = getNextOrderNumber(orderId);
      user.setNextOrderId(orderId);
      ebicsUserRepo.save(user);
    }

    return orderId;
  }

  public String getNextOrderNumber(String orderId) throws AxelorException {

    if (Strings.isNullOrEmpty(orderId) || orderId.matches("[^a-z0-9 ]") || orderId.length() != 4) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD, I18n.get("Invalid order id \"%s\""), orderId);
    }

    if (orderId.substring(1).equals("ZZZ")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD, I18n.get("Maximum order limit reach"));
    }

    char[] orderIds = orderId.toCharArray();

    if (orderIds[3] != 'Z') {
      orderIds[3] = getNextChar(orderIds[3]);
    } else {
      orderIds[3] = '0';
      if (orderIds[2] != 'Z') {
        orderIds[2] = getNextChar(orderIds[2]);
      } else {
        orderIds[2] = '0';
        if (orderIds[1] != 'Z') {
          orderIds[1] = getNextChar(orderIds[1]);
        }
      }
    }

    return new String(orderIds);
  }

  private char getNextChar(char c) {

    if (c == '9') {
      return 'A';
    }

    return (char) ((int) c + 1);
  }

  private void trace(EbicsRequestLog requestLog, EbicsRootElement[] rootElements)
      throws JDOMException, IOException, EbicsLibException {

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    rootElements[0].save(bout);
    requestLog.setRequestTraceText(bout.toString());
    bout.close();

    bout = new ByteArrayOutputStream();
    rootElements[1].save(bout);
    requestLog.setResponseTraceText(bout.toString());
    bout.close();
  }
}
