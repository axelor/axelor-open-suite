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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.utils.helpers.StringHelper;
import java.util.Collection;
import java.util.stream.Collectors;

public class BankCardServiceImpl implements BankCardService {
  @Override
  public String createDomainForBankCard(BankDetails bankDetails, Company company) {
    if (bankDetails != null) {
      return "self.id IN (" + StringHelper.getIdListString(bankDetails.getBankCardList()) + ")";
    }
    return "self.id IN ("
        + StringHelper.getIdListString(
            company.getBankDetailsList().stream()
                .map(BankDetails::getBankCardList)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()))
        + ")";
  }
}
