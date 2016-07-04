package com.axelor.studio.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		return action.evaluate(handler);

	}

	private ActionHandler createHandler(Action action,
			Map<String, Object> context, Map<String, Object> _parent) {

		log.debug("Context : {}, Model: {}", context, action.getModel());

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
