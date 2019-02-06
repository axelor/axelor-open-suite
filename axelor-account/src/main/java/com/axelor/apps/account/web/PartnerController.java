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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.PartnerAccountService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class PartnerController {

  @Inject private AccountingSituationService accountingSituationService;
  @Inject private PartnerAccountService partnerAccountService;

  public void createAccountingSituations(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Partner partner = request.getContext().asType(Partner.class);

    List<AccountingSituation> accountingSituationList =
        accountingSituationService.createAccountingSituation(
            Beans.get(PartnerRepository.class).find(partner.getId()));

    if (accountingSituationList != null) {
      response.setValue("accountingSituationList", accountingSituationList);
    }
  }

  public void getDefaultSpecificTaxNote(ActionRequest request, ActionResponse response) {

    Partner partner = request.getContext().asType(Partner.class);
    response.setValue("specificTaxNote", partnerAccountService.getDefaultSpecificTaxNote(partner));
  }
}
