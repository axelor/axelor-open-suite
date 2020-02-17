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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.repo.AdvancePaymentSaleRepository;
import com.axelor.apps.supplychain.service.AdvancePaymentServiceSupplychainImpl;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class AdvancePaymentSupplychainRepository extends AdvancePaymentSaleRepository {

  @Inject private AppService appService;

  @Override
  public AdvancePayment save(AdvancePayment advancePayment) {
    try {
      if (appService.isApp("supplychain")) {
        Beans.get(AdvancePaymentServiceSupplychainImpl.class).validate(advancePayment);
      }
      return super.save(advancePayment);
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }
}
