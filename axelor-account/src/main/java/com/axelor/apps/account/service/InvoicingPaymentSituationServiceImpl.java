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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InvoicingPaymentSituationServiceImpl implements InvoicingPaymentSituationService {

  @Inject
  public InvoicingPaymentSituationServiceImpl() {}

  @Override
  public String getCompanyDomain(
      InvoicingPaymentSituation invoicingPaymentSituation, Partner partner) {
    if (invoicingPaymentSituation == null || partner == null) {
      return "self.id = 0";
    }
    String domain = "(self.archived = false OR self.archived is null)";
    List<InvoicingPaymentSituation> partnerInvoicingPaymentSituationList =
        new ArrayList<>(partner.getInvoicingPaymentSituationList());
    partnerInvoicingPaymentSituationList.remove(invoicingPaymentSituation);
    if (ObjectUtils.isEmpty(partnerInvoicingPaymentSituationList)) {
      return domain;
    }

    domain =
        domain.concat(
            String.format(
                " AND self.id NOT IN (%s)",
                StringHelper.getIdListString(
                    partnerInvoicingPaymentSituationList.stream()
                        .map(InvoicingPaymentSituation::getCompany)
                        .collect(Collectors.toList()))));

    return domain;
  }
}
