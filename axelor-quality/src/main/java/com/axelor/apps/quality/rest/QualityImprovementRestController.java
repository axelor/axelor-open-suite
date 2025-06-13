/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.quality.db.QIIdentification;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QualityImprovement;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.rest.dto.QualityImprovementCreateUpdateResult;
import com.axelor.apps.quality.rest.dto.QualityImprovementPostRequest;
import com.axelor.apps.quality.rest.dto.QualityImprovementPutRequest;
import com.axelor.apps.quality.rest.dto.QualityImprovementResponse;
import com.axelor.apps.quality.rest.service.QualityImprovementCreateAPIService;
import com.axelor.apps.quality.rest.service.QualityImprovementUpdateAPIService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import com.axelor.utils.api.SecurityCheck;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/aos/quality-improvement")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class QualityImprovementRestController {

  @Operation(
      summary = "Create a quality improvement",
      tags = {"Quality improvement"})
  @Path("/")
  @POST
  @HttpExceptionHandler
  public Response createQualityImprovement(QualityImprovementPostRequest requestBody)
      throws AxelorException {
    RequestValidator.validateBody(requestBody);
    new SecurityCheck()
        .createAccess(QualityImprovement.class)
        .createAccess(QIIdentification.class)
        .createAccess(QIResolution.class)
        .check();

    QualityImprovementCreateUpdateResult qualityImprovementCreateUpdateResult =
        Beans.get(QualityImprovementCreateAPIService.class).createQualityImprovement(requestBody);
    QualityImprovement qualityImprovement =
        qualityImprovementCreateUpdateResult.getQualityImprovement();

    String responseMessage =
        String.format(
                I18n.get(QualityExceptionMessage.API_QI_CREATION_MESSAGE),
                qualityImprovement.getId())
            + " "
            + Optional.ofNullable(qualityImprovementCreateUpdateResult.getErrorMessage())
                .orElse("");
    return ResponseConstructor.build(
        Response.Status.CREATED,
        responseMessage,
        new QualityImprovementResponse(qualityImprovement));
  }

  @Operation(
      summary = "Update a quality improvement",
      tags = {"Quality improvement"})
  @Path("/update/{qualityImprovementId}")
  @PUT
  @HttpExceptionHandler
  public Response updateQualityImprovement(
      @PathParam("qualityImprovementId") Long qualityImprovementId,
      QualityImprovementPutRequest requestBody)
      throws AxelorException {
    new SecurityCheck().writeAccess(QualityImprovement.class, qualityImprovementId).check();
    RequestValidator.validateBody(requestBody);

    QualityImprovement qualityImprovement =
        ObjectFinder.find(QualityImprovement.class, qualityImprovementId, requestBody.getVersion());

    Beans.get(QualityImprovementUpdateAPIService.class)
        .updateQualityImprovement(qualityImprovement, requestBody);

    return ResponseConstructor.build(
        Response.Status.OK,
        I18n.get(QualityExceptionMessage.QUALITY_IMPROVEMENT_UPDATED),
        new QualityImprovementResponse(qualityImprovement));
  }
}
