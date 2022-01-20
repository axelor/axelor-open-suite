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
package com.axelor.apps.baml.test;

import com.axelor.apps.baml.service.BamlParser;
import com.axelor.apps.baml.xml.ProcessActionNode;
import com.axelor.apps.baml.xml.ProcessActionRootNode;
import com.axelor.auth.db.User;
import com.axelor.common.ResourceUtils;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.rpc.Context;
import com.axelor.rpc.Criteria;
import com.axelor.script.GroovyScriptHelper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TestBamlParser {

  public void test() {

    InputStream is = ResourceUtils.getResourceStream("BamlTest.xml");
    ProcessActionRootNode rootNode = BamlParser.parse(is);
    List<ProcessActionNode> processActionNodes = rootNode.getProcessActions();
    String script = "";
    if (processActionNodes != null) {
      script = processActionNodes.get(0).toCode(true);
    }

    Context ctx = new Context(User.class);
    GroovyScriptHelper helper = new GroovyScriptHelper(ctx);
    helper.eval(script);
  }

  @Test
  public void testJsonQuery() {

    Map<String, Object> rawCriteria = new HashMap<String, Object>();

    Map<String, Object> _domainContext = new HashMap<String, Object>();
    _domainContext.put("jsonModel", "File");

    rawCriteria.put("fieldName", "attrs.integer1");
    rawCriteria.put("operator", "=");
    rawCriteria.put("value", 2);
    rawCriteria.put("_domain", "jsonModel = :jsonModel");
    rawCriteria.put("_domainContext", _domainContext);

    Criteria criteria = Criteria.parse(rawCriteria, MetaJsonRecord.class, false);
    criteria.createQuery(MetaJsonRecord.class).toString();
    System.err.println(criteria.toString());
    System.err.println(criteria.createQuery(MetaJsonRecord.class).toString());
  }
}
