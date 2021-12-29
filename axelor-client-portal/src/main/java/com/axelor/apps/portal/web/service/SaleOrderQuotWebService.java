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

import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.apps.portal.service.response.generator.ResponseGenerator;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.UnauthorizedException;

@Path("/")
public class SaleOrderQuotWebService extends AbstractWebService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse fetch(
      @QueryParam("sort") String sort,
      @QueryParam("page") int page,
      @QueryParam("limit") int limit) {
    final String filter = "self.clientPartner = :partner AND self.statusSelect < :statusSelect";

    Map<String, Object> params =
        ImmutableMap.of(
            "partner", getPartner(), "statusSelect", SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    List<SaleOrder> orders = fetch(SaleOrder.class, filter, params, sort, limit, page);

    Beans.get(JpaSecurity.class)
        .check(
            AccessType.READ,
            SaleOrder.class,
            orders.stream().map(SaleOrder::getId).toArray(Long[]::new));

    ResponseGenerator generator = ResponseGeneratorFactory.of(SaleOrder.class.getName());
    List<Map<String, Object>> data =
        orders.stream().map(generator::generate).collect(Collectors.toList());

    PortalRestResponse response = new PortalRestResponse();
    response.setTotal(totalQuery(SaleOrder.class, filter, params));
    response.setOffset((page - 1) * limit);
    return response.setData(data).success();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public PortalRestResponse fetchOne(@PathParam("id") Long id) throws AxelorException {

    SaleOrder order = findById(id);

    Beans.get(JpaSecurity.class).check(AccessType.READ, SaleOrder.class, order.getId());

    Map<String, Object> data =
        ResponseGeneratorFactory.of(SaleOrder.class.getName()).generate(order);

    PortalRestResponse response = new PortalRestResponse();
    return response.setData(data).success();
  }

  @GET
  @Path("/print/{id}")
  public Response print(@PathParam("id") Long id) {
    try {

      SaleOrder order = findById(id);

      Beans.get(JpaSecurity.class).check(AccessType.READ, SaleOrder.class, order.getId());

      File report =
          Beans.get(SaleOrderPrintService.class).print(order, true, ReportSettings.FORMAT_PDF);
      return Response.ok()
          .entity(report)
          .header("Content-Disposition", "attachment;filename=" + report.getName() + ".pdf")
          .build();
    } catch (UnauthorizedException e) {
      return autherizationFail(e);
    } catch (Exception e) {
      return fail(e);
    }
  }

  private SaleOrder findById(Long id) throws AxelorException {
    final String filter =
        "self.clientPartner = :partner AND self.statusSelect < :statusSelect AND self.id = :id";
    Map<String, Object> params =
        ImmutableMap.of(
            "partner",
            getPartner(),
            "id",
            id,
            "statusSelect",
            SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    List<SaleOrder> orders = fetch(SaleOrder.class, filter, params, null, 0, 0);
    if (ObjectUtils.isEmpty(orders)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("SaleOrder with given id (%s) not found"),
          id);
    }
    return orders.get(0);
  }
}
