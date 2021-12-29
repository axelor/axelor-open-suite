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
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.apps.portal.service.response.generator.ResponseGenerator;
import com.axelor.common.StringUtils;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
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

public class ProductCategoryWebService extends AbstractWebService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse fetchProductCategories() throws AxelorException {
    final Map<String, Object> params = new HashMap<String, Object>();
    StringBuilder filter = new StringBuilder();
    Partner partner = Beans.get(UserService.class).getUserPartner();
    if (partner != null) {
      PartnerCategory partnerCategory = partner.getPartnerCategory();
      filter.append(
          "self.id IN (SELECT productCategory FROM Product product WHERE :partnerCategory MEMBER OF product.partnerCategorySet OR size(product.partnerCategorySet) = 0)");
      params.put("partnerCategory", partnerCategory);
    } else {
      filter.append("self.id = 0");
    }
    List<ProductCategory> categories =
        Query.of(ProductCategory.class).filter(filter.toString()).bind(params).fetch();
    Beans.get(JpaSecurity.class)
        .check(
            AccessType.READ,
            ProductCategory.class,
            categories.stream().map(ProductCategory::getId).toArray(Long[]::new));
    ResponseGenerator generator = ResponseGeneratorFactory.of(ProductCategory.class.getName());
    List<Map<String, Object>> data =
        categories.stream().map(generator::generate).collect(Collectors.toList());
    PortalRestResponse response = new PortalRestResponse();
    return response.setData(data).success();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{categoryId}/products")
  public PortalRestResponse fetchProductsByCategory(
      @PathParam("categoryId") Long categoryId,
      @QueryParam("sort") String sort,
      @QueryParam("page") int page,
      @QueryParam("limit") int limit,
      @QueryParam("searchInput") String searchInput)
      throws AxelorException {
    final Map<String, Object> params = new HashMap<>();
    StringBuilder filter = new StringBuilder();
    filter.append("self.displayOnPortal = true AND self.productCategory.id = :categoryId");
    params.put("categoryId", categoryId);
    Partner partner = Beans.get(UserService.class).getUserPartner();
    if (partner != null) {
      PartnerCategory partnerCategory = partner.getPartnerCategory();
      filter.append(
          " AND (:partnerCategory MEMBER OF self.partnerCategorySet OR size(self.partnerCategorySet) = 0)");
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
            ProductCategory.class,
            products.stream().map(Product::getId).toArray(Long[]::new));

    ResponseGenerator generator = ResponseGeneratorFactory.of(Product.class.getName());
    List<Map<String, Object>> data =
        products.stream().map(generator::generate).collect(Collectors.toList());

    PortalRestResponse response = new PortalRestResponse();
    response.setTotal(totalQuery(Product.class, filter.toString(), params));
    response.setOffset((page - 1) * limit);
    return response.setData(data).success();
  }
}
