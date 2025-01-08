package com.axelor.apps.base.service.apiconfiguration;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.ApiConfiguration;
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
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.eclipse.birt.report.model.api.util.StringUtil;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ApiConfigurationServiceImpl implements ApiConfigurationService {

  protected final PartnerRepository partnerRepository;
  protected final PartnerCategoryRepository partnerCategoryRepository;
  protected final CountryRepository countryRepository;
  protected final CityRepository cityRepository;

  @Inject
  public ApiConfigurationServiceImpl(
          PartnerRepository partnerRepository,
          PartnerCategoryRepository partnerCategoryRepository, CountryRepository countryRepository,
          CityRepository cityRepository) {
    this.partnerRepository = partnerRepository;
    this.partnerCategoryRepository = partnerCategoryRepository;
    this.countryRepository = countryRepository;
    this.cityRepository = cityRepository;
  }

  @Transactional
  @Override
  public void setData(Partner partner, JSONObject resutlJson) throws JSONException {
    String registrationCode = getSafeString(resutlJson, "siret");
    safeSet(partner::setRegistrationCode, registrationCode);
    JSONObject jsonUniteLegal = resutlJson.optJSONObject("uniteLegale");

    if (jsonUniteLegal != null) {
      String partnerCategoryCode = getSafeString(jsonUniteLegal, "categorieEntreprise");
      PartnerCategory partnerCategory = partnerCategoryRepository.findByCode(partnerCategoryCode);
      if(partnerCategory != null){
        partner.setPartnerCategory(partnerCategory);
      }
      String categorieJuridique = getSafeString(jsonUniteLegal, "categorieJuridiqueUniteLegale");
      if (categorieJuridique != null && Integer.parseInt(categorieJuridique) == 1000) {
        partner.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_INDIVIDUAL);
        String name = getSafeString(jsonUniteLegal, "nomUniteLegale");
        safeSet(partner::setName, name);
        String firstName = getSafeString(jsonUniteLegal, "prenom1UniteLegale");
        safeSet(partner::setFirstName, firstName);
        String sexUniteLegale = getSafeString(jsonUniteLegal, "sexUniteLegale");
        if(Objects.equals(sexUniteLegale, "F")){
          partner.setTitleSelect(PartnerRepository.PARTNER_TITLE_MS);
        } else if (Objects.equals(sexUniteLegale, "M")) {
          partner.setTitleSelect(PartnerRepository.PARTNER_TITLE_M);
        }
      } else {
        partner.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_COMPANY);
        String name = getSafeString(jsonUniteLegal, "denominationUniteLegale");
        safeSet(partner::setName, name);
      }
    }

    JSONObject jsonAddresseEtablissement = resutlJson.optJSONObject("adresseEtablissement");
    if (jsonAddresseEtablissement != null) {
      Address address = new Address();
      String zip = getSafeString(jsonAddresseEtablissement, "codePostalEtablissement");
      safeSet(address::setZip, zip);
      String floor = getSafeString(jsonAddresseEtablissement, "complementAdresseEtablissement");
      safeSet(address::setFloor, floor);
      String postBox =
          getSafeString(jsonAddresseEtablissement, "distributionSpecialeEtablissement");
      safeSet(address::setPostBox, postBox);
      String departement = getSafeString(jsonAddresseEtablissement, "enseigne1Etablissement");
      safeSet(address::setDepartment, departement);
      Country currentCountry = countryRepository.findByName("FRANCE");
      if (currentCountry != null) {
        address.setCountry(currentCountry);
      }
      String cityName = getSafeString(jsonAddresseEtablissement, "libelleCommuneEtablissement");
      City currentCity = cityRepository.findByName(cityName);
      if (currentCity != null) {
        address.setCity(currentCity);
      }
      String streetName = getSafeString(jsonAddresseEtablissement, "libelleVoieEtablissement");
      safeSet(address::setStreetName, streetName);
      safeSet(address::setFullName, streetName + zip + cityName);
      PartnerAddress partnerAddress = new PartnerAddress();
      if(address.getStreetName() != null && address.getCity() != null && address.getZip() != null){
        partnerAddress.setPartner(partner);
        partnerAddress.setAddress(address);
      }
      if(partnerAddress.getAddress() != null){
        partner.addPartnerAddressListItem(partnerAddress);
      }
    }
    partnerRepository.save(partner);
  }

  private void safeSet(Consumer<String> setter, String value) {
    if (value != null) {
      setter.accept(value);
    }
  }

  private String getSafeString(JSONObject jsonObject, String key) {
    try {
      if (jsonObject.has(key) && !jsonObject.isNull(key)) {
        return jsonObject.getString(key);
      }
    } catch (JSONException e) {
      System.err.println("Error retrieving key '" + key + "': " + e.getMessage());
    }
    return null;
  }

  @Override
  public String fetchData(ApiConfiguration apiConfiguration, String siretNumber)
      throws AxelorException {
    if (apiConfiguration == null || StringUtils.isEmpty(siretNumber)) {
      return StringUtil.EMPTY_STRING;
    }
    return getData(apiConfiguration, siretNumber);
  }

  public String getData(ApiConfiguration apiConfiguration, String siretNumber)
      throws AxelorException {
    try {

      HttpClient client = HttpClient.newBuilder().build();

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(new URI(getUrl(apiConfiguration, siretNumber)))
              .headers(
                  HttpHeaders.AUTHORIZATION,
                  "Bearer " + apiConfiguration.getApiKey(),
                  HttpHeaders.ACCEPT,
                  MediaType.APPLICATION_JSON)
              .GET()
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      int statusCode = response.statusCode();
      if (statusCode != 200) {
        return "Cannot get information with siret: " + siretNumber + ".";
      }
      return new JSONObject(response.body()).get("etablissement").toString();
    } catch (URISyntaxException | IOException | JSONException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_NO_VALUE);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
    }
  }

  private String getUrl(ApiConfiguration apiConfiguration, String siretNumber) {
    return apiConfiguration.getApiUrl() + "/siret/" + siretNumber;
  }
}
