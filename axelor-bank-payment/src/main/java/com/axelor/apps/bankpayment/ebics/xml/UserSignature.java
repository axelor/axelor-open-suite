/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.ebics.schema.s001.OrderSignatureDataType;
import com.axelor.apps.account.ebics.schema.s001.UserSignatureDataSigBookType;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.bankpayment.ebics.service.EbicsUserService;
import com.axelor.inject.Beans;


/**
 * A root EBICS element representing the user signature
 * element. The user data is signed with the user signature
 * key sent in the INI request to the EBICS bank server
 *
 * @author hachani
 *
 */
public class UserSignature extends DefaultEbicsRootElement {
	
  /**
   * Constructs a new <code>UserSignature</code> element for
   * an Ebics user and a data to sign
   * @param user the ebics user
   * @param signatureVersion the signature version
   * @param toSign the data to be signed
   */
  public UserSignature(EbicsUser user,
                       String name,
                       String signatureVersion,
                       byte[] toSign)
  {
    this.user = user;
    this.toSign = toSign;
    this.name = name;
    this.signatureVersion = signatureVersion;
  }
  

  @Override
  public void build() {
    UserSignatureDataSigBookType 	userSignatureData;
    OrderSignatureDataType		orderSignatureData;
    byte[]				signature = null;

    try {
        if(user.getEbicsTypeSelect() == EbicsUserRepository.EBICS_TYPE_TS)  {
        	signature = toSign;
        }
        else  {
        	signature = Beans.get(EbicsUserService.class).sign(user, toSign);
        }
    } catch (Exception e) {
    	e.printStackTrace();
    	//throw new AxelorException(e.getMessage(), IException.CONFIGURATION_ERROR );
    } 

    orderSignatureData = EbicsXmlFactory.createOrderSignatureDataType(signatureVersion,
                                                                      user.getEbicsPartner().getPartnerId(),
                                                                      user.getUserId(),
                                                                      signature);
    userSignatureData = EbicsXmlFactory.createUserSignatureDataSigBookType(new OrderSignatureDataType[] {orderSignatureData});
    document = EbicsXmlFactory.createUserSignatureDataDocument(userSignatureData);
  }

  @Override
  public String getName() {
    return name + ".xml";
  }

  @Override
  public byte[] toByteArray() {
    setSaveSuggestedPrefixes("http://www.ebics.org/S001", "");

    return super.toByteArray();
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsUser 			user;
  private String 			signatureVersion;
  private byte[]			toSign;
  private String			name;
}
