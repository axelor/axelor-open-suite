package com.axelor.apps.intervention.service;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ContractUpdateEquipmentServiceImpl implements ContractUpdateEquipmentService {

  protected EquipmentRepository equipmentRepository;

  @Inject
  public ContractUpdateEquipmentServiceImpl(EquipmentRepository equipmentRepository) {
    this.equipmentRepository = equipmentRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateEquipment(Contract contract) {

    List<Equipment> equipmentList =
        equipmentRepository
            .all()
            .filter("self.contract.id = :contractId")
            .bind("contractId", contract.getId())
            .fetch();

    equipmentList.forEach(equipment -> equipmentRepository.save(equipment));
  }
}
