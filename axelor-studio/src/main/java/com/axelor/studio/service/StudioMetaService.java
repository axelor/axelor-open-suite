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
package com.axelor.studio.service;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.studio.service.wkf.WkfTrackingService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StudioMetaService {
	
	private final Logger log = LoggerFactory.getLogger(StudioMetaService.class);
	
	@Inject
	private MetaActionRepository metaActionRepo;
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	/**
	 * Remove MetaActions from comma separated names in string.
	 * 
	 * @param actionNames
	 *            Comma separated string of action names.
	 */
	@Transactional
	public void removeMetaActions(String actionNames) {

		if (actionNames != null) {
			actionNames = actionNames.replaceAll(WkfTrackingService.ACTION_OPEN_TRACK, "").replaceAll(WkfTrackingService.ACTION_TRACK,"");
			List<MetaAction> metaActions = metaActionRepo
					.all()
					.filter("self.name in ?1",
							Arrays.asList(actionNames.split(","))).fetch();

			for (MetaAction action : metaActions) {
				metaActionRepo.remove(action);
			}
		}
	}
	
	@Transactional
	public void updateMetaAction(String actionName, String actionType,
			String xml, String model) {

		MetaAction action = metaActionRepo.findByName(actionName);

		if (action == null) {
			action = new MetaAction(actionName);
		}
		action.setType(actionType);
		action.setModel(model);
		action.setXml(xml);
		action = metaActionRepo.save(action);

	}
	
	/**
	 * Create or Update metaView from AbstractView.
	 * 
	 * @param viewIterator
	 *            ViewBuilder iterator
	 */
	@Transactional
	public MetaView generateMetaView(AbstractView view) {

		String name = view.getName();
		String xmlId = view.getXmlId();
		String model = view.getModel();
		String viewType = view.getType();

		log.debug("Search view name: {}, xmlId: {}", name, xmlId);

		MetaView metaView;
		if (xmlId != null) {
			metaView = metaViewRepo
					.all()
					.filter("self.name = ?1 and self.xmlId = ?2 and self.type = ?3",
							name, xmlId, viewType).fetchOne();
		} else {
			metaView = metaViewRepo
					.all()
					.filter("self.name = ?1 and self.type = ?2", name, viewType)
					.fetchOne();
		}

		log.debug("Meta view found: {}", metaView);

		if (metaView == null) {
			metaView = metaViewRepo
					.all()
					.filter("self.name = ?1 and self.type = ?2", name, viewType)
					.order("-priority").fetchOne();
			Integer priority = 20;
			if (metaView != null) {
				priority = metaView.getPriority() + 1;
			}
			metaView = new MetaView();
			metaView.setName(name);
			metaView.setXmlId(xmlId);
			metaView.setModel(model);
			metaView.setPriority(priority);
			metaView.setType(viewType);
			metaView.setTitle(view.getTitle());
		}

		String viewXml = XMLViews.toXml(view, true);
		metaView.setXml(viewXml.toString());
		return metaViewRepo.save(metaView);

	}
	
	public String updateAction(String oldAction, String newAction, boolean remove) {
		
		if (oldAction == null) {
			return newAction;
		}
		if (newAction == null) {
			return oldAction;
		}
		
		if (remove) {
			oldAction = oldAction.replace(newAction, "");
		}
		else if(!oldAction.contains(newAction)) {
			oldAction = oldAction + "," + newAction;
		}
		
		oldAction.replace(",,", ",");
		if (oldAction.isEmpty()) {
			return null;
		}
		
		return oldAction;
	}

}
