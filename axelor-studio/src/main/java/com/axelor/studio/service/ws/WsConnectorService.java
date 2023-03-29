/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.ws;

import com.axelor.exception.AxelorException;
import com.axelor.studio.db.WsAuthenticator;
import com.axelor.studio.db.WsConnector;
import com.axelor.studio.db.WsRequest;
import com.axelor.text.Templates;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public interface WsConnectorService {

  public Map<String, Object> callConnector(
      WsConnector wsConnector, WsAuthenticator authenticator, Map<String, Object> ctx)
      throws AxelorException;

  public Entity<?> createEntity(WsRequest wsRequest, Templates templates, Map<String, Object> ctx);

  public Response callRequest(
      WsRequest wsRequest, String url, Client client, Templates templates, Map<String, Object> ctx);
}
