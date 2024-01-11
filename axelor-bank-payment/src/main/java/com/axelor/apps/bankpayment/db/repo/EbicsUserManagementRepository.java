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
package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.EbicsRequestLog;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.inject.Beans;
import java.util.List;

public class EbicsUserManagementRepository extends EbicsUserRepository {

  @Override
  public void remove(EbicsUser entity) {
    EbicsRequestLogRepository ebicsRequestLogRepository =
        Beans.get(EbicsRequestLogRepository.class);
    List<EbicsRequestLog> ebicsRequestLogList =
        ebicsRequestLogRepository.all().filter("self.ebicsUser = ?1", entity).fetch();
    for (EbicsRequestLog ebicsRequestLog : ebicsRequestLogList) {
      ebicsRequestLogRepository.remove(ebicsRequestLog);
    }
    super.remove(entity);
  }
}
