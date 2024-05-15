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
package com.axelor.apps.contract.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class ContractRepository extends AbstractContractRepository {
  @Override
  public Contract save(Contract contract) {
    try {
      if (contract.getContractId() == null) {
        contract.setContractId(computeSeq(contract.getCompany(), contract.getTargetTypeSelect()));
      }

      ContractVersion currentContractVersion = contract.getCurrentContractVersion();
      currentContractVersion.setIsConsumptionManagement(contract.getIsConsumptionManagement());

      return super.save(contract);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  public String computeSeq(Company company, int type) {
    try {
      String seq =
          Beans.get(SequenceService.class)
              .getSequenceNumber(
                  type == 1 ? CUSTOMER_CONTRACT_SEQUENCE : SUPPLIER_CONTRACT_SEQUENCE,
                  company,
                  Contract.class,
                  "contractId");
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get("The company %s doesn't have any configured sequence for contracts"),
                company.getName()));
      }
      return seq;
    } catch (Exception e) {
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public Contract copy(Contract entity, boolean deep) {
    Contract contract = super.copy(entity, deep);
    ContractVersion version = Beans.get(ContractVersionRepository.class).copy(entity);
    contract.setCurrentContractVersion(version);
    contract.setContractId(null);
    contract.setVersionHistory(null);
    return contract;
  }
}
