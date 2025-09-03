/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.db.repo;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.address.AddressTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.service.LeadComputeNameService;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class LeadManagementRepository extends LeadRepository {

  protected AppCrmService appCrmService;
  protected LeadComputeNameService leadComputeNameService;
  protected AddressTemplateService addressTemplateService;
  protected AddressService addressService;

  @Inject
  public LeadManagementRepository(
      AppCrmService appCrmService,
      LeadComputeNameService leadComputeNameService,
      AddressTemplateService addressTemplateService,
      AddressService addressService) {
    this.appCrmService = appCrmService;
    this.leadComputeNameService = leadComputeNameService;
    this.addressTemplateService = addressTemplateService;
    this.addressService = addressService;
  }

  @Override
  public Lead save(Lead entity) {
    try {
      String fullName =
          leadComputeNameService.processFullName(
              entity.getEnterpriseName(), entity.getName(), entity.getFirstName());
      entity.setFullName(fullName);

      if (entity.getLeadStatus() == null) {
        entity.setLeadStatus(appCrmService.getLeadDefaultStatus());
      }

      Address address = entity.getAddress();
      if (address != null) {
        addressTemplateService.setFormattedFullName(address);
        address.setFullName(addressService.computeFullName(address).toUpperCase());
        addressTemplateService.checkRequiredAddressFields(address);
      }
      return super.save(entity);

    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
