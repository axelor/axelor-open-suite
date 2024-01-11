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
package com.axelor.apps.bankpayment.ebics.client;

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

import com.axelor.apps.base.AxelorException;
import java.io.OutputStream;
import org.jdom.JDOMException;

/**
 * An Ebics root element knows its name.
 *
 * @author hachani
 */
public interface EbicsRootElement extends EbicsElement {

  /**
   * Converts the <code>EbicsElement</code> to a byte array
   *
   * @return the equivalent byte array of this <code>EbicsElement</code>
   */
  public byte[] toByteArray();

  /**
   * Validates the request element according to the EBICS XML schema specification
   *
   * @throws EbicsException throws an EbicsException when validation fails
   */
  public void validate() throws AxelorException;

  /**
   * Adds a namespace declaration for the <code>EbicsRootElement</code>
   *
   * @param prefix namespace prefix
   * @param uri namespace uri
   */
  public void addNamespaceDecl(String prefix, String uri);

  /**
   * Saves the <code>EbicsElement</code> into a given output stream.
   *
   * @param out the output stream
   * @throws EbicsException the save operation fails
   */
  public void save(OutputStream out) throws AxelorException, JDOMException;
}
