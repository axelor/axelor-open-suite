/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSchedule;
import com.axelor.meta.db.MetaScheduleParam;
import com.axelor.meta.db.repo.MetaScheduleParamRepository;
import java.util.Map;

public class ImportMetaScheduleParam {

  @SuppressWarnings("rawtypes")
  public Object importMetaScheduleParam(Object bean, Map values) {
    assert bean instanceof MetaSchedule;
    MetaSchedule metaSchedule = (MetaSchedule) bean;

    if (!values.get("param_names").equals("")) {
      String params = values.get("param_names").toString();
      String[] scheduleParams = params.split(",");

      for (String param : scheduleParams) {
        MetaScheduleParam scheduleParam = new MetaScheduleParam();
        scheduleParam.setName(param);
        scheduleParam.setSchedule(metaSchedule);
        Beans.get(MetaScheduleParamRepository.class).save(scheduleParam);
      }
    }

    return metaSchedule;
  }
}
