/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.FakerApiField;
import com.axelor.apps.base.exceptions.IExceptionMessages;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class AnonymizeServiceImpl implements AnonymizeService {

  protected FakerService fakerService;
  protected MetaJsonFieldRepository metaJsonFieldRepository;
  protected Logger LOG = LoggerFactory.getLogger(getClass());

  @Inject
  public AnonymizeServiceImpl(
      FakerService fakerService, MetaJsonFieldRepository metaJsonFieldRepository) {
    this.fakerService = fakerService;
    this.metaJsonFieldRepository = metaJsonFieldRepository;
  }

  @Override
  public Object anonymizeValue(Object object, Property property) throws AxelorException {
    if (property.isJson()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessages.JSON_FIELD_CAN_NOT_BE_ANONYMIZED));
    }
    if (property.getMaxSize() != null && (int) property.getMaxSize() > 0) {
      return anonymize(object, property.getType().toString(), (int) property.getMaxSize());
    } else {
      return anonymize(object, property.getType().toString(), 0);
    }
  }

  @Override
  public Object anonymizeValue(Object object, Property property, FakerApiField fakerApiField)
      throws AxelorException {

    if (fakerApiField != null) {
      return fakerService.generateFakeData(fakerApiField);
    }

    return anonymizeValue(object, property);
  }

  @Override
  public Object anonymizeJsonValue(Object object, Property property, MetaJsonField metaJsonField)
      throws JSONException {
    JSONObject jsonObject = new JSONObject(object.toString());
    for (Object field : jsonObject.keySet()) {
      if (metaJsonField.getName().equals(field)) {
        return anonymize(
            jsonObject.get(field.toString()), metaJsonField.getType(), metaJsonField.getMaxSize());
      }
    }
    return null;
  }

  @Override
  public Object anonymizeJsonValue(
      Object object, Property property, MetaJsonField metaJsonField, FakerApiField fakerApiField)
      throws JSONException, AxelorException {
    if (fakerApiField != null) {
      return fakerService.generateFakeData(fakerApiField);
    }
    return anonymizeJsonValue(object, property, metaJsonField);
  }

  @Override
  public JSONObject createAnonymizedJson(Object object, List<MetaJsonField> metaJsonFieldToAnoList)
      throws AxelorException {
    List<String> metaJsonFieldsNameToAnonymize = new ArrayList<>();
    JSONObject anonymizedJson = new JSONObject();
    for (MetaJsonField metaJsonField : metaJsonFieldToAnoList) {
      metaJsonFieldsNameToAnonymize.add(metaJsonField.getName());
    }
    try {
      JSONObject jsonObject = new JSONObject(object.toString());
      for (Object field : jsonObject.keySet()) {
        if (metaJsonFieldsNameToAnonymize.contains(field.toString())) {
          MetaJsonField metaJsonField = null;
          for (MetaJsonField metaJsonField1 : metaJsonFieldToAnoList) {
            if (metaJsonField1.getName().equals(field.toString())) {
              metaJsonField = metaJsonField1;
            }
          }
          // OR MetaJsonField metaJsonField = metaJsonFieldRepository.findByName(field.toString());

          anonymizedJson.put(
              field.toString(),
              anonymize(
                  jsonObject.get(field.toString()).toString(),
                  metaJsonField.getType(),
                  metaJsonField.getMaxSize()));
        } else {
          anonymizedJson.put(field.toString(), jsonObject.get(field.toString()).toString());
        }
      }
      return anonymizedJson;
    } catch (JSONException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  @Override
  public JSONObject createAnonymizedJson(
      Object object, HashMap<MetaJsonField, FakerApiField> fakerMap) throws AxelorException {
    List<String> metaJsonFieldsNameToAnonymize = new ArrayList<>();
    JSONObject anonymizedJson = new JSONObject();
    for (MetaJsonField metaJsonField : fakerMap.keySet()) {
      metaJsonFieldsNameToAnonymize.add(metaJsonField.getName());
    }
    try {
      JSONObject jsonObject = new JSONObject(object.toString());
      for (Object field : jsonObject.keySet()) {
        if (metaJsonFieldsNameToAnonymize.contains(field.toString())) {
          MetaJsonField metaJsonField = metaJsonFieldRepository.findByName(field.toString());
          if (fakerMap.get(metaJsonField) != null) {
            anonymizedJson.put(
                field.toString(), fakerService.generateFakeData(fakerMap.get(metaJsonField)));
          } else {
            anonymizedJson.put(
                field.toString(),
                anonymize(
                    jsonObject.get(field.toString()).toString(),
                    metaJsonField.getType(),
                    metaJsonField.getMaxSize()));
          }
        } else {
          anonymizedJson.put(field.toString(), jsonObject.get(field.toString()).toString());
        }
      }
      return anonymizedJson;
    } catch (JSONException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  protected Object anonymize(Object object, String type, int maxSize) {
    switch (type.toLowerCase()) {
      case "text":
      case "string":
        byte[] shaInBytes = hashString(object.toString());

        if (maxSize != 0 && shaInBytes.length > maxSize) {
          return bytesToHex(shaInBytes).substring(0, maxSize);
        } else {
          return bytesToHex(shaInBytes);
        }

      case "long":
      case "double":
        return 0;

      case "integer":
        return BigInteger.ZERO;

      case "decimal":
        return BigDecimal.ZERO;

      case "date":
        return LocalDate.of(1970, 1, 1);

      case "time":
        return LocalTime.of(0, 0, 0);

      case "datetime":
        return LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.of(0, 0, 0));

      case "binary":
      default:
        return null;
    }
  }

  protected byte[] hashString(String data) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-256");
      md.update(getSalt());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    }
    byte[] result = md.digest(data.getBytes(StandardCharsets.UTF_8));
    return result;
  }

  protected byte[] getSalt() throws NoSuchAlgorithmException {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return salt;
  }

  protected String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
