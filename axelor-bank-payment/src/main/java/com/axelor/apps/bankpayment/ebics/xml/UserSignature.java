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

import javax.xml.XMLConstants;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private final Logger log = LoggerFactory.getLogger( getClass() );

	
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
        	System.out.println("\n");
        	System.out.println(new String(toSign));
        	System.out.println("\n");

        	signature = EbicsUserService.removeOSSpecificChars(toSign);

        	signature = DatatypeConverter.parseHexBinary(new String(signature));
        	
            System.out.println("Signature length (in bytes) : " + signature.length);
        	
        	System.out.println("\n");
        	System.out.println(new String(signature));
        	System.out.println("\n");
        	
        }
        else  {
        	signature = Beans.get(EbicsUserService.class).sign(user, toSign);
        	System.out.println(new String(signature));
        }
    } catch (Exception e) {
    	e.printStackTrace();
    } 
    
    
    
    // Include certificate informations
    
//    EbicsCertificate ebicsEertificate = user.getA005Certificate();

//    X509DataType x509Data = EbicsXmlFactory.createX509DataType(ebicsEertificate.getSubject(), 
//    		ebicsEertificate.getCertificate(), ebicsEertificate.getIssuer(), new BigInteger(ebicsEertificate.getSerial(), 16));
    
//    orderSignatureData = EbicsXmlFactory.createOrderSignatureDataType(signatureVersion,
//            user.getEbicsPartner().getPartnerId(),
//            user.getUserId(),
//            signature,
//            x509Data);
    
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
	addNamespaceDecl("xsi", "http://www.w3.org/2001/XMLSchema-instance");

	setSaveSuggestedPrefixes("http://www.ebics.org/S001", XMLConstants.DEFAULT_NS_PREFIX);
    
	insertSchemaLocation("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "xsi", "http://www.ebics.org/S001 http://www.ebics.org/S001/ebics_signature.xsd");

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



