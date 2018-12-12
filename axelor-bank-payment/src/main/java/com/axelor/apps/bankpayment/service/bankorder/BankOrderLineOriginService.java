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

import com.axelor.apps.bankpayment.db.BankOrderLineOrigin;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;

public class BankOrderLineOriginService {

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
}
