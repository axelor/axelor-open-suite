/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface InvoiceTermPfpService {
  void validatePfp(InvoiceTerm invoiceTerm, User currenctUser);

  Integer massValidatePfp(List<Long> invoiceTermIds);

  Integer massRefusePfp(
      List<Long> invoiceTermIds, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr);

  void refusalToPay(
      InvoiceTerm invoiceTerm, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr);

  void generateInvoiceTerm(
      InvoiceTerm originalInvoiceTerm,
      BigDecimal invoiceAmount,
      BigDecimal grantedAmount,
      PfpPartialReason partialReason)
      throws AxelorException;

  void initPftPartialValidation(
      InvoiceTerm originalInvoiceTerm, BigDecimal grantedAmount, PfpPartialReason partialReason);

  void generateInvoiceTermsAfterPfpPartial(Invoice originalInvoice) throws AxelorException;
}
