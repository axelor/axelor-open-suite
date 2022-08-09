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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.AnalyticMoveLineQuery;
import com.axelor.apps.account.db.AnalyticMoveLineQueryParameter;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticAxisRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineQueryRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AnalyticMoveLineQueryServiceImpl implements AnalyticMoveLineQueryService {

  protected AnalyticMoveLineService analyticMoveLineService;
  protected AppBaseService appBaseService;
  protected AnalyticMoveLineQueryRepository analyticMoveLineQueryRepository;
  protected AnalyticAxisRepository analyticAxisRepo;

  @Inject
  public AnalyticMoveLineQueryServiceImpl(
      AnalyticMoveLineService analyticMoveLineService,
      AppBaseService appBaseService,
      AnalyticMoveLineQueryRepository analyticMoveLineQueryRepository,
      AnalyticAxisRepository analyticAxisRepo) {
    this.analyticMoveLineService = analyticMoveLineService;
    this.appBaseService = appBaseService;
    this.analyticMoveLineQueryRepository = analyticMoveLineQueryRepository;
    this.analyticAxisRepo = analyticAxisRepo;
  }

  @Override
  public String getAnalyticMoveLineQuery(AnalyticMoveLineQuery analyticMoveLineQuery) {
    analyticMoveLineQuery = analyticMoveLineQueryRepository.find(analyticMoveLineQuery.getId());

    String query =
        "self.moveLine IS NOT NULL AND self.subTypeSelect = "
            + AnalyticMoveLineRepository.SUB_TYPE_ORIGINAL
            + " AND ";

    String idList = this.filterReversedLines();

    if (!Strings.isNullOrEmpty(idList)) {
      query += "self.id not in (" + idList + ") AND ";
    }

    query += "self.moveLine.move.company.id = " + analyticMoveLineQuery.getCompany().getId();

    if (appBaseService.getAppBase().getEnableTradingNamesManagement()
        && ObjectUtils.notEmpty(analyticMoveLineQuery.getTradingName())) {
      query +=
          " AND self.moveLine.move.tradingName.id = "
              + analyticMoveLineQuery.getTradingName().getId();
    }

    query +=
        String.format(" AND self.date >= '%s'", analyticMoveLineQuery.getFromDate().toString());
    query += String.format(" AND self.date <= '%s'", analyticMoveLineQuery.getToDate().toString());

    query = this.getStatusQuery(analyticMoveLineQuery, query);

    List<AnalyticMoveLineQueryParameter> searchAnalyticMoveLineQueryParameterList =
        analyticMoveLineQuery.getSearchAnalyticMoveLineQueryParameterList().stream()
            .filter(l -> ObjectUtils.notEmpty(l.getAnalyticAccountSet()))
            .collect(Collectors.toList());

    if (analyticMoveLineQuery.getSearchOperatorSelect()
        == AnalyticMoveLineQueryRepository.SEARCH_OPERATOR_AND) {
      return getAndQuery(query, searchAnalyticMoveLineQueryParameterList);
    }

    return getOrQuery(query, searchAnalyticMoveLineQueryParameterList);
  }

  protected String filterReversedLines() {
    return Beans.get(AnalyticMoveLineRepository.class).all().fetch().stream()
        .map(l -> l.getOriginAnalyticMoveLine())
        .filter(Objects::nonNull)
        .map(aml -> String.valueOf(aml.getId()))
        .collect(Collectors.joining(","));
  }

  protected String getOrQuery(
      String query, List<AnalyticMoveLineQueryParameter> searchAnalyticMoveLineQueryParameterList) {
    if (searchAnalyticMoveLineQueryParameterList.size() > 0) {
      query += " AND (";
    }

    List<String> paramFilter = new ArrayList<>();
    for (AnalyticMoveLineQueryParameter parameter : searchAnalyticMoveLineQueryParameterList) {
      String q = " (self.analyticAxis.id = " + parameter.getAnalyticAxis().getId();
      q +=
          " AND self.analyticAccount.id IN ("
              + parameter.getAnalyticAccountSet().stream()
                  .map(AnalyticAccount::getId)
                  .map(String::valueOf)
                  .collect(Collectors.joining(","))
              + "))";

      paramFilter.add(q);
    }
    query += paramFilter.stream().collect(Collectors.joining(" OR ")) + ")";
    return query;
  }

  protected String getAndQuery(
      String query, List<AnalyticMoveLineQueryParameter> searchAnalyticMoveLineQueryParameterList) {
    Map<MoveLine, List<AnalyticMoveLine>> analyticMoveLineList =
        Beans.get(AnalyticMoveLineRepository.class).all().filter("self.moveLine is not null")
            .fetch().stream()
            .collect(Collectors.groupingBy(AnalyticMoveLine::getMoveLine, Collectors.toList()));

    Map<AnalyticAxis, Set<AnalyticAccount>> paramMap =
        searchAnalyticMoveLineQueryParameterList.stream()
            .collect(
                Collectors.toMap(
                    AnalyticMoveLineQueryParameter::getAnalyticAxis,
                    AnalyticMoveLineQueryParameter::getAnalyticAccountSet));

    List<Long> filteredList = new ArrayList<>();
    filteredList.add(0l);

    for (Map.Entry<MoveLine, List<AnalyticMoveLine>> entry : analyticMoveLineList.entrySet()) {
      Map<AnalyticAxis, List<AnalyticMoveLine>> val =
          entry.getValue().stream()
              .collect(Collectors.groupingBy(AnalyticMoveLine::getAnalyticAxis));
      Boolean allCondition = true;
      for (Map.Entry<AnalyticAxis, List<AnalyticMoveLine>> entry2 : val.entrySet()) {

        Set<AnalyticAccount> analyticAccountSet = paramMap.get(entry2.getKey());

        if (analyticAccountSet == null) {
          continue;
        }

        if (entry2.getValue().stream()
            .noneMatch(a -> analyticAccountSet.contains(a.getAnalyticAccount()))) {
          allCondition = false;
          break;
        }
      }
      if (allCondition) {
        filteredList.add(entry.getKey().getId());
      }
    }

    return query
        + " AND "
        + String.format(
            "self.moveLine.id in (%s)",
            filteredList.stream().map(String::valueOf).collect(Collectors.joining(",")));
  }

  protected String getStatusQuery(AnalyticMoveLineQuery analyticMoveLineQuery, String query) {
    query += " AND self.moveLine.move.statusSelect = ";

    Integer specificOriginSelect = analyticMoveLineQuery.getSpecificOriginSelect();
    int statusAccounted = MoveRepository.STATUS_ACCOUNTED;
    int statusDaybook = MoveRepository.STATUS_DAYBOOK;

    switch (specificOriginSelect) {
      case AnalyticMoveLineQueryRepository.SPECIFIC_ORIGIN_ACCOUNTED_MOVES:
        query += statusAccounted + " AND self.moveLine.move.invoice IS null";
        break;
      case AnalyticMoveLineQueryRepository.SPECIFIC_ORIGIN_INVOICE_ACCOUNTED_MOVES:
        query += statusAccounted + " AND self.moveLine.move.invoice IS NOT null";
        break;
      case AnalyticMoveLineQueryRepository.SPECIFIC_ORIGIN_INVOICE_AND_ACCOUNTED_MOVES:
        query += statusAccounted;
        break;
      case AnalyticMoveLineQueryRepository.SPECIFIC_ORIGIN_DAYBOOK_MOVES:
        query += statusDaybook + " AND self.moveLine.move.invoice IS null";
        break;
      case AnalyticMoveLineQueryRepository.SPECIFIC_ORIGIN_DAYBOOK_INVOICE_MOVES:
        query += statusDaybook + " AND self.moveLine.move.invoice IS NOT null";
        break;
      case AnalyticMoveLineQueryRepository.SPECIFIC_ORIGIN_DAYBOOK_MOVES_AND_INVOICE_MOVES:
        query += statusDaybook;
        break;
      case AnalyticMoveLineQueryRepository.SPECIFIC_ORIGIN_SIMULATED_MOVES:
        query += MoveRepository.STATUS_SIMULATED;
        break;
      default:
        break;
    }
    return query;
  }

  @Override
  public Set<AnalyticMoveLine> analyticMoveLineReverses(
      AnalyticMoveLineQuery analyticMoveLineQuery, List<AnalyticMoveLine> analyticMoveLines) {

    Map<AnalyticAxis, AnalyticAccount> reverseRules = getReverseRules(analyticMoveLineQuery);

    Set<AnalyticMoveLine> reverseAnalyticMoveLines = new HashSet<AnalyticMoveLine>();
    for (AnalyticAxis analyticAxis : reverseRules.keySet()) {
      AnalyticAccount analyticAccount = reverseRules.get(analyticAxis);
      List<AnalyticMoveLine> analyticMoveLinesToReverse =
          analyticMoveLines.stream()
              .filter(
                  analyticMoveLine ->
                      Objects.equals(analyticMoveLine.getAnalyticAxis(), analyticAxis))
              .collect(Collectors.toList());

      reverseAnalyticMoveLines.addAll(
          analyticMoveLineReverses(analyticAccount, analyticMoveLinesToReverse));
    }

    return reverseAnalyticMoveLines;
  }

  @Transactional
  protected Set<AnalyticMoveLine> analyticMoveLineReverses(
      AnalyticAccount analyticAccount, List<AnalyticMoveLine> analyticMoveLines) {

    return analyticMoveLines.stream()
        .map(
            analyticMoveLine ->
                analyticMoveLineService.reverseAndPersist(analyticMoveLine, analyticAccount))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<AnalyticMoveLine> createAnalyticaMoveLines(
      AnalyticMoveLineQuery analyticMoveLineQuery, List<AnalyticMoveLine> analyticMoveLines) {

    Map<AnalyticAxis, AnalyticAccount> reverseRules = getReverseRules(analyticMoveLineQuery);

    Set<AnalyticMoveLine> newAnalyticaMoveLines = new HashSet<AnalyticMoveLine>();
    for (AnalyticAxis analyticAxis : reverseRules.keySet()) {
      AnalyticAccount analyticAccount = reverseRules.get(analyticAxis);
      List<AnalyticMoveLine> analyticMoveLinesToCreate =
          analyticMoveLines.stream()
              .filter(
                  analyticMoveLine ->
                      Objects.equals(analyticMoveLine.getAnalyticAxis(), analyticAxis))
              .collect(Collectors.toList());

      newAnalyticaMoveLines.addAll(
          createAnalyticMoveLine(analyticAccount, analyticMoveLinesToCreate));
    }

    return newAnalyticaMoveLines;
  }

  @Transactional
  protected Set<AnalyticMoveLine> createAnalyticMoveLine(
      AnalyticAccount analyticAccount, List<AnalyticMoveLine> analyticMoveLines) {

    return analyticMoveLines.stream()
        .map(
            analyticMoveLine ->
                analyticMoveLineService.generateAnalyticMoveLine(analyticMoveLine, analyticAccount))
        .collect(Collectors.toSet());
  }

  @Override
  public Map<AnalyticAxis, AnalyticAccount> getReverseRules(
      AnalyticMoveLineQuery analyticMoveLineQuery) {
    List<AnalyticMoveLineQueryParameter> reverseAnalyticMoveLineQueryParameterList =
        analyticMoveLineQuery.getReverseAnalyticMoveLineQueryParameterList().stream()
            .filter(l -> ObjectUtils.notEmpty(l.getAnalyticAccount()))
            .collect(Collectors.toList());

    return reverseAnalyticMoveLineQueryParameterList.stream()
        .collect(
            Collectors.toMap(
                AnalyticMoveLineQueryParameter::getAnalyticAxis,
                AnalyticMoveLineQueryParameter::getAnalyticAccount));
  }

  @Override
  public List<AnalyticAxis> getAvailableAnalyticAxes(
      AnalyticMoveLineQuery analyticMoveLineQuery, boolean reverse) {
    String alreadyPresentSearchAnalyticAxesIds =
        this.getAlreadyPresentAnalyticAxesIds(
            analyticMoveLineQuery.getSearchAnalyticMoveLineQueryParameterList());
    String alreadyPresentReverseAnalyticAxesIds =
        reverse
            ? this.getAlreadyPresentAnalyticAxesIds(
                analyticMoveLineQuery.getReverseAnalyticMoveLineQueryParameterList())
            : "0";

    return analyticAxisRepo
        .all()
        .filter(
            String.format(
                "self.company = :company AND self.id %s IN (:alreadyPresentSearchAnalyticAxes) AND self.id NOT IN (:alreadyPresentReverseAnalyticAxes)",
                reverse ? "" : "NOT"))
        .bind("company", analyticMoveLineQuery.getCompany())
        .bind("alreadyPresentSearchAnalyticAxes", alreadyPresentSearchAnalyticAxesIds)
        .bind("alreadyPresentReverseAnalyticAxes", alreadyPresentReverseAnalyticAxesIds)
        .fetch();
  }

  protected String getAlreadyPresentAnalyticAxesIds(
      List<AnalyticMoveLineQueryParameter> analyticMoveLineQueryParameterList) {
    String alreadyPresentAnalyticAxesIds =
        analyticMoveLineQueryParameterList.stream()
            .map(AnalyticMoveLineQueryParameter::getAnalyticAxis)
            .filter(Objects::nonNull)
            .map(AnalyticAxis::getId)
            .map(Objects::toString)
            .collect(Collectors.joining(","));

    return alreadyPresentAnalyticAxesIds.isEmpty() ? "0" : alreadyPresentAnalyticAxesIds;
  }
}
