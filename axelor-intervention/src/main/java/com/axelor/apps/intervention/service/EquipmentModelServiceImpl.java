/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.EquipmentModel;
import com.axelor.apps.intervention.db.repo.EquipmentModelRepository;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.db.JPA;
import com.axelor.utils.helpers.WrappingHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections.CollectionUtils;

public class EquipmentModelServiceImpl implements EquipmentModelService {

  protected final EquipmentRepository equipmentRepository;
  protected final EquipmentModelRepository equipmentModelRepository;
  protected final PartnerRepository partnerRepository;
  protected final ContractRepository contractRepository;

  @Inject
  public EquipmentModelServiceImpl(
      EquipmentRepository equipmentRepository,
      EquipmentModelRepository equipmentModelRepository,
      PartnerRepository partnerRepository,
      ContractRepository contractRepository) {
    this.equipmentRepository = equipmentRepository;
    this.equipmentModelRepository = equipmentModelRepository;
    this.partnerRepository = partnerRepository;
    this.contractRepository = contractRepository;
  }

  protected List<Long> getModels(Long equipmentModelId) {
    CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
    CriteriaQuery<Long> cr = cb.createQuery(Long.class);
    Root<EquipmentModel> root = cr.from(EquipmentModel.class);
    cr.select(root.get("id"));

    Predicate belongToModels =
        cb.equal(root.get("parentEquipmentModel").get("id"), equipmentModelId);

    cr.where(belongToModels);

    return WrappingHelper.wrap(JPA.em().createQuery(cr).getResultList());
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public List<Long> generate(
      Long parentId,
      List<Long> models,
      Map<Long, Integer> quantitiesMap,
      Long partnerId,
      LocalDate commissioningDate,
      LocalDate customerWarrantyOnPartEndDate,
      LocalDate customerMoWarrantyEndDate,
      Long contractId) {
    List<Long> ids = new ArrayList<>();
    if (CollectionUtils.isEmpty(models)) {
      return ids;
    }
    for (Long modelId : models) {
      Integer quantity = quantitiesMap.get(modelId);
      if (quantity == null || quantity <= 0) {
        continue;
      }
      List<Long> equipmentModelIds = getModels(modelId);
      if (CollectionUtils.isEmpty(equipmentModelIds)) {
        for (int i = 0; i < quantity; i++) {
          Equipment equipment =
              generate(
                  parentId,
                  modelId,
                  partnerId,
                  commissioningDate,
                  customerWarrantyOnPartEndDate,
                  customerMoWarrantyEndDate,
                  contractId);
          ids.add(equipment.getId());
        }
        JPA.flush();
        JPA.clear();
      } else {
        Equipment equipment =
            generate(
                parentId,
                modelId,
                partnerId,
                commissioningDate,
                customerWarrantyOnPartEndDate,
                customerMoWarrantyEndDate,
                contractId);
        ids.add(equipment.getId());
        JPA.flush();
        JPA.clear();
        ids.addAll(
            generate(
                equipment.getId(),
                equipmentModelIds,
                quantitiesMap,
                partnerId,
                commissioningDate,
                customerWarrantyOnPartEndDate,
                customerMoWarrantyEndDate,
                contractId));
      }
    }
    return ids;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public Equipment generate(
      Long parentId,
      Long modelId,
      Long partnerId,
      LocalDate commissioningDate,
      LocalDate customerWarrantyOnPartEndDate,
      LocalDate customerMoWarrantyEndDate,
      Long contractId) {
    Equipment parent = parentId == null ? null : equipmentRepository.find(parentId);
    EquipmentModel model = equipmentModelRepository.find(modelId);
    Equipment equipment = new Equipment();
    equipment.setParentEquipment(parent);
    equipment.setCode(model.getCode());
    equipment.setName(model.getName());
    equipment.setTypeSelect(model.getTypeSelect());
    equipment.setInService(model.getInService());
    equipment.setScheduleOfOperation(model.getScheduleOfOperation());
    equipment.setSpecificAccessSchedule(model.getSpecificAccessSchedule());
    equipment.setComments(model.getComments());
    equipment.setEquipmentFamily(model.getEquipmentFamily());
    if (partnerId != null) {
      equipment.setPartner(partnerRepository.find(partnerId));
    }
    equipment.setCommissioningDate(commissioningDate);
    equipment.setCustomerWarrantyOnPartEndDate(customerWarrantyOnPartEndDate);
    if (contractId != null) {
      equipment.setContract(contractRepository.find(contractId));
    }
    return equipmentRepository.save(equipment);
  }
}
