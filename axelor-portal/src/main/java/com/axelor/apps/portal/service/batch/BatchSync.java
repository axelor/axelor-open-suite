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
package com.axelor.apps.portal.service.batch;

import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.AppPortalRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.portal.db.DynamicFieldSync;
import com.axelor.apps.portal.db.OpenSuiteGooveeSync;
import com.axelor.apps.portal.db.PortalBatch;
import com.axelor.apps.portal.db.ValueMapping;
import com.axelor.apps.portal.db.repo.DynamicFieldSyncRepository;
import com.axelor.apps.portal.db.repo.OpenSuiteGooveeSyncRepository;
import com.axelor.apps.portal.exceptions.IExceptionMessages;
import com.axelor.apps.portal.service.CommonLibraryService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.Hibernate;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class BatchSync extends AbstractBatch {

  private static final String SPACES_KEY = "spaces";
  private static final Integer LIMIT = 100;
  private int add = 0, update = 0;
  List<String> errorList = null;

  @Override
  protected void process() {
    errorList = new ArrayList<>();
    StringBuffer osToGooveeBuffer = new StringBuffer();
    StringBuffer gooveeToOsBuffer = new StringBuffer();

    try {
      List<OpenSuiteGooveeSync> openSuiteGooveeSyncList =
          Beans.get(OpenSuiteGooveeSyncRepository.class).all().order("sequence").fetch();
      if (CollectionUtils.isEmpty(openSuiteGooveeSyncList)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessages.SYNC_BINDING_ERROR));
      }

      AppPortal appPortal = Beans.get(AppPortalRepository.class).all().fetchOne();
      LocalDateTime lastSyncDateTime = CommonLibraryService.getLastSyncDate();

      String accessTocken = CommonLibraryService.getAccessTocken(appPortal);
      if (accessTocken == null) {
        return;
      }
      Map<String, String> headers = new HashMap<>();
      headers.put("authorization", accessTocken);

      String portalUrl =
          appPortal.getUrl().endsWith("/") ? appPortal.getUrl() : appPortal.getUrl() + "/";

      List<Long> gooveeSpaceIds = getGooveeSpaceIds(portalUrl, accessTocken);

      for (OpenSuiteGooveeSync openSuiteGooveeSync : openSuiteGooveeSyncList) {
        if (openSuiteGooveeSync.getOsObject() == null
            || StringUtils.isBlank(openSuiteGooveeSync.getGooveeObjectName())) {
          continue;
        }
        MetaModel metaModel = ((MetaModel) Hibernate.unproxy(openSuiteGooveeSync.getOsObject()));
        @SuppressWarnings("unchecked")
        Class<AuditableModel> klass =
            (Class<AuditableModel>) Class.forName(metaModel.getFullName());
        Mapper mapper = Mapper.of(klass);
        @SuppressWarnings("unchecked")
        JpaRepository<AuditableModel> repo =
            JpaRepository.of((Class<AuditableModel>) mapper.getBeanClass());
        String url = portalUrl + "api/" + openSuiteGooveeSync.getGooveeObjectName().toLowerCase();
        if (openSuiteGooveeSync.getOsToGooveeSyncSelect() != 1) {
          osToGooveeSync(openSuiteGooveeSync, url, lastSyncDateTime, headers, mapper, repo);
          osToGooveeBuffer.append(
              String.format(
                  "%n%s - %s : %s New Added, %s updated",
                  metaModel.getName(), openSuiteGooveeSync.getGooveeObjectName(), add, update));
          add = update = 0;
        }
        if (openSuiteGooveeSync.getGooveeToOsSyncSelect() != 1) {
          gooveeToOsSync(
              openSuiteGooveeSync, url, lastSyncDateTime, headers, mapper, repo, gooveeSpaceIds);
          gooveeToOsBuffer.append(
              String.format(
                  "%n%s - %s : %s New Added, %s updated",
                  openSuiteGooveeSync.getGooveeObjectName(), metaModel.getName(), add, update));
          add = update = 0;
        }
      }
    } catch (Exception e) {
      if (!errorList.contains(e.getMessage())) {
        errorList.add(e.getMessage());
      }
      incrementAnomaly();
      TraceBackService.trace(e, "", batch.getId());
    }
    setLog(osToGooveeBuffer, gooveeToOsBuffer);
  }

  @Transactional
  public void setLog(StringBuffer osToGooveeLog, StringBuffer gooveeToOsLog) {
    PortalBatch portalBatch = batch.getPortalBatch();

    if (!StringUtils.isEmpty(gooveeToOsLog.toString())) {
      osToGooveeLog.append(
          String.format("%n%n%nGoovee To Opensuite Sync : %n%s", gooveeToOsLog.toString()));
    }
    if (!StringUtils.isEmpty(osToGooveeLog.toString())) {
      portalBatch.setSyncLog(
          String.format("%n%nOpensuite To Goovee Sync :%n%s", osToGooveeLog.toString()).trim());
    } else {
      portalBatch.setSyncLog(null);
    }
    if (errorList.size() > 0) {
      portalBatch.setErrorLog(String.format("Sync Error : %n%s", String.join("\n", errorList)));
    } else {
      portalBatch.setErrorLog(null);
    }
  }

  private void osToGooveeSync(
      OpenSuiteGooveeSync openSuiteGooveeSync,
      String url,
      LocalDateTime lastSyncDateTime,
      Map<String, String> headers,
      Mapper mapper,
      JpaRepository<AuditableModel> repo) {
    try {
      List<AuditableModel> dataList;
      Integer total = 0, offset = 0;

      Query<AuditableModel> query =
          getQuery(repo, lastSyncDateTime, openSuiteGooveeSync.getOsToGooveeFilter());
      total = (int) query.count();
      do {
        dataList = query.fetch(LIMIT, offset);
        if (dataList == null || dataList.isEmpty()) {
          break;
        }

        List<DynamicFieldSync> dynamicFieldSyncList =
            openSuiteGooveeSync
                .getDynamicFieldSyncList()
                .stream()
                .filter(
                    field ->
                        field.getMappingSelect() == DynamicFieldSyncRepository.MAP_BOTH_SIDE
                            || field.getMappingSelect()
                                == DynamicFieldSyncRepository.MAP_OS_TO_GOOVEE)
                .collect(Collectors.toList());
        for (AuditableModel obj : dataList) {
          exportObject(mapper, obj, url, headers, dynamicFieldSyncList);
        }
        offset = offset + dataList.size();
      } while (offset < total);
    } catch (Exception e) {
      if (!errorList.contains(e.getMessage())) {
        errorList.add(e.getMessage());
      }
      incrementAnomaly();
      TraceBackService.trace(e, "", batch.getId());
    }
  }

  private Query<AuditableModel> getQuery(
      JpaRepository<AuditableModel> repo, LocalDateTime lastSyncDateTime, String filter) {
    Query<AuditableModel> query = repo.all();

    if (lastSyncDateTime != null) {
      if (!StringUtils.isBlank(filter)) {
        return query.filter(
            filter
                + " AND ((self.gooveeId != 0 AND self.gooveeId IS NOT NULL AND self.updatedOn > ?1 ) OR self.createdOn > ?1 OR self.gooveeId = 0)",
            lastSyncDateTime);
      } else {
        return query.filter(
            "((self.gooveeId != 0 AND self.gooveeId IS NOT NULL AND self.updatedOn > ?1 ) OR self.createdOn > ?1 OR self.gooveeId = 0)",
            lastSyncDateTime);
      }
    }

    if (!StringUtils.isBlank(filter)) {
      return query.filter(filter);
    }
    return query.order("id");
  }

  private void exportObject(
      Mapper mapper,
      AuditableModel obj,
      String url,
      Map<String, String> headers,
      List<DynamicFieldSync> dynamicFieldSyncList)
      throws JSONException {
    boolean isExist = false;
    String idUrl = null;
    JSONObject gooveeObj = null;
    long gooveeId = (long) mapper.get(obj, "gooveeId");
    if (gooveeId > 0) {
      idUrl = url + "/" + gooveeId;
      try {
        gooveeObj = CommonLibraryService.executeHTTPGet(idUrl, headers, null);
        if (gooveeObj != null
            && gooveeObj.containsKey("id")
            && gooveeObj.getLong("id") == gooveeId) {
          isExist = true;
        }
      } catch (IOException | JSONException | URISyntaxException | AxelorException e) {
        TraceBackService.trace(e);
      }
    }
    if (gooveeObj == null || !isExist) {
      gooveeObj = new JSONObject();
    }
    syncFieldValues(dynamicFieldSyncList, mapper, obj, gooveeObj, true);
    JSONObject jsonObject = null;
    try {
      if (isExist) {
        jsonObject = CommonLibraryService.executeHTTPPut(idUrl, headers, gooveeObj);
      } else {
        jsonObject = CommonLibraryService.executeHTTPPost(url, headers, gooveeObj);
      }
    } catch (IOException | URISyntaxException | AxelorException e) {
      if (!errorList.contains(e.getMessage())) {
        errorList.add(e.getMessage());
      }
      incrementAnomaly();
      TraceBackService.trace(e, obj.getClass().getName(), batch.getId());
    }
    if (jsonObject == null) {
      return;
    } else if (!jsonObject.containsKey("id")
        && jsonObject.containsKey("name")
        && jsonObject.getString("name").equals("goovee-error")
        && jsonObject.containsKey("message")) {
      AxelorException e =
          new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY, jsonObject.getString("message"));
      if (!errorList.contains(e.getMessage())) {
        errorList.add(e.getMessage());
      }
      incrementAnomaly();
      TraceBackService.trace(e, obj.getClass().getName(), batch.getId());
      return;
    }
    setGooveeId(jsonObject, mapper, obj);
    saveObject(obj);
    if (isExist) {
      update++;
    } else {
      add++;
    }
  }

  private void syncFieldValues(
      List<DynamicFieldSync> dynamicFieldSyncList,
      Mapper mapper,
      AuditableModel obj,
      JSONObject gooveeObj,
      Boolean isExport)
      throws JSONException {
    for (DynamicFieldSync dynamicFieldSync : dynamicFieldSyncList) {
      if (StringUtils.isBlank(dynamicFieldSync.getGooveeFieldName())) {
        continue;
      }
      try {
        Object value = getFieldValueFromAuditableModel(dynamicFieldSync, mapper, obj);
        if (value != null) {
          if (!gooveeObj.containsKey(dynamicFieldSync.getGooveeFieldName())
              || (gooveeObj.containsKey(dynamicFieldSync.getGooveeFieldName())
                  && gooveeObj.get(dynamicFieldSync.getGooveeFieldName()) == null)) {
            gooveeObj.put(dynamicFieldSync.getGooveeFieldName(), value);
          }
        }
      } catch (JSONException e) {
        TraceBackService.trace(e);
      }
    }
    if (!gooveeObj.containsKey("osId")) {
      gooveeObj.put("osId", obj.getId());
    }
    gooveeObj.put("lastUpdatedOnSync", LocalDateTime.now().toString());
  }

  public void setGooveeId(JSONObject jsonObject, Mapper mapper, AuditableModel obj)
      throws NumberFormatException, JSONException {
    if (jsonObject.containsKey("id")) {
      Long gooveeId = Long.parseLong(jsonObject.get("id").toString());
      Property gooveeIdProperty = mapper.getProperty("gooveeId");
      if (gooveeIdProperty != null && gooveeId != null) {
        gooveeIdProperty.set(obj, gooveeId);
      }
    }
  }

  @Transactional
  public void saveObject(AuditableModel obj) {
    try {
      JPA.save(obj);
      incrementDone();
    } catch (Exception e) {
      if (JPA.em().getTransaction().isActive()) {
        JPA.em().getTransaction().rollback();
        JPA.em().getTransaction().begin();
      }
      if (!errorList.contains(e.getMessage())) {
        errorList.add(e.getMessage());
      }
      incrementAnomaly();
      TraceBackService.trace(e, obj.getClass().getName(), batch.getId());
    }
  }

  private Object getFieldValueFromAuditableModel(
      DynamicFieldSync dynamicFieldSync, Mapper mapper, AuditableModel obj) {
    if (!StringUtils.isBlank(dynamicFieldSync.getOsFieldName())) {
      Property property = mapper.getProperty(dynamicFieldSync.getOsFieldName());
      if (property != null) {
        Object value = property.get(obj);
        if (value != null) {
          if (dynamicFieldSync.getIsSelectField()) {
            return getSelectFieldValue(dynamicFieldSync, value, true);
          } else {
            switch (property.getType()) {
              case BOOLEAN:
                return value;
              case DATE:
                return (LocalDate) value;
              case TIME:
                LocalTime localTime = (LocalTime) value;
                return localTime.atOffset(ZoneOffset.UTC).toLocalTime();
              case DATETIME:
                LocalDateTime localDateTime = (LocalDateTime) value;
                return localDateTime
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime();
              default:
                return value.toString();
            }
          }
        }
      }
    }
    return getDefaultValue(null, dynamicFieldSync);
  }

  private Object getSelectFieldValue(
      DynamicFieldSync dynamicFieldSync, Object value, Boolean isExport) {
    switch (dynamicFieldSync.getOsTypeSelect()) {
      case DynamicFieldSyncRepository.TYPE_M2O:
        String field = "id";
        if (dynamicFieldSync.getOsTypeSelectFieldName() != null) {
          field = dynamicFieldSync.getOsTypeSelectFieldName();
        }
        Mapper referenceMapper = Mapper.of(Hibernate.unproxy(value).getClass());
        Object referenceValue = referenceMapper.get(value, field);
        return referenceValue != null && !referenceValue.equals(0l)
            ? referenceValue.toString()
            : null;
      case DynamicFieldSyncRepository.TYPE_SELECTION:
        return getSelectionFieldValue(dynamicFieldSync, value, isExport);
    }
    return null;
  }

  private Object getSelectionFieldValue(
      DynamicFieldSync dynamicFieldSync, Object value, Boolean isExport) {
    Object propertyValue = null;
    if (value != null && !StringUtils.isBlank(value.toString())) {
      List<ValueMapping> valueMappingList = dynamicFieldSync.getValueMappingList();
      Optional<ValueMapping> optValueMapping = null;
      Stream<ValueMapping> stream = valueMappingList.stream();
      if (isExport) {
        optValueMapping =
            stream
                .filter(valueMap -> valueMap.getOsSelectValue().equals(value.toString()))
                .findFirst();
        if (optValueMapping.isPresent()) {
          Boolean booleanValue =
              BooleanUtils.toBooleanObject(optValueMapping.get().getGooveeSelectValue());
          if (booleanValue != null) {
            return booleanValue;
          }
          propertyValue = optValueMapping.get().getGooveeSelectValue();
        }
      } else {
        optValueMapping =
            stream
                .filter(valueMap -> valueMap.getGooveeSelectValue().equals(value.toString()))
                .findFirst();
        if (optValueMapping.isPresent()) {
          Boolean booleanValue =
              BooleanUtils.toBooleanObject(optValueMapping.get().getOsSelectValue());
          if (booleanValue != null) {
            return booleanValue;
          }
          propertyValue = optValueMapping.get().getOsSelectValue();
        }
      }
    }
    propertyValue = getDefaultValue(propertyValue, dynamicFieldSync);
    return propertyValue;
  }

  private Object getM2OFieldValue(
      DynamicFieldSync dynamicFieldSync, Object gooveeValue, Property property) {
    String fieldName = dynamicFieldSync.getOsTypeSelectFieldName();
    @SuppressWarnings("unchecked")
    Class<? extends Model> fieldClass = (Class<? extends Model>) property.getTarget();
    Object propertyValue = null;
    if (gooveeValue != null && !StringUtils.isBlank(gooveeValue.toString())) {
      propertyValue =
          JPA.all(fieldClass).filter("self." + fieldName + " = ?", gooveeValue).fetchOne();
    }
    propertyValue =
        getM20DefaultValue(
            propertyValue, dynamicFieldSync.getOsDefaultValue(), fieldClass, fieldName);
    return propertyValue;
  }

  private Object getDefaultValue(Object value, DynamicFieldSync dynamicFieldSync) {
    if (value == null && !StringUtils.isEmpty(dynamicFieldSync.getOsDefaultValue())) {
      value = dynamicFieldSync.getOsDefaultValue();
    }
    return value;
  }

  private void gooveeToOsSync(
      OpenSuiteGooveeSync openSuiteGooveeSync,
      String portalUrl,
      LocalDateTime lastSyncDateTime,
      Map<String, String> headers,
      Mapper mapper,
      JpaRepository<AuditableModel> repo,
      List<Long> gooveeSpaceIds) {
    try {
      List<NameValuePair> queryParams = new ArrayList<>();
      NameValuePair limitQuery = new BasicNameValuePair("$limit", LIMIT.toString());
      NameValuePair skipQuery = new BasicNameValuePair("$skip", "0");
      queryParams.add(limitQuery);
      queryParams.add(skipQuery);

      if (!StringUtils.isBlank(openSuiteGooveeSync.getGooveeToOsFilter())) {
        String[] filterArr = openSuiteGooveeSync.getGooveeToOsFilter().split(" AND ");
        // TODO : add filters to queryParams
        for (String str : filterArr) {
          String[] queryParam = str.split("=");
          queryParams.add(new BasicNameValuePair(queryParam[0].trim(), queryParam[1].trim()));
        }
      }

      List<DynamicFieldSync> dynamicFieldSyncList =
          openSuiteGooveeSync
              .getDynamicFieldSyncList()
              .stream()
              .filter(
                  field ->
                      field.getMappingSelect() == DynamicFieldSyncRepository.MAP_BOTH_SIDE
                          || field.getMappingSelect()
                              == DynamicFieldSyncRepository.MAP_GOOVEE_TO_OS)
              .collect(Collectors.toList());

      for (Long spaceId : gooveeSpaceIds) {
        headers.put("goovee-space-id", spaceId.toString());
        syncSpaceData(
            portalUrl,
            headers,
            queryParams,
            skipQuery,
            dynamicFieldSyncList,
            mapper,
            repo,
            lastSyncDateTime);
      }
    } catch (Exception e) {
      if (!errorList.contains(e.getMessage())) {
        errorList.add(e.getMessage());
      }
      incrementAnomaly();
      TraceBackService.trace(e, "", batch.getId());
    }
  }

  private List<Long> getGooveeSpaceIds(String url, String accessTocken) {
    List<Long> spaceIds = new ArrayList<>();
    try {
      CloseableHttpClient client = HttpClients.createDefault();
      url = String.format("%s/api/%s", url, SPACES_KEY);
      HttpGet httpGet = new HttpGet(url);
      httpGet.setHeader("authorization", accessTocken);
      CloseableHttpResponse response = client.execute(httpGet);

      String body = IOUtils.toString(response.getEntity().getContent(), Consts.UTF_8);
      JSONObject jsonObject = new JSONObject(body);
      if (jsonObject.containsKey("data")) {
        @SuppressWarnings("unchecked")
        List<JSONObject> spaceList = (List<JSONObject>) jsonObject.get("data");
        for (JSONObject space : spaceList) {
          Long spaceId = getCompanyGooveeId(space);
          if (spaceId != null) {
            spaceIds.add(spaceId);
          }
        }
      }
      client.close();
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return spaceIds;
  }

  @Transactional
  public Long getCompanyGooveeId(JSONObject space) {
    try {
      String spaceName = space.getString("name");
      Long spaceId = Long.parseLong(space.get("id").toString());

      Company company =
          Beans.get(CompanyRepository.class)
              .all()
              .filter(
                  "self.gooveeId = ? OR self.name = ? OR self.code = ?",
                  spaceId,
                  spaceName,
                  spaceName.toUpperCase())
              .fetchOne();
      if (company == null) {
        company = new Company();
        company.setName(spaceName);
        company.setCode(spaceName.toUpperCase());
        company.setGooveeId(spaceId);
        Beans.get(CompanyRepository.class).save(company);
      }
      return spaceId;
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return null;
  }

  private void syncSpaceData(
      String url,
      Map<String, String> headers,
      List<NameValuePair> queryParams,
      NameValuePair skipQuery,
      List<DynamicFieldSync> dynamicFieldSyncList,
      Mapper mapper,
      JpaRepository<AuditableModel> repo,
      LocalDateTime lastSyncDateTime) {
    try {
      Integer total = 0, skip = 0;
      do {
        skipQuery = new BasicNameValuePair("$skip", skip.toString());
        queryParams.set(1, skipQuery);
        JSONObject jsonObject = CommonLibraryService.executeHTTPGet(url, headers, queryParams);
        if (jsonObject.containsKey("data")) {
          @SuppressWarnings("unchecked")
          List<JSONObject> dataList = (List<JSONObject>) jsonObject.get("data");
          for (JSONObject dataObj : dataList) {
            importObject(dynamicFieldSyncList, mapper, dataObj, lastSyncDateTime, repo);
          }
          total = jsonObject.getInt("total");
          skip = skip + dataList.size();
        }
      } while (skip < total);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  private void importObject(
      List<DynamicFieldSync> dynamicFieldSyncList,
      Mapper mapper,
      JSONObject dataObj,
      LocalDateTime lastSyncDateTime,
      JpaRepository<AuditableModel> repo)
      throws JSONException {

    LocalDateTime gooveeCreatedOn = null, gooveeUpdatedOn = null;
    gooveeCreatedOn = getFormatedDate(dataObj, "createdAt");
    gooveeUpdatedOn = getFormatedDate(dataObj, "updatedAt");

    @SuppressWarnings("unchecked")
    Class<? extends AuditableModel> klass = (Class<? extends AuditableModel>) mapper.getBeanClass();
    AuditableModel obj = null;

    Long id = null;
    if (dataObj.containsKey("id")) {
      id = dataObj.getLong("id");
      if (id != null) {
        obj = repo.all().filter("self.gooveeId = ?", id).fetchOne();
      }
    }

    String osId = null;
    if (dataObj.containsKey("osId")) {
      osId = dataObj.get("osId").toString();
      if (osId != null && !osId.equals("null")) {
        obj = repo.find(Long.parseLong(osId.toString()));
      }
    }

    boolean isExist = false;
    if (obj != null) {
      if (lastSyncDateTime != null
              && ((gooveeCreatedOn != null
                      && lastSyncDateTime.isAfter(gooveeCreatedOn)
                      && gooveeUpdatedOn == null)
                  || (gooveeUpdatedOn != null && lastSyncDateTime.isAfter(gooveeUpdatedOn)))
          || lastSyncDateTime == null
              && dataObj.getString("osId") != null
              && !dataObj.getString("osId").toString().equals("0")) {
        return;
      }
      LocalDateTime updatedOn = ((AuditableModel) obj).getUpdatedOn();
      if (gooveeUpdatedOn != null && updatedOn != null && updatedOn.isAfter(gooveeUpdatedOn)) {
        return;
      }
      updateLastSyncVersion(mapper, obj);
      isExist = true;
    }

    if (obj == null) {
      obj = Beans.get(klass);
      setGooveeId(dataObj, mapper, obj);
    }

    for (DynamicFieldSync dynamicFieldSync : dynamicFieldSyncList) {
      Property property = mapper.getProperty(dynamicFieldSync.getOsFieldName());
      if (property == null) {
        continue;
      }
      Object propertyValue =
          getFieldValueFromJsonObj(dynamicFieldSync, mapper, dataObj, obj, property);
      property.set(obj, propertyValue);
    }
    saveObject(obj);
    if (isExist) {
      update++;
    } else {
      add++;
    }
  }

  private LocalDateTime getFormatedDate(JSONObject dataObj, String key) {
    LocalDateTime localDateTime = null;
    try {
      if (dataObj.containsKey(key)) {
        String str = dataObj.get(key).toString();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date utcTime = format.parse(str);
        format.setTimeZone(TimeZone.getDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        localDateTime = LocalDateTime.parse(format.format(utcTime), formatter);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return localDateTime;
  }

  private void updateLastSyncVersion(Mapper mapper, Model obj) {
    Property lastSyncVersionProperty = mapper.getProperty("lastSyncVersion");
    if (lastSyncVersionProperty != null) {
      Integer lastSyncVersion =
          lastSyncVersionProperty.get(obj) != null
              ? Integer.parseInt(lastSyncVersionProperty.get(obj).toString()) + 1
              : 0;
      lastSyncVersionProperty.set(obj, lastSyncVersion);
    }
  }

  private Object getFieldValueFromJsonObj(
      DynamicFieldSync dynamicFieldSync,
      Mapper mapper,
      JSONObject dataObj,
      Model obj,
      Property property)
      throws JSONException {
    Object propertyValue = null;
    if (dynamicFieldSync.getMappingSelect() == DynamicFieldSyncRepository.MAP_BOTH_SIDE
        || dynamicFieldSync.getMappingSelect() == DynamicFieldSyncRepository.MAP_GOOVEE_TO_OS) {
      Object gooveeValue = null;
      if (dataObj.containsKey(dynamicFieldSync.getGooveeFieldName())) {
        gooveeValue =
            dataObj.isNull(dynamicFieldSync.getGooveeFieldName())
                ? null
                : dataObj.get(dynamicFieldSync.getGooveeFieldName());
      }
      if (dynamicFieldSync.getIsSelectField()) {
        switch (dynamicFieldSync.getOsTypeSelect()) {
          case DynamicFieldSyncRepository.TYPE_SELECTION:
            propertyValue = getSelectionFieldValue(dynamicFieldSync, gooveeValue, false);
            break;
          case DynamicFieldSyncRepository.TYPE_M2O:
            propertyValue = getM2OFieldValue(dynamicFieldSync, gooveeValue, property);
            break;
        }
      } else {
        propertyValue = getDefaultValue(gooveeValue, dynamicFieldSync);
      }
    }
    return propertyValue;
  }

  private Object getM20DefaultValue(
      Object propertyValue,
      String defaultValue,
      Class<? extends Model> fieldClass,
      String fieldName) {
    if (propertyValue != null) {
      return propertyValue;
    }
    if (!StringUtils.isBlank(defaultValue) && fieldClass != null && fieldName != null) {
      fieldName = StringUtils.isBlank(fieldName) ? "id" : fieldName;
      return JPA.all(fieldClass).filter("self." + fieldName + " = ?", defaultValue).fetchOne();
    }
    return defaultValue;
  }
}
