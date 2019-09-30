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
package com.axelor.apps.tool.address;

import com.qas.web_2005_02.Address;
import com.qas.web_2005_02.EngineEnumType;
import com.qas.web_2005_02.EngineType;
import com.qas.web_2005_02.PromptSetType;
import com.qas.web_2005_02.QACanSearch;
import com.qas.web_2005_02.QAData;
import com.qas.web_2005_02.QADataSet;
import com.qas.web_2005_02.QAGetAddress;
import com.qas.web_2005_02.QAPortType;
import com.qas.web_2005_02.QASearch;
import com.qas.web_2005_02.QASearchOk;
import com.qas.web_2005_02.QASearchResult;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressTool {

  private static QName SERVICE_NAME = null;
  private static QName PORT_NAME = null;
  private static URL wsdlURL = null;
  private static Service service = null;
  private static QAPortType client = null;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void setService(String wsdlUrl) throws MalformedURLException {
    // TODO: inject this
    if (this.client == null) {
      this.SERVICE_NAME = new QName("http://www.qas.com/web-2005-02", "ProWeb");

      this.PORT_NAME = new QName("http://www.qas.com/web-2005-02", "QAPortType");

      // def wsdlURL = new URL("http://ip.axelor.com:2021/proweb.wsdl")
      this.wsdlURL = new URL(wsdlUrl);
      // println this.wsdlURL

      this.service = Service.create(this.wsdlURL, this.SERVICE_NAME);
      this.client = service.getPort(QAPortType.class);
      // QAPortType client = service.getPort(PORT_NAME, QAPortType.class)
      LOG.debug("setService  this.client = {}", this.client);
    }
  }

  public boolean doCanSearch(String wsdlUrl) {

    try {
      QName SERVICE_NAME = new QName("http://www.qas.com/web-2005-02", "ProWeb");

      QName PORT_NAME = new QName("http://www.qas.com/web-2005-02", "QAPortType");

      // def wsdlURL = new URL("http://ip.axelor.com:2021/proweb.wsdl")
      URL wsdlURL = new URL(wsdlUrl);

      Service service = Service.create(wsdlURL, SERVICE_NAME);
      QAPortType client = service.getPort(QAPortType.class);
      // QAPortType client = service.getPort(PORT_NAME, QAPortType.class)
      LOG.debug("setService  client = {}", client);

      // 1. Pre-check.

      QAData qadata = client.doGetData();
      QADataSet ds = qadata.getDataSet().get(0);

      QACanSearch canSearch = new QACanSearch();
      canSearch.setCountry("FRX");
      canSearch.setLayout("AFNOR INSEE");

      EngineType engType = new EngineType();
      engType.setFlatten(true);

      engType.setValue(EngineEnumType.VERIFICATION);
      canSearch.setEngine(engType);
      QASearchOk resp = client.doCanSearch(canSearch);

      return resp.isIsOk();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public Map<String, Object> doSearch(String wsdlUrl, String searchString) {

    try {
      this.setService(wsdlUrl);
      // 2. Initial search.
      QASearch search = new QASearch();
      search.setCountry("FRX");
      search.setLayout("AFNOR INSEE");
      search.setSearch(searchString);

      EngineType engTypeT = new EngineType();
      engTypeT.setPromptSet(PromptSetType.ONE_LINE); // DEFAULT
      engTypeT.setValue(EngineEnumType.VERIFICATION);
      engTypeT.setFlatten(true);
      search.setEngine(engTypeT);

      QASearchResult respSearch = this.client.doSearch(search);

      Map<String, Object> mapSearch = new HashMap<String, Object>();
      mapSearch.put("verifyLevel", respSearch.getVerifyLevel());
      mapSearch.put("qaPicklist", respSearch.getQAPicklist());
      mapSearch.put("qaAddress", respSearch.getQAAddress());
      return mapSearch;
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap<String, Object>();
    }
  }

  public Address doGetAddress(String wsdlUrl, String moniker) {
    try {
      this.setService(wsdlUrl);

      // 4. Format the final address.
      QAGetAddress getAddress = new QAGetAddress();

      getAddress.setMoniker(moniker);
      getAddress.setLayout("AFNOR INSEE");

      Address formattedAddress = this.client.doGetAddress(getAddress);

      return formattedAddress;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
