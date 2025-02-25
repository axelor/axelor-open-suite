package com.axelor.apps.crm.service.partner.api;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.MainActivityRepository;
import com.axelor.apps.base.db.repo.PartnerCategoryRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.rest.dto.sirene.PartnerDataResponse;
import com.axelor.apps.base.service.partner.api.PartnerApiFetchService;
import com.axelor.apps.base.service.partner.api.PartnerGenerateServiceImpl;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;
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
      MainActivityRepository mainActivityRepository) {
    super(
        partnerRepository,
        partnerCategoryRepository,
        countryRepository,
        cityRepository,
        partnerApiFetchService,
        mainActivityRepository);
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

  protected int getEmployeeCountCode(int employeeCount) {
    Map<Integer, Integer> employeeCodeMap = new LinkedHashMap<>();

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

    for (Map.Entry<Integer, Integer> entry : employeeCodeMap.entrySet()) {
      if (employeeCount <= entry.getKey()) {
        return entry.getValue();
      }
    }

    return 53;
  }
}
