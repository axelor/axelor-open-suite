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

import com.axelor.apps.portal.service.SaleOrderPortalService;
import com.axelor.apps.portal.service.paybox.PayboxErrorConstants;
import com.axelor.apps.portal.service.paybox.PayboxService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.tools.ObjectTool;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.ImmutableMap;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

@Path("/public")
public class PayboxValidationService extends AbstractWebService {

  private static final String PAYBOX_REFERENCE_PARAM = "reference";
  private static final String PAYBOX_ERROR_PARAM = "error";

  @GET
  @Path("/paybox-validate")
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse validate(@Context UriInfo uriInfo)
      throws AxelorException, UnsupportedEncodingException {
    String path = uriInfo.getPath();
    MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters(false);

    validateRequest(path, queryParameters);

    SaleOrder order = findOrder(path, queryParameters);
    Beans.get(SaleOrderPortalService.class).completeOrder(order, null);

    PAYBOX_ORDER.invalidate(
        URLDecoder.decode(
            queryParameters.getFirst(PAYBOX_REFERENCE_PARAM), StandardCharsets.UTF_8.name()));

    PortalRestResponse response = new PortalRestResponse();
    return response.setData(ImmutableMap.of("id", order.getId())).success();
  }

  private void validateRequest(String path, MultivaluedMap<String, String> queryParameters)
      throws AxelorException {

    final String error = queryParameters.getFirst(PAYBOX_ERROR_PARAM);
    final PayboxService payboxService = Beans.get(PayboxService.class);

    // Verify sign with data to check data inetgrity and request source
    if (!payboxService.checkSignature(queryParameters)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("Public key validation failed at %s"),
          path);
    }

    // Check transaction errors
    if (ObjectUtils.notEmpty(error)
        && !PayboxErrorConstants.CODE_ERROR_OPERATION_SUCCESSFUL.equals(error)) {
      payboxService.checkError(error);
    }
  }

  private SaleOrder findOrder(String path, MultivaluedMap<String, String> queryParameters)
      throws AxelorException, UnsupportedEncodingException {

    final String reference = queryParameters.getFirst(PAYBOX_REFERENCE_PARAM);
    if (ObjectUtils.isEmpty(reference)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, I18n.get("No reference present in %s"), path);
    }

    final String orderReference = URLDecoder.decode(reference, StandardCharsets.UTF_8.name());

    Map<String, Object> values = PAYBOX_ORDER.getIfPresent(orderReference);
    if (ObjectUtils.isEmpty(values)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("No values found for reference %s"),
          orderReference);
    }

    return ObjectTool.toBean(SaleOrder.class, values);
  }
}
