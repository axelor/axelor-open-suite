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
import com.axelor.apps.base.db.PartnerCategory;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.apps.portal.service.response.generator.ResponseGenerator;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
public class ProductWebService extends AbstractWebService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse fetchProducts(
      @QueryParam("sort") String sort,
      @QueryParam("page") int page,
      @QueryParam("limit") int limit,
      @QueryParam("searchInput") String searchInput)
      throws AxelorException {
    final Map<String, Object> params = new HashMap<String, Object>();
    StringBuilder filter = new StringBuilder();
    filter.append("self.displayOnPortal = true");
    Partner partner = Beans.get(UserService.class).getUserPartner();
    if (partner != null) {
      PartnerCategory partnerCategory = partner.getPartnerCategory();
      filter.append(
          " AND ((:partnerCategory MEMBER OF self.partnerCategorySet OR size(self.partnerCategorySet) = 0) "
              + "OR (:partnerCategory MEMBER OF self.productCategory.partnerCategorySet))");
      params.put("partnerCategory", partnerCategory);
    } else {
      filter.append(" AND size(self.partnerCategorySet) = 0");
    }
    if (StringUtils.notBlank(searchInput)) {
      filter.append(
          " AND (lower(self.name) like :pattern OR lower(self.description) like :pattern)");
      params.put("pattern", String.format("%%%s%%", searchInput.toLowerCase()));
    }

    List<Product> products = fetch(Product.class, filter.toString(), params, sort, limit, page);

    Beans.get(JpaSecurity.class)
        .check(
            AccessType.READ,
            Product.class,
            products.stream().map(Product::getId).toArray(Long[]::new));

    ResponseGenerator generator = ResponseGeneratorFactory.of(Product.class.getName());
    List<Map<String, Object>> data =
        products.stream().map(generator::generate).collect(Collectors.toList());

    PortalRestResponse response = new PortalRestResponse();
    response.setTotal(totalQuery(Product.class, filter.toString(), params));
    response.setOffset((page - 1) * limit);

    return response.setData(data).success();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{productId}")
  public PortalRestResponse fetchProduct(@PathParam("productId") Long productId)
      throws AxelorException {
    final Map<String, Object> params = new HashMap<String, Object>();
    StringBuilder filter = new StringBuilder();
    filter.append("self.displayOnPortal = true AND self.id = :id");
    params.put("id", productId);
    Partner partner = Beans.get(UserService.class).getUserPartner();
    if (partner != null) {
      PartnerCategory partnerCategory = partner.getPartnerCategory();
      filter.append(
          " AND ((:partnerCategory MEMBER OF self.partnerCategorySet OR size(self.partnerCategorySet) = 0) OR "
              + "(:partnerCategory MEMBER OF self.productCategory.partnerCategorySet))");
      params.put("partnerCategory", partnerCategory);
    } else {
      filter.append(" AND size(self.partnerCategorySet) = 0");
    }
    List<Product> products = fetch(Product.class, filter.toString(), params, null, 1, 1);
    if (ObjectUtils.isEmpty(products)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("Product with id (%s) not found"),
          productId);
    }
    Product product = products.get(0);
    Beans.get(JpaSecurity.class).check(AccessType.READ, Product.class, product.getId());
    Map<String, Object> data =
        ResponseGeneratorFactory.of(Product.class.getName()).generate(product);
    PortalRestResponse response = new PortalRestResponse();
    return response.setData(data).success();
  }
}
