/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.app.AppSettings;
import com.axelor.app.AvailableAppSettings;
import com.axelor.meta.db.MetaFile;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportMetaFile {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public Object importMetaFile(Object bean, Map<String, Object> values) {

    assert bean instanceof MetaFile;

    MetaFile metaFile = (MetaFile) bean;
    String isStorageEnabled =
        AppSettings.get().get(AvailableAppSettings.DATA_OBJECT_STORAGE_ENABLED);
    if (isStorageEnabled != null && isStorageEnabled.equals("true")) {
      metaFile.setStoreType(2);
    } else {
      metaFile.setStoreType(1);
    }

    return metaFile;
  }
}
