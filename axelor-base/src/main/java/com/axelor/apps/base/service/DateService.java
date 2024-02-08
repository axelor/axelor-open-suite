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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DateService {

  public LocalDate date() {
    return Beans.get(AppBaseService.class)
        .getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
  }

  public DateTimeFormatter getDateFormat() throws AxelorException {
    return Beans.get(CompanyDateService.class)
        .getDateFormat(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
  }

  public DateTimeFormatter getDateTimeFormat() throws AxelorException {
    return Beans.get(CompanyDateService.class)
        .getDateTimeFormat(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
  }
}
