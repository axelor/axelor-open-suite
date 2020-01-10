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

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.App;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportApp {

  private static final Logger LOG = LoggerFactory.getLogger(ImportApp.class);

  @Inject private MetaFiles metaFiles;

  public Object importApp(Object bean, Map<String, Object> values) {

    assert bean instanceof App;

    App app = (App) bean;

    final Path path = (Path) values.get("__path__");
    String fileName = (String) values.get("imagePath");

    if (!Strings.isNullOrEmpty(fileName)) {
      try {
        final File image = path.resolve("img" + File.separator + fileName).toFile();
        if (image.exists()) {
          final MetaFile metaFile = metaFiles.upload(image);
          app.setImage(metaFile);
        }
      } catch (Exception e) {
        LOG.warn("Can't load image {} for app {}", fileName, app.getName());
      }
    }

    if (app.getLanguageSelect() == null) {
      String language = AppSettings.get().get("application.locale");
      app.setLanguageSelect(language);
    }

    return app;
  }
}
