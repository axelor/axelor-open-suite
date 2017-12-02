/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;

public class AppPrestaShopServiceImpl implements AppPrestaShopService {
	
	/**
	 * Check connection with prestashop
	 * 
	 * @param ps object of AppPrestashop contains configuration details
	 * @return true or false
	 */
	@Override
	public boolean connection(AppPrestashop ps) {
		
		String shopUrl = ps.getPrestaShopUrl();
		String key = ps.getPrestaShopKey();
		
		try {
			char end = shopUrl.charAt(shopUrl.length() - 1);

			if (end == '/')
				return false;
			
			PSWebServiceClient ws = new PSWebServiceClient(shopUrl + "/api", key);
			HashMap<String, Object> opt = new HashMap<String, Object>();
			opt.put("url", shopUrl + "/api");
			ws.get(opt);
			return true;
			
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Check prestashop url is valid or not
	 * 
	 * @param ps  object of AppPrestashop contains configuration details
	 * @return true or false
	 */
	@Override
	public boolean urlTest(AppPrestashop ps) {
		
		String url = null; 
		url = ps.getPrestaShopUrl();		
		
		if (url == null)
			return true;
		
		char end = url.charAt(url.length() - 1);

		if(end == '/')
			return true;

		return false;
	}

}
