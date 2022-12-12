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

import com.axelor.apps.crm.db.OpportunityStatus;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import javax.persistence.PersistenceException;

public class OpportunityStatusManagementRepository extends OpportunityStatusRepository {

  @Override
  public void remove(OpportunityStatus entity) {
    if (entity.getTypeSelect() == STATUS_TYPE_CLOSED_LOST
        || entity.getTypeSelect() == STATUS_TYPE_CLOSED_WON) {
      String title = this.getTypeSelectTitle(entity);
      throw new PersistenceException(
          I18n.get(
              String.format(
                  CrmExceptionMessage.OPPORTUNITY_STATUS_CLOSED_WON_LOST_NOT_DELETED, title)));
    }
    super.remove(entity);
  }

  @Override
  public OpportunityStatus save(OpportunityStatus entity) {
    if (entity.getTypeSelect() == STATUS_TYPE_CLOSED_LOST
        || entity.getTypeSelect() == STATUS_TYPE_CLOSED_WON) {
      OpportunityStatus status = findByTypeSelect(entity.getTypeSelect());
      if (status != null) {
        String title = this.getTypeSelectTitle(entity);
        throw new PersistenceException(
            I18n.get(
                String.format(
                    CrmExceptionMessage.OPPORTUNITY_STATUS_CLOSED_WON_LOST_ALREADY_EXIST, title)));
      }
    }
    return super.save(entity);
  }

  private String getTypeSelectTitle(OpportunityStatus entity) {
    return MetaStore.getSelectionItem(
            "crm.opportunity.status.type.select", entity.getTypeSelect().toString())
        .getTitle();
  }
}
