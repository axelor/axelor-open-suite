/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service;

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.AppBaseRepository;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.YearServiceImpl;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowanceRate;
import com.axelor.apps.hr.db.KilometricAllowanceRule;
import com.axelor.apps.hr.db.KilometricLog;
import com.axelor.apps.hr.db.repo.KilometricAllowanceRateRepository;
import com.axelor.apps.hr.db.repo.KilometricLogRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.client.utils.URIBuilder;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class KilometricService {

  private AppBaseService appBaseService;
  private KilometricLogRepository kilometricLogRepo;
  private MapService mapService;

  @Inject
  public KilometricService(
      AppBaseService appBaseService,
      KilometricLogRepository kilometricLogRepo,
      MapService mapService) {
    this.appBaseService = appBaseService;
    this.kilometricLogRepo = kilometricLogRepo;
    this.mapService = mapService;
  }

  public KilometricLog getKilometricLog(Employee employee, LocalDate refDate) {

    for (KilometricLog log : employee.getKilometricLogList()) {

      if (log.getYear().getFromDate().isBefore(refDate)
          && log.getYear().getToDate().isAfter(refDate)) {
        return log;
      }
    }
    return null;
  }

  public KilometricLog getCurrentKilometricLog(Employee employee) {
    return getKilometricLog(employee, appBaseService.getTodayDate());
  }

  public KilometricLog createKilometricLog(Employee employee, BigDecimal distance, Year year) {

    KilometricLog log = new KilometricLog();
    log.setEmployee(employee);
    log.setDistanceTravelled(distance);
    log.setYear(year);
    return log;
  }

  public KilometricLog getOrCreateKilometricLog(Employee employee, LocalDate date)
      throws AxelorException {

    KilometricLog log = getKilometricLog(employee, date);

    if (log != null) {
      return log;
    }
    if (employee.getMainEmploymentContract() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.EMPLOYEE_CONTRACT_OF_EMPLOYMENT),
          employee.getName());
    }

    Year year =
        Beans.get(YearServiceImpl.class)
            .getYear(date, employee.getMainEmploymentContract().getPayCompany());

    if (year == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.KILOMETRIC_LOG_NO_YEAR),
          employee.getMainEmploymentContract().getPayCompany(),
          date);
    }

    return createKilometricLog(employee, new BigDecimal("0.00"), year);
  }

  public BigDecimal computeKilometricExpense(ExpenseLine expenseLine, Employee employee)
      throws AxelorException {

    BigDecimal distance = expenseLine.getDistance();
    EmploymentContract mainEmploymentContract = employee.getMainEmploymentContract();
    if (mainEmploymentContract == null || mainEmploymentContract.getPayCompany() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.EMPLOYEE_CONTRACT_OF_EMPLOYMENT),
          employee.getName());
    }
    Company company = mainEmploymentContract.getPayCompany();

    KilometricLog log = getKilometricLog(employee, expenseLine.getExpenseDate());
    BigDecimal previousDistance = log == null ? BigDecimal.ZERO : log.getDistanceTravelled();

    KilometricAllowanceRate allowance =
        expenseLine.getKilometricAllowParam() != null
            ? Beans.get(KilometricAllowanceRateRepository.class)
                .all()
                .filter(
                    "self.kilometricAllowParam.id = :_kilometricAllowParamId "
                        + "and self.hrConfig.id = :_hrConfigId")
                .bind("_kilometricAllowParamId", expenseLine.getKilometricAllowParam().getId())
                .bind("_hrConfigId", Beans.get(HRConfigService.class).getHRConfig(company).getId())
                .fetchOne()
            : null;
    if (allowance == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.KILOMETRIC_ALLOWANCE_RATE_MISSING),
          expenseLine.getKilometricAllowParam() != null
              ? expenseLine.getKilometricAllowParam().getName()
              : "",
          company.getName());
    }

    List<KilometricAllowanceRule> ruleList = new ArrayList<>();
    List<KilometricAllowanceRule> allowanceRuleList = allowance.getKilometricAllowanceRuleList();
    if (ObjectUtils.notEmpty(allowanceRuleList)) {
      for (KilometricAllowanceRule rule : allowanceRuleList) {

        if (rule.getMinimumCondition().compareTo(previousDistance.add(distance)) <= 0
            && rule.getMaximumCondition().compareTo(previousDistance) >= 0) {
          ruleList.add(rule);
        }
      }
    }

    if (ruleList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.KILOMETRIC_ALLOWANCE_NO_RULE),
          allowance.getKilometricAllowParam().getName());
    }

    BigDecimal price = BigDecimal.ZERO;

    if (ruleList.size() == 1) {
      price = distance.multiply(ruleList.get(0).getRate());
    } else {
      Collections.sort(
          ruleList,
          (object1, object2) ->
              object1.getMinimumCondition().compareTo(object2.getMinimumCondition()));
      for (KilometricAllowanceRule rule : ruleList) {
        BigDecimal min = rule.getMinimumCondition().max(previousDistance);
        BigDecimal max = rule.getMaximumCondition().min(previousDistance.add(distance));
        price = price.add(max.subtract(min).multiply(rule.getRate()));
      }
    }

    return price.setScale(2, RoundingMode.HALF_UP);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateKilometricLog(ExpenseLine expenseLine, Employee employee)
      throws AxelorException {

    KilometricLog log = getOrCreateKilometricLog(employee, expenseLine.getExpenseDate());
    log.setDistanceTravelled(log.getDistanceTravelled().add(expenseLine.getDistance()));
    if (log.getExpenseLineList() == null || !log.getExpenseLineList().contains(expenseLine)) {
      log.addExpenseLineListItem(expenseLine);
    }
    kilometricLogRepo.save(log);
  }

  public BigDecimal computeDistance(ExpenseLine expenseLine) throws AxelorException {
    return computeDistance(expenseLine.getFromCity(), expenseLine.getToCity());
  }

  /**
   * Compute the distance between two cities.
   *
   * @param fromCity
   * @param toCity
   * @return
   * @throws AxelorException
   */
  private BigDecimal computeDistance(String fromCity, String toCity) throws AxelorException {
    BigDecimal distance = BigDecimal.ZERO;
    AppBase appBase = appBaseService.getAppBase();
    try {
      switch (appBase.getMapApiSelect()) {
        case AppBaseRepository.MAP_API_GOOGLE:
          distance = this.getDistanceUsingGoogle(fromCity, toCity);
          break;

        case AppBaseRepository.MAP_API_OPEN_STREET_MAP:
          distance = this.getDistanceUsingOSRM(fromCity, toCity);
          break;
      }
      return distance;
    } catch (URISyntaxException | IOException | JSONException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  protected BigDecimal getDistanceUsingGoogle(String fromCity, String toCity)
      throws JSONException, AxelorException, URISyntaxException, IOException {
    User user = AuthUtils.getUser();
    JSONObject json = getGoogleMapsDistanceMatrixResponse(fromCity, toCity, user.getLanguage());
    String status = json.getString("status");

    if (status.equals("OK")) {
      JSONObject response =
          json.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0);
      status = response.getString("status");
      if (status.equals("OK")) {
        return BigDecimal.valueOf(response.getJSONObject("distance").getDouble("value") / 1000.);
      }
    }

    String msg =
        json.has("error_message")
            ? String.format("%s / %s", status, json.getString("error_message"))
            : status;

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        IExceptionMessage.KILOMETRIC_ALLOWANCE_GOOGLE_MAPS_ERROR,
        msg);
  }

  protected BigDecimal getDistanceUsingOSRM(String fromCity, String toCity)
      throws JSONException, AxelorException, URISyntaxException, IOException {
    JSONObject json = getOsmRouteServiceResponse(fromCity, toCity);
    String status = json.getString("code");

    if (status.equals("Ok")) {
      return BigDecimal.valueOf(
          json.getJSONArray("routes").getJSONObject(0).getDouble("distance") / 1000);
    }

    String msg = json.has("message") ? String.format("%s", json.getString("message")) : status;

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        IExceptionMessage.KILOMETRIC_ALLOWANCE_OSM_ERROR,
        msg);
  }

  /**
   * Get JSON response from Google Maps Distance Matrix API.
   *
   * @param origins
   * @param destinations
   * @param language
   * @return
   * @throws URISyntaxException
   * @throws IOException
   * @throws JSONException
   * @throws AxelorException
   */
  private JSONObject getGoogleMapsDistanceMatrixResponse(
      String origins, String destinations, String language)
      throws URISyntaxException, IOException, JSONException, AxelorException {

    URIBuilder ub = new URIBuilder("https://maps.googleapis.com/maps/api/distancematrix/json");
    ub.addParameter("origins", origins);
    ub.addParameter("destinations", destinations);
    ub.addParameter("language", language);
    ub.addParameter("key", mapService.getGoogleMapsApiKey());

    return this.getApiResponse(
        ub.toString(), IExceptionMessage.KILOMETRIC_ALLOWANCE_GOOGLE_MAPS_ERROR);
  }

  /**
   * Get JSON response from Open Street Route Machine API.
   *
   * @param origins
   * @param destinations
   * @return
   * @throws AxelorException
   * @throws JSONException
   * @throws URISyntaxException
   * @throws IOException
   */
  protected JSONObject getOsmRouteServiceResponse(String origins, String destinations)
      throws AxelorException, JSONException, URISyntaxException, IOException {
    Map<String, Object> originMap = mapService.getMap(origins);
    String originCoordinates = originMap.get("longitude") + "," + originMap.get("latitude");
    Map<String, Object> destinationMap = mapService.getMap(destinations);
    String destinationCoordinates =
        destinationMap.get("longitude") + "," + destinationMap.get("latitude");
    String uri =
        String.format(
            "https://router.project-osrm.org/route/v1/driving/%s;%s",
            originCoordinates, destinationCoordinates);

    return this.getApiResponse(uri, IExceptionMessage.KILOMETRIC_ALLOWANCE_OSM_ERROR);
  }

  protected JSONObject getApiResponse(String urlString, String exceptionMessage)
      throws IOException, JSONException, AxelorException {

    URL url = new URL(urlString);
    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
    if (connection.getResponseCode() == 429) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          exceptionMessage,
          ITranslation.REQUEST_OVERFLOW);
    }

    StringBuilder sb = new StringBuilder();
    try (BufferedReader in =
        new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        sb.append(inputLine + "\n");
      }
    }
    return new JSONObject(sb.toString());
  }
}
