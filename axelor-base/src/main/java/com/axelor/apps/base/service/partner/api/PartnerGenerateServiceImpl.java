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
package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.MainActivity;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.PartnerCategory;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.MainActivityRepository;
import com.axelor.apps.base.db.repo.PartnerCategoryRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.rest.dto.sirene.AdresseEtablissementResponse;
import com.axelor.apps.base.rest.dto.sirene.PartnerDataResponse;
import com.axelor.apps.base.rest.dto.sirene.UniteLegaleResponse;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartnerGenerateServiceImpl implements PartnerGenerateService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected final PartnerRepository partnerRepository;
  protected final PartnerCategoryRepository partnerCategoryRepository;
  protected final CountryRepository countryRepository;
  protected final CityRepository cityRepository;
  protected final PartnerApiFetchService partnerApiFetchService;
  protected final MainActivityRepository mainActivityRepository;
  protected final PartnerService partnerService;
  protected final AppBaseService appBaseService;

  @Inject
  public PartnerGenerateServiceImpl(
      PartnerRepository partnerRepository,
      PartnerCategoryRepository partnerCategoryRepository,
      CountryRepository countryRepository,
      CityRepository cityRepository,
      PartnerApiFetchService partnerApiFetchService,
      MainActivityRepository mainActivityRepository,
      PartnerService partnerService,
      AppBaseService appBaseService) {
    this.partnerRepository = partnerRepository;
    this.partnerCategoryRepository = partnerCategoryRepository;
    this.countryRepository = countryRepository;
    this.cityRepository = cityRepository;
    this.partnerApiFetchService = partnerApiFetchService;
    this.mainActivityRepository = mainActivityRepository;
    this.partnerService = partnerService;
    this.appBaseService = appBaseService;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void configurePartner(Partner partner, String siret) throws AxelorException {
    String result = partnerApiFetchService.fetch(siret);
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      PartnerDataResponse partnerData = objectMapper.readValue(result, PartnerDataResponse.class);

      setPartnerBasicDetails(partner, partnerData);
      setPartnerCategoryAndType(partner, partnerData.getUniteLegale());
      setPartnerAddress(partner, partnerData.getAdresseEtablissement());

      partnerRepository.save(partner);
    } catch (JsonProcessingException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, result);
    }
  }

  protected void setPartnerBasicDetails(Partner partner, PartnerDataResponse partnerData)
      throws AxelorException {
    safeSetString(
        partner::setRegistrationCode, partner::getRegistrationCode, partnerData.getSiret());

    String registrationCodeMessage = partnerService.checkIfRegistrationCodeExists(partner);

    if (!StringUtils.isEmpty(registrationCodeMessage)
        && appBaseService.getAppBase().getIsRegistrationCodeCheckBlocking()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, registrationCodeMessage);
    }

    String sirenNb = partnerData.getSiren();
    if (sirenNb != null) {
      partner.setSiren(sirenNb);
      setPartnerTaxNumber(sirenNb, partner);
    }
  }

  protected void setPartnerTaxNumber(String sirenNb, Partner partner) {
    int sirenInt = Integer.parseInt(sirenNb);
    int keyTVA = (12 + 3 * (sirenInt % 97)) % 97;
    String taxNbr = "FR" + keyTVA + sirenInt;
    safeSetString(partner::setTaxNbr, partner::getTaxNbr, taxNbr);
  }

  protected void setPartnerCategoryAndType(Partner partner, UniteLegaleResponse uniteLegale) {
    if (uniteLegale == null) {
      return;
    }

    String partnerCategoryCode = uniteLegale.getCategorieEntreprise();
    PartnerCategory partnerCategory = partnerCategoryRepository.findByCode(partnerCategoryCode);
    if (partnerCategory != null) {
      partner.setPartnerCategory(partnerCategory);
    }

    String nafCode = uniteLegale.getActivitePrincipaleUniteLegale();
    nafCode = nafCode != null ? nafCode.replace(".", "") : nafCode;
    MainActivity mainActivity = mainActivityRepository.findByCode(nafCode);
    if (mainActivity != null) {
      partner.setMainActivity(mainActivity);
    }

    String nic = uniteLegale.getNicSiegeUniteLegale();
    safeSetString(partner::setNic, partner::getNic, nic);

    String categorieJuridique = uniteLegale.getCategorieJuridiqueUniteLegale();
    if (categorieJuridique != null && Integer.parseInt(categorieJuridique) == 1000) {
      setIndividualPartnerDetails(partner, uniteLegale);
    } else {
      setCompanyPartnerDetails(partner, uniteLegale);
    }
  }

  protected void setIndividualPartnerDetails(Partner partner, UniteLegaleResponse uniteLegale) {
    partner.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_INDIVIDUAL);
    safeSetString(partner::setName, partner::getName, uniteLegale.getNomUniteLegale());
    safeSetString(
        partner::setFirstName, partner::getFirstName, uniteLegale.getPrenom1UniteLegale());

    String sex = uniteLegale.getSexeUniteLegale();
    if ("F".equals(sex)) {
      partner.setTitleSelect(PartnerRepository.PARTNER_TITLE_MS);
    } else if ("M".equals(sex)) {
      partner.setTitleSelect(PartnerRepository.PARTNER_TITLE_M);
    }
  }

  protected void setCompanyPartnerDetails(Partner partner, UniteLegaleResponse uniteLegale) {
    partner.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_COMPANY);
    safeSetString(partner::setName, partner::getName, uniteLegale.getDenominationUniteLegale());
  }

  protected void setPartnerAddress(
      Partner partner, AdresseEtablissementResponse adresseEtablissement) {
    if (adresseEtablissement == null) {
      return;
    }

    Address address = new Address();
    setAddressDetails(address, adresseEtablissement);

    if (isValidAddress(address)) {
      PartnerAddress partnerAddress = new PartnerAddress();
      partnerAddress.setPartner(partner);
      partnerAddress.setAddress(address);
      partner.addPartnerAddressListItem(partnerAddress);
    }
  }

  protected void setAddressDetails(
      Address address, AdresseEtablissementResponse adresseEtablissement) {
    safeSetString(
        address::setZip, address::getZip, adresseEtablissement.getCodePostalEtablissement());
    safeSetString(
        address::setFloor,
        address::getFloor,
        adresseEtablissement.getComplementAdresseEtablissement());
    safeSetString(
        address::setPostBox,
        address::getPostBox,
        adresseEtablissement.getDistributionSpecialeEtablissement());
    safeSetString(
        address::setDepartment,
        address::getDepartment,
        adresseEtablissement.getEnseigne1Etablissement());

    Country currentCountry = countryRepository.findByName("FRANCE");
    if (currentCountry != null) {
      address.setCountry(currentCountry);
    }

    String cityName = adresseEtablissement.getLibelleCommuneEtablissement();
    City currentCity = cityRepository.findByName(cityName);
    if (currentCity != null) {
      address.setCity(currentCity);
    } else {
      createCity(address, cityName, currentCountry);
    }

    String numeroVoieEtablissement = adresseEtablissement.getNumeroVoieEtablissement();
    String typeVoieEtablissement = adresseEtablissement.getTypeVoieEtablissement();
    String libelleVoieEtablissement = adresseEtablissement.getLibelleVoieEtablissement();
    String streetName =
        numeroVoieEtablissement + " " + typeVoieEtablissement + " " + libelleVoieEtablissement;
    safeSetString(address::setStreetName, address::getStreetName, streetName);
    safeSetString(
        address::setFullName,
        address::getFullName,
        streetName + " " + address.getZip() + " " + cityName);
  }

  @Transactional
  protected void createCity(Address address, String cityName, Country currentCountry) {
    City newCity = new City();
    newCity.setName(cityName);
    if (!Objects.isNull(currentCountry)) {
      newCity.setCountry(currentCountry);
    }
    cityRepository.save(newCity);
    address.setCity(newCity);
  }

  protected boolean isValidAddress(Address address) {
    return address.getStreetName() != null && address.getCity() != null && address.getZip() != null;
  }

  protected void safeSetString(
      Consumer<String> setter, Supplier<String> currentGetter, String newValue) {
    if (newValue != null && (currentGetter == null || currentGetter.get() == null)) {
      setter.accept(newValue);
    }
  }
}
