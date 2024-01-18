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

import com.axelor.apps.account.db.ImportFECType;
import com.axelor.apps.account.db.repo.ImportFECTypeRepository;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportImportFECType {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  @Inject private ImportFECTypeRepository importFECTypeRepository;
  @Inject MetaFiles metaFiles;

  @Transactional
  public Object importFECType(Object bean, Map<String, Object> values) {

    assert bean instanceof ImportFECType;

    ImportFECType importFECType = (ImportFECType) bean;
    String fileName = (String) values.get("bindMetaFile_name");

    if (!StringUtils.isEmpty(fileName)) {
      final Path path = (Path) values.get("__path__");

      try {
        File xmlFile = File.createTempFile(String.format("input-config-%s", fileName), ".xml");
        InputStream bindFileInputStream =
            this.getClass().getResourceAsStream("/FEC-config/" + fileName);
        FileOutputStream outputStream = new FileOutputStream(xmlFile);
        IOUtils.copy(bindFileInputStream, outputStream);
        if (xmlFile != null) {
          importFECType.setBindMetaFile(metaFiles.upload(xmlFile));
        } else {
          LOG.debug(
              "No xml file found: {}",
              xmlFile == null ? path.toAbsolutePath() : xmlFile.getAbsolutePath());
        }
        FileUtils.forceDelete(xmlFile);

      } catch (Exception e) {
        LOG.error("Error when import fec type bind meta file : {}", e);
      }
    }

    return importFECTypeRepository.save(importFECType);
  }
}
