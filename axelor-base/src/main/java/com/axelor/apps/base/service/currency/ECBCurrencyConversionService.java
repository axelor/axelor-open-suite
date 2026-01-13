/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.currency;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ECBCurrencyConversionService extends CurrencyConversionService {

  @Inject
  public ECBCurrencyConversionService(
      AppBaseService appBaseService, CurrencyConversionLineRepository cclRepo) {
    super(appBaseService, cclRepo);
  }

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final String WSURL =
      "https://data-api.ecb.europa.eu/service/data/EXR/D.%s+%s.EUR.SP00.A?startPeriod=%s&endPeriod=%s";

  @Override
  public void updateCurrencyConverion() throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    LocalDate today =
        appBaseService.getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));

    Map<Long, Set<Long>> currencyMap = new HashMap<Long, Set<Long>>();

    for (CurrencyConversionLine ccl : appBase.getCurrencyConversionLineList()) {
      if (currencyMap.containsKey(ccl.getEndCurrency().getId())) {
        currencyMap.get(ccl.getEndCurrency().getId()).add(ccl.getStartCurrency().getId());

      } else {
        Set<Long> startCurrencyIds = new HashSet<>();
        startCurrencyIds.add(ccl.getStartCurrency().getId());
        currencyMap.put(ccl.getEndCurrency().getId(), startCurrencyIds);
      }
    }

    for (Long key : currencyMap.keySet()) {
      List<CurrencyConversionLine> cclList =
          cclRepo
              .all()
              .filter(
                  "startCurrency.id IN (?1) AND endCurrency.id = ?2 AND fromDate <= ?3 AND toDate is null",
                  currencyMap.get(key),
                  key,
                  today)
              .fetch();

      for (CurrencyConversionLine ccl : cclList) {
        LOG.trace("Currency Conversion Line without toDate : {}", ccl);
        BigDecimal currentRate = BigDecimal.ZERO;
        LocalDate rateRetrieveDate = null;
        try {
          Pair<LocalDate, BigDecimal> pair =
              this.getRateWithDate(ccl.getStartCurrency(), ccl.getEndCurrency());
          currentRate = pair.getRight();
          rateRetrieveDate = pair.getLeft();
        } catch (Exception e) {
          throw new AxelorException(
              e.getCause(),
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              e.getLocalizedMessage());
        }
        if (currentRate.compareTo(new BigDecimal(-1)) == 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BaseExceptionMessage.CURRENCY_6));
        }

        ccl = cclRepo.find(ccl.getId());
        BigDecimal previousRate = ccl.getExchangeRate();
        if (currentRate.compareTo(previousRate) != 0
            && rateRetrieveDate.isAfter(ccl.getFromDate())) {
          ccl.setToDate(
              !rateRetrieveDate.minusDays(1).isBefore(ccl.getFromDate())
                  ? rateRetrieveDate.minusDays(1)
                  : rateRetrieveDate);

          this.saveCurrencyConversionLine(ccl);
          String variations = this.getVariations(currentRate, previousRate);
          this.createCurrencyConversionLine(
              ccl.getStartCurrency(),
              ccl.getEndCurrency(),
              rateRetrieveDate,
              currentRate,
              appBase,
              variations);
        }
      }
    }
  }

  @Override
  public BigDecimal convert(Currency currencyFrom, Currency currencyTo)
      throws MalformedURLException, AxelorException {
    return this.getRateWithDate(currencyFrom, currencyTo).getRight();
  }

  @Override
  public Pair<LocalDate, BigDecimal> getRateWithDate(Currency currencyFrom, Currency currencyTo)
      throws MalformedURLException, AxelorException {

    LocalDate todayDate =
        appBaseService.getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));

    BigDecimal rate = new BigDecimal(-1);
    LOG.trace("Currerncy conversion From: {} To: {}", new Object[] {currencyFrom, currencyTo});

    if (currencyFrom != null && currencyTo != null) {
      Pair<LocalDate, Float> pair =
          this.validateAndGetRateWithDate(1, currencyFrom, currencyTo, todayDate);
      rate =
          BigDecimal.valueOf(pair.getRight())
              .setScale(AppBaseService.DEFAULT_EXCHANGE_RATE_SCALE, RoundingMode.HALF_UP);
      LOG.trace("Currerncy conversion rate: {}", new Object[] {rate});
      return Pair.of(pair.getLeft(), rate);
    } else LOG.trace("Currency from and to must be filled to get rate");
    return Pair.of(todayDate, rate);
  }

  @Override
  public Float validateAndGetRate(
      int dayCount, Currency currencyFrom, Currency currencyTo, LocalDate date)
      throws AxelorException {

    return validateAndGetRateWithDate(dayCount, currencyFrom, currencyTo, date).getRight();
  }

  @Override
  public Pair<LocalDate, Float> validateAndGetRateWithDate(
      int dayCount, Currency currencyFrom, Currency currencyTo, LocalDate date)
      throws AxelorException {
    HttpResponse<String> response = null;
    if (dayCount < 8) {
      try {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(5000)).build();

        HttpRequest request =
            HttpRequest.newBuilder()
                .uri(
                    URI.create(
                        this.getUrlString(
                            date, currencyFrom.getCodeISO(), currencyTo.getCodeISO())))
                .header("Accept", "application/json")
                .GET()
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // JSONObject json = new JSONObject(response.getContentAsString());
        LOG.trace(
            "Webservice response code: {}, response message: {}", response.statusCode()
            /*response.getStatusMessage()*/ );
        if (response.statusCode() != 200) return Pair.of(date, -1f);

      } catch (Exception e) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.CURRENCY_7),
            currencyFrom.getName(),
            currencyTo.getName());
      }
      if (response.body().isEmpty()) {
        return this.validateAndGetRateWithDate(
            (dayCount + 1), currencyFrom, currencyTo, date.minus(Period.ofDays(1)));
      } else {
        Float rate = this.getRateFromJson(currencyFrom, currencyTo, response);
        return Pair.of(date, rate);
      }
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.CURRENCY_7),
          currencyFrom.getName(),
          currencyTo.getName());
    }
  }

  @Override
  public Float getRateFromJson(
      Currency currencyFrom, Currency currencyTo, HttpResponse<String> response)
      throws AxelorException {

    try {

      int compareCode = currencyFrom.getCodeISO().compareTo(currencyTo.getCodeISO());

      String jsonString = response.body();
      ObjectMapper objectMapper = new ObjectMapper();

      JsonNode jsonResult = objectMapper.readTree(jsonString);
      JsonNode dataSets = jsonResult.get("dataSets");
      JsonNode firstDataSet = dataSets.get(0);
      JsonNode series = firstDataSet.get("series");

      float rt = 0.0f;
      String[] currencyRateArr = new String[series.size()];

      if (series.size() > 1) {
        for (int i = 0; i < series.size(); i++) {
          JsonNode seriesOf = series.get("0:" + i + ":0:0:0");
          JsonNode observations = seriesOf.get("observations");
          String index = String.valueOf(observations.size() - 1);
          JsonNode lastObservation = observations.get(index);
          currencyRateArr[i] = lastObservation.get(0).asText();
        }
        if (compareCode > 0) {
          rt = Float.parseFloat(currencyRateArr[0]) / Float.parseFloat(currencyRateArr[1]);
        } else {
          rt = Float.parseFloat(currencyRateArr[1]) / Float.parseFloat(currencyRateArr[0]);
        }
      } else {
        JsonNode seriesOf = series.get("0:0:0:0:0");
        JsonNode observations = seriesOf.get("observations");
        String index = String.valueOf(observations.size() - 1);
        JsonNode lastObservation = observations.get(index);

        if (currencyTo.getCodeISO().equals("EUR")) {
          rt = 1.0f / Float.parseFloat(lastObservation.get(0).asText());
        } else {
          rt = Float.parseFloat(lastObservation.get(0).asText());
        }
      }

      return rt;
    } catch (JsonProcessingException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
    }
  }

  @Override
  public String getUrlString(LocalDate date, String currencyFromCode, String currencyToCode) {
    return String.format(WSURL, currencyFromCode, currencyToCode, date, date);
  }
}
