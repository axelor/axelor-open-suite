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

	@Inject
	private MetaActionRepository metaActionRepo;

	@Inject
	private MetaViewRepository metaViewRepo;

	@Inject
	private MetaMenuRepository metaMenuRepo;

	private Map<String, ObjectViews> modelMap;

	private File viewPath;

	public void removeDeleted(File viewPath) {

		modelMap = new HashMap<String, ObjectViews>();
		this.viewPath = viewPath;

		removeActions();

		removeView();

		removeMenu();

		removeDashboard();

		updateViewFile();

	}

	@Transactional
	public void removeActions() {

		List<MetaAction> metaActions = metaActionRepo.all()
				.filter("self.removeAction = true and model != null").fetch();
		log.debug("Total actions to remove: {}", metaActions.size());

		for (MetaAction metaAction : metaActions) {

			String model = metaAction.getModel();
			String actionName = metaAction.getName();

			metaActionRepo.remove(metaAction);

			model = model.substring(model.lastIndexOf(".") + 1);

			ObjectViews objectViews = getObjectViews(model);
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

		}
	}

	@Transactional
	public void removeView() {

		List<MetaView> metaViews = metaViewRepo.all()
				.filter("self.removeView = true").fetch();
		log.debug("Total views to remove: {}", metaViews.size());

		for (MetaView metaView : metaViews) {

			String model = metaView.getModel();
			if (model == null) {
				continue;
			}
			model = model.substring(model.lastIndexOf(".") + 1);

			ObjectViews objectViews = getObjectViews(model);
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
				if (metaView.getName().equals(view.getName())) {
					viewIter.remove();
				}
			}

			metaViewRepo.remove(metaView);
		}

	}

	@Transactional
	public void removeMenu() {

		List<MetaMenu> metaMenus = metaMenuRepo.all()
				.filter("self.removeMenu = true").fetch();
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
		}

		ObjectViews objectViews = getObjectViews("Menu");

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

		}

	}

	@Transactional
	public void removeDashboard() {

		List<MetaAction> metaActions = metaActionRepo.all()
				.filter("self.removeAction = true and model is null").fetch();
		log.debug("Total dashoboard actions to remove: {}", metaActions.size());

		List<String> actionNames = new ArrayList<String>();
		for (MetaAction action : metaActions) {
			actionNames.add(action.getName());
			metaActionRepo.remove(action);
		}

		ObjectViews objectViews = getObjectViews("Dashboard");
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

	}

	private void updateViewFile() {

		try {
			for (String model : modelMap.keySet()) {

				ObjectViews objectViews = modelMap.get(model);
				File viewFile = new File(viewPath, model + ".xml");
				if (emptyObjectViews(objectViews)) {
					viewFile.delete();
					continue;
				}

				StringWriter xmlWriter = new StringWriter();
				XMLViews.marshal(modelMap.get(model), xmlWriter);

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

	private ObjectViews getObjectViews(String model) {

		if (modelMap.containsKey(model)) {
			return modelMap.get(model);
		}

		try {
			File viewFile = new File(viewPath, model + ".xml");
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

	private boolean emptyObjectViews(ObjectViews objectViews) {

		if (objectViews.getActionMenus() != null) {
			return false;
		}

		if (objectViews.getActions() != null) {
			return false;
		}

		if (objectViews.getMenus() != null) {
			return false;
		}

		if (objectViews.getSelections() != null) {
			return false;
		}

		if (objectViews.getViews() != null) {
			return false;
		}

		return true;
	}

}
