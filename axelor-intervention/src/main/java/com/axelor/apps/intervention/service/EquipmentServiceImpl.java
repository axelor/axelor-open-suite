package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.repo.EquipmentLineRepository;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EquipmentServiceImpl implements EquipmentService {

  protected final EquipmentRepository equipmentRepository;
  protected final EquipmentLineRepository equipmentLineRepository;
  protected AppInterventionService appInterventionService;

  @Inject
  public EquipmentServiceImpl(
      EquipmentRepository equipmentRepository,
      EquipmentLineRepository equipmentLineRepository,
      AppInterventionService appInterventionService) {
    this.equipmentRepository = equipmentRepository;
    this.equipmentLineRepository = equipmentLineRepository;
    this.appInterventionService = appInterventionService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeEquipment(Equipment equipment) throws AxelorException {
    try {
      equipmentRepository.remove(equipment);
    } catch (IllegalArgumentException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }
  }
}
