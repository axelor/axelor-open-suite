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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.contract.db.Contract;
import com.google.inject.Inject;
import java.util.Map;

public class ImportContract {

  protected SequenceService sequenceService;

  @Inject
  public ImportContract(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  public Object importContract(Object bean, Map<String, Object> values) throws AxelorException {

    assert bean instanceof Contract;

    Contract contract = (Contract) bean;
    contract.setContractId(
        sequenceService.getSequenceNumber(
            "customerContract", null, Contract.class, "contractId", contract));
    return contract;
  }
}
