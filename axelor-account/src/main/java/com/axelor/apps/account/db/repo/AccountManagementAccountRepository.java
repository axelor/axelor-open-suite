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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import java.util.Map;
import javax.persistence.PersistenceException;

public class AccountManagementAccountRepository extends AccountManagementRepository {

  @Override
  public Map<String, Object> validate(Map<String, Object> json, Map<String, Object> context) {
    boolean alreadyExists =
        all()
                .filter(
                    "self.interbankCodeLine = :interbankCodeLine and self.bankDetails = :bankDetails and self.paymentMode = :paymentMode and (:id = null or self.id != :id)")
                .bind("interbankCodeLine", json.get("interbankCodeLine"))
                .bind("bankDetails", json.get("bankDetails"))
                .bind("paymentMode", json.get("paymentMode"))
                .bind("id", json.get("id"))
                .count()
            > 0;
    if (alreadyExists) {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ACCOUNT_MANAGEMENT_ALREADY_EXISTS));
      } catch (AxelorException e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
        throw new PersistenceException(e.getMessage(), e);
      }
    }
    return super.validate(json, context);
  }
}
