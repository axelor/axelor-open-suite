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
package com.axelor.apps.bankpayment.ebics.service;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface EbicsPartnerService {

  List<BankStatement> getBankStatements(EbicsPartner ebicsPartner)
      throws AxelorException, IOException;

  List<BankStatement> getBankStatements(
      EbicsPartner ebicsPartner,
      Collection<BankStatementFileFormat> bankStatementFileFormatCollection)
      throws AxelorException, IOException;

  /**
   * Check if bank details miss mandatory currency
   *
   * @param ebicsPartner
   * @throws AxelorException with the name of the bank details missing currency if the currency is
   *     mandatory
   */
  void checkBankDetailsMissingCurrency(EbicsPartner ebicsPartner) throws AxelorException;
}
