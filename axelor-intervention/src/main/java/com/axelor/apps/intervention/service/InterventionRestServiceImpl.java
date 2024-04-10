package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.apps.intervention.exception.InterventionExceptionMessage;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.apps.intervention.rest.dto.InterventionEquipmentPutRequest;
import com.axelor.apps.intervention.rest.dto.InterventionStatusPutRequest;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;

public class InterventionRestServiceImpl implements InterventionRestService {

  protected final InterventionService interventionService;
  protected final UserRepository userRepository;
  protected final EquipmentRepository equipmentRepository;

  @Inject
  public InterventionRestServiceImpl(
      InterventionService interventionService,
      UserRepository userRepository,
      EquipmentRepository equipmentRepository) {
    this.interventionService = interventionService;
    this.userRepository = userRepository;
    this.equipmentRepository = equipmentRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateStatus(InterventionStatusPutRequest request, Intervention intervention)
      throws AxelorException {
    if (Objects.equals(intervention.getStatusSelect(), request.getToStatus())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(InterventionExceptionMessage.INTERVENTION_API_SAME_STATUS));
    }
    switch (request.getToStatus()) {
      case InterventionRepository.INTER_STATUS_PLANNED:
        plan(intervention, request);
        break;
      case InterventionRepository.INTER_STATUS_STARTED:
        start(intervention, request);
        break;
      case InterventionRepository.INTER_STATUS_SUSPENDED:
        suspend(intervention, request);
        break;
      case InterventionRepository.INTER_STATUS_FINISHED:
        finish(intervention, request);
        break;
      case InterventionRepository.INTER_STATUS_CANCELLED:
        cancel(intervention, request);
        break;
      default:
        break;
    }
  }

  protected void plan(Intervention intervention, InterventionStatusPutRequest request)
      throws AxelorException {
    if (intervention.getStatusSelect() != InterventionRepository.INTER_STATUS_TO_PLAN) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(InterventionExceptionMessage.INTERVENTION_API_WRONG_STATUS));
    }
    LocalDateTime dateTime = request.getDateTime();
    checkDateTime(dateTime);
    if (request.getPlannedTechnicianUserId() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(InterventionExceptionMessage.INTERVENTION_API_MISSING_USER_ID));
    }
    if (request.getPlannedDuration() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(InterventionExceptionMessage.INTERVENTION_API_MISSING_PLANNED_DURATION));
    }
    interventionService.plan(
        intervention,
        new ActionResponse(),
        userRepository.find(request.getPlannedTechnicianUserId()),
        dateTime,
        dateTime.plusSeconds(request.getPlannedDuration()));
  }

  protected void start(Intervention intervention, InterventionStatusPutRequest request)
      throws AxelorException {
    if (intervention.getStatusSelect() != InterventionRepository.INTER_STATUS_PLANNED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(InterventionExceptionMessage.INTERVENTION_API_WRONG_STATUS));
    }
    checkDateTime(request.getDateTime());
    interventionService.start(intervention, request.getDateTime());
  }

  protected void suspend(Intervention intervention, InterventionStatusPutRequest request)
      throws AxelorException {
    suspendAndFinishCheck(intervention, request);
    interventionService.start(intervention, request.getDateTime());
  }

  protected void finish(Intervention intervention, InterventionStatusPutRequest request)
      throws AxelorException {
    suspendAndFinishCheck(intervention, request);
    interventionService.finish(intervention, request.getDateTime());
  }

  protected void suspendAndFinishCheck(
      Intervention intervention, InterventionStatusPutRequest request) throws AxelorException {
    if (intervention.getStatusSelect() != InterventionRepository.INTER_STATUS_STARTED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(InterventionExceptionMessage.INTERVENTION_API_WRONG_STATUS));
    }
    checkDateTime(request.getDateTime());
  }

  protected void cancel(Intervention intervention, InterventionStatusPutRequest request)
      throws AxelorException {
    checkDateTime(request.getDateTime());
    if (request.getToStatus() == InterventionRepository.INTER_STATUS_FINISHED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(InterventionExceptionMessage.INTERVENTION_API_WRONG_STATUS));
    }
    interventionService.cancel(intervention);
  }

  protected void checkDateTime(LocalDateTime dateTime) throws AxelorException {
    if (dateTime == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(InterventionExceptionMessage.INTERVENTION_API_MISSING_DATE_TIME));
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void addEquipment(InterventionEquipmentPutRequest request, Intervention intervention)
      throws AxelorException {
    Equipment equipment = equipmentRepository.find(request.getEquipmentId());
    if (equipment == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(InterventionExceptionMessage.INTERVENTION_API_EQUIPMENT_NOT_FOUND),
              request.getEquipmentId()));
    }
    intervention.addEquipmentSetItem(equipment);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeEquipment(InterventionEquipmentPutRequest request, Intervention intervention)
      throws AxelorException {
    Equipment equipment = equipmentRepository.find(request.getEquipmentId());
    if (equipment == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(InterventionExceptionMessage.INTERVENTION_API_EQUIPMENT_NOT_FOUND),
              request.getEquipmentId()));
    }
    intervention.removeEquipmentSetItem(equipment);
  }
}
