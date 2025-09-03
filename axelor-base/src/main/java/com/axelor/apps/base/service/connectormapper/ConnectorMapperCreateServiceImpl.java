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
import com.axelor.meta.service.MetaModelService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ConnectorMapperCreateServiceImpl implements ConnectorMapperCreateService {

  protected ConnectorMapperRepository connectorMapperRepository;

  @Inject
  public ConnectorMapperCreateServiceImpl(ConnectorMapperRepository connectorMapperRepository) {
    this.connectorMapperRepository = connectorMapperRepository;
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
}
