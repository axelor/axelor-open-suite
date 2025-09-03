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
import com.axelor.db.Model;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.service.MetaModelService;
import com.google.inject.Inject;

public class ConnectorMapperManagementServiceImpl implements ConnectorMapperManagementService {

  protected ConnectorMapperCreateService connectorMapperCreateService;
  protected ConnectorMapperFetchService connectorMapperFetchService;

  @Inject
  public ConnectorMapperManagementServiceImpl(
      ConnectorMapperCreateService connectorMapperCreateService,
      ConnectorMapperFetchService connectorMapperFetchService) {
    this.connectorMapperCreateService = connectorMapperCreateService;
    this.connectorMapperFetchService = connectorMapperFetchService;
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
        connectorMapperFetchService.getConnectorMapper(
            metaModel, connectorSelect, externalReference, company, tradingName);
    if (connectorMapper == null) {
      return connectorMapperCreateService.createConnectorMapper(
          model, connectorSelect, externalReference, company, tradingName);
    }
    return connectorMapper;
  }
}
