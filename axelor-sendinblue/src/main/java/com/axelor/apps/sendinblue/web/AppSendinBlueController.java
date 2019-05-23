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
package com.axelor.apps.sendinblue.web;

import com.axelor.apps.base.db.AppSendinblue;
import com.axelor.apps.sendinblue.service.AppSendinBlueService;
import com.axelor.apps.sendinblue.translation.ITranslation;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sendinblue.auth.ApiKeyAuth;

public class AppSendinBlueController {

  @Inject protected AppSendinBlueService appSendinBlueService;

  private Logger LOG = LoggerFactory.getLogger(getClass());

  public void authenticateSendinBlue(ActionRequest request, ActionResponse response) {
    try {
      appSendinBlueService.getApiKeyAuth();
      response.setFlash(I18n.get(ITranslation.AUTHENTICATE_MESSAGE));
    } catch (AxelorException e) {
      response.setFlash(e.getLocalizedMessage());
    }
  }

  public void openServer(ActionRequest request, ActionResponse response) {
    try {
      ApiKeyAuth apiKeyAuth = appSendinBlueService.getApiKeyAuth();

      // https://my.sendinblue.com/dashboard
      // https://app.sendinblue.com/account/profile/
      // https://api.sendinblue.com/v2.0
      // https://app.sendinblue.com/embed
      // https://www.sendinblue.com/

      OkHttpClient client = new OkHttpClient();
      Request request1 =
          new Request.Builder()
              .url("https://api.sendinblue.com/v3/account")
              .addHeader("api-key", apiKeyAuth.getApiKey())
              .get()
              .build();
      // Response response1 =
      client.newCall(request1).execute();

      response.setView(
          ActionView.define("SendinBlue Server")
              .add("html", "https://my.sendinblue.com/dashboard")
              .param("api-key", apiKeyAuth.getApiKey())
              .map());

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void addContactFields(ActionRequest request, ActionResponse response)
      throws AxelorException {
    appSendinBlueService.exportContactFields();
    LOG.debug("Contact Fields Export Completed");
  }

  public void exportContacts(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AppSendinblue appSendinblue = request.getContext().asType(AppSendinblue.class);
    if (appSendinblue.getPartnerSet().isEmpty() && appSendinblue.getLeadSet().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(ITranslation.EXPORT_CONTACT_ERROR));
    }
    appSendinBlueService.exportContacts(appSendinblue);
    LOG.debug("Contacts Export Completed");
  }

  public void exportTemplates(ActionRequest request, ActionResponse response)
      throws AxelorException {
    appSendinBlueService.exportTemplate();
    LOG.debug("Templates Export Completed");
  }

  public void exportCampaigns(ActionRequest request, ActionResponse response)
      throws AxelorException {
    appSendinBlueService.exportCampaign();
    LOG.debug("Campaigns Export Completed");
  }

  public void report(ActionRequest request, ActionResponse response) throws AxelorException {
    LocalDate fromDate =
        LocalDate.parse(
            request.getContext().get("fromDate").toString(), DateTimeFormatter.ISO_DATE);
    LocalDate toDate =
        LocalDate.parse(request.getContext().get("toDate").toString(), DateTimeFormatter.ISO_DATE);
    List<Map<String, Object>> dataList = appSendinBlueService.getReport(fromDate, toDate);
    response.setData(dataList);
  }

  public void importCampaignReport(ActionRequest request, ActionResponse response)
      throws AxelorException {
    appSendinBlueService.importCampaignReport();
    LOG.debug("Campaigns Report Import Completed");
  }

  public void importEvents(ActionRequest request, ActionResponse response) throws AxelorException {
    appSendinBlueService.importEvents();
    LOG.debug("Event Report Import Completed");
  }

  public void importContactStat(ActionRequest request, ActionResponse response)
      throws AxelorException {
    appSendinBlueService.importContactStat();
    LOG.debug("Contact Statistics Import Completed");
  }

  public void importCampaignStat(ActionRequest request, ActionResponse response)
      throws AxelorException {
    appSendinBlueService.importCampaignStat();
    LOG.debug("Campaigns Statistics Import Completed");
  }
}
