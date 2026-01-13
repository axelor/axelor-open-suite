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
import java.util.List;

public interface ConnectorMapperFetchService {

  /**
   * Retrieves a ConnectorMapper based on the given meta model, connector selection, external
   * reference, company, and trading name.
   *
   * @param metaModel the meta model associated with the connector mapper
   * @param connectorSelect the connector selection criteria
   * @param externalReference the external reference for the connector mapper
   * @param company the company associated with the connector mapper; if null, returns connector
   *     mapper for any company
   * @param tradingName the trading name associated with the connector mapper; if null, returns
   *     connector mapper for any trading name
   * @return the retrieved ConnectorMapper, or null if no match is found
   */
  ConnectorMapper getConnectorMapper(
      MetaModel metaModel,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName);

  /**
   * Retrieves a list of ConnectorMappers based on the given meta model, connector selection,
   * external reference, company, and trading name.
   *
   * @param metaModel the meta model associated with the connector mappers
   * @param connectorSelect the connector selection criteria
   * @param externalReference the external reference for the connector mapper
   * @param company the company associated with the connector mapper; if null, returns connector
   *     mappers for any company
   * @param tradingName the trading name associated with the connector mapper; if null, returns
   *     connector mappers for any trading name
   * @return a list of matching ConnectorMappers, or an empty list if no matches are found
   */
  List<ConnectorMapper> getConnectorMapperList(
      MetaModel metaModel,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName);

  /**
   * Retrieves a ConnectorMapper based on the given model class, connector selection, external
   * reference, company, and trading name.
   *
   * @param modelClass the model class associated with the connector mapper
   * @param connectorSelect the connector selection criteria
   * @param externalReference the external reference for the connector mapper
   * @param company the company associated with the connector mapper; if null, returns connector
   *     mapper for any company
   * @param tradingName the trading name associated with the connector mapper; returns connector
   *     mapper for any trading name
   * @return the retrieved ConnectorMapper, or null if no match is found
   */
  ConnectorMapper getConnectorMapper(
      Class<? extends Model> modelClass,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName);

  /**
   * Retrieves a list of ConnectorMappers based on the given model class, connector selection,
   * external reference, company, and trading name.
   *
   * @param modelClass the model class associated with the connector mappers
   * @param connectorSelect the connector selection criteria
   * @param externalReference the external reference for the connector mapper
   * @param company the company associated with the connector mapper; if null, returns connector
   *     mappers for any company
   * @param tradingName the trading name associated with the connector mapper; if null, returns
   *     connector mappers for any trading name
   * @return a list of matching ConnectorMappers, or an empty list if no matches are found
   */
  List<ConnectorMapper> getConnectorMapperList(
      Class<? extends Model> modelClass,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName);
}
