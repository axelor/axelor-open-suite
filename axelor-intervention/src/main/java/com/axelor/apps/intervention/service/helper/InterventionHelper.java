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
package com.axelor.apps.intervention.service.helper;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.CustomerRequest;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.InterventionCategory;
import com.axelor.apps.intervention.db.InterventionQuestion;
import com.axelor.apps.intervention.db.InterventionRange;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import java.util.HashSet;
import java.util.Optional;
import java.util.StringJoiner;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class InterventionHelper {
  private InterventionHelper() {}

  public static String computeInterventionTypeDomain(Intervention intervention) {
    StringJoiner domain = new StringJoiner(" OR ");

    domain.add(
        String.format(
            "self.interventionCategory.id = %s",
            Optional.ofNullable(intervention)
                .map(Intervention::getInterventionCategory)
                .map(InterventionCategory::getId)
                .orElse(null)));
    domain.add(
        String.format(
            "%s in self.exclusiveInterventionSet.id",
            Optional.ofNullable(intervention).map(Intervention::getId).orElse(null)));

    return domain.toString();
  }

  public static void computeTag(Intervention intervention) {
    CriteriaBuilder cb = JPA.em().getCriteriaBuilder();

    CriteriaQuery<Long> countFollowing = cb.createQuery(Long.class);
    Root<InterventionQuestion> rootFollowing = countFollowing.from(InterventionQuestion.class);
    countFollowing.select(cb.count(rootFollowing));
    Predicate questionHasFollowing =
        cb.equal(rootFollowing.get("listAnswer").get("following"), Boolean.TRUE);
    countFollowing.where(
        belongToInterventionFromInterventionQuestion(cb, rootFollowing, intervention.getId()),
        questionHasFollowing);
    if (JPA.em().createQuery(countFollowing).getSingleResult() > 0) {
      intervention.setFollowing(Boolean.TRUE);
    } else {
      intervention.setFollowing(Boolean.FALSE);
    }

    CriteriaQuery<Long> countNonConforming = cb.createQuery(Long.class);
    Root<InterventionQuestion> rootNonConforming =
        countNonConforming.from(InterventionQuestion.class);
    countNonConforming.select(cb.count(rootNonConforming));
    Predicate questionHasNonConforming =
        cb.equal(rootFollowing.get("listAnswer").get("nonConforming"), Boolean.TRUE);
    countNonConforming.where(
        belongToInterventionFromInterventionQuestion(cb, rootFollowing, intervention.getId()),
        questionHasNonConforming);
    intervention.setNonConforming(
        JPA.em().createQuery(countNonConforming).getSingleResult().intValue());
  }

  public static Predicate belongToInterventionFromInterventionQuestion(
      CriteriaBuilder cb, Root<InterventionQuestion> root, Long interventionId) {
    return cb.equal(root.get("interventionRange").get("intervention").get("id"), interventionId);
  }

  public static Long computeUnderContractEquipmentsNbr(Intervention intervention) {
    try {
      CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
      CriteriaQuery<Long> cr = cb.createQuery(Long.class);
      Root<Equipment> root = cr.from(Equipment.class);
      cr.select(cb.count(root));

      Predicate belongToIntervention = cb.equal(root.get("contract"), intervention.getContract());
      Predicate faulty =
          cb.equal(root.get("indicatorSelect"), EquipmentRepository.INTERVENTION_INDICATOR_UC_OP);

      cr.where(cb.and(belongToIntervention, faulty));
      return JPA.em().createQuery(cr).getSingleResult();
    } catch (Exception e) {
      TraceBackService.trace(e);
      return null;
    }
  }

  public static void fillDefaultFromParent(
      Intervention intervention, Intervention interventionParent) {

    fillDefaultFromParentMainPanel(intervention, interventionParent);

    fillDefaultFromParentInterventionPanel(intervention, interventionParent);

    intervention.setEquipmentSet(new HashSet<>(interventionParent.getEquipmentSet()));
  }

  public static void fillDefaultFromParentMainPanel(
      Intervention intervention, Intervention interventionParent) {
    intervention.setCompany(interventionParent.getCompany());
    if (Beans.get(AppBaseService.class).getAppBase().getEnableTradingNamesManagement()) {
      intervention.setTradingName(interventionParent.getTradingName());
    }
    intervention.setDeliveredPartner(interventionParent.getDeliveredPartner());
    intervention.setAddress(interventionParent.getAddress());
    intervention.setInvoicedPartner(interventionParent.getInvoicedPartner());

    intervention.setContact(interventionParent.getContact());

    intervention.setRequestSource(interventionParent.getRequestSource());
    intervention.setRequestSubject(interventionParent.getRequestSubject());
    intervention.setOutsourcing(interventionParent.getOutsourcing());
    intervention.setSupplierPartner(interventionParent.getSupplierPartner());
    intervention.setDescription(interventionParent.getDescription());
    intervention.setUserInCharge(interventionParent.getUserInCharge());
  }

  public static void fillDefaultFromParentInterventionPanel(
      Intervention intervention, Intervention interventionParent) {
    intervention.setInterventionCategory(interventionParent.getInterventionCategory());
    intervention.setInterventionType(interventionParent.getInterventionType());
    intervention.setPlanningPreferenceSelect(interventionParent.getPlanningPreferenceSelect());
    intervention.setPriority(interventionParent.getPriority());
    intervention.setContract(interventionParent.getContract());
    intervention.setCustomerRequest(interventionParent.getCustomerRequest());
    intervention.setRescheduledIntervention(interventionParent.getRescheduledIntervention());
  }

  public static void fillDefault(Intervention intervention) {
    if (AuthUtils.getUser() != null) {
      intervention.setUserInCharge(AuthUtils.getUser());
      intervention.setCompany(AuthUtils.getUser().getActiveCompany());
      if (Beans.get(AppBaseService.class).getAppBase().getEnableTradingNamesManagement()) {
        intervention.setTradingName(AuthUtils.getUser().getTradingName());
      }
    }
  }

  public static <T> Optional<T> findFirstLinkedInterventionField(
      CustomerRequest customerRequest,
      boolean finished,
      Class<T> selectFieldClass,
      String fieldName) {
    try {
      CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
      CriteriaQuery<T> cr = cb.createQuery(selectFieldClass);
      Root<Intervention> root = cr.from(Intervention.class);
      cr.select(root.get(fieldName));
      Predicate belongToClientRequest = cb.equal(root.get("customerRequest"), customerRequest);
      if (finished) {
        Predicate statusFinished =
            cb.equal(root.get("statusSelect"), InterventionRepository.INTER_STATUS_FINISHED);
        cr.where(cb.and(belongToClientRequest, statusFinished));
      } else {
        cr.where(belongToClientRequest);
      }
      cr.orderBy(cb.asc(root.get("id")));
      return Optional.ofNullable(JPA.em().createQuery(cr).setMaxResults(1).getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  public static boolean isSurveyGenerated(Intervention intervention) {
    CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
    CriteriaQuery<Long> cr = cb.createQuery(Long.class);
    Root<InterventionRange> root = cr.from(InterventionRange.class);
    cr.select(cb.count(root));

    Predicate belongToIntervention = cb.equal(root.get("intervention"), intervention);

    cr.where(belongToIntervention);
    Long count = JPA.em().createQuery(cr).getSingleResult();
    return count != null
        && count > 0L
        && !Boolean.TRUE.equals(intervention.getIsSurveyGenerationRunning());
  }

  public static long roundToNextHalfHour(long seconds) {
    long halfHour = 30L * 60L;
    long remainder = seconds % halfHour;
    return remainder == 0 ? seconds : seconds + halfHour - remainder;
  }
}
