package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressValidationServiceImpl implements AddressValidationService {

  public static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

  public static final String ADRESSE_DATA_GOUV_FR_URL = "https://api-adresse.data.gouv.fr/search/";

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public boolean validateAddressByAdresseDataGouvFr(Address address) throws AxelorException {
    String formattedFullName = address.getFormattedFullName();
    if (formattedFullName == null || formattedFullName.isEmpty()) {
      return false;
    }
    String queryUri =
        String.format(
            "%s?q=%s&limit=10",
            ADRESSE_DATA_GOUV_FR_URL, formattedFullName.replace("\n", "%20").replace(" ", "%20"));
    LOG.debug("query URI = {}", queryUri);
    JsonNode parentNode = getJsonNodeFromAPI(queryUri);
    JsonNode featuresNodeArray = parentNode.get("features");
    if (featuresNodeArray == null || !featuresNodeArray.isArray() || featuresNodeArray.isEmpty()) {
      return false;
    }
    for (JsonNode featuresNode : featuresNodeArray) {
      JsonNode propertiesNode = featuresNode.get("properties");
      boolean result = isValidByDataGouvFr(propertiesNode, formattedFullName);
      if (result) {
        return true;
      }
    }
    // out of loop, no valid result
    return false;
  }

  @Override
  public boolean validateAddressByNominatim(Address address) throws AxelorException {
    String queryUri =
        String.format(
            "%s?q=%s&format=jsonv2&addressdetails=1&limit=10",
            NOMINATIM_URL, address.getFormattedFullName().replace("\n", "%20").replace(" ", "%20"));
    LOG.debug("query URI = {}", queryUri);
    JsonNode parentNode = getJsonNodeFromAPI(queryUri);
    if (parentNode == null || !parentNode.isArray() || parentNode.isEmpty()) {
      return false;
    }
    /*
    TODO: We need a further data mapping process to determine if an address is valid. For now we only check if the api returns some results.
     */
    return !parentNode.isEmpty();
  }

  protected boolean isValidByDataGouvFr(JsonNode propertiesNode, String addressToBeValidated) {
    JsonNode housenumberNode = propertiesNode.get("housenumber");
    JsonNode streetNode = propertiesNode.get("street");
    JsonNode postcodeNode = propertiesNode.get("postcode");
    JsonNode cityNode = propertiesNode.get("city");
    /*
    TODO: For now, we check if the Address.formattedFullName contains the fields we get from json node.
          Problem: for some address, though it has a housenumber in the query, but the results from api don't have the housenumber (housenumberNode == null), for now the method returns true for this case.
     */
    return (housenumberNode == null
            || containsIgnoreCaseAndAccent(addressToBeValidated, housenumberNode.textValue()))
        && (streetNode == null
            || containsIgnoreCaseAndAccent(addressToBeValidated, streetNode.textValue()))
        && (postcodeNode == null
            || containsIgnoreCaseAndAccent(addressToBeValidated, postcodeNode.textValue()))
        && (cityNode == null
            || containsIgnoreCaseAndAccent(addressToBeValidated, cityNode.textValue()));
  }

  protected boolean containsIgnoreCaseAndAccent(String source, String target) {
    if (source == null || target == null) {
      return false;
    }
    return StringHelper.deleteAccent(source)
        .toLowerCase()
        .contains(StringHelper.deleteAccent(target).toLowerCase());
  }

  protected JsonNode getJsonNodeFromAPI(String queryUri) throws AxelorException {

    try {
      HttpClient client = HttpClient.newHttpClient();

      // Build the request
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(queryUri))
              .header("User-Agent", "Address Validation from API")
              .build();
      // Send the request and get the response
      HttpResponse<String> httpResponse =
          client.send(request, HttpResponse.BodyHandlers.ofString());

      // Parse the JSON response
      ObjectMapper objectMapper = new ObjectMapper();

      return objectMapper.readTree(httpResponse.body());
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Unable to get validation information from the API provider."));
    }
  }
}
