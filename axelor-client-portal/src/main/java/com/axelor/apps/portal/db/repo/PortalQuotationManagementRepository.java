/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.db.repo;

import com.axelor.apps.client.portal.db.PortalQuotation;
import com.axelor.apps.client.portal.db.repo.PortalQuotationRepository;

public class PortalQuotationManagementRepository extends PortalQuotationRepository {

  @Override
  public PortalQuotation copy(PortalQuotation portalQuotation, boolean deep) {
    portalQuotation = super.copy(portalQuotation, deep);
    portalQuotation.setStatusSelect(PortalQuotationRepository.STATUS_PROPOSED_QUOTATION);
    portalQuotation.setSignature(null);
    return portalQuotation;
  }
}
