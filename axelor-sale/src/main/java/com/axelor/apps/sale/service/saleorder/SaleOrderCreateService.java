/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorder;

import java.time.LocalDate;
import java.util.List;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.team.db.Team;
import com.google.inject.persist.Transactional;

public interface SaleOrderCreateService {


	public SaleOrder createSaleOrder(User buyerUser, Company company, Partner contactPartner, Currency currency,
			LocalDate deliveryDate, String internalReference, String externalReference, LocalDate orderDate,
			PriceList priceList, Partner clientPartner, Team team) throws AxelorException;

	public SaleOrder createSaleOrder(Company company) throws AxelorException;

	public SaleOrder mergeSaleOrders(List<SaleOrder> saleOrderList, Currency currency, Partner clientPartner, Company company, Partner contactPartner, PriceList priceList, Team team) throws AxelorException;

	@Transactional
	public SaleOrder createTemplate(SaleOrder context);

	@Transactional
	public SaleOrder createSaleOrder(SaleOrder context);


}
