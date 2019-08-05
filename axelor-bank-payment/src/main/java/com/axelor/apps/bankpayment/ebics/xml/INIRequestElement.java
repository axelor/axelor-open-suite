/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.ebics.xml;

import com.axelor.apps.bankpayment.ebics.customer.EbicsSession;
import com.axelor.apps.bankpayment.ebics.customer.EbicsUtils;
import com.axelor.apps.bankpayment.ebics.customer.OrderType;
import com.axelor.apps.bankpayment.ebics.customer.UnsecuredRequestElement;
import com.axelor.exception.AxelorException;
import java.lang.invoke.MethodHandles;
import javax.xml.XMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The INI request XML element. This root element is to be sent to the ebics server to initiate the
 * signature certificate.
 *
 * @author hachani
 */
public class INIRequestElement extends DefaultEbicsRootElement {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Constructs a new INI request element.
   *
   * @param session the ebics session.
   * @param orderId the order id, if null a random one is generated.
   */
  public INIRequestElement(EbicsSession session) {
    super(session);
  }

  @Override
  public String getName() {
    return "INIRequest.xml";
  }

  @Override
  public void build() throws AxelorException {
    SignaturePubKeyOrderDataElement signaturePubKey;

    signaturePubKey = new SignaturePubKeyOrderDataElement(session);
    log.debug("SignaturePubKeyOrderDataElement OK");
    signaturePubKey.build();
    log.debug("signaturePubKey.build OK");
    unsecuredRequest =
        new UnsecuredRequestElement(
            session, OrderType.INI, EbicsUtils.zip(signaturePubKey.prettyPrint()));

    log.debug("UnsecuredRequestElement OK");
    unsecuredRequest.build();
    log.debug("unsecuredRequest.build OK");
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/H003", XMLConstants.DEFAULT_NS_PREFIX);

    return unsecuredRequest.toByteArray();
  }

  @Override
  public void validate() throws AxelorException {
    unsecuredRequest.validate();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private UnsecuredRequestElement unsecuredRequest;
}
