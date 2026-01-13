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
import com.axelor.apps.base.db.TradingName;
import com.axelor.db.Model;
import java.util.List;

public interface ConnectorMapperReferenceService {

  /**
   * Retrieves the list of external references for a given model and connector selection.
   *
   * @param model the model for which the external references are to be retrieved
   * @param connectorSelect the connector selection criteria
   * @return a list of external references as Strings
   */
  List<String> getExternalReferenceList(Model model, String connectorSelect);

  /**
   * Retrieves the list of external references for a given model, connector selection, and company.
   *
   * @param model the model for which the external references are to be retrieved
   * @param connectorSelect the connector selection criteria
   * @param company
   * @return a list of external references as Strings
   */
  List<String> getExternalReferenceList(Model model, String connectorSelect, Company company);

  /**
   * Retrieves the list of external references for a given model, connector selection, and trading
   * name.
   *
   * @param model the model for which the external references are to be retrieved
   * @param connectorSelect the connector selection criteria
   * @param tradingName
   * @return a list of external references as Strings
   */
  List<String> getExternalReferenceList(
      Model model, String connectorSelect, TradingName tradingName);

  /**
   * Retrieves the list of external references for a given model, connector selection, company, and
   * trading name.
   *
   * @param model the model for which the external references are to be retrieved
   * @param connectorSelect the connector selection criteria
   * @param company
   * @param tradingName
   * @return a list of external references as Strings
   */
  List<String> getExternalReferenceList(
      Model model, String connectorSelect, Company company, TradingName tradingName);

  /**
   * Retrieves the list of referenced models for a given external reference and connector selection.
   *
   * @param <M> the type of the model
   * @param modelClass the class of the model
   * @param externalReference the external reference to be used
   * @param connectorSelect the connector selection criteria
   * @return a list of referenced models of type M
   */
  <M extends Model> List<M> getReferencedModelList(
      Class<M> modelClass, String externalReference, String connectorSelect);

  /**
   * Retrieves the list of referenced models for a given external reference, connector selection,
   * and company.
   *
   * @param <M> the type of the model
   * @param modelClass the class of the model
   * @param externalReference the external reference to be used
   * @param connectorSelect the connector selection criteria
   * @param company
   * @return a list of referenced models of type M
   */
  <M extends Model> List<M> getReferencedModelList(
      Class<M> modelClass, String externalReference, String connectorSelect, Company company);

  /**
   * Retrieves the list of referenced models for a given external reference, connector selection,
   * and trading name.
   *
   * @param <M> the type of the model
   * @param modelClass the class of the model
   * @param externalReference the external reference to be used
   * @param connectorSelect the connector selection criteria
   * @param tradingName
   * @return a list of referenced models of type M
   */
  <M extends Model> List<M> getReferencedModelList(
      Class<M> modelClass,
      String externalReference,
      String connectorSelect,
      TradingName tradingName);

  /**
   * Retrieves the list of referenced models for a given external reference, connector selection,
   * company, and trading name.
   *
   * @param <M> the type of the model
   * @param modelClass the class of the model
   * @param externalReference the external reference to be used
   * @param connectorSelect the connector selection criteria
   * @param company
   * @param tradingName
   * @return a list of referenced models of type M
   */
  <M extends Model> List<M> getReferencedModelList(
      Class<M> modelClass,
      String externalReference,
      String connectorSelect,
      Company company,
      TradingName tradingName);
}
