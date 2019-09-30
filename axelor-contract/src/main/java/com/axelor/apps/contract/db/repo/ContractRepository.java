/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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
      return super.save(contract);
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  public String computeSeq(Company company, int type) {
    try {
      String seq =
          Beans.get(SequenceService.class)
              .getSequenceNumber(
                  type == 1 ? CUSTOMER_CONTRACT_SEQUENCE : SUPPLIER_CONTRACT_SEQUENCE, company);
      if (seq == null) {
        throw new AxelorException(
            String.format(
                I18n.get("The company %s doesn't have any configured sequence for contracts"),
                company.getName()),
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
      }
      return seq;
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  @Override
  public Contract copy(Contract entity, boolean deep) {
    Contract contract = super.copy(entity, deep);
    ContractVersion version = Beans.get(ContractVersionRepository.class).copy(entity);
    contract.setCurrentContractVersion(version);
    return contract;
  }
}
