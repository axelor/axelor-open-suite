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
package com.axelor.studio.db.repo;

import com.axelor.studio.db.AppBuilder;
import com.axelor.studio.db.WsConnector;
import com.axelor.studio.db.WsRequest;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class WsConnectorRepo extends WsConnectorRepository {

  @Override
  public WsConnector save(WsConnector connector) {
    connector = super.save(connector);

    List<WsRequest> requests = connector.getWsRequestList();
    if (CollectionUtils.isEmpty(requests)) {
      return connector;
    }

    final AppBuilder appBuilder = connector.getAppBuilder();
    requests.forEach(
        req -> {
          req.setAppBuilder(appBuilder);
        });

    return connector;
  }
}
