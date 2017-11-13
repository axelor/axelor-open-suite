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
package com.axelor.studio.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.ActionBuilderView;
import com.google.inject.Inject;

public class MenuBuilderController {
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	public void fetchMenu(ActionRequest request, ActionResponse response) {
		
		Context context = request.getContext();
		
		Map<String, Object> values = null;
		Map<String, Object> existingMenu = (Map<String, Object>) context.get("existingMenu");
		if (existingMenu == null) {
			values = getEmptyMenu();
		}
		else {
			Long menuId = Long.parseLong(existingMenu.get("id").toString());
			values = getMenu(metaMenuRepo.find(menuId));
		}
		
		response.setValues(values);
		
	}

	private Map<String, Object> getMenu(MetaMenu menu) {
		
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("name", menu.getName());
		values.put("title", menu.getTitle());
		values.put("icon", menu.getIcon());
		values.put("iconBackground", menu.getIconBackground());
		values.put("order", menu.getOrder());
		values.put("conditionToCheck", menu.getConditionToCheck());
		values.put("parentMenu", menu.getParent());
		values.put("tag", menu.getTag());
		values.put("tagGet", menu.getTagGet());
		values.put("tagCount", menu.getTagCount());
		values.put("tagStyle", menu.getTagStyle());
		values.put("groups", menu.getGroups());
		values.put("top", menu.getTop());
		values.put("roles", menu.getRoles());
		values.put("conditionTocheck", menu.getConditionToCheck());
		values.put("link", menu.getLink());
		values.put("left", menu.getLeft());
		values.put("mobile", menu.getMobile());
		values.put("hidden", menu.getHidden());
		
		if (menu.getAction() != null && menu.getAction().getType().contentEquals("action-view")) {
			ActionBuilder actionBuilder = createActionBuilder(menu.getAction());
			if (actionBuilder != null) {
				values.put("actionBuilder", actionBuilder);
				values.put("showAction", true);
			}
		}
		
		return values;
	}

	private ActionBuilder createActionBuilder(MetaAction metaAction) {
		
		try {
			ObjectViews objectViews = XMLViews.fromXML(metaAction.getXml());
			List<Action> actions = objectViews.getActions();
			if (actions != null && !actions.isEmpty()) {
				ActionView action = (ActionView) actions.get(0);
				if (action.getModel() != null && action.getModel().contentEquals(MetaJsonRecord.class.getName())) {
					return null;
				}
				ActionBuilder actionBuilder = new ActionBuilder(action.getName());
				actionBuilder.setTitle(action.getTitle());
				actionBuilder.setModel(action.getModel());
				actionBuilder.setTypeSelect(3);
				String domain = action.getDomain();
				actionBuilder.setDomainCondition(domain);
				for (ActionView.View view : action.getViews()) {
					ActionBuilderView builderView = new ActionBuilderView();
					builderView.setViewType(view.getType());
					builderView.setViewName(view.getName());
					actionBuilder.addActionBuilderView(builderView);
				}
				if (action.getParams() != null) {
					for (ActionView.Param param : action.getParams()) {
						ActionBuilderLine paramLine = new ActionBuilderLine();
						paramLine.setName(param.getName());
						paramLine.setValue(param.getValue());
						actionBuilder.addViewParam(paramLine);
					}
				}
				if (action.getContext() != null) {
					for (ActionView.Context ctx : (List<ActionView.Context>)action.getContext()) {
						ActionBuilderLine ctxLine = new ActionBuilderLine();
						ctxLine.setName(ctx.getName());
						if (ctx.getName().contentEquals("jsonModel") && domain != null && domain.contains("self.jsonModel = :jsonModel") ) {
							actionBuilder.setIsJson(true);
							actionBuilder.setModel(ctx.getExpression());
						}
						ctxLine.setValue(ctx.getExpression());
						actionBuilder.addLine(ctxLine);
					}
				}
				
				return actionBuilder;
				
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		
		
		
		return null;
	}

	private Map<String, Object> getEmptyMenu() {
		
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("name", null);
		values.put("title", null);
		values.put("icon", null);
		values.put("iconBackground", null);
		values.put("order", null);
		values.put("conditionToCheck", null);
		values.put("parentMenu", null);
		values.put("tag", null);
		values.put("tagGet", null);
		values.put("tagCount", null);
		values.put("tagStyle", null);
		values.put("groups", null);
		values.put("top", null);
		values.put("roles", null);
		values.put("conditionTocheck", null);
		values.put("link", null);
		values.put("left", null);
		values.put("mobile", null);
		values.put("hidden", null);
		values.put("actionBuilder", null);
		values.put("showAction", false);
		
		return values;
	}
}
