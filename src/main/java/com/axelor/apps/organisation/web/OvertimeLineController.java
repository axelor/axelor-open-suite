/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.organisation.web;

import com.axelor.apps.organisation.db.OvertimeLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class OvertimeLineController {

	public void computeTotal(ActionRequest request, ActionResponse response) {
		
		OvertimeLine overtimeLine = request.getContext().asType(OvertimeLine.class);
		
		if (overtimeLine.getQuantity() != null && overtimeLine.getUnitPrice() != null) {
			response.setValue("total", overtimeLine.getQuantity().multiply(overtimeLine.getUnitPrice()));
		}
		else {
			response.setValue("total", 0.00);
		}
	}
}
