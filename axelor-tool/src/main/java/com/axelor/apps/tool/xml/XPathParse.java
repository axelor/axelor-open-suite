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
package com.axelor.apps.tool.xml;

import com.axelor.exception.service.TraceBackService;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XPathParse {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Document doc;

  protected Document getDoc() {
    return doc;
  }

  protected void setDoc(Document doc) {
    this.doc = doc;
  }

  public XPathParse(String xml) {

    DocumentBuilderFactory domFactory = getDocumentBuilderFactory();
    domFactory.setNamespaceAware(true); // never forget this!
    DocumentBuilder builder;

    try {
      builder = domFactory.newDocumentBuilder();
      this.doc = builder.parse(xml);

    } catch (Exception e) {

      LOG.error(e.getMessage());
    }
  }

  public DocumentBuilderFactory getDocumentBuilderFactory() {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

    try {
      String feature = "http://apache.org/xml/features/disallow-doctype-decl";
      domFactory.setFeature(feature, true);

      // Disable #external-general-entities
      feature = "http://xml.org/sax/features/external-general-entities";
      domFactory.setFeature(feature, false);

      // Disable #external-parameter-entities
      feature = "http://xml.org/sax/features/external-parameter-entities";
      domFactory.setFeature(feature, false);

      // Disable external DTDs as well
      feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
      domFactory.setFeature(feature, false);

      // and these as well
      domFactory.setXIncludeAware(false);
      domFactory.setExpandEntityReferences(false);
      domFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    } catch (ParserConfigurationException e) {
      LOG.error(e.getMessage());
    }

    return domFactory;
  }

  /**
   * public static TreeMap<String,String> parse(String xml, ArrayList<String> xpeList) throws
   * ParserConfigurationException, SAXException, IOException, XPathExpressionException {
   *
   * <p>Ref: http://www.ibm.com/developerworks/library/x-javaxpathapi/index.html HashMap replaced by
   * TreeMap since it is sorted Exceptions catched here since no way to catch them in aml
   */
  public Map<String, String> parse(List<String> xpeList) {

    Map<String, String> dict = new TreeMap<String, String>();

    try {

      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();

      for (String xpe : xpeList) {

        XPathExpression expr = xpath.compile(xpe); // /text()

        Object result = expr.evaluate(this.doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        if (nodes.getLength() == 1) {

          dict.put(xpe, nodes.item(0).getNodeValue());

        } else {

          for (int i = 0; i < nodes.getLength(); i++) {

            dict.put(i + "__" + xpe, nodes.item(i).getNodeValue());
          }
        }
      }
    } catch (Exception e) {
      LOG.error("some pb occurred during xml scan");
    }

    return dict;
  }
}
