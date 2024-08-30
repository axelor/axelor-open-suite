package com.axelor.apps.base.service.address.validation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.fasterxml.jackson.databind.JsonNode;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressValidationNominatimService extends AddressValidationAbstractService {

  public static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public boolean validateAddress(Address address) throws AxelorException {
    String formattedFullName = address.getFormattedFullName();
    if (StringUtils.isEmpty(formattedFullName)) {
      return false;
    }
    String apiUrl = Beans.get(AppBaseService.class).getAppBase().getNominatimUrl();
    if (StringUtils.isEmpty(apiUrl)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          BaseExceptionMessage.ADDRESS_VALIDATION_NOMINATIM_URL_MISSING);
    }
    String queryUri =
        String.format(
            "%s?q=%s&format=jsonv2&addressdetails=1&limit=10",
            apiUrl, formattedFullName.replace("\n", "%20").replace(" ", "%20"));
    LOG.debug("query URI = {}", queryUri);
    JsonNode parentNode = getJsonNodeFromAPI(queryUri);
    if (parentNode == null) {
      return false;
    }
    for (JsonNode addressNode : parentNode) {
      if (addressNode == null) {
        return false;
      }
      JsonNode displayName = addressNode.get("display_name");
      if (displayName == null) {
        return false;
      }
      boolean result =
          checkIfAddressValid(
              displayName.textValue(), address.getFormattedFullName().replace("\n", " "));
      if (result) {
        return true;
      }
    }
    return false;
  }
}
