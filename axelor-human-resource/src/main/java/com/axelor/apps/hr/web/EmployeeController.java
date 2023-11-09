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
package com.axelor.apps.hr.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PrintFromBirtTemplateService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.DPAE;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.MedicalVisitService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;
import wslite.json.JSONObject;

@Singleton
public class EmployeeController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void showAnnualReport(ActionRequest request, ActionResponse response)
      throws JSONException, NumberFormatException, AxelorException {

    String employeeId = request.getContext().get("_id").toString();
    String year = request.getContext().get("year").toString();
    int yearId = new JSONObject(year).getInt("id");
    String yearName = new JSONObject(year).getString("name");
    User user = AuthUtils.getUser();

    String name =
        I18n.get("Annual expenses report") + " :  " + user.getFullName() + " (" + yearName + ")";

    Employee employee = Beans.get(EmployeeRepository.class).find(Long.valueOf(employeeId));
    BirtTemplate annualReportBirtTemplate =
        Beans.get(EmployeeService.class).getAnnualReportBirtTemplate(employee);
    String fileLink =
        Beans.get(BirtTemplateService.class)
            .generateBirtTemplateLink(
                annualReportBirtTemplate,
                EntityHelper.getEntity(employee),
                Map.of("YearId", Long.valueOf(yearId)),
                name,
                annualReportBirtTemplate.getAttach(),
                annualReportBirtTemplate.getFormat());

    response.setView(ActionView.define(name).add("html", fileLink).map());

    response.setCanClose(true);
  }

  public void setEmployeeSocialNetworkUrl(ActionRequest request, ActionResponse response) {

    Employee employee = request.getContext().asType(Employee.class);
    if (employee.getContactPartner() != null) {
      Map<String, String> urlMap =
          Beans.get(EmployeeService.class)
              .getSocialNetworkUrl(
                  employee.getContactPartner().getName(),
                  employee.getContactPartner().getFirstName());
      response.setAttr("contactPartner.facebookLabel", "title", urlMap.get("facebook"));
      response.setAttr("contactPartner.twitterLabel", "title", urlMap.get("twitter"));
      response.setAttr("contactPartner.linkedinLabel", "title", urlMap.get("linkedin"));
      response.setAttr("contactPartner.youtubeLabel", "title", urlMap.get("youtube"));
    }
  }

  public void setContactSocialNetworkUrl(ActionRequest request, ActionResponse response) {

    Partner partnerContact = request.getContext().asType(Partner.class);
    Map<String, String> urlMap =
        Beans.get(EmployeeService.class)
            .getSocialNetworkUrl(partnerContact.getName(), partnerContact.getFirstName());
    response.setAttr("facebookLabel", "title", urlMap.get("facebook"));
    response.setAttr("twitterLabel", "title", urlMap.get("twitter"));
    response.setAttr("linkedinLabel", "title", urlMap.get("linkedin"));
    response.setAttr("youtubeLabel", "title", urlMap.get("youtube"));
  }

  public void printEmployeePhonebook(ActionRequest request, ActionResponse response)
      throws AxelorException {

    BirtTemplate employeePhoneBookBirtTemplate =
        Beans.get(EmployeeService.class).getEmpPhoneBookBirtTemplate();
    String name = I18n.get("Employee PhoneBook");

    String fileLink =
        Beans.get(PrintFromBirtTemplateService.class).print(employeePhoneBookBirtTemplate, null);

    LOG.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  public void generateNewDPAE(ActionRequest request, ActionResponse response) {
    Employee employee = request.getContext().asType(Employee.class);
    employee = Beans.get(EmployeeRepository.class).find(employee.getId());

    try {
      Long dpaeId = Beans.get(EmployeeService.class).generateNewDPAE(employee);

      ActionViewBuilder builder =
          ActionView.define(I18n.get("DPAE"))
              .model(DPAE.class.getName())
              .add("grid", "dpae-grid")
              .add("form", "dpae-form")
              .param("search-filters", "dpae-filters")
              .context("_showRecord", dpaeId);
      response.setView(builder.map());
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }

    response.setReload(true);
  }

  public void updateEmployeeFilesWithMedicalVisit(ActionRequest request, ActionResponse response) {
    Employee employee = request.getContext().asType(Employee.class);
    response.setValue(
        "employeeFileList", Beans.get(MedicalVisitService.class).addToEmployeeFiles(employee));
  }
}
