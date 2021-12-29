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

import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.AppPortalRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.common.StringUtils;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.UnauthorizedException;

public class AbstractWebService {

  protected static final int STATUS_SUCCESS = 0;
  protected static final int STATUS_FAILURE = -1;

  protected static final Cache<String, Map<String, Object>> PAYBOX_ORDER =
      CacheBuilder.newBuilder().maximumSize(1000).weakValues().build();

  public Response autherizationFail(UnauthorizedException e) {
    PortalRestResponse response = new PortalRestResponse();
    response.setException(e);
    return Response.ok().type(MediaType.APPLICATION_JSON).entity(response).build();
  }

  public Response fail(Exception e) {
    PortalRestResponse response = new PortalRestResponse();
    response.setException(e);
    TraceBackService.trace(e);
    return Response.ok().type(MediaType.APPLICATION_JSON).entity(response).build();
  }

  /**
   * Returns the number of total records matched.
   *
   * @param <T>
   * @param klass Domain to search
   * @param filter domain filter
   * @param params named params for domain filter
   * @return total records matched
   */
  protected <T extends Model> Long totalQuery(
      Class<T> klass, String filter, Map<String, Object> params) {
    Query<T> query = Query.of(klass);
    if (StringUtils.notBlank(filter)) {
      query.filter(filter);
      query.bind(params);
    }
    return query.count();
  }

  /**
   * Method to fetch records for given domain using filter and paginations
   *
   * @param klass Domain to search
   * @param filter domain filter
   * @param params named params for domain filter
   * @param sort sorting spec see {@link Query#order(String)} for more
   * @param limit
   * @param page
   * @return resultset fetched from Database
   */
  protected <T extends Model> List<T> fetch(
      Class<T> klass, String filter, Map<String, Object> params, String sort, int limit, int page) {
    Query<T> query = Query.of(klass);
    int offset = (page - 1) * limit;
    if (StringUtils.notBlank(sort)) {
      query.order(sort);
    }
    if (StringUtils.notBlank(filter)) {
      query.filter(filter);
      query.bind(params);
    }
    return query.fetch(limit, offset);
  }

  /** Method to get connected User's Partner */
  protected Partner getPartner() {
    return Beans.get(UserService.class).getUserPartner();
  }

  protected AppPortal getPortalApp() throws AxelorException {
    AppPortal app = Beans.get(AppPortalRepository.class).all().fetchOne();
    if (app == null || !app.getApp().getActive()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Please install axelor portal app"));
    }
    return app;
  }
}
