/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package com.axelor.apps.account.ebics.xml;

import com.axelor.apps.account.ebics.client.EbicsSession;
import com.axelor.apps.account.ebics.client.EbicsUtils;
import com.axelor.apps.account.ebics.client.OrderType;
import com.axelor.apps.account.ebics.client.UnsecuredRequestElement;
import com.axelor.exception.AxelorException;

/**
 * The <code>HIARequestElement</code> is the root element used
 * to send the authentication and encryption keys to the ebics
 * bank server
 *
 * @author hachani
 *
 */
public class HIARequestElement extends DefaultEbicsRootElement {

  /**
   * Constructs a new HIA Request root element
   * @param session the current ebics session
   * @param orderId the order id, if null a random one is generated.
   */
  public HIARequestElement(EbicsSession session, String orderId) {
    super(session);
    this.orderId = orderId;
  }

  @Override
  public String getName() {
    return "HIARequest.xml";
  }

  @Override
  public void build() {
    HIARequestOrderDataElement		requestOrderData;

    requestOrderData = new HIARequestOrderDataElement(session);
    try {
		requestOrderData.build();
		
		unsecuredRequest = new UnsecuredRequestElement(session,
                OrderType.HIA,
                orderId == null ? session.getUser().getEbicsPartner().getNextOrderId() : orderId,
                EbicsUtils.zip(requestOrderData.prettyPrint()));
				unsecuredRequest.build();

	} catch (Exception e) {
		e.printStackTrace();
	}
    
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/H003", "");

    return unsecuredRequest.toByteArray();
  }

  @Override
  public void validate() throws AxelorException {
    unsecuredRequest.validate();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private String			orderId;
  private UnsecuredRequestElement	unsecuredRequest;
}
