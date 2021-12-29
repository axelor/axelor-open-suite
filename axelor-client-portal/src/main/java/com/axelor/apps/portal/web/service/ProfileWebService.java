/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.web.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.inject.Beans;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class ProfileWebService extends AbstractWebService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse get() {
    Partner partner = getPartner();
    Beans.get(JpaSecurity.class).check(AccessType.READ, Partner.class, partner.getId());
    Map<String, Object> data =
        ResponseGeneratorFactory.of(Partner.class.getName()).generate(partner);

    PortalRestResponse response = new PortalRestResponse();
    return response.setData(data).success();
  }
}
