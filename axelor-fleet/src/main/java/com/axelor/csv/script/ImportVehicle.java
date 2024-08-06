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

import com.axelor.apps.fleet.db.Vehicle;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class ImportVehicle {

  @Inject private MetaFiles metaFiles;

  public Object importVehicle(Object bean, Map<String, Object> values) {
    assert bean instanceof Vehicle;

    Vehicle vehicle = (Vehicle) bean;

    final Path path = (Path) values.get("__path__");
    String fileName = (String) values.get("image_fileName");
    if (Strings.isNullOrEmpty(fileName)) {
      return bean;
    }

    final File image = path.resolve(fileName).toFile();

    try {
      final MetaFile metaFile = metaFiles.upload(image);
      vehicle.setImage(metaFile);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return bean;
  }
}
