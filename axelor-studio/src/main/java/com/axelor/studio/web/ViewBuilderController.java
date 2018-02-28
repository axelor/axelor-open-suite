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
package com.axelor.studio.web;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.repo.ActionBuilderRepo;
import com.axelor.studio.service.ViewLoaderService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

public class ViewBuilderController {

	private Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	private ViewLoaderService viewLoaderService;

	@Inject
	private MetaActionRepository metaActionRepo;

	@Inject
	private ActionBuilderRepo actionBuilderRepo;

	public void loadPanels(ActionRequest request, ActionResponse response) {

		ViewBuilder viewBuilder = request.getContext()
				.asType(ViewBuilder.class);

		viewBuilder = viewLoaderService.loadMetaView(viewBuilder);

		response.setValue("viewPanelList", viewBuilder.getViewPanelList());
		response.setValue("viewSidePanelList",
				viewBuilder.getViewSidePanelList());

	}

	public void loadFields(ActionRequest request, ActionResponse response) {

		ViewBuilder viewBuilder = request.getContext()
				.asType(ViewBuilder.class);

		viewBuilder = viewLoaderService.loadFields(viewBuilder);

		response.setValue("viewItemList", viewBuilder.getViewItemList());
	}

	public boolean hasSelection(MetaField metaField) {

		try {
			Mapper mapper = Mapper.of(Class.forName(metaField.getMetaModel()
					.getFullName()));
			Property property = mapper.getProperty(metaField.getName());
			log.debug("Selection name: {}", property.getSelection());
			if (!Strings.isNullOrEmpty(property.getSelection())) {
				return true;
			}
		} catch (ClassNotFoundException e) {
			log.debug(e.getMessage());
		}
		return false;

	}

	public void openViewEditor(ActionRequest request, ActionResponse response) {

		ViewBuilder viewBuilder = request.getContext()
				.asType(ViewBuilder.class);

		String baseUrl = AppSettings.get().getBaseURL();

		String url = baseUrl + "/studio/#/View/" + viewBuilder.getId();

		Map<String, Object> mapView = new HashMap<String, Object>();
		mapView.put("title", I18n.get("View editor"));
		mapView.put("resource", url);
		mapView.put("viewType", "html");
		response.setView(mapView);

	}

	public void getActions(ActionRequest request, ActionResponse response) {
		Context context = request.getContext();
		log.debug("Action context: {}", context);

		List<String> actions = new ArrayList<String>();

		String query = "LOWER(self.name) LIKE ?1";

		List<MetaAction> metaActions = metaActionRepo.all()
				.filter(query, "%" + context.get("action") + "%").fetch();
		for (MetaAction action : metaActions) {
			actions.add(action.getName());
		}

		List<ActionBuilder> actionBuilders = actionBuilderRepo.all()
				.filter(query, "%" + context.get("action") + "%").fetch();
		for (ActionBuilder action : actionBuilders) {
			actions.add(action.getName());
		}

		log.debug("Actions found : {}", actions);
		response.setValue("actions", ImmutableSet.copyOf(actions));
	}

}