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
package com.axelor.apps.portal.service.response;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.client.portal.db.Card;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.portal.service.response.generator.AddressResponseGenerator;
import com.axelor.apps.portal.service.response.generator.AppPortalResponseGenerator;
import com.axelor.apps.portal.service.response.generator.CardResponseGenerator;
import com.axelor.apps.portal.service.response.generator.CityResponseGenerator;
import com.axelor.apps.portal.service.response.generator.CountryResponseGenerator;
import com.axelor.apps.portal.service.response.generator.CurrencyResponseGenerator;
import com.axelor.apps.portal.service.response.generator.EmailAddressResponseGenerator;
import com.axelor.apps.portal.service.response.generator.InvoiceLineResponseGenerator;
import com.axelor.apps.portal.service.response.generator.InvoiceLineTaxResponseGenerator;
import com.axelor.apps.portal.service.response.generator.InvoicePaymentResponseGenerator;
import com.axelor.apps.portal.service.response.generator.InvoiceResponseGenerator;
import com.axelor.apps.portal.service.response.generator.PartnerAddressResponseGenerator;
import com.axelor.apps.portal.service.response.generator.PartnerResponseGenerator;
import com.axelor.apps.portal.service.response.generator.ProductCategoryResponseGenerator;
import com.axelor.apps.portal.service.response.generator.ProductResponseGenerator;
import com.axelor.apps.portal.service.response.generator.ResponseGenerator;
import com.axelor.apps.portal.service.response.generator.SaleOrderLineResponseGenerator;
import com.axelor.apps.portal.service.response.generator.SaleOrderLineTaxResponseGenerator;
import com.axelor.apps.portal.service.response.generator.SaleOrderResponseGenerator;
import com.axelor.apps.portal.service.response.generator.TaxLineResponseGenerator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;

public class ResponseGeneratorFactory {

  private static final Map<String, Class<? extends ResponseGenerator>> CUSTOMISED_MODELS =
      new HashMap<>();

  static {
    Map<String, Class<? extends ResponseGenerator>> map =
        new ImmutableMap.Builder<String, Class<? extends ResponseGenerator>>()
            .put(Partner.class.getName(), PartnerResponseGenerator.class)
            .put(Address.class.getName(), AddressResponseGenerator.class)
            .put(PartnerAddress.class.getName(), PartnerAddressResponseGenerator.class)
            .put(EmailAddress.class.getName(), EmailAddressResponseGenerator.class)
            .put(City.class.getName(), CityResponseGenerator.class)
            .put(Country.class.getName(), CountryResponseGenerator.class)
            .put(SaleOrder.class.getName(), SaleOrderResponseGenerator.class)
            .put(Currency.class.getName(), CurrencyResponseGenerator.class)
            .put(SaleOrderLine.class.getName(), SaleOrderLineResponseGenerator.class)
            .put(TaxLine.class.getName(), TaxLineResponseGenerator.class)
            .put(SaleOrderLineTax.class.getName(), SaleOrderLineTaxResponseGenerator.class)
            .put(Invoice.class.getName(), InvoiceResponseGenerator.class)
            .put(InvoiceLine.class.getName(), InvoiceLineResponseGenerator.class)
            .put(InvoiceLineTax.class.getName(), InvoiceLineTaxResponseGenerator.class)
            .put(InvoicePayment.class.getName(), InvoicePaymentResponseGenerator.class)
            .put(AppPortal.class.getName(), AppPortalResponseGenerator.class)
            .put(Product.class.getName(), ProductResponseGenerator.class)
            .put(ProductCategory.class.getName(), ProductCategoryResponseGenerator.class)
            .put(Card.class.getName(), CardResponseGenerator.class)
            .build();
    CUSTOMISED_MODELS.putAll(map);
  }

  private ResponseGeneratorFactory() {
    throw new IllegalStateException("ResponseGeneratorFactory class");
  }

  public static ResponseGenerator of(String modelConcerned) {

    if (!isValid(modelConcerned)) {
      return null;
    }

    return Beans.get(CUSTOMISED_MODELS.get(modelConcerned));
  }

  public static boolean isValid(String modelConcerned) {
    return StringUtils.notBlank(modelConcerned) && CUSTOMISED_MODELS.containsKey(modelConcerned);
  }
}
