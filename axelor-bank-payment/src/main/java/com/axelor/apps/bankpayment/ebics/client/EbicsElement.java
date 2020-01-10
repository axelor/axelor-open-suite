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
package com.axelor.apps.bankpayment.ebics.client;

import com.axelor.exception.AxelorException;
import java.io.PrintStream;

public interface EbicsElement {

  /**
   * Returns the name of this <code>EbicsElement</code>
   *
   * @return the name of the element
   */
  public String getName();

  /**
   * Builds the <code>EbicsElement</code> XML fragment
   *
   * @throws EbicsException
   */
  public void build() throws AxelorException;

  /**
   * Prints the <code>EbicsElement</code> into the given stream.
   *
   * @param stream the print stream
   */
  public void print(PrintStream stream);
}
