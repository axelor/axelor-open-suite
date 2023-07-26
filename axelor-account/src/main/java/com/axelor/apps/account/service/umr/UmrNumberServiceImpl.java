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
package com.axelor.apps.account.service.umr;

import com.axelor.apps.base.db.Partner;
import java.text.SimpleDateFormat;
import java.time.Instant;
import net.fortuna.ical4j.model.Date;

public class UmrNumberServiceImpl implements UmrNumberService {

  @Override
  public String getUmrNumber(Partner partner) {
    return getUmrNumber(partner, false);
  }

  @Override
  public String getUmrNumber(Partner partner, boolean isRecovery) {

    String rumNumber = "";

    if (isRecovery) {
      rumNumber += "++";
    }

    rumNumber += partner.getPartnerSeq();

    rumNumber += "/" + new SimpleDateFormat("yyyyMMdd").format(Date.from(Instant.now()));

    return rumNumber;
  }
}
