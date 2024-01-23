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
package com.axelor.csv;

import com.axelor.utils.file.CsvTool;
import com.google.common.base.CaseFormat;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class PrepareCsv {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Test
  void prepareCsv() {
    String xmlDir = System.getProperty("xmlDir");
    String csvDir = System.getProperty("csvDir");
    List<String> ignoreType = Arrays.asList("one-to-one", "many-to-many", "one-to-many");
    try {
      if (xmlDir != null && csvDir != null) {
        File xDir = new File(xmlDir);
        File cDir = new File(csvDir);
        List<String[]> blankData = new ArrayList<String[]>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        if (xDir.isDirectory() && cDir.isDirectory()) {
          for (File xf : xDir.listFiles()) {
            LOG.info("Processing XML: " + xf.getName());
            List<String> fieldList = new ArrayList<String>();
            Document doc = dBuilder.parse(xf);
            NodeList nList = doc.getElementsByTagName("module");
            String module = nList.item(0).getAttributes().getNamedItem("name").getNodeValue();
            nList = doc.getElementsByTagName("entity");
            if (nList != null) {
              NodeList fields = nList.item(0).getChildNodes();
              Integer count = 0;
              String csvFileName =
                  module
                      + "_"
                      + CaseFormat.UPPER_CAMEL.to(
                          CaseFormat.LOWER_CAMEL, xf.getName().replace(".xml", ".csv"));
              while (count < fields.getLength()) {
                Node field = fields.item(count);
                NamedNodeMap attrs = field.getAttributes();
                String type = field.getNodeName();
                if (attrs != null
                    && attrs.getNamedItem("name") != null
                    && !ignoreType.contains(type)) {
                  String fieldName = attrs.getNamedItem("name").getNodeValue();
                  if (type.equals("many-to-one")) {
                    String[] objName = attrs.getNamedItem("ref").getNodeValue().split("\\.");
                    String refName = objName[objName.length - 1];
                    String nameColumn = getNameColumn(xmlDir + "/" + refName + ".xml");
                    if (nameColumn != null) fieldList.add(fieldName + "." + nameColumn);
                    else {
                      fieldList.add(fieldName);
                      LOG.error(
                          "No name column found for "
                              + refName
                              + ", field '"
                              + attrs.getNamedItem("name").getNodeValue()
                              + "'");
                    }
                  } else fieldList.add(fieldName);
                }

                count++;
              }
              CsvTool.csvWriter(
                  csvDir, csvFileName, ';', StringUtils.join(fieldList, ",").split(","), blankData);
              LOG.info("CSV file prepared: " + csvFileName);
            }
          }

        } else LOG.error("XML and CSV paths must be directory");
      } else LOG.error("Please input XML and CSV directory path");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String getNameColumn(String fileName)
      throws SAXException, IOException, ParserConfigurationException {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    File domainFile = new File(fileName);
    if (!domainFile.exists()) return null;
    Document doc = dBuilder.parse(domainFile);
    NodeList nList = doc.getElementsByTagName("entity");
    if (nList != null) {
      NodeList fields = nList.item(0).getChildNodes();
      Integer count = 0;
      while (count < fields.getLength()) {
        NamedNodeMap attrs = fields.item(count).getAttributes();
        count++;
        if (attrs != null && attrs.getNamedItem("name") != null) {
          String name = attrs.getNamedItem("name").getNodeValue();
          if (name.equals("importId")) return "importId";
          else if (name.equals("code")) return "code";
          else if (name.equals("name")) return "name";
          else continue;
        }
      }
    }
    return null;
  }
}
