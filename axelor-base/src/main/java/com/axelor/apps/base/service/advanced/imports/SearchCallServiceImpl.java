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
package com.axelor.apps.base.service.advanced.imports;

import com.axelor.common.ObjectUtils;
import java.util.Map;

public class SearchCallServiceImpl implements SearchCallService {

  @Override
  public Boolean validate(String searchCall) {
    if (ObjectUtils.notEmpty(searchCall)) {
      try {
        String className = searchCall.split("\\:")[0];
        String method = searchCall.split("\\:")[1];
        Class<?> klass = Class.forName(className);
        klass.getMethod(method, Map.class);
      } catch (Exception e) {
        return false;
      }
    }
    return true;
  }
}
