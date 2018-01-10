/*
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
package com.axelor.apps.supplychain.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import com.axelor.apps.account.db.InvoiceLine;

public class InvoiceServiceImpl implements InvoiceService {

	@Override
	public List<InvoiceLine> addSubLines(List<InvoiceLine> invoiceLine) {
		
		if (invoiceLine == null) {
            return invoiceLine;
        }
        
        List<InvoiceLine> lines = new ArrayList<InvoiceLine>();
        lines.addAll(invoiceLine);
        for (InvoiceLine line : lines) {
            if (line.getSubLineList() == null) {
                continue;
            }
            for (InvoiceLine subLine : line.getSubLineList()) {
                if (subLine.getSaleOrder() == null) {
                	invoiceLine.add(subLine);
                }
            }
            for (InvoiceLine subLine : lines) {
                if (subLine.getParentLine() != null && subLine.getParentLine().getId() == line.getId() 
                        && !line.getSubLineList().contains(subLine)) {
                	invoiceLine.remove(subLine);
                }
            }
        }
		return invoiceLine;
	}
	
	@Override
	public List<InvoiceLine> removeSubLines(List<InvoiceLine> invoiceLine) {
		
		if (invoiceLine == null) {
            return invoiceLine;
        }

        List<InvoiceLine> packLines = invoiceLine.stream()
                .filter(it->it.getTypeSelect() == 2).collect(Collectors.toList());

        Iterator<InvoiceLine> lines = invoiceLine.iterator();

        while (lines.hasNext()) {
        	InvoiceLine subLine = lines.next();
            if (subLine.getId() != null 
                    && subLine.getParentLine() != null
                    && !packLines.contains(subLine.getParentLine())) {
                lines.remove();
            }
        }
        return invoiceLine;
	}
}
