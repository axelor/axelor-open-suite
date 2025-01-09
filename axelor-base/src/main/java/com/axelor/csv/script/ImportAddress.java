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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.address.AddressTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;

public class ImportAddress {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AddressService addressService;
  protected AddressTemplateService addressTemplateService;
  protected AddressRepository addressRepository;

  @Inject
  public ImportAddress(
      AddressService addressService,
      AddressTemplateService addressTemplateService,
      AddressRepository addressRepository) {
    this.addressService = addressService;
    this.addressTemplateService = addressTemplateService;
    this.addressRepository = addressRepository;
  }

  public Object importAddress(Object bean, Map<String, Object> values) {

    Address address = (Address) bean;

    if (address.getCity() != null) {
      address.setTownName(address.getCity().getName());
    }
    if (address.getStreet() != null) {
      address.setStreetName(address.getStreet().getName());
    }

    try {
      addressTemplateService.setFormattedFullName(address);
    } catch (Exception e) {
      TraceBackService.trace(
          e, BaseExceptionMessage.ADDRESS_TEMPLATE_ERROR, Long.parseLong(address.getImportId()));
    }
    address.setFullName(addressService.computeFullName(address));

    return address;
  }

  public void computeLongitAndLatit(Object bean, Map<String, Object> values) {

    List<Address> addressList = addressRepository.all().fetch();

    boolean forceUpdate = "true".equals(values.getOrDefault("forceUpdate", "false").toString());

    LOG.debug(
        forceUpdate
            ? "Force updating longit and latit for all addresses"
            : "Computing missing longit and latit for the addresses");

    AddressService addressService = Beans.get(AddressService.class);

    for (Address address : addressList) {
      try {
        if (forceUpdate) {
          addressService.updateLatLong(address);
        } else {
          addressService.getOrUpdateLatLong(address);
        }
      } catch (AxelorException | JSONException e) {
        TraceBackService.trace(e, address.getImportId());
      }
    }
  }
}
