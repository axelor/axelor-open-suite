/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportBirtTemplate {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject MetaFiles metaFiles;

  public Object importTemplate(Object bean, Map<String, Object> values) {

    BirtTemplate birtTemplate = (BirtTemplate) bean;
    String fileName = (String) values.get("templateLink");

    if (!StringUtils.isEmpty(fileName)) {
      try {
        InputStream stream = this.getClass().getResourceAsStream("/reports/" + fileName);
        if (stream != null) {
          final MetaFile metaFile = metaFiles.upload(stream, fileName);
          birtTemplate.setTemplateMetaFile(metaFile);
        }
      } catch (Exception e) {
        LOG.error("Error when importing birt template : {0}", e);
      }
    }

    return birtTemplate;
  }
}
