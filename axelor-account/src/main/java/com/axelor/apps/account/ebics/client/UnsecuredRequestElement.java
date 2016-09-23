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

package com.axelor.apps.account.ebics.client;

import com.axelor.app.AppSettings;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer.OrderData;
import com.axelor.apps.account.ebics.schema.h003.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Header;
import com.axelor.apps.account.ebics.schema.h003.EmptyMutableHeaderType;
import com.axelor.apps.account.ebics.schema.h003.OrderDetailsType;
import com.axelor.apps.account.ebics.schema.h003.ProductElementType;
import com.axelor.apps.account.ebics.schema.h003.UnsecuredRequestStaticHeaderType;
import com.axelor.apps.account.ebics.xml.EbicsXmlFactory;
import com.axelor.exception.AxelorException;

/**
 * The <code>UnsecuredRequestElement</code> is the common element
 * used for key management requests.
 *
 * @author hachani
 *
 */
public class UnsecuredRequestElement extends DefaultEbicsRootElement {

  /**
   * Constructs a Unsecured Request Element.
   * @param session the ebics session.
   * @param orderType the order type (INI | HIA).
   * @param orderId the order id, if null a random one is generated.
   */
  public UnsecuredRequestElement(EbicsSession session,
                                 OrderType orderType,
                                 String orderId,
                                 byte[] orderData)
  {
    super(session);
    this.orderType = orderType;
    this.orderId = orderId;
    this.orderData = orderData;
  }

  @Override
  public void build() {
    Header 					header;
    Body 					body;
    EmptyMutableHeaderType 			mutable;
    UnsecuredRequestStaticHeaderType 		xstatic;
    ProductElementType 				productType;
    OrderDetailsType 				orderDetails;
    DataTransfer 				dataTransfer;
    OrderData 					orderData;
    EbicsUnsecuredRequest			request;

    orderDetails = EbicsXmlFactory.createOrderDetailsType("DZNNN",
						          orderId == null ? session.getUser().getEbicsPartner().getNextOrderId() : orderId,
	                                                  orderType.getOrderType());

    productType = EbicsXmlFactory.creatProductElementType(AppSettings.get().get(""),
	                                                  session.getProduct().getName());

    try {
		xstatic = EbicsXmlFactory.createUnsecuredRequestStaticHeaderType(session.getBankID(),
									     session.getUser().getEbicsPartner().getPartnerId(),
									     session.getUser().getUserId(),
		                                                             productType,
		                                                             orderDetails,
		                                                             session.getUser().getSecurityMedium());
		mutable = EbicsXmlFactory.createEmptyMutableHeaderType();

	    header = EbicsXmlFactory.createHeader(true,
		                                  mutable,
		                                  xstatic);
	    
	    orderData = EbicsXmlFactory.createOrderData(this.orderData);
	    dataTransfer = EbicsXmlFactory.createDataTransfer(orderData);
	    body = EbicsXmlFactory.createBody(dataTransfer);
	    request = EbicsXmlFactory.createEbicsUnsecuredRequest(header,
		                                                  body,
		                                                  session.getConfiguration().getRevision(),
		                                                  session.getConfiguration().getVersion());

	    document = EbicsXmlFactory.createEbicsUnsecuredRequestDocument(request);
	    
	    
	} catch (AxelorException e) {
		e.printStackTrace();
		
	}
   
  }

  @Override
  public String getName() {
    return "UnsecuredRequest.xml";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private OrderType			orderType;
  private String			orderId;
  private byte[]			orderData;
  private static final long 		serialVersionUID = -3548730114599886711L;
}
