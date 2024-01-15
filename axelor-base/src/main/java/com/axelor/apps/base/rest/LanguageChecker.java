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
package com.axelor.apps.base.rest;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import javax.ws.rs.NotFoundException;

public class LanguageChecker {

  public static void check(String languageCode) throws NotFoundException {
    Query<Language> query = Beans.get(LanguageRepository.class).all().filter("self.code = :code ");
    query.bind("code", languageCode);
    if (query.count() == 0) {
      throw new NotFoundException("Language with code " + languageCode + " was not found.");
    }
  }
}
