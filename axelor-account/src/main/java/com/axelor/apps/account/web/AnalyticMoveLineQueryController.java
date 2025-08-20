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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.AnalyticMoveLineQuery;
import com.axelor.apps.account.db.AnalyticMoveLineQueryParameter;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticAccountService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineParentService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineQueryPercentageService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineQueryService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ContextHelper;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticMoveLineQueryController {

  public void filterAnalyticMoveLines(ActionRequest request, ActionResponse response) {
    try {
      AnalyticMoveLineQuery analyticMoveLineQuery =
          request.getContext().asType(AnalyticMoveLineQuery.class);
      String query =
          Beans.get(AnalyticMoveLineQueryService.class)
              .getAnalyticMoveLineQuery(analyticMoveLineQuery);

      List<Long> analyticMoveLineList =
          Beans.get(AnalyticMoveLineRepository.class)
              .all()
              .filter(query)
              .select("id")
              .fetch(0, 0)
              .stream()
              .map(m -> (Long) m.get("id"))
              .collect(Collectors.toList());

      response.setValue("__analyticMoveLineList", analyticMoveLineList);
      response.setAttr("filteredAnalyticmoveLinesDashlet", "refresh", true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void selectedReverse(ActionRequest request, ActionResponse response) {
    try {

      Context context = request.getContext();
      AnalyticMoveLineQuery analyticMoveLineQuery =
          context.getParent().asType(AnalyticMoveLineQuery.class);
      if (!context.containsKey("_ids")
          || ObjectUtils.isEmpty(request.getContext().get("_ids"))
          || ObjectUtils.isEmpty(
              analyticMoveLineQuery.getReverseAnalyticMoveLineQueryParameterList())) {
        return;
      }

      List<AnalyticMoveLine> analyticMoveLines =
          Beans.get(AnalyticMoveLineRepository.class)
              .all()
              .filter("self.id in :ids")
              .bind("ids", context.get("_ids"))
              .fetch();

      reverses(response, analyticMoveLineQuery, analyticMoveLines);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void allReverse(ActionRequest request, ActionResponse response) {
    try {

      AnalyticMoveLineQuery analyticMoveLineQuery =
          request.getContext().getParent().asType(AnalyticMoveLineQuery.class);
      if (ObjectUtils.isEmpty(analyticMoveLineQuery.getSearchAnalyticMoveLineQueryParameterList())
          || ObjectUtils.isEmpty(
              analyticMoveLineQuery.getReverseAnalyticMoveLineQueryParameterList())) {
        return;
      }
      String query =
          Beans.get(AnalyticMoveLineQueryService.class)
              .getAnalyticMoveLineQuery(analyticMoveLineQuery);
      List<AnalyticMoveLine> analyticMoveLines =
          Beans.get(AnalyticMoveLineRepository.class).all().filter(query).fetch();
      reverses(response, analyticMoveLineQuery, analyticMoveLines);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void reverses(
      ActionResponse response,
      AnalyticMoveLineQuery analyticMoveLineQuery,
      List<AnalyticMoveLine> analyticMoveLines)
      throws AxelorException {
    AnalyticMoveLineQueryService analyticMoveLineQueryService =
        Beans.get(AnalyticMoveLineQueryService.class);

    Beans.get(AnalyticMoveLineQueryPercentageService.class)
        .validateReverseParameterAxisPercentage(
            analyticMoveLineQuery.getReverseAnalyticMoveLineQueryParameterList());

    Set<AnalyticMoveLine> reverseAnalyticMoveLines =
        analyticMoveLineQueryService.analyticMoveLineReverses(
            analyticMoveLineQuery, analyticMoveLines);
    Set<AnalyticMoveLine> newAnalyticMoveLines =
        analyticMoveLineQueryService.createAnalyticMoveLines(
            analyticMoveLineQuery, analyticMoveLines);

    List<AnalyticMoveLine> filteredAnalyticMoveLineList =
        Beans.get(AnalyticMoveLineRepository.class)
            .all()
            .filter(analyticMoveLineQueryService.getAnalyticMoveLineQuery(analyticMoveLineQuery))
            .fetch();

    response.setInfo(
        String.format(
            I18n.get(
                "The analytic revision process has ended and generated %s reverse and %s revision analytic move lines."),
            reverseAnalyticMoveLines.size(),
            newAnalyticMoveLines.size()));

    response.setValue(
        "__analyticMoveLineList",
        filteredAnalyticMoveLineList.stream().map(l -> l.getId()).collect(Collectors.toList()));
    response.setAttr("filteredAnalyticmoveLinesDashlet", "refresh", true);
  }

  public void searchQueryAxisDomain(ActionRequest request, ActionResponse response) {
    this.queryAxisDomain(request, response, false);
  }

  public void reverseQueryAxisDomain(ActionRequest request, ActionResponse response) {
    this.queryAxisDomain(request, response, true);
  }

  protected void queryAxisDomain(
      ActionRequest request, ActionResponse response, boolean isReverseQuery) {
    try {
      AnalyticMoveLineQuery analyticMoveLineQuery =
          ContextHelper.getContextParent(request.getContext(), AnalyticMoveLineQuery.class, 1);

      if (analyticMoveLineQuery != null) {
        List<AnalyticAxis> analyticAxisList =
            Beans.get(AnalyticMoveLineQueryService.class)
                .getAvailableAnalyticAxes(analyticMoveLineQuery, isReverseQuery);
        String analyticAxisIds =
            analyticAxisList.isEmpty()
                ? "0"
                : analyticAxisList.stream()
                    .map(AnalyticAxis::getId)
                    .map(Objects::toString)
                    .collect(Collectors.joining(","));

        response.setAttr(
            "analyticAxis", "domain", String.format("self.id IN (%s)", analyticAxisIds));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setAnalyticAccountDomains(ActionRequest request, ActionResponse response) {
    try {
      AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);
      InvoiceLine invoiceLine =
          ContextHelper.getContextParent(request.getContext(), InvoiceLine.class, 1);
      MoveLine moveLine = ContextHelper.getContextParent(request.getContext(), MoveLine.class, 1);

      AnalyticLineService analyticLineService = Beans.get(AnalyticLineService.class);
      List<Long> analyticAccountList = new ArrayList<>();

      StringBuilder domain =
          new StringBuilder(
              Beans.get(AnalyticAccountService.class).getIsNotParentAnalyticAccountQuery());
      domain.append(" AND self.id in (");

      if (invoiceLine != null) {
        analyticAccountList =
            analyticLineService.getAnalyticAccountsByAxis(
                invoiceLine, analyticMoveLine.getAnalyticAxis());
      } else if (moveLine != null) {
        analyticAccountList =
            analyticLineService.getAnalyticAccountsByAxis(
                moveLine, analyticMoveLine.getAnalyticAxis());
      }
      if (CollectionUtils.isEmpty(analyticAccountList)) {
        domain.append("0");
      } else {
        String idList = Joiner.on(",").join(analyticAccountList);
        domain.append(idList);
      }
      domain.append(")");
      response.setAttr("analyticAccount", "domain", domain.toString());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAnalyticAxisDomain(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      AnalyticMoveLine analyticMoveLine = context.asType(AnalyticMoveLine.class);
      Company company;
      if (analyticMoveLine.getAnalyticJournal() != null
          && analyticMoveLine.getAnalyticJournal().getCompany() != null) {
        company = analyticMoveLine.getAnalyticJournal().getCompany();
      } else {
        company = ContextHelper.getFieldFromContextParent(context, "company", Company.class);
        if (company == null) {
          Move move = ContextHelper.getFieldFromContextParent(context, "move", Move.class);
          if (move != null) {
            company = move.getCompany();
          }
        }
      }
      response.setAttr(
          "analyticAxis",
          "domain",
          Beans.get(AnalyticMoveLineService.class).getAnalyticAxisDomain(company));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void refreshAxis(ActionRequest request, ActionResponse response) throws AxelorException {
    AnalyticMoveLine analyticMoveLine = request.getContext().asType(AnalyticMoveLine.class);

    Beans.get(AnalyticMoveLineParentService.class).refreshAxisOnParent(analyticMoveLine);
    response.setReload(true);
  }

  public void initPercentage(ActionRequest request, ActionResponse response) {
    AnalyticMoveLineQueryParameter parameter =
        request.getContext().asType(AnalyticMoveLineQueryParameter.class);
    List<AnalyticMoveLineQueryParameter> reverseList =
        request
            .getContext()
            .getParent()
            .asType(AnalyticMoveLineQuery.class)
            .getReverseAnalyticMoveLineQueryParameterList();

    response.setValue(
        "percentage",
        Beans.get(AnalyticMoveLineQueryPercentageService.class)
            .getMissingPercentageOnAxis(parameter, reverseList));
  }
}
