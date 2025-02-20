package com.axelor.apps.crm.service.partner.api;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.PartnerCategoryRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.rest.dto.sirene.UniteLegaleResponse;
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
  protected void setPartnerCategoryAndType(Partner partner, UniteLegaleResponse uniteLegale) {
    super.setPartnerCategoryAndType(partner, uniteLegale);
    String sizeSelect = uniteLegale.getTrancheEffectifsUniteLegale();
    if (sizeSelect != null) {
      partner.setSizeSelect(Integer.parseInt(sizeSelect));
    }
  }
}
