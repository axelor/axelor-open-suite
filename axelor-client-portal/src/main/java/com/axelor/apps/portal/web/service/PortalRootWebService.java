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

import com.axelor.inject.Beans;
import javax.ws.rs.Path;

/** root (/portal) web-service class for Portal web-services */
@Path("/portal")
public class PortalRootWebService {

  @Path("/profile")
  public AbstractWebService profile() {
    return Beans.get(ProfileWebService.class);
  }

  @Path("/payment-cards")
  public AbstractWebService paymentCards() {
    return Beans.get(PaymentCardWebService.class);
  }

  @Path("/config")
  public AbstractWebService getConfig() {
    return Beans.get(AppPortalWebService.class);
  }

  @Path("/addresses")
  public AbstractWebService getAddresses() {
    return Beans.get(AddressWebService.class);
  }

  @Path("/countries")
  public AbstractWebService getCountry() {
    return Beans.get(CountryWebService.class);
  }

  @Path("/cities")
  public AbstractWebService getCity() {
    return Beans.get(CityWebService.class);
  }

  @Path("/metafile")
  public AbstractWebService getMetaFile() {
    return Beans.get(MetaFileWebService.class);
  }

  @Path("/products")
  public AbstractWebService getProduct() {
    return Beans.get(ProductWebService.class);
  }

  @Path("/categories")
  public AbstractWebService getProductCategory() {
    return Beans.get(ProductCategoryWebService.class);
  }

  @Path("/orders")
  public AbstractWebService getOrders() {
    return Beans.get(SaleOrderWebService.class);
  }

  @Path("/quotations")
  public AbstractWebService getQuotation() {
    return Beans.get(SaleOrderQuotWebService.class);
  }

  @Path("/cart")
  public AbstractWebService getCart() {
    return Beans.get(CartWebService.class);
  }

  @Path("/invoices")
  public AbstractWebService getInvoices() {
    return Beans.get(InvoiceWebService.class);
  }

  @Path("/translate")
  public AbstractWebService getTranslations() {
    return Beans.get(TranslationService.class);
  }
}
