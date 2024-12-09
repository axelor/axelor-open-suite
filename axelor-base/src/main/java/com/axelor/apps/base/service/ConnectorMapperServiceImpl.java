/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ConnectorMapper;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.ConnectorMapperRepository;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.service.MetaModelService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class ConnectorMapperServiceImpl implements ConnectorMapperService {

  protected ConnectorMapperRepository connectorMapperRepository;
  protected MetaModelRepository metaModelRepository;

  @Inject
  public ConnectorMapperServiceImpl(
      ConnectorMapperRepository connectorMapperRepository,
      MetaModelRepository metaModelRepository) {
    this.connectorMapperRepository = connectorMapperRepository;
    this.metaModelRepository = metaModelRepository;
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
        getConnectorMapperList(metaModel, connectorSelect, null, company, tradingName);
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
        getConnectorMapperList(metaModel, connectorSelect, externalReference, company, tradingName);
    if (CollectionUtils.isEmpty(connectorMapperList)) {
      return null;
    }
    return connectorMapperList.stream()
        .map(connectorMapper -> JPA.find(modelClass, connectorMapper.getModelId()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public ConnectorMapper createConnectorMapper(
      Model model, String connectorSelect, String externalReference) {
    return createConnectorMapper(model, connectorSelect, externalReference, null, null);
  }

  @Override
  public ConnectorMapper createConnectorMapper(
      Model model, String connectorSelect, String externalReference, Company company) {
    return createConnectorMapper(model, connectorSelect, externalReference, company, null);
  }

  @Override
  public ConnectorMapper createConnectorMapper(
      Model model, String connectorSelect, String externalReference, TradingName tradingName) {
    return createConnectorMapper(model, connectorSelect, externalReference, null, tradingName);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ConnectorMapper createConnectorMapper(
      Model model,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName) {
    ConnectorMapper connectorMapper = new ConnectorMapper();
    connectorMapper.setConnectorSelect(connectorSelect);
    connectorMapper.setMetaModel(MetaModelService.getMetaModel(model.getClass()));
    connectorMapper.setModelId(model.getId());
    connectorMapper.setExternalReference(externalReference);
    connectorMapper.setCompany(company);
    connectorMapper.setTradingName(tradingName);
    return connectorMapperRepository.save(connectorMapper);
  }

  @Override
  public ConnectorMapper getOrCreateConnectorMapper(
      Model model, String connectorSelect, String externalReference) {
    return getOrCreateConnectorMapper(model, connectorSelect, externalReference, null, null);
  }

  @Override
  public ConnectorMapper getOrCreateConnectorMapper(
      Model model, String connectorSelect, String externalReference, Company company) {
    return getOrCreateConnectorMapper(model, connectorSelect, externalReference, company, null);
  }

  @Override
  public ConnectorMapper getOrCreateConnectorMapper(
      Model model, String connectorSelect, String externalReference, TradingName tradingName) {
    return getOrCreateConnectorMapper(model, connectorSelect, externalReference, null, tradingName);
  }

  @Override
  public ConnectorMapper getOrCreateConnectorMapper(
      Model model,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName) {
    MetaModel metaModel = MetaModelService.getMetaModel(model.getClass());
    ConnectorMapper connectorMapper =
        getConnectorMapper(metaModel, connectorSelect, externalReference, company, tradingName);
    if (connectorMapper == null) {
      return createConnectorMapper(model, connectorSelect, externalReference, company, tradingName);
    }
    return connectorMapper;
  }

  protected ConnectorMapper getConnectorMapper(
      MetaModel metaModel,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName) {
    List<ConnectorMapper> connectorMapperList =
        getConnectorMapperList(metaModel, connectorSelect, externalReference, company, tradingName);
    if (CollectionUtils.isEmpty(connectorMapperList)) {
      return null;
    }
    return connectorMapperList.get(0);
  }

  protected List<ConnectorMapper> getConnectorMapperList(
      MetaModel metaModel,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName) {
    Query<ConnectorMapper> query = connectorMapperRepository.all();
    StringBuilder filter =
        new StringBuilder(
            "self.connectorSelect = :connectorSelect AND self.metaModel = :metaModel");

    if (StringUtils.isNotEmpty(externalReference)) {
      filter.append(" AND self.externalReference = :externalReference");
      query.bind("externalReference", externalReference);
    }
    if (company != null) {
      filter.append(" AND self.company = :company");
      query.bind("company", company);
    }
    if (tradingName != null) {
      filter.append(" AND self.tradingName = :tradingName");
      query.bind("tradingName", tradingName);
    }
    query
        .filter(filter.toString())
        .bind("connectorSelect", connectorSelect)
        .bind("metaModel", metaModel);
    return query.fetch();
  }
}
