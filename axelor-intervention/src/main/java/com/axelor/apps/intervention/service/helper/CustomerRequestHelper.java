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
package com.axelor.apps.intervention.service.helper;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.intervention.db.CustomerRequest;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.InterventionConfig;
import com.axelor.apps.intervention.db.repo.CustomerRequestRepository;
import com.axelor.apps.intervention.service.planning.PlanningDateTimeService;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.service.TemplateMessageService;
import java.time.LocalDateTime;
import java.util.Optional;

public class CustomerRequestHelper {

  private static InterventionConfig getInterventionConfig(CustomerRequest customerRequest) {
    InterventionConfig interventionConfig = null;
    if (customerRequest != null
        && customerRequest.getCompany() != null
        && customerRequest.getCompany().getInterventionConfig() != null) {
      interventionConfig = customerRequest.getCompany().getInterventionConfig();
    }
    return interventionConfig;
  }

  public static Optional<Template> getTemplate(CustomerRequest customerRequest) {
    InterventionConfig interventionConfig = getInterventionConfig(customerRequest);
    if (interventionConfig == null) {
      return Optional.empty();
    }
    if (!Boolean.TRUE.equals(interventionConfig.getCustomerRequestCreationAutomaticMail())
        || interventionConfig.getCustomerRequestCreationMessageTemplate() == null) {
      return Optional.empty();
    }
    return Optional.of(interventionConfig.getCustomerRequestCreationMessageTemplate());
  }

  public static void generateAndSendEmail(Template template, CustomerRequest entity) {
    try {
      TemplateMessageService templateMessageService = Beans.get(TemplateMessageService.class);
      if (!Boolean.TRUE.equals(entity.getEmailSent())) {
        templateMessageService.generateAndSendMessage(entity, template);
        entity.setEmailSent(Boolean.TRUE);
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static CustomerRequest create(Intervention intervention) throws AxelorException {
    CustomerRequest request = new CustomerRequest();
    fillFromIntervention(request, intervention);
    return Beans.get(CustomerRequestRepository.class).save(request);
  }

  public static void fillFromIntervention(CustomerRequest request, Intervention intervention) {
    request.setContract(intervention.getContract());
    request.setDeliveredPartner(intervention.getDeliveredPartner());
    request.setCompany(intervention.getCompany());
    request.setTradingName(intervention.getTradingName());
    request.setDescription(intervention.getDescription());
    request.setRequestSource(intervention.getRequestSource());
    request.setRequestSubject(intervention.getRequestSubject());
    request.setPriority(intervention.getPriority());
    request.setInterventionAddress(intervention.getAddress());
    request.setInterventionCategory(intervention.getInterventionCategory());
    request.setUserInCharge(intervention.getUserInCharge());
    request.setRequestDateTime(LocalDateTime.now());
  }

  public static void computeGt(CustomerRequest customerRequest) {
    PlanningDateTimeService planningDateTimeService = Beans.get(PlanningDateTimeService.class);

    if (customerRequest.getMaxGitDateTime() == null) {
      customerRequest.setMaxGitDateTime(
          computeMaxGitDateTime(customerRequest, planningDateTimeService));
    }
    if (customerRequest.getMaxGrtDateTime() == null) {
      customerRequest.setMaxGrtDateTime(
          computeMaxGrtDateTime(customerRequest, planningDateTimeService));
    }
    if (customerRequest.getRealGit() == null) {
      customerRequest.setRealGit(computeRealGit(customerRequest, planningDateTimeService));
    }
    if (customerRequest.getRealGrt() == null) {
      customerRequest.setRealGrt(computeRealGrt(customerRequest, planningDateTimeService));
    }
  }

  public static LocalDateTime computeMaxGitDateTime(
      CustomerRequest customerRequest, PlanningDateTimeService planningDateTimeService) {
    if (customerRequest.getOnCallPlanning() != null
        && customerRequest.getRequestDateTime() != null
        && customerRequest.getContract() != null
        && customerRequest.getContract().getGuaranteedInterventionTime() != null
        && customerRequest.getContract().getGuaranteedInterventionTime() != 0L) {
      return planningDateTimeService.add(
          customerRequest.getCompany(),
          customerRequest.getOnCallPlanning(),
          customerRequest.getRequestDateTime(),
          customerRequest.getContract().getGuaranteedInterventionTime());
    }
    return null;
  }

  public static LocalDateTime computeMaxGrtDateTime(
      CustomerRequest customerRequest, PlanningDateTimeService planningDateTimeService) {
    if (customerRequest.getOnCallPlanning() != null
        && customerRequest.getRequestDateTime() != null
        && customerRequest.getContract() != null
        && customerRequest.getContract().getGuaranteedRecoveryTime() != null
        && customerRequest.getContract().getGuaranteedRecoveryTime() != 0L) {
      return planningDateTimeService.add(
          customerRequest.getCompany(),
          customerRequest.getOnCallPlanning(),
          customerRequest.getRequestDateTime(),
          customerRequest.getContract().getGuaranteedRecoveryTime());
    }
    return null;
  }

  public static Long computeRealGit(
      CustomerRequest customerRequest, PlanningDateTimeService planningDateTimeService) {
    if (customerRequest.getOnCallPlanning() != null
        && customerRequest.getRequestDateTime() != null
        && customerRequest.getContract() != null) {
      return InterventionHelper.findFirstLinkedInterventionField(
              customerRequest, false, LocalDateTime.class, "startDateTime")
          .map(
              it ->
                  planningDateTimeService.diff(
                      customerRequest.getCompany(),
                      customerRequest.getOnCallPlanning(),
                      customerRequest.getRequestDateTime(),
                      it))
          .orElse(null);
    }
    return null;
  }

  public static Long computeRealGrt(
      CustomerRequest customerRequest, PlanningDateTimeService planningDateTimeService) {
    if (customerRequest.getOnCallPlanning() != null
        && customerRequest.getRequestDateTime() != null
        && customerRequest.getContract() != null) {
      return InterventionHelper.findFirstLinkedInterventionField(
              customerRequest, true, LocalDateTime.class, "endDateTime")
          .map(
              it ->
                  planningDateTimeService.diff(
                      customerRequest.getCompany(),
                      customerRequest.getOnCallPlanning(),
                      customerRequest.getRequestDateTime(),
                      it))
          .orElse(null);
    }
    return null;
  }
}
