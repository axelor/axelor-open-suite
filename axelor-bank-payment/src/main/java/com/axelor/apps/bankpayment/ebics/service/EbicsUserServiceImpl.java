/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.ebics.service;

import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsRequestLog;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.EbicsRequestLogRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.bankpayment.ebics.client.EbicsRootElement;
import com.axelor.apps.bankpayment.ebics.client.EbicsUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom.JDOMException;

public class EbicsUserServiceImpl implements EbicsUserService {

  protected final EbicsRequestLogRepository requestLogRepo;
  protected final EbicsUserRepository ebicsUserRepo;

  @Inject
  public EbicsUserServiceImpl(
      EbicsRequestLogRepository requestLogRepo, EbicsUserRepository ebicsUserRepo) {
    this.requestLogRepo = requestLogRepo;
    this.ebicsUserRepo = ebicsUserRepo;
  }

  @Override
  public byte[] sign(EbicsUser ebicsUser, byte[] digest)
      throws IOException, GeneralSecurityException {

    Signature signature =
        Signature.getInstance("SHA256WithRSA", BouncyCastleProvider.PROVIDER_NAME);
    signature.initSign(getPrivateKey(ebicsUser.getA005Certificate().getPrivateKey()));
    signature.update(EbicsUserService.removeOSSpecificChars(digest));
    return signature.sign();
  }

  protected RSAPrivateKey getPrivateKey(byte[] encoded)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey) kf.generatePrivate(keySpec);
  }

  @Override
  public byte[] authenticate(EbicsUser ebicsUser, byte[] digest) throws GeneralSecurityException {
    Signature signature;
    signature = Signature.getInstance("SHA256WithRSA", BouncyCastleProvider.PROVIDER_NAME);
    signature.initSign(getPrivateKey(ebicsUser.getX002Certificate().getPrivateKey()));
    signature.update(digest);
    return signature.sign();
  }

  @Override
  public byte[] decrypt(EbicsUser user, byte[] encryptedData, byte[] transactionKey)
      throws AxelorException, GeneralSecurityException, IOException {
    Cipher cipher;
    int blockSize;
    ByteArrayOutputStream outputStream;

    cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
    cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(user.getE002Certificate().getPrivateKey()));
    blockSize = cipher.getBlockSize();
    outputStream = new ByteArrayOutputStream();
    for (int j = 0; j * blockSize < transactionKey.length; j++) {
      outputStream.write(cipher.doFinal(transactionKey, j * blockSize, blockSize));
    }

    return decryptData(encryptedData, outputStream.toByteArray());
  }

  /**
   * Decrypts the <code>encryptedData</code> using the decoded transaction key.
   *
   * <p>EBICS Specification 2.4.2 - 15.2 Workflows at the recipient’s end:
   *
   * <p><b>Decryption of the message</b>
   *
   * <p>The encrypted original message is decrypted in CBC mode in accordance with the 2-key triple
   * DES process via the secret DES key (comprising DEK<SUB>left</SUB> and DEK<SUP>right<SUB>). In
   * doing this, the following initialization value ICV is again used.
   *
   * <p><b>Removal of the padding information</b>
   *
   * <p>The method “Padding with Octets” according to ANSI X9.23 is used to remove the padding
   * information from the decrypted message. The original message is then available in decrypted
   * form.
   *
   * @param input The encrypted data
   * @param key The secret key.
   * @return The decrypted data sent from the EBICS bank.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  protected byte[] decryptData(byte[] input, byte[] key) throws AxelorException {
    return EbicsUtils.decrypt(input, new SecretKeySpec(key, "EAS"));
  }

  @Override
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

  @Override
  @Transactional
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

  @Override
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

  protected char getNextChar(char c) {

    if (c == '9') {
      return 'A';
    }

    return (char) ((int) c + 1);
  }

  protected void trace(EbicsRequestLog requestLog, EbicsRootElement[] rootElements)
      throws AxelorException, JDOMException, IOException {

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
