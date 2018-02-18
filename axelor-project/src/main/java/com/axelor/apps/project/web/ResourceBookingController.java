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
package com.axelor.apps.project.web;

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;

@Singleton
public class ResourceBookingController {
	
	public void openResourceBooking(ActionRequest request, ActionResponse response) {
		
		Context context = request.getContext();
		
		Long resoureTypeId = null;
		if (context.get("$resourceType") != null) {
			resoureTypeId = (Long) ((Map<String,Object>)context.get("$resourceType")).get("id");
		}
		BigDecimal priceFrom = null;
		if (context.get("$priceFrom") != null) {
			priceFrom = (BigDecimal) context.get("$priceFrom");
		}
		
		BigDecimal priceTo = null;
		if (context.get("$priceTo") != null) {
			priceTo = (BigDecimal) context.get("$priceTo");
		}
		
		response.setView(ActionView.define("Booking")
			.add("html", "/project-pro/resource-booking?resourceTypeId="
					+ resoureTypeId + "&" + "priceFrom=" + priceFrom + "&" + "priceTo=" + priceTo)
			.map());
		
	}
}
