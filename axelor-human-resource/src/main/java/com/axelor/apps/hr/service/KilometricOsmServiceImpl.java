package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.MapOsmService;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.translation.ITranslation;
import jakarta.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class KilometricOsmServiceImpl implements KilometricOsmService {

  protected final KilometricResponseToolService kilometricResponseToolService;
  protected final MapOsmService mapOsmService;

  @Inject
  public KilometricOsmServiceImpl(
      KilometricResponseToolService kilometricResponseToolService, MapOsmService mapOsmService) {
    this.kilometricResponseToolService = kilometricResponseToolService;
    this.mapOsmService = mapOsmService;
  }

  @Override
  public BigDecimal getDistanceUsingOSRMApi(String fromCity, String toCity)
      throws AxelorException, IOException {
    Map<String, Object> json = getOSRMApiResponse(fromCity, toCity);
    String status = json.get("code").toString();

    if (status.equals("Ok")) {
      List<Map<String, Object>> routes = (List<Map<String, Object>>) json.get("routes");
      double distance = (Double) routes.getFirst().get("distance");
      return BigDecimal.valueOf(distance / 1000);
    }

    String msg = "";
    if (json.get("message") != null) {
      msg = json.get("message").toString();
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_OSM_ERROR,
        msg);
  }

  /**
   * Get JSON response from Open Street Route Machine API.
   *
   * @param origins
   * @param destinations
   * @return
   * @throws AxelorException
   * @throws URISyntaxException
   * @throws IOException
   */
  protected Map<String, Object> getOSRMApiResponse(String origins, String destinations)
      throws AxelorException, IOException {

    Map<String, Object> originMap = this.getLocationMap(origins);
    Map<String, Object> destinationMap = this.getLocationMap(destinations);

    String originCoordinates = originMap.get("longitude") + "," + originMap.get("latitude");
    String destinationCoordinates =
        destinationMap.get("longitude") + "," + destinationMap.get("latitude");
    String uri =
        String.format(
            "https://router.project-osrm.org/route/v1/driving/%s;%s",
            originCoordinates, destinationCoordinates);

    return kilometricResponseToolService.getApiResponse(
        uri, HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_OSM_ERROR);
  }

  protected Map<String, Object> getLocationMap(String location) throws AxelorException {
    Map<String, Object> locationMap;
    try {
      locationMap = mapOsmService.getMapOsm(location);
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_OSM_ERROR,
          e.getMessage());
    }

    if (locationMap == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_OSM_ERROR,
          ITranslation.NO_SUCH_PLACE);
    }
    return locationMap;
  }
}
