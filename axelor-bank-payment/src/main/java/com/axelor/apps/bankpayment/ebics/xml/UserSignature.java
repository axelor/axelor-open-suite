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

import com.axelor.apps.account.ebics.schema.s001.OrderSignatureDataType;
import com.axelor.apps.account.ebics.schema.s001.UserSignatureDataSigBookType;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;
import com.axelor.apps.bankpayment.ebics.service.EbicsUserService;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import java.util.Base64;
import javax.xml.XMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A root EBICS element representing the user signature element. The user data is signed with the
 * user signature key sent in the INI request to the EBICS bank server
 *
 * @author hachani
 */
public class UserSignature extends DefaultEbicsRootElement {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private EbicsUser user;
  private String signatureVersion;
  private byte[] data;
  private byte[] signature;
  private String name;

  /**
   * Constructs a new <code>UserSignature</code> element for an Ebics user and a data to sign
   *
   * @param user the ebics user
   * @param signatureVersion the signature version
   * @param toSign the data to be signed
   */
  public UserSignature(
      EbicsUser user, String name, String signatureVersion, byte[] data, byte[] signature) {
    this.user = user;
    this.data = data;
    this.signature = signature;
    this.name = name;
    this.signatureVersion = signatureVersion;
  }

  @Override
  public void build() throws AxelorException {
    UserSignatureDataSigBookType userSignatureData;
    OrderSignatureDataType orderSignatureData;

    try {

      if (user.getEbicsPartner().getEbicsTypeSelect() == EbicsPartnerRepository.EBICS_TYPE_TS) {

        log.debug("Signature (base64) : {}", new String(signature));
        log.debug("Signature (base64) length : {}", signature.length);

        signature = EbicsUserService.removeOSSpecificChars(signature);

        log.debug(
            "Signature (base64) length after remove OS specific chars : {}", signature.length);

        signature = Base64.getDecoder().decode(signature);

        log.debug("Signature (byte) length : {}", signature.length);

      } else {
        signature = Beans.get(EbicsUserService.class).sign(user, data);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    UserSignatureVerify userSignatureVerify = new UserSignatureVerify(user, data, signature);
    userSignatureVerify.verify();

    /**
     * Include certificate information
     *
     * <p>EbicsCertificate ebicsEertificate = user.getA005Certificate();
     *
     * <p>X509DataType x509Data = EbicsXmlFactory.createX509DataType(ebicsEertificate.getSubject(),
     * ebicsEertificate.getCertificate(), ebicsEertificate.getIssuer(), new
     * BigInteger(ebicsEertificate.getSerial(), 16));
     *
     * <p>orderSignatureData = EbicsXmlFactory.createOrderSignatureDataType(signatureVersion,
     * user.getEbicsPartner().getPartnerId(), user.getUserId(), signature, x509Data); *
     */
    orderSignatureData =
        EbicsXmlFactory.createOrderSignatureDataType(
            signatureVersion, user.getEbicsPartner().getPartnerId(), user.getUserId(), signature);
    userSignatureData =
        EbicsXmlFactory.createUserSignatureDataSigBookType(
            new OrderSignatureDataType[] {orderSignatureData});
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

    insertSchemaLocation(
        "http://www.w3.org/2001/XMLSchema-instance",
        "schemaLocation",
        "xsi",
        "http://www.ebics.org/S001 http://www.ebics.org/S001/ebics_signature.xsd");

    return super.toByteArray();
  }
}
