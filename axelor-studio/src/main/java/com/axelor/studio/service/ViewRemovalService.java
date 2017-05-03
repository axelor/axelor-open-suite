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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.MenuItem;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ViewRemovalService {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, ObjectViews> modelMap;

	@Inject
	private MetaActionRepository metaActionRepo;

	@Inject
	private MetaViewRepository metaViewRepo;

	@Inject
	private MetaMenuRepository metaMenuRepo;

	public void remove(String module, File viewDir) {

		modelMap = new HashMap<String, ObjectViews>();
		
		removeActions(module, viewDir);

		removeView(module, viewDir);

		removeMenu(module, viewDir);

		removeDashboard(module, viewDir);

		updateViewFile(viewDir);
		
	}
	
	@Transactional
	public void removeActions(String module, File viewDir) {

		List<MetaAction> metaActions = metaActionRepo.all()
				.filter("self.removeAction = true "
						+ "and self.model != null "
						+ "and self.module = ?1", module).fetch();
		log.debug("Total actions to remove: {}", metaActions.size());

		for (MetaAction metaAction : metaActions) {

			String model = metaAction.getModel();
			String actionName = metaAction.getName();

			metaActionRepo.remove(metaAction);

			model = model.substring(model.lastIndexOf(".") + 1);

			ObjectViews objectViews = getObjectViews(viewDir, model);
			if (objectViews == null) {
				continue;
			}

			List<Action> actions = objectViews.getActions();
			if (actions == null) {
				continue;
			}

			Iterator<Action> actionIter = actions.iterator();
			while (actionIter.hasNext()) {
				Action action = actionIter.next();
				if (actionName.equals(action.getName())) {
					log.debug("Action to remove from xml file: {}",
							action.getName());
					actionIter.remove();
				}
			}
			
			modelMap.put(model, objectViews);

		}
	}

	@Transactional
	public void removeView(String module, File viewDir) {

		List<MetaView> metaViews = metaViewRepo.all()
				.filter("self.removeView = true "
						+ "and self.module = ?1", module).fetch();
		log.debug("Total views to remove: {}", metaViews.size());

		for (MetaView metaView : metaViews) {

			String model = metaView.getModel();
			if (model == null) {
				continue;
			}
			model = model.substring(model.lastIndexOf(".") + 1);
			
			String viewName = metaView.getName();
			metaViewRepo.remove(metaView);
			
			ObjectViews objectViews = getObjectViews(viewDir, model);
			if (objectViews == null) {
				continue;
			}

			List<AbstractView> views = objectViews.getViews();
			if (views == null) {
				continue;
			}

			Iterator<AbstractView> viewIter = views.iterator();
			while (viewIter.hasNext()) {
				AbstractView view = viewIter.next();
				if (viewName.equals(view.getName())) {
					viewIter.remove();
				}
			}
			
			modelMap.put(model, objectViews);
		}

	}

	@Transactional
	public void removeMenu(String module, File viewDir) {

		List<MetaMenu> metaMenus = metaMenuRepo.all()
				.filter("self.removeMenu = true "
						+ "and self.module = ?1", module).fetch();
		log.debug("Total menus to remove: {}", metaMenus.size());

		List<String> menuNames = new ArrayList<String>();
		List<String> actionNames = new ArrayList<String>();
		for (MetaMenu metaMenu : metaMenus) {
			menuNames.add(metaMenu.getName());
			MetaAction metaAction = metaMenu.getAction();
			if (metaAction != null) {
				actionNames.add(metaAction.getName());
				metaActionRepo.remove(metaAction);
			}
			metaMenuRepo.remove(metaMenu);
		}

		ObjectViews objectViews = getObjectViews(viewDir, "Menu");

		if (objectViews != null) {

			List<MenuItem> menus = objectViews.getMenus();
			if (menus != null) {
				Iterator<MenuItem> menuIter = menus.iterator();
				while (menuIter.hasNext()) {
					MenuItem menuItem = menuIter.next();
					if (menuNames.contains(menuItem.getName())) {
						menuIter.remove();
					}
				}
			}

			List<Action> actions = objectViews.getActions();
			if (actions != null) {
				Iterator<Action> actionIter = actions.iterator();
				while (actionIter.hasNext()) {
					Action action = actionIter.next();
					if (actionNames.contains(action.getName())) {
						actionIter.remove();
					}
				}
			}
			
			modelMap.put("Menu", objectViews);

		}
		

	}

	@Transactional
	public void removeDashboard(String module, File viewDir) {

		List<MetaAction> metaActions = metaActionRepo.all()
				.filter("self.removeAction = true "
						+ "and self.model is null "
						+ "and self.module = ?1", module).fetch();
		log.debug("Total dashoboard actions to remove: {}", metaActions.size());

		List<String> actionNames = new ArrayList<String>();
		for (MetaAction action : metaActions) {
			actionNames.add(action.getName());
			metaActionRepo.remove(action);
		}

		ObjectViews objectViews = getObjectViews(viewDir, "Dashboard");
		if (objectViews == null) {
			return;
		}

		List<Action> actions = objectViews.getActions();
		if (actions != null) {
			Iterator<Action> actionIter = actions.iterator();
			while (actionIter.hasNext()) {
				Action action = actionIter.next();
				if (actionNames.contains(action.getName())) {
					actionIter.remove();
				}
			}
		}
		
		modelMap.put("Dashboard", objectViews);

	}

	private void updateViewFile(File viewDir) {

		try {
			for (String model : modelMap.keySet()) {

				ObjectViews objectViews = modelMap.get(model);
				File viewFile = new File(viewDir, model + ".xml");
				if (checkEmpty(objectViews)) {
					viewFile.delete();
					continue;
				}
				else {
					log.debug("Not empty model : {}", model);
				}

				StringWriter xmlWriter = new StringWriter();
				XMLViews.marshal(objectViews, xmlWriter);

				FileWriter fileWriter = new FileWriter(viewFile);
				fileWriter.write(xmlWriter.toString());
				fileWriter.close();
				xmlWriter.close();
			}

		} catch (IOException | JAXBException e) {
			log.debug("Exception :{}", e.getMessage());
			e.printStackTrace();
		}

	}

	private ObjectViews getObjectViews(File viewDir, String model) {

		if (modelMap.containsKey(model)) {
			return modelMap.get(model);
		}

		try {
			File viewFile = new File(viewDir, model + ".xml");
			if (viewFile.exists()) {
				String xml = Files.toString(viewFile, Charsets.UTF_8);
				ObjectViews objectViews = XMLViews.fromXML(xml);
				modelMap.put(model, objectViews);
				return objectViews;
			}
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean checkEmpty(ObjectViews objectViews) {

		if (objectViews.getActionMenus() != null && !objectViews.getActionMenus().isEmpty()) {
			log.debug("Action menu exist");
			return false;
		}

		if (objectViews.getActions() != null && !objectViews.getActions().isEmpty()) {
			log.debug("Actions exist");
			return false;
		}

		if (objectViews.getMenus() != null && !objectViews.getMenus().isEmpty()) {
			log.debug("Menu exist");
			return false;
		}

		if (objectViews.getSelections() != null && !objectViews.getSelections().isEmpty()) {
			log.debug("Selection exist");
			return false;
		}

		if (objectViews.getViews() != null && !objectViews.getViews().isEmpty()) {
			log.debug("Views exist");
			return false;
		}

		return true;
	}

}
