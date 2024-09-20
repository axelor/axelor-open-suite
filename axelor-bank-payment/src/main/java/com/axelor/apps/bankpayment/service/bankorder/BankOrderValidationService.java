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
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.base.AxelorException;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import javax.xml.datatype.DatatypeConfigurationException;

public interface BankOrderValidationService {

  void validateFromBankOrder(InvoicePayment invoicePayment, boolean force)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException;

  void realize(BankOrder bankOrder)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException;

  void validatePayment(BankOrder bankOrder)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException;

  void confirm(BankOrder bankOrder)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException;
}
