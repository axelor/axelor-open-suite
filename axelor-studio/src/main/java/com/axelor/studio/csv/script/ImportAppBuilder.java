/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.csv.script;

import com.axelor.csv.script.ImportApp;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.AppBuilder;
import com.google.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportAppBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ImportApp.class);

  @Inject private MetaFiles metaFiles;

  public Object importAppBuilder(Object bean, Map<String, Object> values) {

    assert bean instanceof AppBuilder;

    AppBuilder appBuilder = (AppBuilder) bean;

    final Path path = (Path) values.get("__path__");
    String fileName = (String) values.get("imagePath");

    try {
      final File image = path.resolve("img" + File.separator + fileName).toFile();
      final MetaFile metaFile = metaFiles.upload(image);
      appBuilder.setImage(metaFile);

    } catch (Exception e) {
      e.printStackTrace();
      LOG.warn("Can't load image {} for app {}", fileName, appBuilder.getName());
    }

    return appBuilder;
  }
}
