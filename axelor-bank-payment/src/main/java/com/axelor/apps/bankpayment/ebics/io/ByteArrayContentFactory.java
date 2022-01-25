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
package com.axelor.apps.bankpayment.ebics.io;

import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Byte array content factory that delivers the file content as a <code>ByteArrayInputStream</code>.
 * This object is serializable in a way to recover interrupted file transfers.
 *
 * @author hachani
 */
public class ByteArrayContentFactory implements ContentFactory {

  /**
   * Constructs a new <code>ByteArrayContentFactory</code> with a given byte array content.
   *
   * @param content the byte array content
   */
  public ByteArrayContentFactory(byte[] content) {
    this.content = content;
  }

  @Override
  public InputStream getContent() throws IOException {
    return new ByteArrayInputStream(content);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private byte[] content;
}
