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
package com.axelor.apps.baml.service;

import com.axelor.apps.baml.xml.ProcessActionNode;
import com.axelor.apps.baml.xml.ProcessActionRootNode;
import com.axelor.apps.bpm.context.WkfContextHelper;
import com.axelor.apps.bpm.db.BamlModel;
import com.axelor.apps.bpm.service.WkfCommonService;
import com.axelor.apps.bpm.translation.ITranslation;
import com.axelor.apps.tool.StringTool;
import com.axelor.apps.tool.xml.XPathParse;
import com.axelor.db.EntityHelper;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Adapter;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.script.GroovyScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BamlServiceImpl implements BamlService {

  @Override
  public String generateGroovyCode(String xml) throws AxelorException {

    ProcessActionRootNode rootNode = null;
    try {
      rootNode = BamlParser.parse(IOUtils.toInputStream(xml, "utf-8"));
    } catch (IOException e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(ITranslation.INVALID_BAML_XML));
    }

    if (rootNode == null) {
      return null;
    }

    List<ProcessActionNode> processActionNodes = rootNode.getProcessActions();

    StringBuilder codeBuilder = new StringBuilder();

    if (processActionNodes != null) {

      ProcessActionNode processActionNode = processActionNodes.get(0);

      generateCode(codeBuilder, processActionNode);
    }

    return codeBuilder.toString();
  }

  private void generateCode(StringBuilder codeBuilder, ProcessActionNode processActionNode) {

    String returnType = "void";
    String returnVar = "";

    if (processActionNode.getTargetModel() != null) {
      returnType = processActionNode.getTargetModel();
      returnVar = StringTool.toFirstLower(returnType);
      if (!processActionNode.getStaticCompile()) {
        returnVar += ".getTarget()";
        returnType = Model.class.getSimpleName();
      }
    }

    String parameter = "";
    String varName = "";

    if (processActionNode.getSourceModel() != null) {
      String parameterType = processActionNode.getSourceModel();
      varName = parameterType;
      if (varName.contains(".")) {
        varName = varName.substring(varName.lastIndexOf(".") + 1);
      }
      varName = StringTool.toFirstLower(varName);
      if (!processActionNode.getStaticCompile()) {
        varName = varName + "V";
        parameterType = Model.class.getSimpleName();
      }
      parameter = parameterType + " " + varName;
    }

    codeBuilder.append("import " + Adapter.class.getName() + "\n");
    codeBuilder.append("import " + Beans.class.getName() + "\n");

    if (processActionNode.getStaticCompile()) {
      codeBuilder.append("@groovy.transform.CompileStatic\n");
    } else {
      codeBuilder.append("import com.axelor.apps.bpm.context.*\n");
    }
    codeBuilder.append("\n" + returnType + " _execute(" + parameter + "){\n");
    if (!processActionNode.getStaticCompile() && !Strings.isNullOrEmpty(varName)) {
      codeBuilder.append(
          varName.substring(0, varName.length() - 1)
              + " = WkfContextHelper.create("
              + varName
              + ")\n");
      varName = varName.substring(0, varName.length() - 1);
    }
    codeBuilder.append(processActionNode.toCode(!processActionNode.getStaticCompile()));
    codeBuilder.append("\nreturn " + returnVar);
    codeBuilder.append("\n}\nreturn _execute(" + varName + ")\n");
  }

  @Override
  @Transactional
  public Model execute(BamlModel bamlModel, Model entity) {

    Bindings bindings = new SimpleBindings();

    if (entity != null) {
      String varName = Beans.get(WkfCommonService.class).getVarName(entity);
      bindings.put(varName, entity);
    }

    bindings.put("$ctx", WkfContextHelper.class);

    GroovyScriptHelper helper = new GroovyScriptHelper(bindings);
    Object object = helper.eval(bamlModel.getResultScript());

    if (object != null && object instanceof Model) {
      return JpaRepository.of(EntityHelper.getEntityClass((Model) object)).save((Model) object);
    }

    return null;
  }

  @Override
  @Transactional
  public Model execute(BamlModel bamlModel, Map<String, Object> context) {

    Bindings bindings = new SimpleBindings();

    if (context != null) {
      bindings.putAll(context);
    }

    GroovyScriptHelper helper = new GroovyScriptHelper(bindings);
    Object object = helper.eval(bamlModel.getResultScript());

    if (object != null && object instanceof Model) {
      return JpaRepository.of(EntityHelper.getEntityClass((Model) object)).save((Model) object);
    }

    return null;
  }

  @Override
  public String extractBamlXml(String xml) throws AxelorException {

    if (xml == null) {
      return null;
    }

    DocumentBuilderFactory docBuilderFactory = new XPathParse().getDocumentBuilderFactory();

    try {
      docBuilderFactory.setNamespaceAware(false);
      DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
      InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
      Document doc = builder.parse(inputStream);
      inputStream.close();
      NodeList nodeList = doc.getElementsByTagName("process-action");
      xml = BamlParser.createEmptyBamlXml();
      inputStream = new ByteArrayInputStream(xml.getBytes());
      doc = builder.parse(inputStream);
      inputStream.close();

      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = doc.importNode(nodeList.item(i), true);
        doc.getFirstChild().appendChild(node);
      }

      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer transformer = tFactory.newTransformer();

      DOMSource source = new DOMSource(doc);
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      StreamResult result = new StreamResult(bout);
      transformer.transform(source, result);

      xml = bout.toString();

      bout.close();
      return xml;

    } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
      e.printStackTrace();
    }

    return xml;
  }
}
