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
package com.axelor.apps.tool.service;

import com.axelor.common.ObjectUtils;
import java.util.ArrayList;
import java.util.List;

public class ListToolServiceImpl implements ListToolService {

  @Override
  public <T> List<T> intersection(List<T> list1, List<T> list2) {
    List<T> list = new ArrayList<T>();

    if (ObjectUtils.notEmpty(list1) && ObjectUtils.notEmpty(list2)) {
      for (T t : list1) {
        if (list2.contains(t)) {
          list.add(t);
        }
      }
    }

    return list;
  }
}
