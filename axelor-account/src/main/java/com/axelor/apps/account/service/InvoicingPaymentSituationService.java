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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import java.util.List;

public interface InvoicingPaymentSituationService {
  String getCompanyDomain(InvoicingPaymentSituation invoicingPaymentSituation, Partner partner);

  String getBankDetailsDomain(InvoicingPaymentSituation invoicingPaymentSituation, Partner partner);

  List<BankDetails> getAvailableBankDetailsList(
      InvoicingPaymentSituation invoicingPaymentSituation, Partner partner, Company company);

  InvoicingPaymentSituation initInvoicingPaymentSituation(
      InvoicingPaymentSituation invoicingPaymentSituation, Partner partner);

  /**
   * Clears the active UMR of every invoicing/payment situation of the given partner, so the partner
   * can be deleted without leaving a reference to an UMR removed by the same transaction.
   *
   * @param partner the partner about to be deleted
   */
  void clearActiveUmr(Partner partner);

  /**
   * Fetches, among the given partners, the ones holding an invoicing/payment situation with an
   * active UMR.
   *
   * @param partnerIdList the partners to check
   * @return the partners having an active UMR, ordered by name
   */
  List<Partner> getPartnerWithActiveUmrList(List<Long> partnerIdList);
}
