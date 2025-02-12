/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.auth.db.User;
import java.util.List;

public interface InvoiceTermPfpToolService {

  int getPfpValidateStatusSelect(InvoiceTerm invoiceTerm);

  List<Integer> getAlreadyValidatedStatusList();

  boolean canUpdateInvoiceTerm(InvoiceTerm invoiceTerm, User currentUser);

  boolean checkPfpValidatorUser(InvoiceTerm invoiceTerm);

  User getPfpValidatorUser(Partner partner, Company company);

  boolean isPfpValidatorUser(InvoiceTerm invoiceTerm, User user);
}
