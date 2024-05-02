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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationCheckService;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationInitService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerBaseRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.utils.PartnerUtilsService;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class PartnerAccountRepository extends PartnerBaseRepository {

  protected AppService appService;
  protected AccountingSituationInitService accountingSituationInitService;
  protected AccountingSituationCheckService accountingSituationCheckService;

  @Inject
  public PartnerAccountRepository(
      PartnerAddressRepository partnerAddressRepository,
      PartnerUtilsService partnerUtilsService,
      AppService appService,
      AccountingSituationInitService accountingSituationInitService,
      AccountingSituationCheckService accountingSituationCheckService) {
    super(partnerAddressRepository, partnerUtilsService);
    this.appService = appService;
    this.accountingSituationInitService = accountingSituationInitService;
    this.accountingSituationCheckService = accountingSituationCheckService;
  }

  @Override
  public Partner save(Partner partner) {
    try {

      if (partner.getId() == null) {
        partner = super.save(partner);
      }

      if (appService.isApp("account")) {
        if (!partner.getIsContact() || partner.getIsEmployee()) {
          // Create & fill
          accountingSituationInitService.createAccountingSituation(this.find(partner.getId()));
        }

        accountingSituationCheckService.checkDuplicatedCompaniesInAccountingSituation(partner);

        // We do this for contacts too as it seems this is the way employees are handled
        if (CollectionUtils.isNotEmpty(partner.getAccountingSituationList())) {
          for (AccountingSituation situation : partner.getAccountingSituationList()) {
            accountingSituationInitService.createPartnerAccounts(situation);
          }
        }
      }

      return super.save(partner);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public Partner copy(Partner partner, boolean deep) {

    Partner copy = super.copy(partner, deep);

    if (appService.isApp("account")) {
      copy.setAccountingSituationList(null);
    }

    return copy;
  }
}
