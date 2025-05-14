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
package com.axelor.apps.intervention.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.AbstractContractRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.InterventionBatch;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.apps.intervention.service.InterventionService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BatchContractInterventionGenerationService extends AbstractBatch {

  private final ContractRepository contractRepository;
  private final InterventionRepository interventionRepository;
  private final InterventionService interventionService;
  private final EquipmentRepository equipmentRepository;
  private final DurationService durationService;
  private final Set<Long> idsOk;
  private final Set<Long> idsFail;
  private LocalDateTime endDateTime;

  @Inject
  public BatchContractInterventionGenerationService(
      ContractRepository contractRepository,
      InterventionRepository interventionRepository,
      InterventionService interventionService,
      EquipmentRepository equipmentRepository,
      DurationService durationService) {
    this.contractRepository = contractRepository;
    this.interventionRepository = interventionRepository;
    this.interventionService = interventionService;
    this.equipmentRepository = equipmentRepository;
    this.durationService = durationService;
    this.idsOk = new HashSet<>();
    this.idsFail = new HashSet<>();
  }

  @Override
  protected void process() {
    generateInterventions();
  }

  protected void generateInterventions() {
    Integer nextVisitDelay =
        Optional.ofNullable(batch.getInterventionBatch())
            .map(InterventionBatch::getNextVisitDelay)
            .orElse(12);
    endDateTime = LocalDateTime.now().plusMonths(nextVisitDelay);
    idsOk.add(0L);
    idsFail.add(0L);
    long total =
        contractRepository
            .all()
            .filter(
                "self.statusSelect = :statusSelect "
                    + "AND self.activateRecurrencePlanning IS TRUE "
                    + "AND ((self.nextAnnualVisitDate IS NOT NULL AND self.nextAnnualVisitDate <= :endDateTime) OR (self.nextAnnualVisitDate IS NULL AND self.lastMaintenanceVisitDateDone IS NOT NULL))")
            .bind("endDateTime", endDateTime)
            .bind("statusSelect", AbstractContractRepository.ACTIVE_CONTRACT)
            .count();
    long processed = 0L;
    Query<Contract> query =
        contractRepository
            .all()
            .filter(
                "self.statusSelect = :statusSelect "
                    + "AND self.activateRecurrencePlanning IS TRUE "
                    + "AND ((self.nextAnnualVisitDate IS NOT NULL AND self.nextAnnualVisitDate <= :endDateTime) OR (self.nextAnnualVisitDate IS NULL AND self.lastMaintenanceVisitDateDone IS NOT NULL))"
                    + "AND self.id NOT IN (:idsOk) "
                    + "AND self.id NOT IN (:idsFail)");
    List<Contract> contracts;
    while (!(contracts =
            query
                .bind("endDateTime", endDateTime)
                .bind("statusSelect", AbstractContractRepository.ACTIVE_CONTRACT)
                .bind("idsOk", idsOk)
                .bind("idsFail", idsFail)
                .fetch(FETCH_LIMIT))
        .isEmpty()) {
      for (Contract contract : contracts) {
        try {
          processContract(contract.getId());
          idsOk.add(contract.getId());
          incrementDone();
        } catch (Exception e) {
          TraceBackService.trace(
              new AxelorException(e, contract, TraceBackRepository.CATEGORY_INCONSISTENCY),
              null,
              batch.getId());
          idsFail.add(contract.getId());
          incrementAnomaly();
        } finally {
          processed++;
        }
      }
      JPA.clear();
      LOG.debug("Contract's interventions generation progress : {}%", processed * 100L / total);
    }
    LOG.debug("Interventions generated for {} Contracts.", idsOk.size() - 1);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void processContract(Long contractId) throws AxelorException {
    Contract contract = contractRepository.find(contractId);
    if (contract.getNextAnnualVisitDate() != null) {
      Intervention intervention = interventionService.create(contract);
      linkAllContractEquipmentsToIntervention(contract, intervention);
      interventionRepository.save(intervention);
      processDates(contract, contract.getNextAnnualVisitDate());
      if (contract.getNextAnnualVisitDate() != null
          && !contract.getNextAnnualVisitDate().isAfter(endDateTime.toLocalDate())) {
        processContract(contract.getId());
      }
    } else if (contract.getLastMaintenanceVisitDateDone() != null) {
      processDates(contract, contract.getLastMaintenanceVisitDateDone());
    }
  }

  protected void processDates(Contract contract, LocalDate date) {
    LocalDate nextAnnualVisitDate =
        durationService.computeDuration(contract.getPeriodicity(), date);
    contract.setLastMaintenanceVisitDateDone(date);
    contract.setNextAnnualVisitDate(nextAnnualVisitDate.isAfter(date) ? nextAnnualVisitDate : null);
    contractRepository.save(contract);
  }

  protected void linkAllContractEquipmentsToIntervention(
      Contract contract, Intervention intervention) {
    for (Equipment equipment : equipmentRepository.findByContract(contract.getId())) {
      intervention.addEquipmentSetItem(equipment);
    }
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_INTERVENTION_BATCH);
  }
}
