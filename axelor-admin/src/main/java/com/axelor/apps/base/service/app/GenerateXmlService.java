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
package com.axelor.apps.base.service.app;

import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVInput;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GenerateXmlService {

  /* Generate XML String from CSVInput List */
  public void generateConfig(String dirPath, List<CSVInput> csvInputs) {
    try {
      File file = new File(dirPath, DataBackupServiceImpl.configFileName);
      FileWriter fileWriter = new FileWriter(file, true);
      fileWriter.append("<csv-inputs>").append('\n');
      for (CSVInput csvInput : csvInputs) {
        fileWriter.append(getXmlString(csvInput)).append('\n');
      }
      fileWriter.append("</csv-inputs>");
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getXmlString(CSVInput csvInputs) {
    XStream xStream = new XStream();
    xStream.processAnnotations(CSVInput.class);
    xStream.processAnnotations(CSVBind.class);
    String xml = xStream.toXML(csvInputs);
    return xml.replaceAll("&apos;", "'");
  }
}
