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
package com.axelor.apps.base.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.rest.dto.GlobalTranslationsResponse;
import com.axelor.apps.base.rest.dto.TranslationResponse;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.ResponseConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import wslite.json.JSONException;
import wslite.json.JSONObject;

@Path("/aos/translation")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TranslationRestController {

  @Path("/{lng}")
  @POST
  @HttpExceptionHandler
  @Transactional(rollbackOn = {Exception.class})
  public Response setTranslationJSON(@PathParam("lng") String language, JSONObject translationJson)
      throws AxelorException {
    try {
      LanguageChecker.check(language);
      Map<String, String> translationMap =
          new ObjectMapper().readValue(translationJson.toString(), Map.class);

      int addedTranslation =
          Beans.get(TranslationRestService.class).createNewTranslation(translationMap, language);

      if (addedTranslation == 0) {
        return ResponseConstructor.build(Response.Status.OK, "Translations already up-to-date.");
      }
      return ResponseConstructor.build(
          Response.Status.CREATED, addedTranslation + " translation(s) successfully added.");

    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Error while mapping json file.");
    }
  }

  @Path("/{lng}")
  @GET
  @HttpExceptionHandler
  public Response sendTranslationJSON(@PathParam("lng") String language)
      throws AxelorException, JSONException {
    LanguageChecker.check(language);

    Query<MetaTranslation> query =
        Beans.get(MetaTranslationRepository.class)
            .all()
            .filter("self.language = :language " + " AND self.key LIKE :key");
    query.bind("language", language);
    query.bind("key", "mobile_app_%");

    return ResponseConstructor.build(
        Response.Status.OK,
        new GlobalTranslationsResponse(
            query.fetchStream().map(TranslationResponse::build).collect(Collectors.toList())));
  }
}
