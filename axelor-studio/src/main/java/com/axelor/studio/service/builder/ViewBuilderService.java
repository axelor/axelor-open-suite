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
package com.axelor.studio.service.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.actions.ActionRecord;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.service.StudioMetaService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * This service class handle all view side processing. It call recording of
 * different types of ViewBuilder. Provides common method to create view and
 * action xml for all types of view.
 * 
 * @author axelor
 *
 */
public class ViewBuilderService {

	@Inject
	private MetaViewRepository metaViewRepo;

	@Inject
	private ChartBuilderService chartBuilderService;

	@Inject
	private DashboardBuilderService dashboardBuilderService;

	@Inject
	private StudioMetaService metaService;

	/**
	 * Method to process ViewBuilder according to its type. It will call method
	 * to create view xml and generate MetaView.
	 * 
	 * @param model
	 *            Name of model.
	 * @param viewBuilders
	 *            List of ViewBuilders to process.
	 * @throws JAXBException
	 *             Xml processing exception
	 * @throws IOException
	 *             File handling exception
	 */
	@Transactional
	public void build(ViewBuilder viewBuilder) {

		AbstractView view = null;
		List<Action> actions = new ArrayList<Action>();
		
		try {
			switch (viewBuilder.getViewType()) {
				case "chart":
					view = chartBuilderService.getView(viewBuilder);
					ActionRecord actionRecord = chartBuilderService
							.getOnNewAction();
					if (actionRecord != null) {
						actions.add(actionRecord);
					}
					break;
				case "dashboard":
					view = dashboardBuilderService.getView(viewBuilder);
					actions.addAll(dashboardBuilderService.getActions());
					break;
			}
	
			if (view != null) {
				buildView(viewBuilder, view, actions);
			}
		
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	private void buildView(ViewBuilder viewBuilder, AbstractView view, List<Action> actions) {
		
		MetaView metaView = metaService.generateMetaView(view);
		viewBuilder.setMetaViewGenerated(metaView);
		metaViewRepo.save(metaView);
		
		for (Action action : actions) {
			if (action == null) {
				continue;
			}
			Class<?> klass = action.getClass();
			String type = klass.getSimpleName()
					.replaceAll("([a-z\\d])([A-Z]+)", "$1-$2")
					.toLowerCase();
			
			String xml = XMLViews.toXml(action, true);
			
			metaService.updateMetaAction(action.getName(), type, xml);
		}
	}

}