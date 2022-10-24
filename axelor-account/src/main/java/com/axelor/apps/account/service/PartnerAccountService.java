/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PartnerAccountService {

  protected MoveLineRepository moveLineRepository;

  @Inject
  public PartnerAccountService(MoveLineRepository moveLineRepository) {
    this.moveLineRepository = moveLineRepository;
  }

  public String getDefaultSpecificTaxNote(Partner partner) {
    FiscalPosition fiscalPosition = partner.getFiscalPosition();

    if (fiscalPosition == null || !fiscalPosition.getCustomerSpecificNote()) {
      return "";
    }

    return fiscalPosition.getCustomerSpecificNoteText();
  }

  @Transactional
  public void updateMoveLineDas2Activity(Partner partner) {
    moveLineRepository
        .all()
        .filter("self.partner = :partner")
        .bind("partner", partner)
        .update("das2Activity", partner.getDas2Activity());
  }
}
