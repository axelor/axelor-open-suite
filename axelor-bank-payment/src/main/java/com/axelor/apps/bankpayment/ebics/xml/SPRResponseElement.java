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

import com.axelor.apps.account.ebics.schema.h003.EbicsResponseDocument;
import com.axelor.apps.account.ebics.schema.h003.EbicsResponseDocument.EbicsResponse;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.ebics.exception.ReturnCode;
import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.exception.AxelorException;

/**
 * The <code>SPRResponseElement</code> is the response element for an ebics subscriber revoking.
 *
 * @author Hachani
 */
public class SPRResponseElement extends DefaultResponseElement {

  /**
   * Constructs a new SPR response element.
   *
   * @param factory the content factory
   */
  public SPRResponseElement(ContentFactory factory, EbicsUser ebicsUser) {
    super(factory, "SPRResponse.xml", ebicsUser);
  }

  @Override
  public void build() throws AxelorException {
    String code;
    String text;

    parse(factory);
    response = ((EbicsResponseDocument) document).getEbicsResponse();
    code = response.getHeader().getMutable().getReturnCode();
    text = response.getHeader().getMutable().getReportText();
    returnCode = ReturnCode.toReturnCode(code, text);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private EbicsResponse response;
}
