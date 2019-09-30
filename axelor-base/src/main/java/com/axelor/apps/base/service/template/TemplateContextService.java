/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.template;

import com.axelor.apps.base.db.TemplateContext;
import com.axelor.apps.base.db.TemplateContextLine;
import com.axelor.db.Model;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.Map;

public class TemplateContextService {

  @Inject private TemplateContextLineService tcls;

  public Map<String, Object> getContext(TemplateContext templateContext, Model bean) {
    Map<String, Object> map = Maps.newHashMap();

    if (templateContext.getTemplateContextLineList() != null) {
      for (TemplateContextLine line : templateContext.getTemplateContextLineList()) {
        Object o = tcls.evaluate(line, bean);
        map.put(line.getKey(), o);
      }
    }
    return map;
  }
}
