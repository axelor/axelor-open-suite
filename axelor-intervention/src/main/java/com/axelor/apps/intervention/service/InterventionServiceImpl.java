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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.intervention.db.CustomerRequest;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.InterventionConfig;
import com.axelor.apps.intervention.db.InterventionQuestion;
import com.axelor.apps.intervention.db.InterventionType;
import com.axelor.apps.intervention.db.repo.CustomerRequestRepository;
import com.axelor.apps.intervention.db.repo.InterventionQuestionRepository;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.apps.intervention.exception.InterventionExceptionMessage;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.apps.intervention.service.helper.InterventionHelper;
import com.axelor.apps.intervention.service.helper.InterventionQuestionHelper;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Template;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterventionServiceImpl implements InterventionService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected final AppInterventionService appInterventionService;
  protected final AppBaseService appBaseService;
  protected final InterventionRepository interventionRepository;
  protected final CustomerRequestRepository customerRequestRepository;
  protected final EquipmentRepository equipmentRepository;
  protected final TemplateMessageService templateMessageService;
  protected final InterventionQuestionService interventionQuestionService;
  protected final InterventionQuestionRepository interventionQuestionRepository;
  protected final SaleOrderRepository saleOrderRepository;
  protected final InterventionSurveyGenerator interventionSurveyGenerator;
  protected final SaleOrderCreateService saleOrderCreateService;
  protected final InterventionPartnerService interventionPartnerService;

  @Inject
  public InterventionServiceImpl(
      AppInterventionService appInterventionService,
      AppBaseService appBaseService,
      InterventionRepository interventionRepository,
      CustomerRequestRepository customerRequestRepository,
      EquipmentRepository equipmentRepository,
      TemplateMessageService templateMessageService,
      InterventionQuestionService interventionQuestionService,
      InterventionQuestionRepository interventionQuestionRepository,
      SaleOrderRepository saleOrderRepository,
      InterventionSurveyGenerator interventionSurveyGenerator,
      SaleOrderCreateService saleOrderCreateService,
      InterventionPartnerService interventionPartnerService) {
    this.appInterventionService = appInterventionService;
    this.appBaseService = appBaseService;
    this.interventionRepository = interventionRepository;
    this.customerRequestRepository = customerRequestRepository;
    this.equipmentRepository = equipmentRepository;
    this.templateMessageService = templateMessageService;
    this.interventionQuestionService = interventionQuestionService;
    this.interventionQuestionRepository = interventionQuestionRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.interventionSurveyGenerator = interventionSurveyGenerator;
    this.saleOrderCreateService = saleOrderCreateService;
    this.interventionPartnerService = interventionPartnerService;
  }

  protected List<String> findZipFileNames(String path) {
    List<String> zipFileNames = new ArrayList<>();
    File directory = new File(path);
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          zipFileNames.addAll(findZipFileNames(file.getAbsolutePath()));
        } else if (file.getName().toLowerCase().endsWith(".zip")) {
          zipFileNames.add(file.getName());
        }
      }
    }
    return zipFileNames;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public Intervention create(CustomerRequest request) throws AxelorException {
    Intervention intervention = new Intervention();
    fillFromContract(intervention, request.getContract());
    fillFromRequest(intervention, request);
    intervention = interventionRepository.save(intervention);
    Optional<Template> optionalTemplate =
        getTemplate(intervention, null, intervention.getStatusSelect());
    if (optionalTemplate.isPresent()) {
      generateAndSendMessage(intervention, optionalTemplate.get());
    }
    return intervention;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public Intervention create(Contract contract) throws AxelorException {
    Intervention intervention = new Intervention();
    fillFromContract(intervention, contract);
    intervention = interventionRepository.save(intervention);
    Optional<Template> optionalTemplate =
        getTemplate(intervention, null, intervention.getStatusSelect());
    if (optionalTemplate.isPresent()) {
      generateAndSendMessage(intervention, optionalTemplate.get());
    }
    return intervention;
  }

  @Override
  public void fillFromContract(Intervention intervention, Contract contract) {
    if (contract != null) {
      intervention.setContract(contract);
      intervention.setEstimatedDate(contract.getNextAnnualVisitDate());
      fill(
          intervention,
          contract.getInterventionType(),
          contract.getInvoicedPartner(),
          contract.getPlanningPreferenceSelect(),
          contract.getPartner(),
          contract.getCompany());
      intervention.setDescription(
          appInterventionService.getAppIntervention().getDefaultInterventionDescription());
      intervention.setRequestSource(
          appInterventionService.getAppIntervention().getDefaultRequestSourceBatchContract());
      intervention.setRequestSubject(
          appInterventionService.getAppIntervention().getDefaultRequestSubjectBatchContract());
      intervention.setInterventionCategory(
          appInterventionService
              .getAppIntervention()
              .getDefaultInterventionCategoryBatchContract());

      if (intervention.getInterventionType() == null) {
        intervention.setInterventionType(
            appInterventionService
                .getAppIntervention()
                .getDefaultTypeForInterventionsFromContracts());
      }
      if (intervention.getEquipmentSet() != null) {
        intervention.getEquipmentSet().clear();
      }
      intervention.setEquipmentSet(
          new HashSet<>(equipmentRepository.findByContract(contract.getId())));
    }
  }

  @Override
  public void fillFromRequest(Intervention intervention, CustomerRequest request) {
    if (request != null) {
      intervention.setCustomerRequest(request);
      intervention.setDeliveredPartner(request.getDeliveredPartner());
      intervention.setCompany(request.getCompany());
      if (intervention.getCompany() == null && AuthUtils.getUser() != null) {
        intervention.setCompany(AuthUtils.getUser().getActiveCompany());
      }
      if (appBaseService.getAppBase().getEnableTradingNamesManagement()) {
        intervention.setTradingName(request.getTradingName());
        if (intervention.getTradingName() == null && AuthUtils.getUser() != null) {
          intervention.setTradingName(AuthUtils.getUser().getTradingName());
        }
      }
      intervention.setDescription(request.getDescription());
      intervention.setRequestSource(request.getRequestSource());
      intervention.setRequestSubject(request.getRequestSubject());
      intervention.setPriority(request.getPriority());
      intervention.setAddress(request.getInterventionAddress());
      intervention.setInterventionCategory(request.getInterventionCategory());
      intervention.setUserInCharge(request.getUserInCharge());
      intervention.setContact(request.getContact());

      if (CollectionUtils.isNotEmpty(request.getEquipmentSet())) {
        for (Equipment equipment : request.getEquipmentSet()) {
          intervention.addEquipmentSetItem(equipment);
        }
      }
    }
  }

  protected Optional<Template> getTemplate(
      Intervention intervention, Integer fromStatus, Integer toStatus) {
    InterventionConfig interventionConfig =
        intervention != null ? intervention.getCompany().getInterventionConfig() : null;
    if (interventionConfig == null) {
      return Optional.empty();
    }

    if (Objects.equals(InterventionRepository.INTER_STATUS_PLANNED, toStatus)) {
      if (!Boolean.TRUE.equals(interventionConfig.getInterventionPlanificationAutomaticMail())
          || interventionConfig.getInterventionPlanificationMessageTemplate() == null) {
        return Optional.empty();
      }
      return Optional.of(interventionConfig.getInterventionPlanificationMessageTemplate());
    }

    if (Arrays.asList(
                InterventionRepository.INTER_STATUS_PLANNED,
                InterventionRepository.INTER_STATUS_CANCELLED)
            .contains(fromStatus)
        && Objects.equals(InterventionRepository.INTER_STATUS_TO_PLAN, toStatus)) {
      if (!Boolean.TRUE.equals(interventionConfig.getInterventionRePlanificationAutomaticMail())
          || interventionConfig.getInterventionRePlanificationMessageTemplate() == null) {
        return Optional.empty();
      }
      return Optional.of(interventionConfig.getInterventionRePlanificationMessageTemplate());
    }

    if (Objects.equals(InterventionRepository.INTER_STATUS_FINISHED, fromStatus)) {
      if (!Boolean.TRUE.equals(interventionConfig.getInterventionValidationAutomaticMail())
          || interventionConfig.getInterventionValidationMessageTemplate() == null) {
        return Optional.empty();
      }
      return Optional.of(interventionConfig.getInterventionValidationMessageTemplate());
    }

    return Optional.empty();
  }

  protected void generateAndSendMessage(Intervention intervention, Template optionalTemplate)
      throws AxelorException {
    try {
      templateMessageService.generateAndSendMessage(intervention, optionalTemplate);
    } catch (Exception e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void start(Intervention intervention, LocalDateTime dateTime) {
    intervention.setStatusSelect(InterventionRepository.INTER_STATUS_STARTED);
    if (intervention.getLastStartDateTime() == null) {
      intervention.setStartDateTime(dateTime);
    }
    intervention.setLastStartDateTime(dateTime);
    interventionRepository.save(intervention);
  }

  @Override
  public void reschedule(Intervention intervention) throws AxelorException {
    Long id = intervention.getId();
    interventionQuestionService.deleteSurvey(intervention);
    rescheduleIntervention(interventionRepository.find(id));
  }

  @Transactional(rollbackOn = Exception.class)
  public void rescheduleIntervention(Intervention intervention) throws AxelorException {
    removePlanification(intervention);
    updateInterventionStatus(intervention);
  }

  protected void removePlanification(Intervention intervention) {
    intervention.setAssignedTo(null);
    intervention.setPlanifStartDateTime(null);
    intervention.setPlanifEndDateTime(null);
  }

  protected void updateInterventionStatus(Intervention intervention) throws AxelorException {
    Integer fromStatus = intervention.getStatusSelect();
    intervention.setStatusSelect(InterventionRepository.INTER_STATUS_TO_PLAN);
    intervention = interventionRepository.save(intervention);
    Optional<Template> optionalTemplate =
        getTemplate(intervention, fromStatus, intervention.getStatusSelect());
    if (optionalTemplate.isPresent()) {
      generateAndSendMessage(intervention, optionalTemplate.get());
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void cancel(Intervention intervention) {
    removePlanification(intervention);
    intervention.setStatusSelect(InterventionRepository.INTER_STATUS_CANCELLED);
    interventionRepository.save(intervention);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void suspend(Intervention intervention, LocalDateTime dateTime) {
    intervention.setStatusSelect(InterventionRepository.INTER_STATUS_SUSPENDED);
    if (intervention.getTotalDuration() == null) {
      intervention.setTotalDuration(0L);
    }
    intervention.setTotalDuration(
        intervention.getTotalDuration()
            + ChronoUnit.SECONDS.between(intervention.getLastStartDateTime(), dateTime));
    interventionRepository.save(intervention);
  }

  @Override
  public LocalDateTime computeEstimatedEndDateTime(
      LocalDateTime planificationDateTime, Duration plannedInterventionDuration) {
    if (planificationDateTime != null && plannedInterventionDuration != null) {
      return computeDuration(plannedInterventionDuration, planificationDateTime);
    }
    return null;
  }

  public LocalDateTime computeDuration(Duration duration, LocalDateTime dateTime) {
    if (duration == null) {
      return dateTime;
    }
    switch (duration.getTypeSelect()) {
      case DurationRepository.TYPE_MONTH:
        return dateTime.plusMonths(duration.getValue());
      case DurationRepository.TYPE_DAY:
        return dateTime.plusDays(duration.getValue());
      case DurationRepository.TYPE_HOUR:
        return dateTime.plusHours(duration.getValue());
      default:
        return dateTime;
    }
  }

  @Override
  public void plan(
      Intervention intervention,
      ActionResponse response,
      User technicianUser,
      LocalDateTime planificationDateTime,
      LocalDateTime estimatedEndDateTime)
      throws AxelorException {
    Integer fromStatus = intervention.getStatusSelect();
    updateIntervention(intervention, technicianUser, planificationDateTime, estimatedEndDateTime);
    ControllerCallableTool<Integer> controllerCallableTool = new ControllerCallableTool<>();
    interventionSurveyGenerator.configure(intervention);
    controllerCallableTool.runInSeparateThread(interventionSurveyGenerator, response);
    Optional<Template> optionalTemplate =
        getTemplate(intervention, fromStatus, intervention.getStatusSelect());
    if (optionalTemplate.isPresent()) {
      generateAndSendMessage(intervention, optionalTemplate.get());
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void computeTag(Long interventionId) {
    Intervention intervention =
        Query.of(Intervention.class)
            .autoFlush(false)
            .filter("self.id = :id")
            .bind("id", interventionId)
            .fetchOne();

    InterventionHelper.computeTag(intervention);
    JPA.save(intervention);
  }

  @Transactional(rollbackOn = Exception.class)
  public void updateIntervention(
      Intervention intervention,
      User technicianUser,
      LocalDateTime planificationDateTime,
      LocalDateTime estimatedEndDateTime)
      throws AxelorException {
    if (intervention.getStatusSelect().compareTo(InterventionRepository.INTER_STATUS_PLANNED) < 0) {
      intervention.setStatusSelect(InterventionRepository.INTER_STATUS_PLANNED);
    }
    intervention.setAssignedTo(technicianUser);
    intervention.setPlanifStartDateTime(planificationDateTime);
    intervention.setPlanifEndDateTime(estimatedEndDateTime);

    interventionRepository.save(intervention);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void finish(Intervention intervention, LocalDateTime dateTime) throws AxelorException {
    checkSurvey(intervention);
    intervention.setEndDateTime(dateTime);
    if (intervention.getStatusSelect().equals(InterventionRepository.INTER_STATUS_STARTED)) {
      if (intervention.getTotalDuration() == null) {
        intervention.setTotalDuration(0L);
      }
      intervention.setTotalDuration(
          intervention.getTotalDuration()
              + ChronoUnit.SECONDS.between(
                  intervention.getLastStartDateTime(), intervention.getEndDateTime()));
    }
    intervention.setTotalDuration(
        InterventionHelper.roundToNextHalfHour(intervention.getTotalDuration()));
    intervention.setStatusSelect(InterventionRepository.INTER_STATUS_FINISHED);

    if (intervention.getCustomerRequest() != null
        && intervention
                .getCustomerRequest()
                .getStatusSelect()
                .compareTo(CustomerRequestRepository.CUSTOMER_REQUEST_STATUS_FINISHED)
            < 0) {
      intervention
          .getCustomerRequest()
          .setStatusSelect(CustomerRequestRepository.CUSTOMER_REQUEST_STATUS_FINISHED);
      customerRequestRepository.save(intervention.getCustomerRequest());
    }
    interventionRepository.save(intervention);
  }

  protected void checkSurvey(Intervention intervention) throws AxelorException {
    CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
    CriteriaQuery<Long> cr = cb.createQuery(Long.class);
    Root<InterventionQuestion> root = cr.from(InterventionQuestion.class);
    cr.select(root.get("id"));

    Predicate belongToIntervention =
        cb.equal(root.get("interventionRange").get("intervention"), intervention);
    Predicate required = cb.isTrue(root.get("isRequired"));
    Predicate notAnswered = cb.not(cb.isTrue(root.get("isAnswered")));

    cr.where(cb.and(belongToIntervention, required, notAnswered));
    List<Long> ids = JPA.em().createQuery(cr).getResultList();

    for (Long id : ids) {
      InterventionQuestion question = interventionQuestionRepository.find(id);
      if (InterventionQuestionHelper.isRequired(question)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(InterventionExceptionMessage.ALL_REQUIRED_QUESTIONS_NOT_ANSWERED));
      }
    }
  }

  protected ZipFile openZipFile(String path, String zipFileName) {
    File root = new File(path);
    File zipFile = findZipFile(root, zipFileName);

    if (zipFile == null) {
      throw new IllegalArgumentException(I18n.get("No valid zip file found."));
    }

    try {
      ZipFile zf = new ZipFile(zipFile);
      log.info("Found zip file: {}", zipFile.getAbsolutePath());
      return zf;
    } catch (ZipException e) {
      throw new IllegalStateException(
          String.format(
              I18n.get("Zip file %s is corrupted: %s"), zipFile.getAbsolutePath(), e.getMessage()));
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format(
              I18n.get("Error opening zip file %s: %s"),
              zipFile.getAbsolutePath(),
              e.getMessage()));
    }
  }

  protected File findZipFile(File dir, String zipFileName) {
    File[] files = dir.listFiles();
    if (files == null) {
      return null;
    }
    Arrays.sort(files, fileComparator());
    for (File file : files) {
      if (file.isDirectory()) {
        File zipFile = findZipFile(file, zipFileName);
        if (zipFile != null) {
          return zipFile;
        }
      } else if (file.getName().equals(zipFileName)) {
        return file;
      }
    }
    return null;
  }

  private static Comparator<File> fileComparator() {
    return (f1, f2) -> {
      if (f1.isFile() && f2.isDirectory()) {
        return -1;
      } else if (f1.isDirectory() && f2.isFile()) {
        return 1;
      } else {
        return f1.getName().compareTo(f2.getName());
      }
    };
  }

  protected void fill(
      Intervention intervention,
      InterventionType interventionType,
      Partner invoicedPartner,
      Integer planningPreferenceSelect,
      Partner deliveredPartner,
      Company invoicingCompany) {
    intervention.setInterventionType(interventionType);
    intervention.setInterventionType(interventionType);
    intervention.setInvoicedPartner(invoicedPartner);
    intervention.setPlanningPreferenceSelect(planningPreferenceSelect);

    // More valuations
    intervention.setDeliveredPartner(deliveredPartner);
    intervention.setCompany(invoicingCompany);
    intervention.setRequestSource(
        appInterventionService.getAppIntervention().getDefaultRequestSource());
    intervention.setRequestSubject(
        appInterventionService.getAppIntervention().getDefaultRequestSubject());
    intervention.setInterventionCategory(
        appInterventionService.getAppIntervention().getDefaultInterventionCategory());
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public SaleOrder generateSaleOrder(Intervention intervention) throws AxelorException {
    Partner contactPartner = intervention.getContact();
    SaleOrder saleOrder =
        saleOrderCreateService.createSaleOrder(
            AuthUtils.getUser(),
            intervention.getCompany(),
            contactPartner,
            intervention.getCompany().getCurrency(),
            null,
            null,
            null,
            null,
            intervention.getDeliveredPartner(),
            AuthUtils.getUser().getActiveTeam(),
            null,
            null,
            intervention.getTradingName());
    saleOrder.setInvoicedPartner(intervention.getInvoicedPartner());
    saleOrder.setDeliveredPartner(intervention.getDeliveredPartner());
    saleOrder.setTradingName(intervention.getTradingName());
    saleOrder = saleOrderRepository.save(saleOrder);
    intervention.setLinkedSaleOrder(saleOrder);
    interventionRepository.save(intervention);
    return saleOrder;
  }

  @Override
  public Partner getDefaultInvoicedPartner(Intervention intervention) {
    if (intervention == null) {
      return null;
    }
    return interventionPartnerService.getDefaultInvoicedPartner(intervention.getDeliveredPartner());
  }
}
