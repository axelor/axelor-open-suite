/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.MinStockRules;
import com.axelor.apps.stock.db.repo.MinStockRulesRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MinStockRulesServiceImpl implements MinStockRulesService  {

	protected LocalDate today;

	protected User user;
	
	@Inject
	protected MinStockRulesRepository minStockRuleRepo;

	@Inject
	public MinStockRulesServiceImpl() {

		this.today = Beans.get(GeneralService.class).getTodayDate();
		this.user = AuthUtils.getUser();
	}


	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generatePurchaseOrder(Product product, BigDecimal qty, LocationLine locationLine, int type) throws AxelorException  {

		Location location = locationLine.getLocation();

		//TODO à supprimer après suppression des variantes
		if(location == null)  {
			return;
		}

		MinStockRules minStockRules = this.getMinStockRules(product, location, type);

		if(minStockRules == null)  {
			return;
		}

		if(this.useMinStockRules(locationLine, minStockRules, qty, type))  {

			if(minStockRules.getOrderAlertSelect() == MinStockRulesRepository.ORDER_ALERT_ALERT)  {

				//TODO
			}


		}

	}


	@Override
	public boolean useMinStockRules(LocationLine locationLine, MinStockRules minStockRules, BigDecimal qty, int type)  {

		BigDecimal currentQty = locationLine.getCurrentQty();
		BigDecimal futureQty = locationLine.getFutureQty();

		BigDecimal minQty = minStockRules.getMinQty();

		if(type == MinStockRulesRepository.TYPE_CURRENT)  {

			if(currentQty.compareTo(minQty) >= 0 && (currentQty.subtract(qty)).compareTo(minQty) == -1)  {
				return true;
			}

		}
		else  if(type == MinStockRulesRepository.TYPE_FUTURE){

			if(futureQty.compareTo(minQty) >= 0 && (futureQty.subtract(qty)).compareTo(minQty) == -1)  {
				return true;
			}

		}
		return false;

	}

	@Override
	public MinStockRules getMinStockRules(Product product, Location location, int type)  {

		return minStockRuleRepo.all().filter("self.product = ?1 AND self.location = ?2 AND self.typeSelect = ?3", product, location, type).fetchOne();

		//TODO , plusieurs régles min de stock par produit (achat a 500 et production a 100)...

	}


}
