package com.axelor.apps.base.service.partner.api;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.PartnerCategory;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.PartnerCategoryRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class PartnerApiCreateServiceImpl extends GenericApiCreateService
    implements PartnerApiCreateService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected final PartnerRepository partnerRepository;
  protected final PartnerCategoryRepository partnerCategoryRepository;
  protected final CountryRepository countryRepository;
  protected final CityRepository cityRepository;

  @Inject
  public PartnerApiCreateServiceImpl(
      PartnerRepository partnerRepository,
      PartnerCategoryRepository partnerCategoryRepository,
      CountryRepository countryRepository,
      CityRepository cityRepository) {
    this.partnerRepository = partnerRepository;
    this.partnerCategoryRepository = partnerCategoryRepository;
    this.countryRepository = countryRepository;
    this.cityRepository = cityRepository;
  }

  @Transactional
  @Override
  public void setData(Partner partner, String result) throws AxelorException {
    try {
      JSONObject resultJson = new JSONObject(result);
      setPartnerBasicDetails(partner, resultJson);
      setPartnerCategoryAndType(partner, resultJson.optJSONObject("uniteLegale"));
      setPartnerAddress(partner, resultJson.optJSONObject("adresseEtablissement"));
      partnerRepository.save(partner);
    } catch (JSONException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, result);
    }
  }

  protected void setPartnerBasicDetails(Partner partner, JSONObject resultJson) {
    String registrationCode = getSafeString(resultJson, "siret");
    safeSetString(partner::setRegistrationCode, partner::getRegistrationCode, registrationCode);
  }

  protected void setPartnerCategoryAndType(Partner partner, JSONObject jsonUniteLegal) {
    if (jsonUniteLegal == null) return;

    String partnerCategoryCode = getSafeString(jsonUniteLegal, "categorieEntreprise");
    PartnerCategory partnerCategory = partnerCategoryRepository.findByCode(partnerCategoryCode);
    if (partnerCategory != null) {
      partner.setPartnerCategory(partnerCategory);
    }

    String categorieJuridique = getSafeString(jsonUniteLegal, "categorieJuridiqueUniteLegale");
    if (categorieJuridique != null && Integer.parseInt(categorieJuridique) == 1000) {
      setIndividualPartnerDetails(partner, jsonUniteLegal);
    } else {
      setCompanyPartnerDetails(partner, jsonUniteLegal);
    }
  }

  protected void setIndividualPartnerDetails(Partner partner, JSONObject jsonUniteLegal) {
    safeSetInteger(partner::setPartnerTypeSelect, partner::getPartnerTypeSelect, PartnerRepository.PARTNER_TYPE_INDIVIDUAL);
    safeSetString(partner::setName, partner::getName, getSafeString(jsonUniteLegal, "nomUniteLegale"));
    safeSetString(
        partner::setFirstName,
        partner::getFirstName,
        getSafeString(jsonUniteLegal, "prenom1UniteLegale"));

    String sexUniteLegale = getSafeString(jsonUniteLegal, "sexUniteLegale");
    if (Objects.equals(sexUniteLegale, "F")) {
      partner.setTitleSelect(PartnerRepository.PARTNER_TITLE_MS);
    } else if (Objects.equals(sexUniteLegale, "M")) {
      partner.setTitleSelect(PartnerRepository.PARTNER_TITLE_M);
    }
  }

  protected void setCompanyPartnerDetails(Partner partner, JSONObject jsonUniteLegal) {
    partner.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_COMPANY);
    safeSetString(
        partner::setName,
        partner::getName,
        getSafeString(jsonUniteLegal, "denominationUniteLegale"));
  }

  protected void setPartnerAddress(Partner partner, JSONObject jsonAddresseEtablissement) {
    if (jsonAddresseEtablissement == null) return;

    Address address = new Address();
    setAddressDetails(address, jsonAddresseEtablissement);

    if (isValidAddress(address)) {
      PartnerAddress partnerAddress = new PartnerAddress();
      partnerAddress.setPartner(partner);
      partnerAddress.setAddress(address);
      partner.addPartnerAddressListItem(partnerAddress);
    }
  }

  protected void setAddressDetails(Address address, JSONObject jsonAddress) {
    safeSetString(
        address::setZip, address::getZip, getSafeString(jsonAddress, "codePostalEtablissement"));
    safeSetString(
        address::setFloor,
        address::getFloor,
        getSafeString(jsonAddress, "complementAdresseEtablissement"));
    safeSetString(
        address::setPostBox,
        address::getPostBox,
        getSafeString(jsonAddress, "distributionSpecialeEtablissement"));
    safeSetString(
        address::setDepartment,
        address::getDepartment,
        getSafeString(jsonAddress, "enseigne1Etablissement"));

    Country currentCountry = countryRepository.findByName("FRANCE");
    if (currentCountry != null) {
      address.setCountry(currentCountry);
    }

    String cityName = getSafeString(jsonAddress, "libelleCommuneEtablissement");
    City currentCity = cityRepository.findByName(cityName);
    if (currentCity != null) {
      address.setCity(currentCity);
    }

    String streetName = getSafeString(jsonAddress, "libelleVoieEtablissement");
    safeSetString(address::setStreetName, address::getStreetName, streetName);
    safeSetString(
        address::setFullName,
        address::getFullName,
        streetName + " " + address.getZip() + " " + cityName);
  }

  protected boolean isValidAddress(Address address) {
    return address.getStreetName() != null && address.getCity() != null && address.getZip() != null;
  }

  private void safeSetString(Consumer<String> setter, Supplier<String> currentGetter, String newValue) {
    if (newValue != null && (currentGetter == null || currentGetter.get() == null)) {
      setter.accept(newValue);
    }
  }

  private void safeSetInteger(Consumer<Integer> setter, Supplier<Integer> currentGetter, Integer newValue) {
    if (newValue != null && (currentGetter == null || currentGetter.get() == null)) {
      setter.accept(newValue);
    }
  }

  private String getSafeString(JSONObject jsonObject, String key) {
    try {
      if (jsonObject.has(key) && !jsonObject.isNull(key)) {
        return jsonObject.getString(key);
      }
    } catch (JSONException e) {
      LOG.error(String.format("Error retrieving key %s : %s", key, e.getMessage()));
    }
    return null;
  }
}
