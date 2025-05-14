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
import com.axelor.apps.base.db.repo.ConnectorMapperRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.service.MetaModelService;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class ConnectorMapperFetchServiceImpl implements ConnectorMapperFetchService {

  protected ConnectorMapperRepository connectorMapperRepository;

  @Inject
  public ConnectorMapperFetchServiceImpl(ConnectorMapperRepository connectorMapperRepository) {
    this.connectorMapperRepository = connectorMapperRepository;
  }

  @Override
  public ConnectorMapper getConnectorMapper(
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

  @Override
  public List<ConnectorMapper> getConnectorMapperList(
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

  @Override
  public ConnectorMapper getConnectorMapper(
      Class<? extends Model> modelClass,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName) {
    MetaModel metaModel = MetaModelService.getMetaModel(modelClass);
    return getConnectorMapper(metaModel, connectorSelect, externalReference, company, tradingName);
  }

  @Override
  public List<ConnectorMapper> getConnectorMapperList(
      Class<? extends Model> modelClass,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName) {
    MetaModel metaModel = MetaModelService.getMetaModel(modelClass);
    return getConnectorMapperList(
        metaModel, connectorSelect, externalReference, company, tradingName);
  }
}
