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

import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;

public class AccountingReportTypeServiceImpl implements AccountingReportTypeService {
  @Override
  public void setDefaultName(AccountingReportType accountingReportType) {
    if (accountingReportType.getTypeSelect() != null) {
      String name =
          I18n.get(
              MetaStore.getSelectionItem(
                      "accounting.report.type.select",
                      accountingReportType.getTypeSelect().toString())
                  .getTitle());
      accountingReportType.setName(name);
    }
  }
}
