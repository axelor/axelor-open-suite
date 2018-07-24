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

import com.axelor.apps.base.db.IDataBackup;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateDataBackupService {

  private static final String SEPARATOR = ",";

  @Inject EntityManager em;

  @Inject private MetaModelRepository metaModelRepo;

  private List<String> tablesNameList;

  private Logger LOG = LoggerFactory.getLogger(getClass());

  private String headerStr = "", dataStr = "", dirPath = "";

  private PrintWriter pw = null;

  private List<String> fileList = null;

  private Map<String, Map<String, String>> bindMap = null;

  private Map<String, String> bindAttributeMap = null;

  private Document document = null, document2 = null, document3 = null;
  private Element csv_input = null, csv_input2 = null, csv_input3 = null;
  private boolean bindFlag, configFlag, parentFlag;

  public File create() {
    tablesNameList =
        new ArrayList<String>(
            Arrays.asList(
                "Role",
                "User",
                "Permission",
                "Group",
                "MailMessage",
                "Language",
                "BirtTemplate",
                "BirtTemplateParameter",
                "EventCategory",
                "AccountChart",
                "BankOrderFileFormat",
                "BankStatementFileFormat"));
    fileList = new ArrayList<>();
    String fileName = "";
    File tempDir = new File("temp");
    if (!tempDir.exists()) tempDir.mkdir();
    dirPath = tempDir.getAbsolutePath();
    try {
      setXmlConfigDocument();
      List<MetaModel> metaModelList = getMetaModels();
      for (MetaModel metaModel : metaModelList) {
        LOG.debug(IDataBackup.EXPORT_MODULE_NAME + metaModel.getFullName());
        bindMap = new HashMap<String, Map<String, String>>();
        bindFlag = false;
        configFlag = false;
        headerStr = "";
        dataStr = "";
        fileName = metaModel.getName() + ".csv";
        pw =
            new PrintWriter(
                new FileOutputStream(new File(dirPath + File.pathSeparator + fileName), true));
        fileList.add(fileName);
        List<?> dataList = getModelData(metaModel);
        if (dataList.size() > 0) {
          writeDataList(dataList, metaModel);
        } else {
          writeEmptyList(metaModel);
        }
      }
      appendXmlDocumet(document, document3);
      appendXmlDocumet(document2, document3);
      formateXmlDocument(document, "config1.xml");
      formateXmlDocument(document2, "config2.xml");
    } catch (InterruptedException
        | FileNotFoundException
        | ClassNotFoundException
        | ParserConfigurationException
        | TransformerFactoryConfigurationError
        | TransformerException e) {
      e.printStackTrace();
      LOG.error(e.getMessage());
    }
    return generateZIP(fileList);
  }

  /* Get All MetaModels */
  private List<MetaModel> getMetaModels() {
    String filterStr =
        "self.packageName NOT LIKE '%meta%' AND self.packageName !='com.axelor.studio.db' AND self.name!='DataBackup' AND self.tableName NOT LIKE 'BASE_APP%'";
    return metaModelRepo.all().filter(filterStr).fetch();
  }

  /* Get All Data of Specific MetaModel */
  private List<?> getModelData(MetaModel metaModel)
      throws InterruptedException, ClassNotFoundException {
    return em.createQuery(
            "SELECT m FROM " + metaModel.getName() + " m", Class.forName(metaModel.getFullName()))
        .setHint(QueryHints.MAINTAIN_CACHE, HintValues.FALSE)
        .getResultList();
  }

  /* Prepare Header For .csv File */
  private void getHeader(Field field) {
    if (field.getType().toString().startsWith("class java.")
        || field.getType().isPrimitive()
        || field.getType() == byte[].class) {
      if (field.getName().equalsIgnoreCase("id")) {
        headerStr += "importId" + SEPARATOR;
      } else {
        headerStr += field.getName() + SEPARATOR;
      }
    } else if (field.getType() == java.util.List.class
        || field.getType() == java.util.Set.class
        || field.getType().toString().startsWith("class com.axelor.")) {
      headerStr += field.getName() + "_importId" + SEPARATOR;
    } else {
      headerStr += SEPARATOR;
    }
  }

  /* Prepare Data String Of Specific Object */
  private void getData(Field field, Object object) {
    String objStr = "";
    try {
      if (field.get(object) != null) {
        if (field.getType().toString().startsWith("class java.") || field.getType().isPrimitive()) {
          if (ZonedDateTime.class == field.getType()) {
            LocalDateTime tempDateT = ((ZonedDateTime) field.get(object)).toLocalDateTime();
            dataStr += "\"" + tempDateT.toString() + "\"" + SEPARATOR;
          } else {
            dataStr += "\"" + field.get(object).toString() + "\"" + SEPARATOR;
          }
        } else if (field
            .getType()
            .toString()
            .startsWith("class com.axelor.")) { // one-to-one || // many-to-one
          dataStr += "\"" + getReferenceId(field.get(object)) + "\"" + SEPARATOR;
        } else if (field.getType() == java.util.List.class) { // one-to-many
          List<?> listObj = (List<?>) field.get(object);
          objStr = "";
          for (Object object2 : listObj) {
            objStr += getReferenceId(object2) + "|";
          }
          if (objStr.length() > 0) {
            dataStr += "\"" + objStr.substring(0, objStr.length() - 1) + "\"" + SEPARATOR;
          } else {
            dataStr += SEPARATOR;
          }
        } else if (field.getType() == java.util.Set.class) { // many-to-many
          Set<?> listObj = (Set<?>) field.get(object);
          objStr = "";
          for (Object object2 : listObj) {
            objStr += getReferenceId(object2) + "|";
          }
          if (objStr.length() > 0) {
            dataStr += "\"" + objStr.substring(0, objStr.length() - 1) + "\"" + SEPARATOR;
          } else {
            dataStr += SEPARATOR;
          }
        } else if (field.getType() == byte[].class) {
          dataStr += "\"" + Arrays.toString((byte[]) field.get(object)) + "\"" + SEPARATOR;
        } else {
          dataStr += SEPARATOR;
        }
      } else {
        dataStr += SEPARATOR;
      }
    } catch (IllegalArgumentException
        | IllegalAccessException
        | NoSuchFieldException
        | SecurityException e) {
      e.printStackTrace();
      LOG.error(e.getMessage());
    }
  }

  /* Get Id of Reference Field */
  private String getReferenceId(Object obj)
      throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
          SecurityException {
    String objStr = obj.toString();
    if (objStr != null
        && objStr.length() > 0
        && objStr.indexOf("id=") > 0
        && objStr.indexOf(",") > 0) {
      return objStr.substring(objStr.indexOf("id=") + 3, objStr.indexOf(","));
    } else {
      return "";
    }
  }

  /* Append csv and config Data */
  private void writeDataList(List<?> dataList, MetaModel metaModel) throws ClassNotFoundException {
    String data = "";
    boolean headerFlag = true;
    parentFlag = false;
    for (Object object : dataList) {
      dataStr = "";
      if (object.getClass().getName().contains("$$")) {
        object = Hibernate.unproxy(object);
      }
      Field[] fields = object.getClass().getDeclaredFields();
      if (object.getClass().getSuperclass() != Class.forName("com.axelor.auth.db.AuditableModel")) {
        fields = object.getClass().getSuperclass().getDeclaredFields();
        parentFlag = true;
      }
      for (Field field : fields) {
        field.setAccessible(true);
        if (headerFlag) {
          getHeader(field);
          getBindFieldValue(field);
        }
        getData(field, object);
        field.setAccessible(false);
      }
      if (dataStr.length() > 0) {
        data += dataStr.substring(0, dataStr.length() - 1) + "\n";
      }
      if (dataList.indexOf(object) % 20 == 0) {
        if (headerFlag) {
          if (headerStr.length() > 0) {
            headerStr = headerStr.substring(0, headerStr.length() - 1) + "\n";
          }
          pw.append(headerStr);
          headerFlag = false;
        }
        pw.append(data);
        data = "";
      }
    }
    pw.append(data);
    pw.close();
    appendConfigXml(metaModel, bindMap);
  }

  /* Prepare Binding value for config File */
  private void getBindFieldValue(Field field) {
    String csvName = "", searchField = "importId";
    bindAttributeMap = new HashMap<String, String>();
    if (field.isAnnotationPresent(ManyToMany.class)
        || field.isAnnotationPresent(OneToMany.class)
        || field.isAnnotationPresent(ManyToOne.class)
        || field.isAnnotationPresent(OneToOne.class)) {
      bindFlag = true;
      if (field.isAnnotationPresent(NotNull.class)) {
        configFlag = true;
      }
      csvName = field.getName() + "_importId";
      if (field.getType().getPackage().getName().equals("com.axelor.meta.db")) {
        searchField = "id";
      } else {
        searchField = "importId";
      }
      if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
        bindValue(csvName, field.getName(), "self." + searchField + " = :" + csvName, null);
      }
      if (field.isAnnotationPresent(ManyToMany.class)
          || field.isAnnotationPresent(OneToMany.class)) {
        if (field.isAnnotationPresent(OneToMany.class)) {
          OneToMany f = field.getAnnotation(OneToMany.class);
          if (f.mappedBy() == null || f.mappedBy().equals("")) {
            bindValue(
                csvName,
                field.getName(),
                "self." + searchField + " in :" + csvName,
                csvName + ".split('\\\\|') as List");
          }
        } else {
          bindValue(
              csvName,
              field.getName(),
              "self." + searchField + " in :" + csvName,
              csvName + ".split('\\\\|') as List");
        }
      }
    }
  }

  /* Get Bind value */
  private void bindValue(String csvName, String fieldName, String search, String eval) {
    bindAttributeMap.put("column", csvName);
    bindAttributeMap.put("to", fieldName);
    if (search != null && search.length() != 0) {
      bindAttributeMap.put("search", search);
      bindAttributeMap.put("update", "true");
    }
    if (eval != null && eval.length() != 0) {
      bindAttributeMap.put("eval", eval);
    }
    bindMap.put(fieldName, bindAttributeMap);
  }

  private void appendConfigXml(MetaModel metaModel, Map<String, Map<String, String>> bindMap) {
    String search = null, update = null;
    if (tablesNameList.contains(metaModel.getName())) {
      search = "self.id = :importId";
    }
    if (bindFlag) {
      if (configFlag) {
        csv_input3.appendChild(
            appendXml(metaModel, document3, "self.importId = :importId", null, bindMap));
      } else {
        update = "true";
        if (parentFlag || tablesNameList.contains(metaModel.getName())) {
          update = null;
        }
        csv_input2.appendChild(
            appendXml(metaModel, document2, "self.importId = :importId", update, bindMap));
        csv_input.appendChild(appendXml(metaModel, document, search, null, null));
      }
    } else {
      csv_input.appendChild(appendXml(metaModel, document, search, null, null));
    }
  }

  /* Prepare Tags for config File */
  private Element appendXml(
      MetaModel metaModel,
      Document doc,
      String search,
      String update,
      Map<String, Map<String, String>> bindMap) {
    Element input = null, bind = null;
    input = doc.createElement("input");
    input.setAttribute("file", metaModel.getName() + ".csv");
    input.setAttribute("type", metaModel.getFullName());
    if (search != null && search.length() > 0) {
      input.setAttribute("search", search);
      if (update != null && update.length() > 0) {
        input.setAttribute("update", update);
      }
    }
    if (bindMap != null && bindMap.size() > 0) {
      for (Map.Entry<String, Map<String, String>> entry : bindMap.entrySet()) {
        bind = doc.createElement("bind");
        Map<String, String> tempMap = entry.getValue();
        for (Map.Entry<String, String> tempEntry : tempMap.entrySet()) {
          bind.setAttribute(tempEntry.getKey(), tempEntry.getValue());
        }
        input.appendChild(bind);
      }
    }
    return input;
  }

  /* Prepare Data String for Empty MetaModel Object  */
  private void writeEmptyList(MetaModel metaModel) throws ClassNotFoundException {
    Class<?> cls = Class.forName(metaModel.getFullName());
    Field[] fieldList = cls.getDeclaredFields();
    Element input = null;
    for (Field field : fieldList) {
      if (field.getName().equalsIgnoreCase("id")) {
        headerStr += "importId" + SEPARATOR;
      } else {
        headerStr += field.getName() + SEPARATOR;
      }
    }
    if (headerStr.length() > 0) {
      headerStr = headerStr.substring(0, headerStr.length() - 1) + "\n";
    }
    pw.write(headerStr);
    input = document.createElement("input");
    input.setAttribute("file", metaModel.getName() + ".csv");
    input.setAttribute("type", metaModel.getFullName());
    csv_input.appendChild(input);
    pw.close();
  }

  private void setXmlConfigDocument() throws ParserConfigurationException {
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    document = builder.newDocument();
    document2 = builder.newDocument();
    document3 = builder.newDocument();
    csv_input = document.createElement("csv-inputs");
    csv_input.setAttribute("xmlns", "http://axelor.com/xml/ns/data-import");
    csv_input.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    csv_input.setAttribute(
        "xsi:schemaLocation",
        "http://axelor.com/xml/ns/data-import "
            + "http://axelor.com/xml/ns/data-import/data-import_5.0.xsd");
    document.appendChild(csv_input);

    csv_input2 = document2.createElement("csv-inputs");
    csv_input2.setAttribute("xmlns", "http://axelor.com/xml/ns/data-import");
    csv_input2.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    csv_input2.setAttribute(
        "xsi:schemaLocation",
        "http://axelor.com/xml/ns/data-import "
            + "http://axelor.com/xml/ns/data-import/data-import_5.0.xsd");
    document2.appendChild(csv_input2);

    csv_input3 = document3.createElement("csv-inputs");
    document3.appendChild(csv_input3);
  }

  private void formateXmlDocument(Document doc, String fileName)
      throws TransformerFactoryConfigurationError, TransformerException {
    Transformer tf = TransformerFactory.newInstance().newTransformer();
    tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    tf.setOutputProperty(OutputKeys.INDENT, "yes");
    tf.transform(
        new DOMSource(doc), new StreamResult(new File(dirPath + File.pathSeparator + fileName)));
    fileList.add(fileName);
  }

  private Document appendXmlDocumet(Document mainDoc, Document doc) {
    NodeList list = doc.getElementsByTagName("input");
    if (list != null) {
      for (int i = 0; i < list.getLength(); i++) {
        if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
          Node copiedNode = mainDoc.importNode((Element) list.item(i), true);
          mainDoc.getDocumentElement().appendChild(copiedNode);
        }
      }
    }
    return mainDoc;
  }

  private File generateZIP(List<String> fileNameList) {
    int length = 0;
    byte[] data = null;
    File zipFile = new File("temp/" + new Date().getTime() + "_backUp.zip");
    try {
      ZipOutputStream out = null;
      out = new ZipOutputStream(new FileOutputStream(zipFile));
      for (String fileName : fileNameList) {
        ZipEntry e = new ZipEntry(fileName);
        out.putNextEntry(e);
        File file = new File(dirPath + File.pathSeparator + fileName);
        length = (int) file.length();
        data = new byte[length];
        data = FileUtils.readFileToByteArray(file);
        out.write(data, 0, data.length);
        out.closeEntry();
        file.delete();
      }
      out.close();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    LOG.debug(IDataBackup.EXPORT_COMPLETE);
    return zipFile;
  }
}
