/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.ebics.io;

import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.client.EbicsUtils;
import com.axelor.apps.bankpayment.ebics.service.EbicsUserService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.inject.Beans;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

/**
 * A simple mean to join downloaded segments from the bank ebics server.
 *
 * @author Hachani
 */
public class Joiner {

  /**
   * Constructs a new <code>Joiner</code> object.
   *
   * @param user the ebics user.
   */
  public Joiner(EbicsUser user) {
    this.user = user;
    buffer = new ByteArrayOutputStream();
  }

  public void append(byte[] data) throws AxelorException {
    try {
      buffer.write(data);
      buffer.flush();
    } catch (IOException e) {
      throw new AxelorException(e.getCause(), TraceBackRepository.TYPE_TECHNICAL, e.getMessage());
    }
  }

  /**
   * Writes the joined part to an output stream.
   *
   * @param output the output stream.
   * @param transactionKey the transaction key
   * @throws EbicsException
   */
  public void writeTo(OutputStream output, byte[] transactionKey) throws AxelorException {
    try {
      byte[] decrypted;

      buffer.close();
      decrypted =
          Beans.get(EbicsUserService.class).decrypt(user, buffer.toByteArray(), transactionKey);
      output.write(EbicsUtils.unzip(decrypted));
      output.close();
    } catch (GeneralSecurityException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    } catch (IOException e) {
      throw new AxelorException(e.getCause(), TraceBackRepository.TYPE_TECHNICAL, e.getMessage());
    }
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsUser user;
  private ByteArrayOutputStream buffer;
}
