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

import com.axelor.apps.stock.db.StockMoveLine;

public class StockMoveServiceImpl implements StockMoveService {

	@Override
	public List<StockMoveLine> addSubLines(List<StockMoveLine> moveLines) {
		
		if (moveLines == null) {
            return moveLines;
        }
        
        List<StockMoveLine> lines = new ArrayList<StockMoveLine>();
        lines.addAll(moveLines);
        for (StockMoveLine line : lines) {
            if (line.getSubLineList() == null) {
                continue;
            }
            for (StockMoveLine subLine : line.getSubLineList()) {
                if (subLine.getStockMove() == null) {
                	moveLines.add(subLine);
                }
            }
            for (StockMoveLine subLine : lines) {
                if (subLine.getParentLine() != null && subLine.getParentLine().getId() == line.getId() 
                        && !line.getSubLineList().contains(subLine)) {
                	moveLines.remove(subLine);
                }
            }
        }
		return lines;
	}

	@Override
	public List<StockMoveLine> removeSubLines(List<StockMoveLine> moveLines) {
	
		if (moveLines == null) {
			return moveLines;
		}
		
		List<StockMoveLine> packLines = moveLines.stream()
				.filter(it->it.getLineTypeSelect() == 2).collect(Collectors.toList());
		Iterator<StockMoveLine> lines = moveLines.iterator();
		
		while (lines.hasNext()) {
			StockMoveLine subLine = lines.next();
			if (subLine.getId() != null 
					&& subLine.getParentLine() != null
					&& !packLines.contains(subLine.getParentLine())) {
				lines.remove();
			}
		}
		return packLines;
	}
}
