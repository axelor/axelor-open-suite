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

import com.axelor.apps.base.db.Company;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportCompany {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject MetaFiles metaFiles;

  public Object importCompany(Object bean, Map<String, Object> values) {

    assert bean instanceof Company;

    Company company = (Company) bean;
    String fileName = (String) values.get("logo_fileName");

    if (!StringUtils.isEmpty(fileName)) {
      final Path path = (Path) values.get("__path__");

      try {
        final File image = path.resolve(fileName).toFile();
        if (image != null && image.isFile()) {
          final MetaFile metaFile = metaFiles.upload(image);
          company.setLogo(metaFile);
        }
      } catch (Exception e) {
        LOG.error("Error when importing company : {}", e);
      }
    }

    return company;
  }
}
