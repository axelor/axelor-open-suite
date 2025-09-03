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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.AnalyticMoveLineQuery;
import com.axelor.apps.account.db.AnalyticMoveLineQueryParameter;
import com.axelor.apps.account.db.repo.AnalyticAxisRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineQueryRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.TypedQuery;

public class AnalyticMoveLineQueryServiceImpl implements AnalyticMoveLineQueryService {

  protected AnalyticMoveLineService analyticMoveLineService;
  protected AppBaseService appBaseService;
  protected AnalyticMoveLineQueryRepository analyticMoveLineQueryRepository;
  protected AnalyticAxisRepository analyticAxisRepo;
  protected AnalyticMoveLineRepository analyticMoveLineRepository;
  protected AnalyticMoveLineQueryPercentageService analyticMoveLineQueryPercentageService;

  @Inject
  public AnalyticMoveLineQueryServiceImpl(
      AnalyticMoveLineService analyticMoveLineService,
      AppBaseService appBaseService,
      AnalyticMoveLineQueryRepository analyticMoveLineQueryRepository,
      AnalyticAxisRepository analyticAxisRepo,
      AnalyticMoveLineRepository analyticMoveLineRepository,
      AnalyticMoveLineQueryPercentageService analyticMoveLineQueryPercentageService) {
    this.analyticMoveLineService = analyticMoveLineService;
    this.appBaseService = appBaseService;
    this.analyticMoveLineQueryRepository = analyticMoveLineQueryRepository;
    this.analyticAxisRepo = analyticAxisRepo;
    this.analyticMoveLineRepository = analyticMoveLineRepository;
    this.analyticMoveLineQueryPercentageService = analyticMoveLineQueryPercentageService;
  }

  @Override
  public String getAnalyticMoveLineQuery(AnalyticMoveLineQuery analyticMoveLineQuery) {
    analyticMoveLineQuery = analyticMoveLineQueryRepository.find(analyticMoveLineQuery.getId());

    String query =
        "self.moveLine IS NOT NULL AND self.subTypeSelect = "
            + AnalyticMoveLineRepository.SUB_TYPE_ORIGINAL
            + " AND ";

    query +=
        "NOT EXISTS (SELECT 1 FROM AnalyticMoveLine l WHERE l.originAnalyticMoveLine = self.id) AND ";
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

    if (ObjectUtils.notEmpty(searchAnalyticMoveLineQueryParameterList)) {
      query += " AND (" + getOrQuery(searchAnalyticMoveLineQueryParameterList) + ")";
    }
    return query;
  }

  protected String getOrQuery(
      List<AnalyticMoveLineQueryParameter> searchAnalyticMoveLineQueryParameterList) {
    return searchAnalyticMoveLineQueryParameterList.stream()
        .map(
            parameter ->
                String.format(
                    " (self.analyticAxis.id = %d AND self.analyticAccount.id IN (%s))",
                    parameter.getAnalyticAxis().getId(),
                    parameter.getAnalyticAccountSet().stream()
                        .map(AnalyticAccount::getId)
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))))
        .collect(Collectors.joining(" OR "));
  }

  protected String getAndQuery(
      String query, List<AnalyticMoveLineQueryParameter> searchAnalyticMoveLineQueryParameterList) {
    Map<AnalyticAxis, Set<AnalyticAccount>> paramMap =
        searchAnalyticMoveLineQueryParameterList.stream()
            .filter(queryParam -> ObjectUtils.notEmpty(queryParam.getAnalyticAccountSet()))
            .collect(
                Collectors.toMap(
                    AnalyticMoveLineQueryParameter::getAnalyticAxis,
                    AnalyticMoveLineQueryParameter::getAnalyticAccountSet));

    StringBuilder queryString =
        new StringBuilder(
            "SELECT DISTINCT aml.moveLine.id FROM AnalyticMoveLine aml WHERE aml.moveLine IS NOT NULL");

    for (Map.Entry<AnalyticAxis, Set<AnalyticAccount>> entry : paramMap.entrySet()) {
      queryString
          .append(
              " AND EXISTS (SELECT 1 FROM AnalyticMoveLine aml_sub WHERE aml_sub.moveLine = aml.moveLine")
          .append(" AND aml_sub.analyticAxis = ")
          .append(entry.getKey().getId())
          .append(" AND aml_sub.analyticAccount IN ")
          .append("(" + StringHelper.getIdListString(entry.getValue()) + "))");
    }
    TypedQuery<Long> amlQuery = JPA.em().createQuery(queryString.toString(), Long.class);
    List<Long> moveLineIds = amlQuery.getResultList();
    String filteredIds = moveLineIds.stream().map(String::valueOf).collect(Collectors.joining(","));

    return String.format(
        "%s AND self.moveLine.id in (%s)",
        query, StringUtils.isEmpty(filteredIds) ? "0" : filteredIds);
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
    Set<AnalyticMoveLine> reverseAnalyticMoveLines = new HashSet<AnalyticMoveLine>();
    List<AnalyticMoveLineQueryParameter> reverseRuleList = getReverseRules(analyticMoveLineQuery);

    if (ObjectUtils.isEmpty(reverseRuleList)) {
      return reverseAnalyticMoveLines;
    }

    List<AnalyticAxis> analyticAxisToReverseList =
        reverseRuleList.stream()
            .map(AnalyticMoveLineQueryParameter::getAnalyticAxis)
            .distinct()
            .collect(Collectors.toList());

    for (AnalyticAxis axis : analyticAxisToReverseList) {
      List<AnalyticMoveLine> analyticMoveLinesToReverse =
          analyticMoveLines.stream()
              .filter(analyticMoveLine -> Objects.equals(analyticMoveLine.getAnalyticAxis(), axis))
              .collect(Collectors.toList());

      reverseAnalyticMoveLines.addAll(analyticMoveLineReverses(analyticMoveLinesToReverse));
    }

    return reverseAnalyticMoveLines;
  }

  @Transactional
  protected Set<AnalyticMoveLine> analyticMoveLineReverses(
      List<AnalyticMoveLine> analyticMoveLines) {

    return analyticMoveLines.stream()
        .map(analyticMoveLine -> analyticMoveLineService.reverseAndPersist(analyticMoveLine))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<AnalyticMoveLine> createAnalyticMoveLines(
      AnalyticMoveLineQuery analyticMoveLineQuery, List<AnalyticMoveLine> analyticMoveLines) {

    List<AnalyticMoveLineQueryParameter> reverseRules = getReverseRules(analyticMoveLineQuery);

    Set<AnalyticMoveLine> newAnalyticaMoveLines = new HashSet<AnalyticMoveLine>();
    for (AnalyticMoveLineQueryParameter parameter : reverseRules) {
      AnalyticAccount analyticAccount = parameter.getAnalyticAccount();
      List<AnalyticMoveLine> analyticMoveLinesToCreate =
          analyticMoveLines.stream()
              .filter(
                  analyticMoveLine ->
                      Objects.equals(
                          analyticMoveLine.getAnalyticAxis(), parameter.getAnalyticAxis()))
              .collect(Collectors.toList());

      newAnalyticaMoveLines.addAll(
          createAnalyticMoveLine(
              analyticAccount, analyticMoveLinesToCreate, parameter.getPercentage()));
    }

    return newAnalyticaMoveLines;
  }

  @Transactional
  protected Set<AnalyticMoveLine> createAnalyticMoveLine(
      AnalyticAccount analyticAccount,
      List<AnalyticMoveLine> analyticMoveLines,
      BigDecimal percentage) {

    return analyticMoveLines.stream()
        .map(
            analyticMoveLine ->
                analyticMoveLineService.generateAnalyticMoveLine(
                    analyticMoveLine, analyticAccount, percentage))
        .collect(Collectors.toSet());
  }

  @Override
  public List<AnalyticMoveLineQueryParameter> getReverseRules(
      AnalyticMoveLineQuery analyticMoveLineQuery) {
    return analyticMoveLineQuery.getReverseAnalyticMoveLineQueryParameterList().stream()
        .filter(
            l ->
                ObjectUtils.notEmpty(l.getAnalyticAxis())
                    && ObjectUtils.notEmpty(l.getAnalyticAccount()))
        .collect(Collectors.toList());
  }

  @Override
  public List<AnalyticAxis> getAvailableAnalyticAxes(
      AnalyticMoveLineQuery analyticMoveLineQuery, boolean isReverseQuery) {
    List<Long> alreadyPresentSearchAnalyticAxesIds =
        this.getAlreadyPresentAnalyticAxesIds(
            analyticMoveLineQuery.getSearchAnalyticMoveLineQueryParameterList());
    List<Long> alreadyPresentReverseAnalyticAxesIds = new ArrayList<>();
    if (isReverseQuery) {
      alreadyPresentReverseAnalyticAxesIds.addAll(
          this.getAlreadyPresentAnalyticAxesIds(
              analyticMoveLineQuery.getReverseAnalyticMoveLineQueryParameterList()));
    } else {
      alreadyPresentReverseAnalyticAxesIds.add(0L);
    }

    return analyticAxisRepo
        .all()
        .filter(
            String.format(
                "(self.company = :company OR self.company IS NULL) AND self.id %s IN :alreadyPresentSearchAnalyticAxes AND self.id NOT IN :alreadyPresentReverseAnalyticAxes",
                isReverseQuery ? "" : "NOT"))
        .bind("company", analyticMoveLineQuery.getCompany())
        .bind("alreadyPresentSearchAnalyticAxes", alreadyPresentSearchAnalyticAxesIds)
        .bind("alreadyPresentReverseAnalyticAxes", alreadyPresentReverseAnalyticAxesIds)
        .fetch();
  }

  protected List<Long> getAlreadyPresentAnalyticAxesIds(
      List<AnalyticMoveLineQueryParameter> analyticMoveLineQueryParameterList) {
    List<Long> alreadyPresentAnalyticAxesIds = new ArrayList<>();
    alreadyPresentAnalyticAxesIds.add(0L);
    if (ObjectUtils.isEmpty(analyticMoveLineQueryParameterList)) {
      return alreadyPresentAnalyticAxesIds;
    }

    Map<AnalyticAxis, BigDecimal> percentageAxisMap =
        analyticMoveLineQueryPercentageService.buildPercentageAxisMap(
            analyticMoveLineQueryParameterList);

    alreadyPresentAnalyticAxesIds.addAll(
        percentageAxisMap.entrySet().stream()
            .filter(it -> it.getValue().compareTo(new BigDecimal(100)) >= 0)
            .map(Map.Entry::getKey)
            .map(AnalyticAxis::getId)
            .collect(Collectors.toList()));
    return alreadyPresentAnalyticAxesIds;
  }
}
