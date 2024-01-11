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

import com.axelor.apps.bankpayment.db.EbicsCertificate;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.service.EbicsUserService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.math.BigInteger;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A root EBICS element representing the user signature element. The user data is signed with the
 * user signature key sent in the INI request to the EBICS bank server
 *
 * @author hachani
 */
public class UserSignatureVerify {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private EbicsUser user;
  private byte[] signature;
  private byte[] bankOrderContent;
  private String modulus;
  private String exponent;

  /**
   * Constructs a new <code>UserSignature</code> element for an Ebics user and a data to sign
   *
   * @param user the ebics user
   * @param signatureVersion the signature version
   * @param toSign the data to be signed
   */
  public UserSignatureVerify(EbicsUser user, byte[] bankOrderContent, byte[] signature) {
    this.user = user;
    this.bankOrderContent = bankOrderContent;
    this.signature = signature;
    loadCertificate();
  }

  public void verify() throws AxelorException {

    String comptedSha256Digest = computeSha256Digest();
    String originalDigestFromSignature = getOriginalDigestFromSignature();
    if (!comptedSha256Digest.equals(originalDigestFromSignature)) {
      String message =
          I18n.get(
                  "Computed digest (SHA256) of the bank order file doesn't match with the digest extract from the signature")
              + " \n";
      message +=
          I18n.get("Computed digest (SHA256) of the bank order file:")
              + " "
              + comptedSha256Digest
              + "\n";
      message +=
          I18n.get("Original digest extracted from the signature:")
              + " "
              + originalDigestFromSignature;
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message);
    }
  }

  public void loadCertificate() {

    EbicsCertificate certificate = user.getA005Certificate();

    this.modulus = certificate.getPublicKeyModulus();
    this.exponent = certificate.getPublicKeyExponent();
  }

  public String computeSha256Digest() {

    bankOrderContent = EbicsUserService.removeOSSpecificChars(bankOrderContent);

    String sha = DigestUtils.sha256Hex(bankOrderContent);

    log.debug("Digest (SHA256) of bank order content : {}", sha);

    return sha;
  }

  public String getOriginalDigestFromSignature() {

    String hexSignature = getHexSignature();

    System.out.println("Signature (HEX STRING) : " + hexSignature);

    String result =
        new BigInteger(hexSignature, 16)
            .modPow(new BigInteger(exponent, 16), new BigInteger(modulus, 16))
            .toString(16);

    log.debug("Orignal digest from signature (with padding) : {}", result);

    result = result.substring(result.length() - 64, result.length());

    log.debug("Orignal digest from signature : {}", result);

    return result;
  }

  public String getHexSignature() {

    return Hex.encodeHexString(signature);
  }
}
