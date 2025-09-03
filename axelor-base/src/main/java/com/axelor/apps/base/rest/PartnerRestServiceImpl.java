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
package com.axelor.apps.base.rest;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PartnerRestServiceImpl implements PartnerRestService {

  protected PartnerService partnerService;
  protected PartnerRepository partnerRepository;

  @Inject
  public PartnerRestServiceImpl(
      PartnerService partnerService, PartnerRepository partnerRepository) {
    this.partnerService = partnerService;
    this.partnerRepository = partnerRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addPartnerAddress(Partner partner, Address address) {
    partnerService.addPartnerAddress(partner, address, false, false, false);
    partnerRepository.save(partner);
  }
}
