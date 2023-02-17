/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.gdpr.service;

import com.axelor.exception.AxelorException;
import java.util.List;
import java.util.Map;

public interface GdprSearchEngineService {

  List<Map<String, Object>> searchObject(Map<String, Object> searchParams) throws AxelorException;

  Map<String, Object> checkSelectedObject(List<Map<String, Object>> resultList)
      throws AxelorException;
}
