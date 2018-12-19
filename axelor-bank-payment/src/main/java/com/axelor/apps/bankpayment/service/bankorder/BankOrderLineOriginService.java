/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLineOrigin;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineOriginRepository;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.google.inject.Inject;

public class BankOrderLineOriginService {

  protected BankOrderLineOriginRepository bankOrderLineOriginRepository;

  @Inject
  public BankOrderLineOriginService(BankOrderLineOriginRepository bankOrderLineOriginRepository) {
    this.bankOrderLineOriginRepository = bankOrderLineOriginRepository;
  }

  public BankOrderLineOrigin createBankOrderLineOrigin(Model model) {

    Class<?> klass = EntityHelper.getEntityClass(model);

    return this.createBankOrderLineOrigin(klass.getCanonicalName(), model.getId());
  }

  public BankOrderLineOrigin createBankOrderLineOrigin(
      String relatedToSelect, Long relatedToSelectId) {
    BankOrderLineOrigin bankOrderLineOrigin = new BankOrderLineOrigin();

    bankOrderLineOrigin.setRelatedToSelect(relatedToSelect);
    bankOrderLineOrigin.setRelatedToSelectId(relatedToSelectId);

    return bankOrderLineOrigin;
  }

  protected boolean existBankOrderLineOrigin(BankOrder bankOrder, Model model) {

    Class<?> klass = EntityHelper.getEntityClass(model);

    Long count =
        bankOrderLineOriginRepository
            .all()
            .filter(
                "self.relatedToSelect = ?1 AND self.relatedToSelectId = ?2",
                klass.getCanonicalName(),
                model.getId())
            .count();

    if (count != null && count > 0) {
      return true;
    }
    return false;
  }
}
