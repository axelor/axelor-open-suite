/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.auth.db.User;
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

  Integer checkOtherInvoiceTerms(List<InvoiceTerm> invoiceTermList);

  int getPfpValidateStatusSelect(InvoiceTerm invoiceTerm);

  boolean getUserCondition(User pfpValidatorUser, User user);

  boolean getInvoiceTermsCondition(List<InvoiceTerm> invoiceTermList);

  void initPftPartialValidation(
      InvoiceTerm originalInvoiceTerm, BigDecimal grantedAmount, PfpPartialReason partialReason);

  boolean generateInvoiceTermsAfterPfpPartial(List<InvoiceTerm> invoiceTermList)
      throws AxelorException;
}
