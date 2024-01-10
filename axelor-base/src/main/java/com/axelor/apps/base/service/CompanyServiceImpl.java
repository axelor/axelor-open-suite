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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBase;
import java.util.List;

public class CompanyServiceImpl implements CompanyService {

  /** {@inheritDoc} */
  @Override
  public void checkMultiBanks(Company company) {
    if (countActiveBankDetails(company) > 1) {
      AppBaseService appBaseService = Beans.get(AppBaseService.class);
      AppBase appBase = appBaseService.getAppBase();
      if (!appBase.getManageMultiBanks()) {
        appBaseService.setManageMultiBanks(true);
      }
    }
  }

  /**
   * Count the number of active bank details on the provided company.
   *
   * @param company the company on which we count the number of active bank details
   * @return the number of active bank details
   */
  protected int countActiveBankDetails(Company company) {
    int count = 0;
    List<BankDetails> bankDetailsList = company.getBankDetailsList();

    if (bankDetailsList != null) {
      for (BankDetails bankDetails : bankDetailsList) {
        if (bankDetails.getActive()) {
          ++count;
        }
      }
    }

    return count;
  }
}
