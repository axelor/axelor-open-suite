/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.service;

import com.axelor.common.ResourceUtils;
import com.axelor.studio.service.loader.AppLoaderImportServiceImpl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class AppLoaderImportBpmServiceImpl extends AppLoaderImportServiceImpl {

  private static final String[] IMPORT_FILES =
      new String[] {"wkf-model.xml", "wkf-dmn-model.xml", "baml-model.xml"};

  @Override
  public List<File> getAppImportConfigFiles(File dataDir)
      throws FileNotFoundException, IOException {

    List<File> configFiles = super.getAppImportConfigFiles(dataDir);

    for (String fileName : IMPORT_FILES) {
      String dataFileName = fileName.replace("-call.xml", ".xml");
      if (!(new File(dataDir, dataFileName)).exists()) {
        continue;
      }
      File configFile = new File(dataDir, fileName.replace(".xml", "-config.xml"));
      FileOutputStream fout = new FileOutputStream(configFile);
      InputStream inStream = ResourceUtils.getResourceStream("data-import/" + fileName);
      IOUtils.copy(inStream, fout);
      inStream.close();
      fout.close();
      configFiles.add(configFile);
    }

    return configFiles;
  }
}
