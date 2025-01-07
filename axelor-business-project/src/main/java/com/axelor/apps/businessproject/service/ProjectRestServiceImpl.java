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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.businessproject.rest.dto.ProjectReportingCategoryResponse;
import com.axelor.apps.businessproject.rest.dto.ProjectReportingIndicatorResponse;
import com.axelor.apps.businessproject.rest.dto.ProjectReportingResponse;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectRestServiceImpl implements ProjectRestService {

  @Override
  public ProjectReportingResponse getProjectReportingValues(Project project) {
    return new ProjectReportingResponse(
        project, List.of(getTimeFollowUp(project), getFinancialFollowUp(project)));
  }

  protected ProjectReportingCategoryResponse getTimeFollowUp(Project project) {
    String unit = fetchProjectTimeUnit(project);
    List<ProjectReportingIndicatorResponse> indicatorList = new ArrayList<>();
    indicatorList.add(
        new ProjectReportingIndicatorResponse(I18n.get("Sold time"), project.getSoldTime(), unit));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("Updated time"), project.getUpdatedTime(), unit));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("Planned time"), project.getPlannedTime(), unit));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("Spent time"), project.getSpentTime(), unit));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("% of progress"), project.getPercentageOfProgress(), "%"));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("% of consumption"), project.getPercentageOfConsumption(), "%"));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("Remaining amount to do"), project.getRemainingAmountToDo(), unit));
    return new ProjectReportingCategoryResponse(I18n.get("Time follow-up"), indicatorList, null);
  }

  protected ProjectReportingCategoryResponse getFinancialFollowUp(Project project) {
    List<ProjectReportingCategoryResponse> categoryList = new ArrayList<>();
    categoryList.add(getFinancialSoldFollowUp(project));
    categoryList.add(getFinancialForecastFollowUp(project));
    categoryList.add(getFinancialRealFollowUp(project));
    categoryList.add(getFinancialLandingFollowUp(project));
    categoryList.add(getFinancialInvoicingFollowUp(project));
    return new ProjectReportingCategoryResponse(
        I18n.get("Financial follow-up"), null, categoryList);
  }

  protected ProjectReportingCategoryResponse getFinancialSoldFollowUp(Project project) {
    String currency = fetchProjectCompanyCurrency(project);
    List<ProjectReportingIndicatorResponse> indicatorList = new ArrayList<>();
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Turnover"), project.getTurnover(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Costs"), project.getInitialCosts(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Margin"), project.getInitialMargin(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Markup"), project.getInitialMarkup(), "%"));
    return new ProjectReportingCategoryResponse(I18n.get("ProjectTask.Sold"), indicatorList, null);
  }

  protected ProjectReportingCategoryResponse getFinancialForecastFollowUp(Project project) {
    String currency = fetchProjectCompanyCurrency(project);
    List<ProjectReportingIndicatorResponse> indicatorList = new ArrayList<>();
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Costs"), project.getForecastCosts(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Margin"), project.getForecastMargin(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Markup"), project.getForecastMarkup(), "%"));
    return new ProjectReportingCategoryResponse(
        I18n.get("ProjectTask.Forecast"), indicatorList, null);
  }

  protected ProjectReportingCategoryResponse getFinancialRealFollowUp(Project project) {
    String currency = fetchProjectCompanyCurrency(project);
    List<ProjectReportingIndicatorResponse> indicatorList = new ArrayList<>();
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("Real turnover"), project.getRealTurnover(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Costs"), project.getRealCosts(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Margin"), project.getRealMargin(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Markup"), project.getRealMarkup(), "%"));
    return new ProjectReportingCategoryResponse(I18n.get("ProjectTask.Real"), indicatorList, null);
  }

  protected ProjectReportingCategoryResponse getFinancialLandingFollowUp(Project project) {
    String currency = fetchProjectCompanyCurrency(project);
    List<ProjectReportingIndicatorResponse> indicatorList = new ArrayList<>();
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Costs"), project.getLandingCosts(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Margin"), project.getLandingMargin(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("ProjectTask.Markup"), project.getLandingMarkup(), "%"));
    return new ProjectReportingCategoryResponse(
        I18n.get("ProjectTask.Landing"), indicatorList, null);
  }

  protected ProjectReportingCategoryResponse getFinancialInvoicingFollowUp(Project project) {
    String currency = fetchProjectCompanyCurrency(project);
    List<ProjectReportingIndicatorResponse> indicatorList = new ArrayList<>();
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("Total invoiced"), project.getTotalInvoiced(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("Invoiced this month"), project.getInvoicedThisMonth(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("Invoiced last month"), project.getInvoicedLastMonth(), currency));
    indicatorList.add(
        new ProjectReportingIndicatorResponse(
            I18n.get("Total paid (incl. VAT)"), project.getTotalPaid(), currency));
    return new ProjectReportingCategoryResponse(
        I18n.get("Invoicing (excl. VAT)"), indicatorList, null);
  }

  protected String fetchProjectTimeUnit(Project project) {
    return Optional.ofNullable(project.getProjectTimeUnit())
        .map(timeUnit -> timeUnit.getName() + "(s)")
        .orElse("");
  }

  protected String fetchProjectCompanyCurrency(Project project) {
    return Optional.ofNullable(project.getCompany())
        .map(Company::getCurrency)
        .map(Currency::getSymbol)
        .orElse("");
  }
}
