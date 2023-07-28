/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

package com.axelor.apps.helpdesk.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.rest.dto.TicketPutRequest;
import com.axelor.apps.helpdesk.rest.dto.TicketResponse;
import com.axelor.apps.helpdesk.rest.service.TicketUpdateRestService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@OpenAPIDefinition(servers = {@Server(url = "../")})
@Path("/aos/ticket")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TicketRestController {

  /** Update status of a given ticket. Full path to request is /ws/aos/ticket/{id} */
  @Operation(
      summary = "Update ticket status",
      tags = {"Ticket"})
  @Path("/{id}")
  @PUT
  @HttpExceptionHandler
  public Response updateTicket(@PathParam("id") long ticketId, TicketPutRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck().writeAccess(Ticket.class).check();

    Ticket ticket = ObjectFinder.find(Ticket.class, ticketId, requestBody.getVersion());

    ticket =
        Beans.get(TicketUpdateRestService.class)
            .updateTicketStatus(ticket, requestBody.getTargetStatus(), requestBody.getDateTime());

    return ResponseConstructor.build(
        Response.Status.OK, "Ticket updated.", new TicketResponse(ticket));
  }
}
