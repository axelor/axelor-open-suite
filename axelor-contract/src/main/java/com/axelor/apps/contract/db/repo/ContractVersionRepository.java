/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.contract.db.repo;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.tool.ModelTool;
import com.axelor.inject.Beans;
import java.util.List;

public class ContractVersionRepository extends AbstractContractVersionRepository {

  public ContractVersion copy(Contract contract) {
    ContractVersion newVersion = new ContractVersion();
    ContractVersion currentVersion = contract.getCurrentContractVersion();

    newVersion.setStatusSelect(ContractVersionRepository.DRAFT_VERSION);
    newVersion.setNextContract(contract);
    newVersion.setPaymentMode(currentVersion.getPaymentMode());
    newVersion.setPaymentCondition(currentVersion.getPaymentCondition());
    newVersion.setInvoicingDuration(currentVersion.getInvoicingDuration());
    newVersion.setInvoicingMomentSelect(currentVersion.getInvoicingMomentSelect());
    newVersion.setIsPeriodicInvoicing(currentVersion.getIsPeriodicInvoicing());
    newVersion.setAutomaticInvoicing(currentVersion.getAutomaticInvoicing());
    newVersion.setIsProratedInvoice(currentVersion.getIsProratedInvoice());
    newVersion.setIsProratedFirstInvoice(currentVersion.getIsProratedFirstInvoice());
    newVersion.setIsProratedLastInvoice(currentVersion.getIsProratedLastInvoice());
    newVersion.setDescription(currentVersion.getDescription());

    newVersion.setIsTacitRenewal(currentVersion.getIsTacitRenewal());
    newVersion.setRenewalDuration(currentVersion.getRenewalDuration());
    newVersion.setIsAutoEnableVersionOnRenew(currentVersion.getIsAutoEnableVersionOnRenew());

    newVersion.setIsWithEngagement(currentVersion.getIsWithEngagement());
    newVersion.setEngagementDuration(currentVersion.getEngagementDuration());

    newVersion.setIsWithPriorNotice(currentVersion.getIsWithPriorNotice());
    newVersion.setPriorNoticeDuration(currentVersion.getPriorNoticeDuration());

    newVersion.setEngagementStartFromVersion(currentVersion.getEngagementStartFromVersion());

    newVersion.setDoNotRenew(currentVersion.getDoNotRenew());

    ContractLineRepository repository = Beans.get(ContractLineRepository.class);
    List<ContractLine> lines =
        ModelTool.copy(repository, currentVersion.getContractLineList(), false);
    newVersion.setContractLineList(lines);

    newVersion.setIsTimeProratedInvoice(currentVersion.getIsTimeProratedInvoice());
    newVersion.setIsVersionProratedInvoice(currentVersion.getIsVersionProratedInvoice());

    newVersion.setIsConsumptionBeforeEndDate(currentVersion.getIsConsumptionBeforeEndDate());
    newVersion.setIsConsumptionManagement(currentVersion.getIsConsumptionManagement());

    return newVersion;
  }
}
