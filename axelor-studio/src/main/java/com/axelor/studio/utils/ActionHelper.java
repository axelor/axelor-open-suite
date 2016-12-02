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
package com.axelor.studio.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.ActionHandler;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.actions.Action;
import com.axelor.rpc.ActionRequest;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class ActionHelper {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public Object execute(String name, Object entity, Object parent) {

		log.debug("Execute action: {}, object: {}", name, entity);
		Map context = null;
		if (entity instanceof Map) {
			context = (Map) entity;
		} else {
			context = Mapper.toMap(entity);
		}
		
		Action action = MetaStore.getAction(name);

		ActionHandler handler = createHandler(action, context,
				Mapper.toMap(parent));
		
		Object object = action.evaluate(handler);
		log.debug("Object id: {}", ((Model) object).getId());
		return object;

	}

	private ActionHandler createHandler(Action action,
			Map<String, Object> context, Map<String, Object> _parent) {

		log.debug("Context : {}, Model: {}", context, action.getModel());
		
		log.debug("Id in context: {}", context.get("id"));
		Preconditions.checkArgument(action != null, "action is null");

		ActionRequest request = new ActionRequest();

		Map<String, Object> data = Maps.newHashMap();
		context.put("_parent", _parent);
		data.put("context", context);
		request.setData(data);
		request.setModel(action.getModel());
		request.setAction(action.getName());

		return new ActionHandler(request);

	}
}
