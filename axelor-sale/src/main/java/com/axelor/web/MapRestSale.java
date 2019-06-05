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
package com.axelor.web;

import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/map")
public class MapRestSale {

  @Inject MapService mapService;

  @Inject private SaleOrderRepository saleOrderRepo;

  @Path("/geomap/turnover")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getGeoMapData() {

    Map<String, BigDecimal> data = new HashMap<String, BigDecimal>();
    List<? extends SaleOrder> orders = saleOrderRepo.all().filter("self.statusSelect=?", 3).fetch();
    JsonNodeFactory factory = JsonNodeFactory.instance;
    ObjectNode mainNode = factory.objectNode();
    ArrayNode arrayNode = factory.arrayNode();

    ArrayNode labelNode = factory.arrayNode();
    labelNode.add("Country");
    labelNode.add("Turnover");
    arrayNode.add(labelNode);

    for (SaleOrder so : orders) {

      Country country = so.getMainInvoicingAddress().getAddressL7Country();
      BigDecimal value = so.getExTaxTotal();

      if (country != null) {
        String key = country.getName();

        if (data.containsKey(key)) {
          BigDecimal oldValue = data.get(key);
          oldValue = oldValue.add(value);
          data.put(key, oldValue);
        } else {
          data.put(key, value);
        }
      }
    }

    Iterator<String> keys = data.keySet().iterator();
    while (keys.hasNext()) {
      String key = keys.next();
      ArrayNode dataNode = factory.arrayNode();
      dataNode.add(key);
      dataNode.add(data.get(key));
      arrayNode.add(dataNode);
    }

    mainNode.put("status", 0);
    mainNode.set("data", arrayNode);

    return mainNode;
  }
}
