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
package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.rpc.Context;

public class ContractLineContextToolServiceImpl implements ContractLineContextToolService {

  @Override
  public Contract getContract(Context context) {
    Context parentContext = context.getParent();

    // Classic contract line
    if (parentContext != null && ContractVersion.class.equals(parentContext.getContextClass())) {
      Context parentParentContext = parentContext.getParent();
      if (parentParentContext != null
          && Contract.class.equals(parentParentContext.getContextClass())) {
        return parentParentContext.asType(Contract.class);
      }
      return parentContext.asType(ContractVersion.class).getNextContract();
    }

    // Additional line
    if (parentContext != null && Contract.class.equals(parentContext.getContextClass())) {
      return parentContext.asType(Contract.class);
    }

    return null;
  }
}
