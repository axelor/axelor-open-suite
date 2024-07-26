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
package com.axelor.apps.sale.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.rest.dto.SaleOrderPostRequest;
import com.axelor.inject.Beans;
import com.axelor.utils.api.*;
import io.swagger.v3.oas.annotations.Operation;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/expense")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SaleOrderRestController {
  @Operation(
          summary = "Create a sale oder",
          tags = {"SaleOrder"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createExpense(SaleOrderPostRequest requestBody) throws AxelorException {

    return null;
  }
}

