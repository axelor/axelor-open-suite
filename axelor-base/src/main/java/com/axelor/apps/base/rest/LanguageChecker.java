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
