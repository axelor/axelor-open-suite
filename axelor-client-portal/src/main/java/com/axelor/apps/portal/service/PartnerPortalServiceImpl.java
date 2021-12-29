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
package com.axelor.apps.portal.service;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.supplychain.service.PartnerSupplychainServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import javax.ws.rs.NotFoundException;

public class PartnerPortalServiceImpl extends PartnerSupplychainServiceImpl
    implements PartnerPortalService {

  protected PartnerAddressRepository partnerAddressRepo;
  protected AddressRepository addressRepo;
  protected AddressService addressService;

  @Inject
  public PartnerPortalServiceImpl(
      PartnerRepository partnerRepo,
      AppBaseService appBaseService,
      InvoiceRepository invoiceRepository,
      AccountConfigService accountConfigService,
      PartnerAddressRepository partnerAddressRepo,
      AddressRepository addressRepo,
      AddressService addressService) {
    super(partnerRepo, appBaseService, invoiceRepository, accountConfigService);
    this.partnerAddressRepo = partnerAddressRepo;
    this.addressRepo = addressRepo;
    this.addressService = addressService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Address addPartnerAddress(Partner partner, Address address) {
    addressService.autocompleteAddress(address);
    address = addressRepo.save(address);
    addPartnerAddress(partner, address, true, false, false);
    partnerRepo.save(partner);
    return address;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void removePartnerAddress(Partner partner, Address address) {

    for (PartnerAddress partnerAddress : partner.getPartnerAddressList()) {
      if (partnerAddress.getAddress().equals(address)) {
        if (address.equals(partner.getMainAddress())) {
          partner.setMainAddress(null);
        }
        partner.removePartnerAddressListItem(partnerAddress);
        partnerAddressRepo.remove(partnerAddress);
        partnerRepo.save(partner);
        return;
      }
    }
    throw new NotFoundException();
  }
}
