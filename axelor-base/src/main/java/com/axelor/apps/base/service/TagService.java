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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Tag;
import com.axelor.apps.base.db.TradingName;
import java.util.Map;
import java.util.Set;

public interface TagService {
  void addMetaModelToTag(Tag tag, String fullName);

  Map<String, Object> getOnNewValuesMap(Tag tag, String fullNameModel, String fieldModel);

  String getTagDomain(String fullNameModel, Company company, TradingName tradingName);

  String getTagDomain(String fullNameModel, Set<Company> companySet, TradingName tradingName);
}
