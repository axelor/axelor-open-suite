package com.axelor.apps.base.rest;

import com.axelor.apps.base.rest.dto.GlobalTranslationsResponse;
import com.axelor.apps.base.rest.dto.TranslationResponse;
import com.axelor.apps.tool.api.HttpExceptionHandler;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.google.inject.persist.Transactional;
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
      throws AxelorException, JSONException {
    LanguageChecker.check(language);

    int addedTranslation =
        Beans.get(TranslationRestService.class).createNewTranslation(translationJson, language);

    if (addedTranslation == 0) {
      return ResponseConstructor.build(Response.Status.OK, "Translations already up-to-date.");
    }
    return ResponseConstructor.build(
        Response.Status.CREATED, addedTranslation + " translation(s) successfully added.");
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
