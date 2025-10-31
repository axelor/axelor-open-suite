package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.translation.ITranslation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class KilometricResponseToolServiceImpl implements KilometricResponseToolService {

  @Override
  public Map<String, Object> getApiResponse(String urlString, String exceptionMessage)
      throws IOException, AxelorException {

    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    int responseCode = connection.getResponseCode();

    this.checkResponseStatus(responseCode, exceptionMessage);

    StringBuilder sb = new StringBuilder();
    try (BufferedReader in =
        new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        sb.append(inputLine + "\n");
      }
    }

    String response = sb.toString();
    Map<String, Object> map;
    // throw exception if response is not json
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      map = objectMapper.readValue(response, new TypeReference<>() {});
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, exceptionMessage, response);
    }

    return map;
  }

  protected void checkResponseStatus(int responseCode, String exceptionMessage)
      throws AxelorException {
    if (responseCode == 200) {
      return;
    } else if (responseCode == 429) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          exceptionMessage,
          ITranslation.REQUEST_OVERFLOW);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          exceptionMessage,
          "Server returned status code " + responseCode);
    }
  }
}
