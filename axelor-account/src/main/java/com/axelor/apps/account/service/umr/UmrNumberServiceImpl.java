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
package com.axelor.apps.account.service.umr;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public class UmrNumberServiceImpl implements UmrNumberService {

  @Override
  public String getUmrNumber(InvoicingPaymentSituation invoicingPaymentSituation, LocalDate date) {
    return getUmrNumber(invoicingPaymentSituation, false, date);
  }

  @Override
  public String getUmrNumber(
      InvoicingPaymentSituation invoicingPaymentSituation, boolean isRecovery, LocalDate date) {
    Partner partner = invoicingPaymentSituation.getPartner();
    Company company = invoicingPaymentSituation.getCompany();
    if (partner == null || company == null) {
      return "";
    }

    StringJoiner rumNumber = new StringJoiner("/");
    rumNumber.add(company.getCode());

    if (isRecovery) {
      rumNumber.add("++");
    }
    rumNumber
        .add(partner.getPartnerSeq())
        .add(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

    String result = rumNumber.toString();

    if (ObjectUtils.isEmpty(invoicingPaymentSituation.getUmrList())) {
      return result;
    }

    long umrSize =
        invoicingPaymentSituation.getUmrList().stream()
            .filter(
                umr ->
                    StringUtils.notEmpty(umr.getUmrNumber()) && umr.getUmrNumber().contains(result))
            .count();
    if (umrSize == 0) {
      return result;
    }

    return String.format("%s -%s", result, umrSize);
  }
}
