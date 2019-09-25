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

import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.axelor.script.ScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections.CollectionUtils;

public class ImportAdvancedImport {

  @Inject protected MetaFiles metaFiles;

  @SuppressWarnings("unchecked")
  public Object importGeneral(Object bean, Map<String, Object> values) {
    if (bean == null) {
      return bean;
    }

    boolean isTrue = false;
    ScriptHelper scriptHelper = new GroovyScriptHelper(new ScriptBindings(values));

    for (Entry<String, Object> entry : values.entrySet()) {
      if (entry.getKey().equals(bean.getClass().getName())) {
        List<String> exprs = (List<String>) entry.getValue();
        if (!CollectionUtils.isEmpty(exprs)) {
          isTrue = (boolean) scriptHelper.eval(String.join(" || ", exprs));
        }
      }
    }

    if (isTrue) {
      return null;
    } else {
      return bean;
    }
  }

  public Object importPicture(String value, String pathVal) throws IOException {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }

    Path path = Paths.get(pathVal);
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }

    File image = path.resolve(value).toFile();
    if (!image.exists() || image.isDirectory()) {
      return null;
    }

    MetaFile metaFile = metaFiles.upload(image);
    return metaFile;
  }
}
