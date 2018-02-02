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
package com.axelor.apps.prestashop.app;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;

public class AppPrestaShopServiceImpl implements AppPrestaShopService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * Check connection with prestashop
	 *
	 * @param ps object of AppPrestashop contains configuration details
	 * @return true or false
	 */
	@Override
	public boolean checkAccess(AppPrestashop ps) {

		String shopUrl = ps.getPrestaShopUrl();
		String key = ps.getPrestaShopKey();

		try {
			if(validateUrl(ps) == false) {
				return false;
			}

			PSWebServiceClient ws = new PSWebServiceClient(shopUrl + "/api", key);
			HashMap<String, Object> opt = new HashMap<String, Object>();
			opt.put("url", shopUrl + "/api");
			ws.get(opt);
			return true;

		} catch (Exception e) {
			logger.error("An error occured while checking Prestashop access rights", e);
			return false;
		}
	}

	@Override
	public boolean validateUrl(AppPrestashop ps) {

		String url = null;
		url = ps.getPrestaShopUrl();

		return StringUtils.isNotBlank(url) && url.endsWith("/") == false;
	}

}
