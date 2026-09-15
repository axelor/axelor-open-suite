/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.auth.AuthUtils;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.studio.db.AppCrm;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.Set;

public class CrmReportingServiceImpl implements CrmReportingService {

  protected AppBaseService appBaseService;
  protected static final String PARTNER = "Partner";
  protected static final String LEAD = "eventLead";
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
          model = LEAD;
        }

        if (className.equals(OPPORTUNITY)) {
          isOpportunity = true;
        }
      }

      String query = this.prepareQuery(crmReporting, isPartner, model);
      String idList = null;
      Query<Model> q = Query.of((Class<Model>) klass).filter(query);

      if (crmReporting.getFromDate() != null) {
        q.bind("fromDate", crmReporting.getFromDate());
      }
      if (crmReporting.getToDate() != null) {
        q.bind("toDate", crmReporting.getToDate());
      }

      idList = StringHelper.getIdListString(q.fetch());

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

  @Override
  public Set<Company> prefillCompanySet(CrmReporting crmReporting) {
    Set<Company> companySet = new HashSet<>();
    if (crmReporting.getCompanySet() != null) {
      companySet = crmReporting.getCompanySet();
    }
    if (AuthUtils.getUser() != null && AuthUtils.getUser().getActiveCompany() != null) {
      companySet.add(AuthUtils.getUser().getActiveCompany());
    }
    return companySet;
  }

  protected String prepareQuery(CrmReporting crmReporting, boolean isPartner, String model) {
    StringBuilder query = new StringBuilder();
    model = Strings.isNullOrEmpty(model) ? "" : model + ".";

    if (isPartner) {
      partnerQuery(query, crmReporting, model);
    } else {
      leadQuery(query, crmReporting, model);
    }

    if (!crmReporting.getAgencySet().isEmpty()
        && ((AppCrm) appBaseService.getApp("crm")).getAgenciesManagement())
      this.addParams(
          query,
          "self."
              + model
              + "agency.id IN ("
              + StringHelper.getIdListString(crmReporting.getAgencySet())
              + ")");

    if (!crmReporting.getIndustrySectorSet().isEmpty())
      this.addParams(
          query,
          "self."
              + model
              + "industrySector.id IN ("
              + StringHelper.getIdListString(crmReporting.getIndustrySectorSet())
              + ")");

    if (appBaseService.getAppBase().getTeamManagement() && !crmReporting.getTeamSet().isEmpty())
      this.addParams(
          query,
          "self.team.id IN (" + StringHelper.getIdListString(crmReporting.getTeamSet()) + ")");

    if (crmReporting.getFromDate() != null)
      this.addParams(query, "date(self.createdOn) >= :fromDate");

    if (crmReporting.getToDate() != null) this.addParams(query, "date(self.createdOn) <= :toDate");

    return query.toString();
  }

  private void partnerQuery(StringBuilder query, CrmReporting crmReporting, String model) {
    if (appBaseService.getAppBase().getEnableMultiCompany()
        && !crmReporting.getCompanySet().isEmpty())
      this.addParams(
          query,
          "(" + companyQuery("self." + model + "companySet", crmReporting.getCompanySet()) + ")");

    if (!crmReporting.getCategorySet().isEmpty())
      this.addParams(
          query,
          "self."
              + model
              + "partnerCategory.id "
              + "IN ("
              + StringHelper.getIdListString(crmReporting.getCategorySet())
              + ")");

    if (!crmReporting.getCountrySet().isEmpty())
      this.addParams(
          query,
          "self."
              + model
              + "partnerAddressList.address.country.id "
              + "IN ("
              + StringHelper.getIdListString(crmReporting.getCountrySet())
              + ")");
  }

  private void leadQuery(StringBuilder query, CrmReporting crmReporting, String model) {
    if (appBaseService.getAppBase().getEnableMultiCompany()
        && !crmReporting.getCompanySet().isEmpty())
      this.addParams(
          query,
          "self."
              + model
              + "company.id IN ("
              + StringHelper.getIdListString(crmReporting.getCompanySet())
              + ")");

    if (!crmReporting.getCategorySet().isEmpty())
      this.addParams(
          query,
          "self."
              + model
              + "type.id "
              + "IN ("
              + StringHelper.getIdListString(crmReporting.getCategorySet())
              + ")");

    if (!crmReporting.getCountrySet().isEmpty())
      this.addParams(
          query,
          "self."
              + model
              + "address.country.id "
              + "IN ("
              + StringHelper.getIdListString(crmReporting.getCountrySet())
              + ")");
  }

  protected String companyQuery(String queryStr, Set<Company> companies) {
    return queryStr + ".id IN (" + StringHelper.getIdListString(companies) + ")";
  }

  protected void addParams(StringBuilder query, String paramQuery) {
    if (query.length() > 0) {
      query.append(" AND ");
    }

    query.append(paramQuery);
  }
}
