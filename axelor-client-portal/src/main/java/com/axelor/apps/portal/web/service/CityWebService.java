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

import com.axelor.apps.base.db.City;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.apps.portal.service.response.generator.ResponseGenerator;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.inject.Beans;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
public class CityWebService extends AbstractWebService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse fetch(
      @QueryParam("sort") String sort,
      @QueryParam("page") int page,
      @QueryParam("limit") int limit) {

    List<City> cities = fetch(City.class, null, null, sort, limit, page);
    Beans.get(JpaSecurity.class)
        .check(AccessType.READ, City.class, cities.stream().map(City::getId).toArray(Long[]::new));

    ResponseGenerator generator = ResponseGeneratorFactory.of(City.class.getName());

    List<Map<String, Object>> data =
        cities.stream().map(generator::generate).collect(Collectors.toList());

    PortalRestResponse response = new PortalRestResponse();
    response.setTotal(totalQuery(City.class, null, null));
    response.setOffset((page - 1) * limit);
    return response.setData(data).success();
  }
}
