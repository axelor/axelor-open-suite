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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.MapRestService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import java.math.BigDecimal;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.tuple.Pair;

@Path("/map")
public class MapRestStock {

  @Inject private MapService mapService;

  @Inject private MapRestService mapRestService;

  @Inject private StockMoveRepository stockMoveRepository;

  JsonNodeFactory factory = JsonNodeFactory.instance;

  @Path("/stockMove/{id}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getAddress(@PathParam("id") long id) {
    ObjectNode mainNode = factory.objectNode();

    try {
      StockMove stockMove = stockMoveRepository.find(id);

      Address fromAddress = stockMove.getFromAddress();
      Address toAddress = stockMove.getToAddress();
      ArrayNode arrayNode = factory.arrayNode();
      addAddressNode(arrayNode, fromAddress, stockMove);
      addAddressNode(arrayNode, toAddress, stockMove);

      mapRestService.setData(mainNode, arrayNode);
    } catch (Exception e) {
      mapRestService.setError(mainNode, e);
    }

    return mainNode;
  }

  protected void addAddressNode(ArrayNode arrayNode, Address address, StockMove stockMove) {
    BigDecimal lat = BigDecimal.ZERO;
    BigDecimal lng = BigDecimal.ZERO;
    if (address == null) {
      address = stockMove.getCompany().getAddress();
      lat = address.getLatit();
      lng = address.getLongit();
    }
    String addressString = mapRestService.makeAddressString(address);
    Pair<BigDecimal, BigDecimal> latLng =
        mapService.getLatLong(addressString.replace("<br/>", " "), lat, lng);
    ObjectNode objectNode = factory.objectNode();
    objectNode.put("latit", latLng.getLeft());
    objectNode.put("longit", latLng.getRight());
    objectNode.put("address", addressString.replace("<br/>", ", "));
    arrayNode.add(objectNode);
  }
}
