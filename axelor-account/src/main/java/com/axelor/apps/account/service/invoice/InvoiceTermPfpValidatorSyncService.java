/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;

public interface InvoiceTermPfpValidatorSyncService {

  /**
   * Synchronizes pfpValidatorUser from Invoice to all its InvoiceTerms. Called when
   * Invoice.pfpValidatorUser changes.
   *
   * @param invoice The invoice with the updated pfpValidatorUser
   */
  void syncPfpValidatorFromInvoiceToTerms(Invoice invoice);

  /**
   * Synchronizes pfpValidatorUser from InvoiceTerm to Invoice based on rules:
   *
   * <ul>
   *   <li>If only 1 InvoiceTerm: update Invoice.pfpValidatorUser
   *   <li>If multiple InvoiceTerms with SAME pfpValidatorUser: update Invoice.pfpValidatorUser
   *   <li>If multiple InvoiceTerms with DIFFERENT pfpValidatorUsers: do NOT update Invoice
   * </ul>
   *
   * @param invoiceTerm The invoice term with the updated pfpValidatorUser
   * @return true if Invoice was updated, false otherwise
   */
  boolean syncPfpValidatorFromTermToInvoice(InvoiceTerm invoiceTerm);
}
