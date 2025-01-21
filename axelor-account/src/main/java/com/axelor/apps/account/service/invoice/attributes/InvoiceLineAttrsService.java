/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import java.util.Map;

public interface InvoiceLineAttrsService {

  void addInTaxPriceScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);

  void addExTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);

  void addInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);

  void addCompanyExTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);

  void addCompanyInTaxTotalScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);

  void addCoefficientScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix);

  void addTaxLineSetDomain(int operationTypeSelect, Map<String, Map<String, Object>> attrsMap);
}
