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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.CrmReporting;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.studio.db.AppCrm;
import com.axelor.utils.StringTool;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Set;

public class CrmReportingServiceImpl implements CrmReportingService {

  protected String query = "";

  protected AppBaseService appBaseService;
  protected static final String PARTNER = "Partner";
  protected static final String LEAD = "Lead";
  protected static final String OPPORTUNITY = "Opportunity";
  protected static final String EVENT = "Event";

  @Inject
  public CrmReportingServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ActionViewBuilder createActionViewBuilder(CrmReporting crmReporting, Class<?> klass)
      throws ClassNotFoundException, AxelorException {
    if (crmReporting.getTypeSelect() != null) {
      String className = klass.getSimpleName();
      String model = null;

      boolean isPartner = crmReporting.getTypeSelect().equals(Partner.class.getName());
      boolean isOpportunity = false;

      if (className.equals(OPPORTUNITY) || className.equals(EVENT)) {

        if (isPartner) {
          model = PARTNER.toLowerCase();
        } else {
          model = LEAD.toLowerCase();
        }

        if (className.equals(OPPORTUNITY)) {
          isOpportunity = true;
        }
      }

      this.prepareQuery(crmReporting, isPartner, model);

      String idList =
          StringTool.getIdListString(
              !Strings.isNullOrEmpty(query)
                  ? Query.of((Class<Model>) klass).filter(query).fetch()
                  : null);

      ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get(className));
      actionViewBuilder.model(klass.getName());

      if (isOpportunity) {
        actionViewBuilder.add("kanban", className.toLowerCase() + "-kanban");
      }

      actionViewBuilder.add("grid", className.toLowerCase() + "-grid");
      actionViewBuilder.add("form", className.toLowerCase() + "-form");
      actionViewBuilder.domain(
          (!idList.isEmpty()) ? "self.id IN (" + idList + ")" : "self.id IS NULL");
      return actionViewBuilder;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_MISSING_FIELD,
        I18n.get(CrmExceptionMessage.CRM_REPORTING_TYPE_SELECT_MISSING));
  }

  protected void prepareQuery(CrmReporting crmReporting, boolean isPartner, String model) {
    model = Strings.isNullOrEmpty(model) ? "" : model + ".";

    if (isPartner) {
      partnerQuery(crmReporting, model);
    } else {
      leadQuery(crmReporting, model);
    }

    if (!crmReporting.getAgencySet().isEmpty()
        && ((AppCrm) appBaseService.getApp("crm")).getAgenciesManagement())
      this.addParams(
          "self."
              + model
              + "agency IN ("
              + StringTool.getIdListString(crmReporting.getAgencySet())
              + ")");

    if (!crmReporting.getIndustrySectorSet().isEmpty())
      this.addParams(
          "self."
              + model
              + "industrySector IN ("
              + StringTool.getIdListString(crmReporting.getIndustrySectorSet())
              + ")");

    if (appBaseService.getAppBase().getTeamManagement() && !crmReporting.getTeamSet().isEmpty())
      this.addParams(
          "self.team IN (" + StringTool.getIdListString(crmReporting.getTeamSet()) + ")");

    if (crmReporting.getFromDate() != null)
      this.addParams("date(self.createdOn) >= '" + crmReporting.getFromDate() + "'");

    if (crmReporting.getToDate() != null)
      this.addParams("date(self.createdOn) <= '" + crmReporting.getToDate() + "'");
  }

  private void partnerQuery(CrmReporting crmReporting, String model) {
    if (appBaseService.getAppBase().getEnableMultiCompany()
        && !crmReporting.getCompanySet().isEmpty())
      this.addParams(
          "("
              + companyQuery("MEMBER OF self." + model + "companySet", crmReporting.getCompanySet())
              + ")");

    if (!crmReporting.getCategorySet().isEmpty())
      this.addParams(
          "self."
              + model
              + "partnerCategory "
              + "IN ("
              + StringTool.getIdListString(crmReporting.getCategorySet())
              + ")");

    if (!crmReporting.getCountrySet().isEmpty())
      this.addParams(
          "self."
              + model
              + "partnerAddressList.address.addressL7Country "
              + "IN ("
              + StringTool.getIdListString(crmReporting.getCountrySet())
              + ")");
  }

  private void leadQuery(CrmReporting crmReporting, String model) {
    if (appBaseService.getAppBase().getEnableMultiCompany()
        && !crmReporting.getCompanySet().isEmpty())
      this.addParams(
          "self."
              + model
              + "company IN ("
              + StringTool.getIdListString(crmReporting.getCompanySet())
              + ")");

    if (!crmReporting.getCategorySet().isEmpty())
      this.addParams(
          "self."
              + model
              + "type "
              + "IN ("
              + StringTool.getIdListString(crmReporting.getCategorySet())
              + ")");

    if (!crmReporting.getCountrySet().isEmpty())
      this.addParams(
          "self."
              + model
              + "primaryCountry "
              + "IN ("
              + StringTool.getIdListString(crmReporting.getCountrySet())
              + ")");
  }

  protected String companyQuery(String queryStr, Set<Company> companies) {
    int count = 0;
    StringBuilder comQuery = new StringBuilder();
    for (Company company : companies) {
      comQuery.append("(" + company.getId() + ") " + queryStr);
      count++;
      if (count < companies.size()) {
        comQuery.append(" OR ");
      }
    }
    return comQuery.toString();
  }

  protected void addParams(String paramQuery) {
    if (!Strings.isNullOrEmpty(query)) {
      this.query += " AND ";
    }

    this.query += paramQuery;
  }
}
