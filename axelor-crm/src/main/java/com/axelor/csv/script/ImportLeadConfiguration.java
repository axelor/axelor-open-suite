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
package com.axelor.csv.script;

import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportLeadConfiguration {

  public static final String IMPORT_LEAD_CONFIG = "import-lead-config.xml";
  public static final String IMPORT_LEAD_CSV = "Lead.csv";
  public static final String FILES_DIR = "files";

  private final Logger log = LoggerFactory.getLogger(ImportLeadConfiguration.class);
  @Inject private MetaFiles metaFiles;

  public Object importFiles(Object bean, Map<String, Object> values) {

    assert bean instanceof ImportConfiguration;

    final Path path = (Path) values.get("__path__");

    ImportConfiguration importConfig = (ImportConfiguration) bean;

    try {
      File file = path.resolve(FILES_DIR + File.separator + IMPORT_LEAD_CONFIG).toFile();
      importConfig.setBindMetaFile(metaFiles.upload(file));
      file = path.resolve(FILES_DIR + File.separator + IMPORT_LEAD_CSV).toFile();
      importConfig.setDataMetaFile(metaFiles.upload(file));
    } catch (IOException e) {
      log.debug("Error importing lead import config", e);
      return null;
    }

    return importConfig;
  }
}
