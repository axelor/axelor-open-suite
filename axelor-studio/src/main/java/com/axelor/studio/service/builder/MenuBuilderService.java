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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.auth.db.Group;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.views.MenuItem;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * This service class generate menu from MenuBuilder. It will also create
 * action-view required for menu.
 * 
 * @author axelor
 *
 */
public class MenuBuilderService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private List<MenuItem> menuItems;

	private Map<String, Action> actionMap;

	private List<String> deletedMenus;

	@Inject
	private MenuBuilderRepository menuBuilderRepo;

	@Inject
	private MetaMenuRepository metaMenuRepo;

	@Inject
	private MetaActionRepository metaActionRepo;
	
	private List<MenuBuilder> childMenus;
	
	/**
	 * Root method to access this service. It generate MetaMenu and save
	 * menuitems in menu.xml
	 * 
	 * @param parentPath
	 *            Path to resources directory of custom module.
	 * @param updateMeta
	 *            Boolean to check if only to update meta records.
	 * @throws JAXBException
	 *             Exception throws in parsing xml .
	 * @throws IOException
	 *             Exception throws in saving menu.xml.
	 */
	public void build(String module, File parentPath, boolean updateMeta)
			throws JAXBException, IOException {
		
		menuItems = new ArrayList<MenuItem>();
		actionMap = new HashMap<String, Action>();
		deletedMenus = new ArrayList<String>();
		childMenus = new ArrayList<MenuBuilder>();
		
		String query = "self.edited = true";
		if (!updateMeta) {
			query += " OR self.recorded = false";
		}
		query = "self.metaModule.name = ?1 and (" + query + ")";
		
		List<MenuBuilder> menuBuilders = menuBuilderRepo.all()
				.filter(query, module)
				.order("order")
				.fetch();
		
		log.debug("Total menus to process: {}", menuBuilders.size());
		deleteMetaMenu(menuBuilders.iterator());
		log.debug("Total menus after process: {}", menuBuilders.size());
		createMetaMenu(menuBuilders.iterator());
		updateParent();

		if (!updateMeta || !deletedMenus.isEmpty()) {
			File menuFile = new File(parentPath, "Menu.xml");
			loadFile(menuFile);
			if (!deletedMenus.isEmpty()) {
				deleteMenuItems();
			}
			if (!updateMeta) {
				extractMenu(menuBuilders.iterator());
			}
			if (!menuItems.isEmpty()) {
				writeMenu(menuFile);
			} else {
				menuFile.delete();
			}
		}

		updateEdited(menuBuilders, updateMeta);

	}

	private void deleteMetaMenu(Iterator<MenuBuilder> menuIterator) {
		
		while(menuIterator.hasNext()) {
			MenuBuilder menuBuilder = menuIterator.next();
			if (menuBuilder.getDeleteMenu()) {
				deleteMenu(menuBuilder);
				deletedMenus.add(getXmlId(menuBuilder));
				menuIterator.remove();
			}
		}
		
	}
	
	@Transactional
	public void updateParent() {
		
		for (MenuBuilder menuBuilder : childMenus) {
			MetaMenu parent = menuBuilder.getMenuBuilder().getMenuGenerated();
			menuBuilder.getMenuGenerated().setParent(parent);
			menuBuilderRepo.save(menuBuilder);
		}
	}

	/**
	 * Method to reset edited boolean after processing MenuBuilders.
	 * 
	 * @param menuBuilders
	 *            List of MenuBuilders to process.
	 * @param updateMeta
	 *            Boolean to check if to update recorded boolean too.
	 */
	@Transactional
	public void updateEdited(List<MenuBuilder> menuBuilders, boolean updateMeta) {

		for (MenuBuilder menuBuilder : menuBuilders) {
			if (!updateMeta) {
				menuBuilder.setRecorded(true);
			}
			menuBuilder.setEdited(false);
			menuBuilderRepo.save(menuBuilder);
		}
	}

	/**
	 * Load existing menu file from custom module's resource directory. It
	 * create list of menuItems and update actionMap.
	 * 
	 * @param menuFile
	 *            MenuFile in resource directory.
	 * @throws JAXBException
	 *             Exception thrown in parsing xml.
	 * @throws IOException
	 *             Exception thrown in saving xml file.
	 */
	private void loadFile(File menuFile) throws JAXBException, IOException {

		if (!menuFile.exists()) {
			return;
		}

		String fileContent = Files.toString(menuFile, Charsets.UTF_8);

		ObjectViews objectViews = XMLViews.fromXML(fileContent);

		if (objectViews.getMenus() != null) {
			menuItems = objectViews.getMenus();
		}

		if (objectViews.getActions() != null) {
			for (Action action : objectViews.getActions()) {
				actionMap.put(action.getName(), action);
			}
		}

	}

	/**
	 * Method create MenuItem from MenuBuilder record.
	 * 
	 * @param iterator
	 *            MenuBuilder iterator
	 */
	public void extractMenu(Iterator<MenuBuilder> iterator) {

		if (!iterator.hasNext()) {
			return;
		}

		MenuBuilder menuBuilder = iterator.next();
		String xmlId = getXmlId(menuBuilder);
		String name = menuBuilder.getName();

		Iterator<MenuItem> oldMenuIter = menuItems.iterator();
		while (oldMenuIter.hasNext()) {
			MenuItem oldMenuItem = oldMenuIter.next();
			if (oldMenuItem.getXmlId().equals(xmlId)) {
				oldMenuIter.remove();
			}
		}

		MenuItem menuItem = new MenuItem();
		menuItem.setName(name);
		menuItem.setTitle(menuBuilder.getTitle());
		menuItem.setTop(menuBuilder.getTop());
		menuItem.setXmlId(xmlId);
		
		MenuBuilder parentBuilder = menuBuilder.getMenuBuilder();
		MetaMenu parentMenu = menuBuilder.getMetaMenu();
		if (parentMenu != null) {
			menuItem.setParent(parentMenu.getName());
		}
		else if (parentBuilder != null && parentBuilder.getMenuGenerated() != null) {
			menuItem.setParent(parentBuilder.getMenuGenerated().getName());
		}
		if (parentBuilder == null && parentMenu == null) {
			setIconBackgrond(menuBuilder, menuItem);
		}

		log.debug("Menu name: {}, order: {}", name, menuBuilder.getOrder());
		menuItem.setOrder(menuBuilder.getOrder());
		if (menuBuilder.getActionBuilder() != null) {
			menuItem.setAction(menuBuilder.getActionBuilder().getName());
		}

		if (menuBuilder.getGroups() != null
				&& !menuBuilder.getGroups().isEmpty()) {
			setGroup(menuBuilder, menuItem);
		}

		menuItems.add(menuItem);

		extractMenu(iterator);

	}

	private void setIconBackgrond(MenuBuilder menuBuilder, MenuItem menuItem) {
		
		String icon = menuBuilder.getIcon();
		if ( Strings.isNullOrEmpty(icon)) {
			menuItem.setIcon("fa-list");
		}
		else {
			menuItem.setIcon(icon);
		}
		
		String background = menuBuilder.getIconBackground();
		if ( Strings.isNullOrEmpty(background)) {
			menuItem.setIconBackground("green");
		}
		else {
			menuItem.setIconBackground(menuBuilder.getIconBackground());
		}
	}

	/**
	 * Method set MenuItem groups from menuBuilder's groups M2M.
	 * 
	 * @param menuBuilder
	 *            MenuBuilder source
	 * @param menuItem
	 *            Destination MenuItem to update
	 */
	private void setGroup(MenuBuilder menuBuilder, MenuItem menuItem) {

		String groupNames = "";
		for (Group group : menuBuilder.getGroups()) {
			if (groupNames.isEmpty()) {
				groupNames = group.getCode();
			} else {
				groupNames += "," + group.getCode();
			}
		}

		menuItem.setGroups(groupNames);
	}

	/**
	 * Create or update MetaMenu from MenuBuilder and save it. Also set action
	 * in MetaMenu.
	 * 
	 * @param menuIterator
	 *            MenuBuilder iterator
	 */
	@Transactional
	public void createMetaMenu(Iterator<MenuBuilder> menuIterator) {

		if (!menuIterator.hasNext()) {
			return;
		}

		MenuBuilder menuBuilder = menuIterator.next();
		
		String xmlId = getXmlId(menuBuilder);
		String name = menuBuilder.getName();

		log.debug("Processing meta menu : {}", name);
		
		MetaMenu metaMenu = metaMenuRepo.all().filter("self.name = ?1 and self.xmlId = ?2", name, xmlId)
				.fetchOne();

		if (metaMenu == null) {
			metaMenu = new MetaMenu();
			metaMenu.setName(name);
			metaMenu.setXmlId(xmlId);
		}
		menuBuilder.setMenuGenerated(metaMenu);
		metaMenu.setTitle(menuBuilder.getTitle());
		metaMenu.setTop(menuBuilder.getTop());
		String icon = menuBuilder.getIcon();
		if (menuBuilder.getMenuBuilder() == null && menuBuilder.getMetaMenu() == null && Strings.isNullOrEmpty(icon)) {
			metaMenu.setIcon("fa-list");
		} else {
			metaMenu.setIcon(icon);
		}

		String background = menuBuilder.getIconBackground();
		if (menuBuilder.getMenuBuilder() == null && menuBuilder.getMetaMenu() == null
				&& Strings.isNullOrEmpty(background)) {
			metaMenu.setIconBackground("green");
		} else {
			metaMenu.setIconBackground(menuBuilder.getIconBackground());
		}

		metaMenu.setOrder(menuBuilder.getOrder());
		metaMenu.setModule(menuBuilder.getMetaModule().getName());

		if (menuBuilder.getGroups() != null) {
			Set<Group> groups = new HashSet<Group>();
			groups.addAll(menuBuilder.getGroups());
			metaMenu.setGroups(groups);
		}
		
		MetaMenu parent = menuBuilder.getMetaMenu();
		if (parent != null) {
			metaMenu.setParent(parent);
		}
		else if (menuBuilder.getMenuBuilder() != null) {
			childMenus.add(menuBuilder);
		}
		
		MetaAction action = null;
		if (menuBuilder.getActionBuilder() != null) {
			action = metaActionRepo.findByName(menuBuilder.getActionBuilder().getName());
		}
		
		metaMenu.setAction(action);
		metaMenu = metaMenuRepo.save(metaMenu);

		createMetaMenu(menuIterator);

	}
	
	/**
	 * Method write menu file. Using menuItems and action Map. It will write xml
	 * with menuitem and action-views in menuFile.
	 * 
	 * @param menuFile
	 *            Destination menu file to update.
	 * @throws IOException
	 *             Exception thrown by file writing.
	 * @throws JAXBException
	 *             Exception thrown by xml marshal.
	 */
	private void writeMenu(File menuFile) throws IOException, JAXBException {

		FileWriter fileWriter = new FileWriter(menuFile);

		ObjectViews objectViews = new ObjectViews();

		objectViews.setMenus(menuItems);

		List<Action> actions = new ArrayList<Action>();
		actions.addAll(actionMap.values());
		objectViews.setActions(actions);

		XMLViews.marshal(objectViews, fileWriter);
		
		fileWriter.close();
	}

	/**
	 * Method delete MetaMenu related MetaAction and MenuBuilder if boolean
	 * 'deleteMenu' set true in MenuBuilder. This boolean set when any workflow
	 * deleted which is related to menu.
	 * 
	 * @param menuBuilder
	 *            MenuBuilder to delete.
	 */
	@Transactional
	public void deleteMenu(MenuBuilder menuBuilder) {

		if (menuBuilder == null) {
			return;
		}

		menuBuilderRepo.remove(menuBuilder);

	}

	private void deleteMenuItems() {

		if (menuItems != null) {
			Iterator<MenuItem> menuIter = menuItems.iterator();
			while (menuIter.hasNext()) {
				MenuItem menuItem = menuIter.next();
				if (deletedMenus.contains(menuItem.getXmlId())) {
					menuIter.remove();
				}
			}
		}

	}
	
	private String getXmlId(MenuBuilder menuBuilder) {
		
		return menuBuilder.getMetaModule().getName() + "-" + menuBuilder.getName();
	}
}
