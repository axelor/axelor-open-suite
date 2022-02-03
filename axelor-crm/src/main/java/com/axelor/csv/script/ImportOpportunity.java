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
package com.axelor.csv.script;

import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import java.util.Map;

public class ImportOpportunity {
  public Object importOpportunity(Object bean, Map<String, Object> values) {
    assert bean instanceof Opportunity;

    Opportunity opportunity = (Opportunity) bean;

    try {
      Beans.get(OpportunityService.class).setSequence(opportunity);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return opportunity;
  }
}
