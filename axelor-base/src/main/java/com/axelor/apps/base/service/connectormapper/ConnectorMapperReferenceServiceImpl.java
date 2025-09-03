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
package com.axelor.apps.base.service.connectormapper;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ConnectorMapper;
import com.axelor.apps.base.db.TradingName;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.service.MetaModelService;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class ConnectorMapperReferenceServiceImpl implements ConnectorMapperReferenceService {

  protected ConnectorMapperFetchService connectorMapperFetchService;

  @Inject
  public ConnectorMapperReferenceServiceImpl(
      ConnectorMapperFetchService connectorMapperFetchService) {
    this.connectorMapperFetchService = connectorMapperFetchService;
  }

  @Override
  public List<String> getExternalReferenceList(Model model, String connectorSelect) {
    return getExternalReferenceList(model, connectorSelect, null, null);
  }

  @Override
  public List<String> getExternalReferenceList(
      Model model, String connectorSelect, Company company) {
    return getExternalReferenceList(model, connectorSelect, company, null);
  }

  @Override
  public List<String> getExternalReferenceList(
      Model model, String connectorSelect, TradingName tradingName) {
    return getExternalReferenceList(model, connectorSelect, null, tradingName);
  }

  @Override
  public List<String> getExternalReferenceList(
      Model model, String connectorSelect, Company company, TradingName tradingName) {
    MetaModel metaModel = MetaModelService.getMetaModel(model.getClass());
    List<ConnectorMapper> connectorMapperList =
        connectorMapperFetchService.getConnectorMapperList(
            metaModel, connectorSelect, null, company, tradingName);
    if (CollectionUtils.isEmpty(connectorMapperList)) {
      return null;
    }
    return connectorMapperList.stream()
        .map(ConnectorMapper::getExternalReference)
        .filter(StringUtils::isNotEmpty)
        .collect(Collectors.toList());
  }

  @Override
  public <M extends Model> List<M> getReferencedModelList(
      Class<M> modelClass, String externalReference, String connectorSelect) {
    return getReferencedModelList(modelClass, externalReference, connectorSelect, null, null);
  }

  @Override
  public <M extends Model> List<M> getReferencedModelList(
      Class<M> modelClass, String externalReference, String connectorSelect, Company company) {
    return getReferencedModelList(modelClass, externalReference, connectorSelect, company, null);
  }

  @Override
  public <M extends Model> List<M> getReferencedModelList(
      Class<M> modelClass,
      String externalReference,
      String connectorSelect,
      TradingName tradingName) {
    return getReferencedModelList(
        modelClass, externalReference, connectorSelect, null, tradingName);
  }

  @Override
  public <M extends Model> List<M> getReferencedModelList(
      Class<M> modelClass,
      String externalReference,
      String connectorSelect,
      Company company,
      TradingName tradingName) {
    MetaModel metaModel = MetaModelService.getMetaModel(modelClass);
    List<ConnectorMapper> connectorMapperList =
        connectorMapperFetchService.getConnectorMapperList(
            metaModel, connectorSelect, externalReference, company, tradingName);
    if (CollectionUtils.isEmpty(connectorMapperList)) {
      return null;
    }
    return connectorMapperList.stream()
        .map(connectorMapper -> JPA.find(modelClass, connectorMapper.getModelId()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
