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

import com.axelor.apps.account.db.AccountEquiv;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.common.StringUtils;
import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountEquivServiceImpl implements AccountEquivService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public String getFromAccountDomain(AccountEquiv accountEquiv, FiscalPosition fiscalPosition) {
    String idListStr =
        fiscalPosition.getAccountEquivList().stream()
            .filter(ae -> !accountEquiv.equals(ae))
            .map(ae -> ae.getFromAccount().getId().toString())
            .collect(Collectors.joining(","));
    StringBuilder domain = new StringBuilder("self.id NOT IN (");
    if (StringUtils.notEmpty(idListStr)) {
      domain.append(idListStr);
    } else {
      domain.append("0");
    }
    domain.append(")");
    return domain.toString();
  }
}
