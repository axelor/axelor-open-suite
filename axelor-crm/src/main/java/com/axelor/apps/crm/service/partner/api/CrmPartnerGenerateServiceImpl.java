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
package com.axelor.apps.crm.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.MainActivityRepository;
import com.axelor.apps.base.db.repo.PartnerCategoryRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.rest.dto.sirene.PartnerDataResponse;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.partner.api.PartnerApiFetchService;
import com.axelor.apps.base.service.partner.api.PartnerGenerateServiceImpl;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrmPartnerGenerateServiceImpl extends PartnerGenerateServiceImpl {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public CrmPartnerGenerateServiceImpl(
      PartnerRepository partnerRepository,
      PartnerCategoryRepository partnerCategoryRepository,
      CountryRepository countryRepository,
      CityRepository cityRepository,
      PartnerApiFetchService partnerApiFetchService,
      MainActivityRepository mainActivityRepository,
      PartnerService partnerService,
      AppBaseService appBaseService) {
    super(
        partnerRepository,
        partnerCategoryRepository,
        countryRepository,
        cityRepository,
        partnerApiFetchService,
        mainActivityRepository,
        partnerService,
        appBaseService);
  }

  @Override
  protected void setPartnerBasicDetails(Partner partner, PartnerDataResponse partnerData)
      throws AxelorException {
    super.setPartnerBasicDetails(partner, partnerData);
    Integer sizeSelect = null;

    String trancheEffectif = partnerData.getTrancheEffectifsEtablissement();

    if (trancheEffectif != null) {
      sizeSelect = getEmployeeCountCode(Integer.parseInt(trancheEffectif));
    }

    if (sizeSelect == null
        && partnerData.getUniteLegale() != null
        && partnerData.getUniteLegale().getTrancheEffectifsUniteLegale() != null) {
      sizeSelect = Integer.valueOf(partnerData.getUniteLegale().getTrancheEffectifsUniteLegale());
    }

    if (sizeSelect != null) {
      partner.setSizeSelect(sizeSelect);
    }
  }

  protected int getEmployeeCountCode(int employeeCount) {
    NavigableMap<Integer, Integer> employeeCodeMap = new TreeMap<>();

    employeeCodeMap.put(2, 1);
    employeeCodeMap.put(5, 2);
    employeeCodeMap.put(9, 3);
    employeeCodeMap.put(19, 11);
    employeeCodeMap.put(49, 12);
    employeeCodeMap.put(99, 21);
    employeeCodeMap.put(199, 22);
    employeeCodeMap.put(249, 31);
    employeeCodeMap.put(499, 32);
    employeeCodeMap.put(999, 41);
    employeeCodeMap.put(1999, 42);
    employeeCodeMap.put(4999, 51);
    employeeCodeMap.put(9999, 52);
    employeeCodeMap.put(Integer.MAX_VALUE, 53);

    Map.Entry<Integer, Integer> entry = employeeCodeMap.ceilingEntry(employeeCount);
    return (entry != null) ? entry.getValue() : 53;
  }
}
