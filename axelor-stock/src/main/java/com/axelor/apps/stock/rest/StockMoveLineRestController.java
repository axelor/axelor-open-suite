/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.rest;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.rest.dto.StockMoveLinePutRequest;
import com.axelor.apps.stock.rest.dto.StockMoveLineResponse;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/stock-move-line")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StockMoveLineRestController {

  /**
   * Update realQty and conformity of an incoming stock move. Full path to request is
   * /ws/aos/stock-move-line/{id}
   */
  @Path("/{id}")
  @PUT
  @HttpExceptionHandler
  public Response updateStockMoveLine(
      @PathParam("id") long stockMoveLineId, StockMoveLinePutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(StockMove.class).check();

    StockMoveLine stockmoveLine =
        ObjectFinder.find(StockMoveLine.class, stockMoveLineId, requestBody.getVersion());

    Beans.get(StockMoveLineService.class)
        .updateStockMoveLine(stockmoveLine, requestBody.getRealQty(), requestBody.getConformity());

    return ResponseConstructor.build(
        Response.Status.OK, "Line successfully updated.", new StockMoveLineResponse(stockmoveLine));
  }
}
