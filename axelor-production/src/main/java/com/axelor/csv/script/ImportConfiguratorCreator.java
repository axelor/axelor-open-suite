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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportService;
import com.axelor.meta.MetaScanner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class ImportConfiguratorCreator {

  @Inject ConfiguratorCreatorImportService configuratorCreatorImportService;

  @Transactional(rollbackOn = {Exception.class})
  public Object importConfiguratorCreator(Object bean, Map values)
      throws AxelorException, IOException {
    String path = String.valueOf(values.get("path"));
    File dataFile = new File(path);
    URL url =
        MetaScanner.findAll("axelor-production", dataFile.getParent(), dataFile.getName()).stream()
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
