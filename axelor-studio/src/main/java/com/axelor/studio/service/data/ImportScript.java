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
package com.axelor.studio.service.data;

import java.util.Map;

import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.WkfTransition;

public class ImportScript {
	
	
	public Object updateTransitionNode(Object bean, Map<String, Object> values){
		
		WkfTransition transition = (WkfTransition)bean;
		
		WkfNode source = transition.getSource();
		if(source != null){
			source.addOutgoing(transition);
		}
		
		WkfNode target = transition.getTarget();
		if(target != null){
			target.addIncomming(transition);
		}
		
		
		return transition;
	}
}