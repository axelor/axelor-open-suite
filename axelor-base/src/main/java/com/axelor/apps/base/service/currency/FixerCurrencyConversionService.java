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
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import java.io.IOException;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixerCurrencyConversionService extends CurrencyConversionService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String FIXER_URL =
      "http://data.fixer.io/api/%s?access_key=%s&base=%s&symbols=%s";
  private final String key = appBaseService.getAppBase().getFixerApiKey();
  private static final String EURO_CURRENCY_CODE = "EUR";
  private boolean isRelatedConversion = false;

  @Inject
  public FixerCurrencyConversionService(
      AppBaseService appBaseService, CurrencyConversionLineRepository cclRepo) {
    super(appBaseService, cclRepo);
  }

  @Override
  public void updateCurrencyConverion() throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    try {
      HttpResponse<String> response = null;
      LocalDate today =
          appBaseService.getTodayDate(
              Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));

      response = callApiBaseEuru(null, null, today);
      if (response.statusCode() != 200) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.CURRENCY_6));
      }

      List<CurrencyConversionLine> currencyConversionLines =
          appBase.getCurrencyConversionLineList();
      currencyConversionLines =
          currencyConversionLines.stream()
              .filter(it -> !it.getFromDate().isAfter(today) && it.getToDate() == null)
              .collect(Collectors.toList());

      for (CurrencyConversionLine ccline : currencyConversionLines) {
        BigDecimal currentRate = BigDecimal.ZERO;
        Float rate = 0.0f;

        if (!ccline.getStartCurrency().getCodeISO().equals(EURO_CURRENCY_CODE)
            && !ccline.getEndCurrency().getCodeISO().equals(EURO_CURRENCY_CODE)) {

          isRelatedConversion = true;
          rate = this.getRateFromJson(ccline.getStartCurrency(), ccline.getEndCurrency(), response);
        } else if (ccline.getStartCurrency().getCodeISO().equals(EURO_CURRENCY_CODE)) {
          rate = this.getRateFromJson(ccline.getStartCurrency(), ccline.getEndCurrency(), response);
        } else {
          rate = this.getRateFromJson(ccline.getEndCurrency(), ccline.getStartCurrency(), response);
          rate = 1.0f / rate;
        }

        currentRate =
            BigDecimal.valueOf(rate)
                .setScale(AppBaseService.DEFAULT_EXCHANGE_RATE_SCALE, RoundingMode.HALF_UP);

        if (currentRate.compareTo(new BigDecimal(-1)) == 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BaseExceptionMessage.CURRENCY_6));
        }

        ccline = cclRepo.find(ccline.getId());
        BigDecimal previousRate = ccline.getExchangeRate();
        if (currentRate.compareTo(previousRate) != 0 && today.isAfter(ccline.getFromDate())) {
          ccline.setToDate(
              !today.minusDays(1).isBefore(ccline.getFromDate()) ? today.minusDays(1) : today);
          this.saveCurrencyConversionLine(ccline);
          String variations = this.getVariations(currentRate, previousRate);
          this.createCurrencyConversionLine(
              ccline.getStartCurrency(),
              ccline.getEndCurrency(),
              today,
              currentRate,
              appBase,
              variations);
        }
      }

    } catch (Exception e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
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
    BigDecimal rate = new BigDecimal(-1);

    LocalDate todayDate =
        appBaseService.getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));

    LOG.trace("Currerncy conversion From: {} To: {}", currencyFrom, currencyTo);
    LocalDate date =
        appBaseService.getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
    Boolean isFixerApiPaid = appBaseService.getAppBase().getIsFixerAPIPaid();
    if (currencyFrom != null && currencyTo != null) {

      if (!isFixerApiPaid
          && !currencyFrom.getCodeISO().equals(EURO_CURRENCY_CODE)
          && !currencyTo.getCodeISO().equals(EURO_CURRENCY_CODE)) {
        isRelatedConversion = true;
      }

      Pair<LocalDate, Float> pair =
          this.validateAndGetRateWithDate(1, currencyFrom, currencyTo, todayDate);
      Float rt = pair.getRight();
      if (rt == null) {
        rt = 1.0f / this.validateAndGetRate(1, currencyTo, currencyFrom, date); // reverse
      }
      rate =
          BigDecimal.valueOf(rt)
              .setScale(AppBaseService.DEFAULT_EXCHANGE_RATE_SCALE, RoundingMode.HALF_UP);
      LOG.trace("Currerncy conversion rate: {}", rate);
      return Pair.of(pair.getLeft(), rate);
    } else LOG.trace("Currency from and to must be filled to get rate");
    return Pair.of(todayDate, rate);
  }

  @Override
  public Float validateAndGetRate(
      int dayCount, Currency currencyFrom, Currency currencyTo, LocalDate date)
      throws AxelorException {
    return this.validateAndGetRateWithDate(dayCount, currencyFrom, currencyTo, date).getRight();
  }

  @Override
  public Pair<LocalDate, Float> validateAndGetRateWithDate(
      int dayCount, Currency currencyFrom, Currency currencyTo, LocalDate date)
      throws AxelorException {
    HttpResponse<String> response = null;

    if (dayCount < 8) {
      try {
        response = callApiBaseEuru(currencyFrom, currencyTo, date);
        LOG.trace("Webservice response code: {}, response message: {}", response.statusCode() /*,
            response.getStatusMessage()*/);
        if (response.statusCode() != 200) return Pair.of(date, -1f);

        if (response.body().isEmpty()) {
          return this.validateAndGetRateWithDate(
              (dayCount + 1), currencyFrom, currencyTo, date.minus(Period.ofDays(1)));
        } else {
          Float rate = this.getRateFromJson(currencyFrom, currencyTo, response);
          return Pair.of(date, rate);
        }
      } catch (Exception e) {
        throw new AxelorException(
            e.getCause(), TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
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
    String jsonString = response.body();
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      JsonNode jsonResult = objectMapper.readTree(jsonString);

      if (jsonResult.has("rates")) {
        JsonNode ratesNode = jsonResult.get("rates");

        if (isRelatedConversion) {
          String fromRate = ratesNode.path(currencyFrom.getCodeISO()).asText(null);
          String toRate = ratesNode.path(currencyTo.getCodeISO()).asText(null);

          if (StringUtils.isEmpty(toRate)) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(BaseExceptionMessage.CURRENCY_7),
                currencyFrom.getName(),
                currencyTo.getName());
          }
          isRelatedConversion = false;
          return Float.parseFloat(toRate) / Float.parseFloat(fromRate);
        } else {
          String rate = ratesNode.path(currencyTo.getCodeISO()).asText();
          return Float.parseFloat(rate);
        }
      } else if (jsonResult.has("error") && currencyTo.getCodeISO().equals(EURO_CURRENCY_CODE)) {
        JsonNode errorNode = jsonResult.get("error");
        int code = errorNode.path("code").asInt();
        if (code == 105 || code == 201) {
          return null;
        }
      } else if (jsonResult.has("error") && jsonResult.get("error").path("code").asInt() == 202) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.CURRENCY_7),
            currencyFrom.getName(),
            currencyTo.getName());
      } else if (jsonResult.has("error")) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            jsonResult.get("error").path("info").asText());
      }
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, jsonString);

    } catch (Exception e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
    }
  }

  @Override
  public String getUrlString(LocalDate date, String currencyFromCode, String currencyToCode) {
    String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    return String.format(FIXER_URL, dateStr, key, currencyFromCode, currencyToCode);
  }

  protected String getUrlString(LocalDate date, String currencyFromCode, String... currencyToCode) {
    String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String url = "";
    if (currencyToCode != null && currencyToCode.length > 0) {
      String toCurrencies = String.join(",", currencyToCode);
      url = String.format(FIXER_URL, dateStr, key, currencyFromCode, toCurrencies);
    } else {
      url = String.format(FIXER_URL, dateStr, key, currencyFromCode, "");
    }

    return url;
  }

  protected HttpResponse<String> callApiBaseEuru(
      Currency currencyFrom, Currency currencyTo, LocalDate date)
      throws IOException, InterruptedException {

    String url;
    if (currencyFrom != null && currencyTo != null) {
      if (isRelatedConversion) {
        url =
            this.getUrlString(
                date, EURO_CURRENCY_CODE, currencyFrom.getCodeISO(), currencyTo.getCodeISO());
      } else {
        url = this.getUrlString(date, currencyFrom.getCodeISO(), currencyTo.getCodeISO());
      }
    } else {
      url = this.getUrlString(date, EURO_CURRENCY_CODE);
    }
    LOG.trace("Currency conversion webservice URL: {}", new Object[] {url.toString()});
    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(5000)).build();

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .GET()
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }
}
