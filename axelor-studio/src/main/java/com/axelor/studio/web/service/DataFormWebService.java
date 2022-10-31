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
package com.axelor.studio.web.service;

import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.studio.db.DataForm;
import com.axelor.studio.service.DataFormService;
import com.axelor.studio.variables.DataFormVariables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Singleton
@Path("/public")
public class DataFormWebService {

  protected DataFormService dataFormService;

  @Inject
  public DataFormWebService(DataFormService dataFormService) {
    this.dataFormService = dataFormService;
  }

  @POST
  @Path("/data-form")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getData(MultipartFormDataInput formDataInput)
      throws IOException, ServletException {
    Map<String, List<InputPart>> formDataMap = formDataInput.getFormDataMap();

    String response;
    try {
      DataForm dataForm = dataFormService.getDataForm(formDataMap);
      Class<?> klass;
      String jsonModelName;
      if (dataForm.getCustom()) {
        jsonModelName = dataForm.getMetaJsonModel().getName();
        dataFormService.checkRecordToCreate(formDataMap, jsonModelName);
        klass = Class.forName(MetaJsonRecord.class.getCanonicalName());
      } else {
        dataFormService.checkRecordToCreate(formDataMap, dataForm.getMetaModel().getName());
        jsonModelName = StringUtils.EMPTY;
        klass = Class.forName(dataForm.getMetaModel().getFullName());
      }
      formDataMap.remove(DataFormVariables.RECORD_TO_CREATE);
      formDataMap.remove(DataFormVariables.MODEL_CODE);
      dataFormService.createRecord(formDataMap, klass, dataForm.getCustom(), jsonModelName);

      response =
          String.format(
              DataFormVariables.RESPONSE_FORMAT,
              dataFormService.getSuccessfulFormSubmissionMessage(),
              StringUtils.defaultIfEmpty(dataForm.getRedirectUrl(), StringUtils.EMPTY));
      return Response.ok()
          .header(DataFormVariables.ACCESS_CONTROL_ALLOW_ORIGIN, DataFormVariables.ALLOW_ALL)
          .entity(response)
          .build();
    } catch (Exception e) {
      TraceBackService.trace(e);
      response =
          String.format(
              DataFormVariables.RESPONSE_FORMAT,
              dataFormService.getFailedFormSubmissionMessage(),
              StringUtils.EMPTY);
      return Response.serverError()
          .header(DataFormVariables.ACCESS_CONTROL_ALLOW_ORIGIN, DataFormVariables.ALLOW_ALL)
          .entity(response)
          .build();
    }
  }
}
