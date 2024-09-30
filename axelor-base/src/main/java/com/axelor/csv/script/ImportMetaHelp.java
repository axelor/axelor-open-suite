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
package com.axelor.csv.script;

import com.axelor.apps.base.db.Localization;
import com.axelor.meta.service.MetaHelpService;
import com.google.inject.Inject;
import java.util.Map;

public class ImportMetaHelp {

  protected MetaHelpService metaHelpService;

  @Inject
  public ImportMetaHelp(MetaHelpService metaHelpService) {
    this.metaHelpService = metaHelpService;
  }

  public Localization importMetaHelp(Object bean, Map<String, Object> values) {
    assert bean instanceof Localization;
    Localization localization = (Localization) bean;
    metaHelpService.findOrCreateMetaHelp(localization);
    return localization;
  }
}
