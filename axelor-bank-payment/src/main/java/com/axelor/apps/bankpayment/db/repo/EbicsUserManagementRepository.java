/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.EbicsRequestLog;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.module.BankPaymentModule;
import com.axelor.inject.Beans;
import java.util.List;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(BankPaymentModule.PRIORITY)
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
