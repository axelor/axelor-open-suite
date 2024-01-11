/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.ebics.xml;

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

import com.axelor.apps.bankpayment.ebics.client.EbicsSession;
import com.axelor.apps.bankpayment.ebics.client.EbicsUtils;
import com.axelor.apps.bankpayment.ebics.client.OrderType;
import com.axelor.apps.bankpayment.ebics.client.UnsecuredRequestElement;
import com.axelor.apps.base.AxelorException;
import javax.xml.XMLConstants;

/**
 * The <code>HIARequestElement</code> is the root element used to send the authentication and
 * encryption keys to the ebics bank server
 *
 * @author hachani
 */
public class HIARequestElement extends DefaultEbicsRootElement {

  /**
   * Constructs a new HIA Request root element
   *
   * @param session the current ebics session
   * @param orderId the order id, if null a random one is generated.
   */
  public HIARequestElement(EbicsSession session) {
    super(session);
  }

  @Override
  public String getName() {
    return "HIARequest.xml";
  }

  @Override
  public void build() {
    HIARequestOrderDataElement requestOrderData;

    requestOrderData = new HIARequestOrderDataElement(session);
    try {
      requestOrderData.build();

      unsecuredRequest =
          new UnsecuredRequestElement(
              session, OrderType.HIA, EbicsUtils.zip(requestOrderData.prettyPrint()));
      unsecuredRequest.build();

    } catch (Exception e) {
      e.printStackTrace();
    }
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
