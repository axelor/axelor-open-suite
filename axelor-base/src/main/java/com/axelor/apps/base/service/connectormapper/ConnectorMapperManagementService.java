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

public interface ConnectorMapperManagementService {

  /**
   * Retrieves an existing ConnectorMapper or creates a new one for the given model, connector
   * selection, and external reference.
   *
   * @param model the model for which the connector mapper is to be updated or created
   * @param connectorSelect the connector selection criteria
   * @param externalReference the external reference for the connector mapper
   * @return the retrieved or newly created connector mapper
   */
  ConnectorMapper getOrCreateConnectorMapper(
      Model model, String connectorSelect, String externalReference);

  /**
   * Retrieves an existing ConnectorMapper or creates a new one for the given model, connector
   * selection, external reference, and company.
   *
   * @param model the model for which the connector mapper is to be updated or created
   * @param connectorSelect the connector selection criteria
   * @param externalReference the external reference for the connector mapper
   * @param company
   * @return the retrieved or newly created connector mapper
   */
  ConnectorMapper getOrCreateConnectorMapper(
      Model model, String connectorSelect, String externalReference, Company company);

  /**
   * Retrieves an existing ConnectorMapper or creates a new one for the given model, connector
   * selection, external reference, and trading name.
   *
   * @param model the model for which the connector mapper is to be updated or created
   * @param connectorSelect the connector selection criteria
   * @param externalReference the external reference for the connector mapper
   * @param tradingName
   * @return the retrieved or newly created connector mapper
   */
  ConnectorMapper getOrCreateConnectorMapper(
      Model model, String connectorSelect, String externalReference, TradingName tradingName);

  /**
   * Retrieves an existing ConnectorMapper or creates a new one for the given model, connector
   * selection, external reference, company, and trading name.
   *
   * @param model the model for which the connector mapper is to be updated or created
   * @param connectorSelect the connector selection criteria
   * @param externalReference the external reference for the connector mapper
   * @param company
   * @param tradingName
   * @return the retrieved or newly created connector mapper
   */
  ConnectorMapper getOrCreateConnectorMapper(
      Model model,
      String connectorSelect,
      String externalReference,
      Company company,
      TradingName tradingName);
}
