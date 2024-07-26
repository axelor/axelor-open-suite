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
package com.axelor.apps.intervention.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.InterventionQuestion;
import com.axelor.apps.intervention.db.InterventionRange;
import com.axelor.apps.intervention.db.repo.InterventionQuestionRepository;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class InterventionQuestionServiceImpl implements InterventionQuestionService {
  protected final AppBaseService appBaseService;
  protected final InterventionRangeService interventionRangeService;
  protected final InterventionRepository interventionRepository;
  protected final AddressService addressService;

  @Inject
  public InterventionQuestionServiceImpl(
      AppBaseService appBaseService,
      InterventionRangeService interventionRangeService,
      InterventionRepository interventionRepository,
      AddressService addressService) {
    this.appBaseService = appBaseService;
    this.interventionRangeService = interventionRangeService;
    this.interventionRepository = interventionRepository;
    this.addressService = addressService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void deleteSurvey(Intervention intervention) {
    List<InterventionRange> interventionRangeList = intervention.getInterventionRangeList();
    interventionRangeList.clear();
    interventionRepository.save(intervention);
  }

  @Override
  public void advancedMonitoringAnswer(InterventionQuestion interventionQuestion) {
    switch (interventionQuestion.getAdvancedMonitoringAnswer()) {
      case InterventionQuestionRepository.HOME:
        fillAdvancedMonitoringHome(interventionQuestion);
        break;
      case InterventionQuestionRepository.OFFICE:
        fillAdvancedMonitoringOffice(interventionQuestion);
        break;
      case InterventionQuestionRepository.PREVIOUS_INTERVENTION:
        fillAdvancedMonitoringPrevInter(interventionQuestion);
        break;
      case InterventionQuestionRepository.NEXT_INTERVENTION:
        fillAdvancedMonitoringNextInter(interventionQuestion);
        break;
      default:
        break;
    }
  }

  protected void fillAdvancedMonitoringHome(InterventionQuestion interventionQuestion) {
    Optional.ofNullable(AuthUtils.getUser())
        .map(User::getPartner)
        .map(Partner::getMainAddress)
        .ifPresent(
            it ->
                interventionQuestion.setAdvancedMonitoringAddress(
                    addressService.computeAddressStr(it)));
  }

  protected void fillAdvancedMonitoringOffice(InterventionQuestion interventionQuestion) {
    if (appBaseService.getAppBase().getEnableTradingNamesManagement()) {
      Optional.ofNullable(AuthUtils.getUser()).map(User::getTradingName)
          .map(TradingName::getCompanySet).stream()
          .flatMap(Collection::stream)
          .findFirst()
          .map(Company::getAddress)
          .ifPresent(
              it ->
                  interventionQuestion.setAdvancedMonitoringAddress(
                      addressService.computeAddressStr(it)));
    } else {
      Optional.ofNullable(AuthUtils.getUser())
          .map(User::getActiveCompany)
          .map(Company::getAddress)
          .ifPresent(
              it ->
                  interventionQuestion.setAdvancedMonitoringAddress(
                      addressService.computeAddressStr(it)));
    }
  }

  protected void fillAdvancedMonitoringPrevInter(InterventionQuestion interventionQuestion) {
    User user = AuthUtils.getUser();
    Intervention intervention =
        Optional.ofNullable(interventionQuestion)
            .map(InterventionQuestion::getInterventionRange)
            .map(InterventionRange::getIntervention)
            .orElse(null);
    if (interventionQuestion == null
        || intervention == null
        || user == null
        || intervention.getPlanifStartDateTime() == null) {
      return;
    }
    LocalDateTime dateTime = intervention.getPlanifStartDateTime();

    Intervention previous =
        getInterventionsOfTheDay(user, dateTime).stream()
            .filter(it -> it.getPlanifStartDateTime().isBefore(dateTime))
            .max(Comparator.comparing(it -> it.getPlanifStartDateTime()))
            .orElse(null);
    interventionQuestion.setAdvancedMonitoringAddress(
        previous != null
            ? addressService.computeAddressStr(previous.getAddress())
            : I18n.get("Invalid data"));
  }

  protected void fillAdvancedMonitoringNextInter(InterventionQuestion interventionQuestion) {
    User user = AuthUtils.getUser();
    Intervention intervention =
        Optional.ofNullable(interventionQuestion)
            .map(InterventionQuestion::getInterventionRange)
            .map(InterventionRange::getIntervention)
            .orElse(null);
    if (interventionQuestion == null
        || intervention == null
        || user == null
        || intervention.getPlanifStartDateTime() == null) {
      return;
    }
    LocalDateTime dateTime = intervention.getPlanifStartDateTime();

    Intervention next =
        getInterventionsOfTheDay(user, dateTime).stream()
            .filter(it -> it.getPlanifStartDateTime().isAfter(dateTime))
            .min(Comparator.comparing(Intervention::getPlanifStartDateTime))
            .orElse(null);
    interventionQuestion.setAdvancedMonitoringAddress(
        next != null
            ? addressService.computeAddressStr(next.getAddress())
            : I18n.get("Invalid data"));
  }

  protected List<Intervention> getInterventionsOfTheDay(User user, LocalDateTime dateTime) {
    CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
    CriteriaQuery<Intervention> cr = cb.createQuery(Intervention.class);
    Root<Intervention> root = cr.from(Intervention.class);
    cr.select(root);

    Predicate sameDay =
        cb.and(
            cb.isNotNull(root.get("planifStartDateTime")),
            cb.greaterThanOrEqualTo(
                root.get("planifStartDateTime"), dateTime.toLocalDate().atStartOfDay()),
            cb.lessThan(
                root.get("planifStartDateTime"),
                dateTime.toLocalDate().plusDays(1).atStartOfDay()));

    Predicate belongToTech = cb.equal(root.get("assignedTo"), user);

    cr.where(cb.and(sameDay, belongToTech));
    List<Intervention> interventions = JPA.em().createQuery(cr).getResultList();
    return interventions == null ? new ArrayList<>() : interventions;
  }
}
