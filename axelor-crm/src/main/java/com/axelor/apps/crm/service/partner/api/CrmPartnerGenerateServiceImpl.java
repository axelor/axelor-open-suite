package com.axelor.apps.crm.service.partner.api;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.PartnerCategoryRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.rest.dto.sirene.PartnerDataResponse;
import com.axelor.apps.base.service.partner.api.PartnerApiFetchService;
import com.axelor.apps.base.service.partner.api.PartnerGenerateServiceImpl;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
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
      PartnerApiFetchService partnerApiFetchService) {
    super(
        partnerRepository,
        partnerCategoryRepository,
        countryRepository,
        cityRepository,
        partnerApiFetchService);
  }

  @Override
  protected void setPartnerBasicDetails(Partner partner, PartnerDataResponse partnerData) {
    super.setPartnerBasicDetails(partner, partnerData);
    Integer sizeSelect = null;

    String trancheEffectif = partnerData.getTrancheEffectifsEtablissement();

    if (trancheEffectif != null) {
      sizeSelect = getEmployeeCountCode(Integer.parseInt(trancheEffectif));
    }

    if (sizeSelect == null && partnerData.getUniteLegale() != null) {
      sizeSelect = Integer.valueOf(partnerData.getUniteLegale().getTrancheEffectifsUniteLegale());
    }

    if (sizeSelect != null) {
      partner.setSizeSelect(sizeSelect);
    }
  }

  private int getEmployeeCountCode(int employeeCount) {
    if ((employeeCount <= 2)) {
      return 1;
    } else if ((employeeCount <= 5)) {
      return 2;
    } else if ((employeeCount <= 9)) {
      return 3;
    } else if ((employeeCount <= 19)) {
      return 11;
    } else if ((employeeCount <= 49)) {
      return 12;
    } else if ((employeeCount <= 99)) {
      return 21;
    } else if ((employeeCount <= 199)) {
      return 22;
    } else if ((employeeCount <= 249)) {
      return 31;
    } else if ((employeeCount <= 499)) {
      return 32;
    } else if ((employeeCount <= 999)) {
      return 41;
    } else if ((employeeCount <= 1999)) {
      return 42;
    } else if ((employeeCount <= 4999)) {
      return 51;
    } else if ((employeeCount <= 9999)) {
      return 52;
    } else {
      return 53;
    }
  }
}
