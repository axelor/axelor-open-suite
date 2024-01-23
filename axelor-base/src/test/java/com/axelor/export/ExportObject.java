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
package com.axelor.export;

import com.axelor.utils.file.CsvTool;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ExportObject {

  public static List<String> fieldAttrs =
      Arrays.asList("object", "module", "name", "type", "title", "help");

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

  private List<Map<String, Object>> menuList = new ArrayList<Map<String, Object>>();

  private Map<String, Object> objectMap = new HashMap<String, Object>();

  private List<String[]> fieldDataList = new ArrayList<String[]>();

  private List<String> objectList = new ArrayList<String>();

  private String modulesPath = "/home/axelor/axelor-erp/gradle/axelor-erp/modules";

  private String csvPath = "/home/axelor/Desktop/";

  @Test
  void exportObject() {

    File moduleDir = new File(modulesPath);

    if (moduleDir.exists()) {
      prepareObject(Arrays.asList(moduleDir.listFiles()));
    } else {
      log.debug("Module source directory not exist");
    }
  }

  private void prepareObject(List<File> modules) {

    try {
      XmlHandler xmlHandler = new XmlHandler();
      SAXParser parser = saxParserFactory.newSAXParser();

      for (File module : modules) {
        String modulePath = module.getAbsolutePath();
        File menuFile = new File(modulePath + "/src/main/resources/views/Menu.xml");

        if (menuFile.exists()) {
          log.debug("Parsing menu: {}", menuFile.getAbsolutePath());
          parser.parse(new InputSource(new FileInputStream(menuFile)), xmlHandler);
        }
      }
      updateMenuGraph(xmlHandler, null);
      Collections.sort(menuList, new MenuComparator());
      updateObjectMap(modules, parser, xmlHandler);
      writeCsv();

    } catch (SAXException | IOException | ParserConfigurationException e) {
      e.printStackTrace();
    }
  }

  private Object updateMenuGraph(XmlHandler xmlHandler, String parentName) {

    List<String> keyList = new ArrayList<String>(xmlHandler.menuMap.keySet());
    List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();

    for (String menuName : keyList) {
      Map<String, String> menuMap = xmlHandler.menuMap.get(menuName);
      if (menuMap == null) {
        continue;
      }
      Map<String, Object> menuGraph = new HashMap<String, Object>();
      menuGraph.put("priority", menuMap.get("priority"));
      menuGraph.put("object", menuMap.get("object"));
      menuGraph.put("name", menuName);
      String parent = menuMap.get("parent");

      if (parent == null) {
        xmlHandler.menuMap.remove(menuName);
        menuGraph.put("children", updateMenuGraph(xmlHandler, menuName));
        menuList.add(menuGraph);
        continue;
      }

      if (parent.equals(parentName)) {
        xmlHandler.menuMap.remove(menuName);
        menuGraph.put("children", updateMenuGraph(xmlHandler, menuName));
        children.add(menuGraph);
      }
    }

    Collections.sort(children, new MenuComparator());

    return children;
  }

  @SuppressWarnings("unchecked")
  private void updateObjectMap(List<File> modules, SAXParser parser, XmlHandler xmlHandler)
      throws SAXException, IOException {

    for (File module : modules) {
      String modulePath = module.getAbsolutePath();
      File modelDir = new File(modulePath + "/src/main/resources/domains/");
      if (!modelDir.exists()) {
        continue;
      }
      log.debug("Module : {}", modelDir.getAbsolutePath());

      for (File objectFile : modelDir.listFiles()) {
        log.debug("Parsing domain : {}", objectFile.getName());
        String objectName = objectFile.getName().split("\\.")[0];

        if (xmlHandler.objectList.contains(objectName)) {
          parser.parse(new InputSource(new FileInputStream(objectFile)), xmlHandler);
          Map<String, Object> moduleMap = (Map<String, Object>) objectMap.get(objectName);
          if (moduleMap == null) {
            moduleMap = new HashMap<String, Object>();
          }
          moduleMap.put(module.getName(), xmlHandler.fieldList);
          objectMap.put(objectName, moduleMap);
        }
      }
    }
  }

  private void writeCsv() {
    try {
      updateObjectList(menuList);
      updateFieldList();
      String[] headers = fieldAttrs.toArray(new String[fieldAttrs.size()]);
      CsvTool.csvWriter(csvPath, "ExportObj.csv", ';', headers, fieldDataList);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  private void updateObjectList(List<Map<String, Object>> orderedMenuList) {

    Iterator<Map<String, Object>> menuIterator = orderedMenuList.iterator();
    while (menuIterator.hasNext()) {
      Map<String, Object> menu = menuIterator.next();
      String objectName = (String) menu.get("object");
      if (objectName != null && !objectList.contains(objectName)) {
        objectList.add(objectName);
      }
      List<Map<String, Object>> childrenMenuList = (List<Map<String, Object>>) menu.get("children");
      if (!childrenMenuList.isEmpty()) updateObjectList(childrenMenuList);
    }
  }

  @SuppressWarnings("unchecked")
  private void updateFieldList() {

    for (String objName : objectList) {
      String[] objectName = objName.split("\\.");
      Map<String, Object> moduleMap =
          (Map<String, Object>) objectMap.get(objectName[objectName.length - 1]);
      log.debug("Adding object: {}", objectName[objectName.length - 1]);
      if (moduleMap == null) {
        log.debug("No domain file found for: {}", objName);
        continue;
      }

      for (Entry<String, Object> module : moduleMap.entrySet()) {
        String moduleName = module.getKey();
        for (Map<String, String> field : (List<Map<String, String>>) module.getValue()) {
          Iterator<String> fieldAtt = fieldAttrs.iterator();
          List<String> fieldData = new ArrayList<String>();
          field.put("object", objName);
          field.put("module", moduleName);
          while (fieldAtt.hasNext()) {
            fieldData.add(field.get(fieldAtt.next()));
          }
          fieldDataList.add(fieldData.toArray(new String[fieldData.size()]));
        }
      }
    }
  }
}

class XmlHandler extends DefaultHandler {

  public List<String> objectList = new ArrayList<String>();

  public Map<String, Map<String, String>> menuMap = new HashMap<String, Map<String, String>>();

  private Map<String, String> actionMenuMap = new HashMap<String, String>();

  public List<Map<String, String>> fieldList;

  private boolean isObject = false;

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    switch (qName) {
      case "menuitem":
        {
          if (attributes.getValue("top") == null) {
            handleMenu(attributes);
          }
          break;
        }
      case "action-view":
        {
          handleAction(attributes);
          break;
        }
      case "entity":
        {
          isObject = true;
          fieldList = new ArrayList<Map<String, String>>();
          break;
        }
      default:
        {
          if (isObject) {
            Map<String, String> fieldMap = new HashMap<String, String>();
            fieldMap.put("type", qName);
            for (String fieldAttr : ExportObject.fieldAttrs) {
              if (attributes.getValue(fieldAttr) != null) {
                fieldMap.put(fieldAttr, attributes.getValue(fieldAttr));
              }
            }
            fieldList.add(fieldMap);
          }
        }
    }
    super.startElement(uri, localName, qName, attributes);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {

    if (qName.equals("entity")) {
      isObject = false;
    }
    super.endElement(uri, localName, qName);
  }

  public void handleMenu(Attributes attributes) {

    Map<String, String> menuAttr = new HashMap<String, String>();
    String name = attributes.getValue("name");
    menuAttr.put("parent", attributes.getValue("parent"));
    menuAttr.put("priority", attributes.getValue("priority"));
    if (attributes.getValue("action") != null) {
      actionMenuMap.put(attributes.getValue("action"), name);
    }
    menuMap.put(name, menuAttr);
  }

  public void handleAction(Attributes attributes) {

    String name = attributes.getValue("name");
    String model = attributes.getValue("model");

    if (actionMenuMap.containsKey(name) && model != null) {
      Map<String, String> menu = menuMap.get(actionMenuMap.get(name));
      menu.put("object", model);
      menuMap.put(actionMenuMap.get(name), menu);
      String objectName[] = model.split("\\.");
      if (!objectList.contains(objectName[objectName.length - 1]))
        objectList.add(objectName[objectName.length - 1]);
    }
  }
}

class MenuComparator implements Comparator<Map<String, Object>> {

  @Override
  public int compare(Map<String, Object> o1, Map<String, Object> o2) {

    Integer priority1 =
        o1.get("priority") != null ? Integer.parseInt(o1.get("priority").toString()) : 0;
    Integer priority2 =
        o2.get("priority") != null ? Integer.parseInt(o2.get("priority").toString()) : 0;
    if (priority1 < priority2) {
      return 1;
    }
    return -1;
  }
}
