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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.account.db.repo.PartnerAccountRepository;
import com.axelor.apps.account.service.AccountingSituationInitService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class PartnerHRRepository extends PartnerAccountRepository {

  @Inject
  public PartnerHRRepository(
      AppService appService, AccountingSituationInitService accountingSituationInitService) {
    super(appService, accountingSituationInitService);
  }

  @Override
  public void remove(Partner partner) {
    if (partner.getEmployee() != null) {
      throw new PersistenceException(
          String.format(
              I18n.get(HumanResourceExceptionMessage.CONTACT_CANNOT_DELETE),
              partner.getPartnerSeq(),
              partner.getSimpleFullName()));
    }
    super.remove(partner);
  }
}
