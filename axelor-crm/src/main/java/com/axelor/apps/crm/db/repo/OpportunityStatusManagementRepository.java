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
package com.axelor.apps.crm.db.repo;

import com.axelor.apps.base.db.AppCrm;
import com.axelor.apps.crm.db.OpportunityStatus;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class OpportunityStatusManagementRepository extends OpportunityStatusRepository {

  @Override
  public void remove(OpportunityStatus entity) {
    AppCrm appCrm = Beans.get(AppCrmService.class).getAppCrm();

    if (appCrm.getClosedWinOpportunityStatus() == null) {
      throw new PersistenceException(
          I18n.get(CrmExceptionMessage.CRM_CLOSED_WIN_OPPORTUNITY_STATUS_MISSING));
    }

    if (appCrm.getClosedLostOpportunityStatus() == null) {
      throw new PersistenceException(
          I18n.get(CrmExceptionMessage.CRM_CLOSED_LOST_OPPORTUNITY_STATUS_MISSING));
    }

    if (entity.equals(appCrm.getClosedWinOpportunityStatus())
        || entity.equals(appCrm.getClosedLostOpportunityStatus())) {
      throw new PersistenceException(
          I18n.get(
              String.format(
                  CrmExceptionMessage.OPPORTUNITY_STATUS_CLOSED_WON_LOST_NOT_DELETED,
                  entity.getName())));
    }

    super.remove(entity);
  }
}
