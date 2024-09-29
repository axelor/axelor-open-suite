package com.axelor.apps.base.service.address.validation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.fasterxml.jackson.databind.JsonNode;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressValidationDataGouvFrService extends AddressValidationAbstractService {

  public static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public boolean validateAddress(Address address) throws AxelorException {
    String formattedFullName = address.getFormattedFullName();
    if (StringUtils.isEmpty(formattedFullName)) {
      return false;
    }
    String apiUrl = Beans.get(AppBaseService.class).getAppBase().getDataGouvFrUrl();
    if (StringUtils.isEmpty(apiUrl)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(BaseExceptionMessage.ADDRESS_VALIDATION_DATA_GOUV_FR_URL_MISSING));
    }
    String queryUri =
        String.format("%s%s", apiUrl, formattedFullName.replace("\n", "%20").replace(" ", "%20"));
    LOG.debug("query URI = {}", queryUri);
    JsonNode parentNode = getJsonNodeFromAPI(queryUri);
    if (parentNode == null) {
      return false;
    }
    JsonNode featuresNodeArray = parentNode.get("features");
    if (featuresNodeArray == null || featuresNodeArray.isEmpty() || !featuresNodeArray.isArray()) {
      return false;
    }
    for (JsonNode featuresNode : featuresNodeArray) {
      JsonNode propertiesNode = featuresNode.get("properties");
      if (propertiesNode == null) {
        return false;
      }
      boolean result = isValidByDataGouvFr(propertiesNode, formattedFullName);
      if (result) {
        return true;
      }
    }
    // out of loop, no valid result
    return false;
  }

  protected boolean isValidByDataGouvFr(JsonNode propertiesNode, String addressToBeValidated) {
    if (addressToBeValidated == null) {
      return false;
    }
    JsonNode labelNode = propertiesNode.get("label");
    if (labelNode == null) {
      return false;
    }
    return checkIfAddressValid(
        labelNode.textValue() + " France", addressToBeValidated.replace("\n", " "));
  }
}
