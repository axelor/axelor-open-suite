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
package com.axelor.csv.script;

import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.MetaScanner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class ImportConfiguratorCreator {

  @Inject ConfiguratorCreatorImportService configuratorCreatorImportService;

  @Transactional
  public Object importConfiguratorCreator(Object bean, Map values)
      throws AxelorException, IOException {
    URL url =
        MetaScanner.findAll("production_configuratorCreator.xml")
            .stream()
            .findAny()
            .orElseThrow(
                () ->
                    new AxelorException(
                        TraceBackRepository.CATEGORY_INCONSISTENCY,
                        "Error when importing configurator creators demo data."));
    configuratorCreatorImportService.importConfiguratorCreators(url.openStream());
    return null;
  }
}
