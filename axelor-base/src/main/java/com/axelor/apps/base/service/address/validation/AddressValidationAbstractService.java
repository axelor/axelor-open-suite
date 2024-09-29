package com.axelor.apps.base.service.address.validation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class AddressValidationAbstractService {
  abstract boolean validateAddress(Address address) throws AxelorException;

  public JsonNode getJsonNodeFromAPI(String queryUri) throws AxelorException {

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

  /**
   * @param addressFoundByApi from data.gouv.fr, it is the "label". from nominatim, it is the
   *     "display_name"
   * @param addressToBeValidated
   * @return
   */
  public boolean checkIfAddressValid(String addressFoundByApi, String addressToBeValidated) {
    String[] parts = addressToBeValidated.split(" ");
    for (String part : parts) {
      if (!containsIgnoreCaseAndAccent(addressFoundByApi, part)) {
        return false;
      }
    }
    return true;
  }

  public boolean containsIgnoreCaseAndAccent(String source, String target) {
    if (source == null || target == null) {
      return false;
    }
    return StringHelper.deleteAccent(source)
        .toLowerCase()
        .contains(StringHelper.deleteAccent(target).toLowerCase());
  }
}
