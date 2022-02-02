/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
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

  /** {@inheritDoc} */
  @Override
  public void handleTradingNames(Company company) throws AxelorException {
    if (Beans.get(AppBaseService.class).getAppBase().getEnableTradingNamesManagement())
      if (company.getTradingNameSet().isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(IExceptionMessage.MISSING_TRADING_NAME));
      } else if (company.getTradingNameSet().size() == 1) {
        company.setMainTradingName(company.getTradingNameSet().iterator().next());
      }
  }

  /**
   * Count the number of active bank details on the provided company.
   *
   * @param company the company on which we count the number of active bank details
   * @return the number of active bank details
   */
  private int countActiveBankDetails(Company company) {
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
