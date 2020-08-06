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
package com.axelor.apps.account.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.account.db.DebtRecovery;

public class DebtRecoveryAccountRepository extends DebtRecoveryRepository {

  @Override
  public DebtRecovery save(DebtRecovery debtRecovery) {
    try {
      if (debtRecovery.getAccountingSituation() != null) {
    	  debtRecovery.setPartner(debtRecovery.getAccountingSituation().getPartner());
      } else if (debtRecovery.getTradingNameAccountingSituation() != null) {
    	  debtRecovery.setPartner(debtRecovery.getTradingNameAccountingSituation().getPartner());
      }
      return super.save(debtRecovery);
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }
}
