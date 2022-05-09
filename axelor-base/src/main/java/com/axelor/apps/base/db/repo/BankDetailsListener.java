/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import javax.persistence.PostUpdate;

public class BankDetailsListener {
  @PostUpdate
  private void onPostUpdate(BankDetails bankDetails) throws AxelorException {
    if (bankDetails.getActive()) {
      Company company = bankDetails.getCompany();

      if (company != null) {
        for (BankDetails details : company.getBankDetailsList()) {
          if (!details.getId().equals(bankDetails.getId())
              && details.getIban().equals(bankDetails.getIban())
              && details.getActive()) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(IExceptionMessage.DUPLICATE_ACTIVE_BANK_DETAILS));
          }
        }
      }
    }
  }
}
