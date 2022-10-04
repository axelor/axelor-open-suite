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

import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.client.EbicsProduct;
import com.axelor.exception.AxelorException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import org.jdom.JDOMException;

public interface EbicsService {

  public String makeDN(EbicsUser ebicsUser);

  public RSAPublicKey getPublicKey(String modulus, String exponent)
      throws NoSuchAlgorithmException, InvalidKeySpecException;

  /**
   * Sends an INI request to the ebics bank server
   *
   * @param userId the user ID
   * @param product the application product
   * @throws AxelorException
   * @throws JDOMException
   * @throws IOException
   */
  public void sendINIRequest(EbicsUser ebicsUser, EbicsProduct product) throws AxelorException;

  /**
   * Sends a HIA request to the ebics server.
   *
   * @param userId the user ID.
   * @param product the application product.
   * @throws AxelorException
   */
  public void sendHIARequest(EbicsUser ebicsUser, EbicsProduct product) throws AxelorException;

  /**
   * Sends a HPB request to the ebics server.
   *
   * @param userId the user ID.
   * @param product the application product.
   * @throws AxelorException
   */
  public X509Certificate[] sendHPBRequest(EbicsUser user, EbicsProduct product)
      throws AxelorException;

  /**
   * Sends the SPR order to the bank.
   *
   * @param userId the user ID
   * @param product the session product
   * @throws AxelorException
   */
  public void sendSPRRequest(EbicsUser ebicsUser, EbicsProduct product) throws AxelorException;

  /**
   * Send a file to the EBICS bank server.
   *
   * @param transportUser
   * @param signatoryUser
   * @param product
   * @param file
   * @param format
   * @param signature
   * @throws AxelorException
   */
  public void sendFULRequest(
      EbicsUser transportUser,
      EbicsUser signatoryUser,
      EbicsProduct product,
      File file,
      BankOrderFileFormat format,
      File signature)
      throws AxelorException;

  public File sendFDLRequest(
      EbicsUser user, EbicsProduct product, Date start, Date end, String fileFormat)
      throws AxelorException;

  public File sendHTDRequest(EbicsUser user, EbicsProduct product, Date start, Date end)
      throws AxelorException;

  public File sendPTKRequest(EbicsUser user, EbicsProduct product, Date start, Date end)
      throws AxelorException;

  public File sendHPDRequest(EbicsUser user, EbicsProduct product, Date start, Date end)
      throws AxelorException;

  public void addResponseFile(EbicsUser user, File file) throws IOException;
}
