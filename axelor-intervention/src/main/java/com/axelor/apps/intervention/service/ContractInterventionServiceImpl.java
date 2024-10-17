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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.service.ContractInvoicingService;
import com.axelor.apps.contract.service.ContractLineService;
import com.axelor.apps.contract.service.ContractServiceImpl;
import com.axelor.apps.contract.service.ContractVersionService;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.apps.supplychain.service.PartnerLinkSupplychainService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.List;

public class ContractInterventionServiceImpl extends ContractServiceImpl {

  protected EquipmentRepository equipmentRepository;

  @Inject
  public ContractInterventionServiceImpl(
      ContractLineService contractLineService,
      ContractVersionService contractVersionService,
      SequenceService sequenceService,
      ContractVersionRepository contractVersionRepository,
      AppBaseService appBaseService,
      ContractVersionService versionService,
      DurationService durationService,
      ContractLineRepository contractLineRepo,
      ContractRepository contractRepository,
      PartnerLinkSupplychainService partnerLinkSupplychainService,
      ContractInvoicingService contractInvoicingService,
      EquipmentRepository equipmentRepository) {
    super(
        contractLineService,
        contractVersionService,
        sequenceService,
        contractVersionRepository,
        appBaseService,
        versionService,
        durationService,
        contractLineRepo,
        contractRepository,
        partnerLinkSupplychainService,
        contractInvoicingService);
    this.equipmentRepository = equipmentRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice ongoingCurrentVersion(Contract contract, LocalDate date) throws AxelorException {
    Invoice invoice = super.ongoingCurrentVersion(contract, date);

    List<Equipment> equipmentList =
        equipmentRepository
            .all()
            .filter("self.contract.id = :contractId")
            .bind("contractId", contract.getId())
            .fetch();
    equipmentList.forEach(equipment -> equipmentRepository.save(equipment));

    return invoice;
  }

  @Override
  @Transactional
  public Contract copyFromTemplate(Contract contract, ContractTemplate template)
      throws AxelorException {
    contract = super.copyFromTemplate(contract, template);
    contract.setGuaranteedInterventionTime(template.getGuaranteedInterventionTime());
    contract.setGuaranteedRecoveryTime(template.getGuaranteedRecoveryTime());
    contract.setOnCallManagement(template.getOnCallManagement());
    contract.setOnCallPlanning(template.getOnCallPlanning());
    contract.setDelayToSendTheQuotation(template.getDelayToSendTheQuotation());
    contract.setActivateRecurrencePlanning(template.getActivateRecurrencePlanning());
    contract.setPlanningPreferenceSelect(template.getPlanningPreferenceSelect());
    contract.setPeriodicity(template.getPeriodicity());
    return contract;
  }
}
