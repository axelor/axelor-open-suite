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
package com.axelor.apps.bankpayment.ebics.xml;

import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.client.EbicsRootElement;
import com.axelor.apps.bankpayment.ebics.exception.ReturnCode;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.apps.bankpayment.ebics.service.EbicsUserService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.inject.Beans;
import java.io.IOException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * The <code>DefaultResponseElement</code> is the common element for all ebics server responses.
 *
 * @author Hachani
 */
public abstract class DefaultResponseElement extends DefaultEbicsRootElement {

  /**
   * Constructs a new ebics response element.
   *
   * @param factory the content factory containing the response.
   * @param name the element name
   */
  public DefaultResponseElement(ContentFactory factory, String name, EbicsUser ebicsUser) {
    this.factory = factory;
    this.name = name;
    this.ebicsUser = ebicsUser;
  }

  /**
   * Parses the content of a <code>ContentFactory</code>
   *
   * @param factory the content factory
   * @throws EbicsException parse error
   */
  protected void parse(ContentFactory factory) throws AxelorException {
    try {
      document = XmlObject.Factory.parse(factory.getContent());
    } catch (XmlException | IOException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  /**
   * Reports the return code to the user.
   *
   * @throws EbicsException request fails.
   */
  public void report(EbicsRootElement[] rootElements) throws AxelorException {

    log(rootElements);

    if (!returnCode.isOk()) {
      returnCode.throwException();
    }
  }

  protected void log(EbicsRootElement[] rootElements) {
    if (ebicsUser != null && name != null && returnCode != null) {
      Beans.get(EbicsUserService.class)
          .logRequest(
              ebicsUser.getId(), name.substring(0, 3), returnCode.getSymbolicName(), rootElements);
      ebicsUser = null; // Prevent further log on same request
    }
  }

  @Override
  public String getName() {
    return name + ".xml";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private String name;
  private EbicsUser ebicsUser;
  protected ContentFactory factory;
  protected ReturnCode returnCode;
}
