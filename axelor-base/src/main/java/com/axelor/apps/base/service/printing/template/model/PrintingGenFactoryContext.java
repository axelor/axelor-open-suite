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
package com.axelor.apps.base.service.printing.template.model;

import com.axelor.db.Model;
import java.util.HashMap;
import java.util.Map;

public class PrintingGenFactoryContext {

  private Model model;

  private Map<String, Object> extraContext;

  public PrintingGenFactoryContext(Map<String, Object> extraContext) {
    this.extraContext = extraContext;
  }

  public PrintingGenFactoryContext(Model model) {
    this.model = model;
  }

  public Model getModel() {
    return model;
  }

  public void setModel(Model model) {
    this.model = model;
  }

  public Map<String, Object> getContext() {
    return extraContext == null ? Map.of() : extraContext;
  }

  public void addInContext(String key, Object value) {
    if (extraContext == null) {
      extraContext = new HashMap<>();
    }
    extraContext.put(key, value);
  }

  public void setContext(Map<String, Object> context) {
    this.extraContext = context;
  }
}
