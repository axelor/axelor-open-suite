package com.axelor.studio.service.builder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
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
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.meta.schema.views.MenuItem;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.axelor.studio.service.ConfigurationService;
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

	private Map<String, MetaMenu> menuMap;

	private List<String> deletedMenus;

	@Inject
	private MenuBuilderRepository menuBuilderRepo;

	@Inject
	private MetaMenuRepository metaMenuRepo;

	@Inject
	private MetaActionRepository metaActionRepo;

	@Inject
	private ConfigurationService configService;

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
	public void build(File parentPath, boolean updateMeta)
			throws JAXBException, IOException {

		String query = "self.edited = true";

		if (!updateMeta) {
			query += " OR self.recorded = false";
		}

		List<MenuBuilder> menuBuilders = menuBuilderRepo.all().filter(query)
				.order("-isParent").order("id").fetch();
		menuItems = new ArrayList<MenuItem>();
		actionMap = new HashMap<String, Action>();
		menuMap = new HashMap<String, MetaMenu>();
		deletedMenus = new ArrayList<String>();

		log.debug("Total menus to process: {}", menuBuilders.size());

		generatatMetaMenu(menuBuilders.iterator());

		log.debug("Total menus after process: {}", menuBuilders.size());

		if (!updateMeta || !deletedMenus.isEmpty()) {
			File menuFile = new File(parentPath, "Menu.xml");
			loadFile(menuFile);
			if (!deletedMenus.isEmpty()) {
				deleteMenus();
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

		String name = menuBuilder.getName();

		Iterator<MenuItem> oldMenuIter = menuItems.iterator();
		while (oldMenuIter.hasNext()) {
			MenuItem oldMenuItem = oldMenuIter.next();
			if (oldMenuItem.getName().equals(name)) {
				oldMenuIter.remove();
			}
		}

		MenuItem menuItem = new MenuItem();
		menuItem.setName(name);
		menuItem.setTitle(menuBuilder.getTitle());
		menuItem.setTop(menuBuilder.getTop());
		String icon = menuBuilder.getIcon();
		if (menuBuilder.getParent() == null && Strings.isNullOrEmpty(icon)) {
			menuItem.setIcon("fa-list");
		} else {
			menuItem.setIcon(icon);
		}

		String background = menuBuilder.getIconBackground();
		if (menuBuilder.getParent() == null
				&& Strings.isNullOrEmpty(background)) {
			menuItem.setIconBackground("green");
		} else {
			menuItem.setIconBackground(menuBuilder.getIconBackground());
		}

		log.debug("Menu name: {}, order: {}", name, menuBuilder.getOrder());
		menuItem.setOrder(menuBuilder.getOrder());
		menuItem.setParent(menuBuilder.getParent());
		if (!menuBuilder.getIsParent()) {
			setAction(menuBuilder, menuItem);
		}

		if (menuBuilder.getGroups() != null
				&& !menuBuilder.getGroups().isEmpty()) {
			setGroup(menuBuilder, menuItem);
		}

		menuItems.add(menuItem);

		extractMenu(iterator);

	}

	/**
	 * Method set action in menuItem from MenuBuilder and update actionMap.
	 * 
	 * @param menuBuilder
	 *            MenuBuilder source to create action.
	 * @param menuItem
	 *            MenuItem to update.
	 */
	private void setAction(MenuBuilder menuBuilder, MenuItem menuItem) {

		Action action = getAction(menuBuilder);
		actionMap.put(action.getName(), action);
		menuItem.setAction(action.getName());

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
	 * Method create ActionView from MenuBuilder.
	 * 
	 * @param menuBuilder
	 *            Source MenuBuilder.
	 * @return New ActionView created.
	 */
	private Action getAction(MenuBuilder menuBuilder) {

		String actionName = menuBuilder.getName().replace("-", ".");

		ActionViewBuilder builder = ActionView.define(menuBuilder.getTitle());
		builder.model(menuBuilder.getModel());
		builder.name(actionName);

		String domain = menuBuilder.getDomain();

		if (domain != null) {
			builder.domain(domain);
		}

		if (menuBuilder.getDashboard() != null) {
			builder.add("dashboard", menuBuilder.getDashboard().getName());
		} else {
			builder.add("grid");
			builder.add("form");
		}

		return builder.get();

	}

	/**
	 * Create or update MetaMenu from MenuBuilder and save it. Also set action
	 * in MetaMenu.
	 * 
	 * @param menuIterator
	 *            MenuBuilder iterator
	 */
	@Transactional
	public void generatatMetaMenu(Iterator<MenuBuilder> menuIterator) {

		if (!menuIterator.hasNext()) {
			return;
		}

		MenuBuilder menuBuilder = menuIterator.next();

		if (menuBuilder.getDeleteMenu()) {
			deleteMenu(menuBuilder);
			deletedMenus.add(menuBuilder.getName());
			menuIterator.remove();
			generatatMetaMenu(menuIterator);
			return;
		}

		String name = menuBuilder.getName();

		log.debug("Processing meta menu : {}", name);

		MetaMenu metaMenu = metaMenuRepo.all().filter("self.name = ?1", name)
				.fetchOne();

		if (metaMenu == null) {
			metaMenu = new MetaMenu();
			metaMenu.setName(name);
		}
		menuBuilder.setMenuGenerated(metaMenu);
		metaMenu.setTitle(menuBuilder.getTitle());
		metaMenu.setTop(menuBuilder.getTop());
		String icon = menuBuilder.getIcon();
		if (menuBuilder.getParent() == null && Strings.isNullOrEmpty(icon)) {
			metaMenu.setIcon("fa-list");
		} else {
			metaMenu.setIcon(icon);
		}

		String background = menuBuilder.getIconBackground();
		if (menuBuilder.getParent() == null
				&& Strings.isNullOrEmpty(background)) {
			metaMenu.setIconBackground("green");
		} else {
			metaMenu.setIconBackground(menuBuilder.getIconBackground());
		}

		metaMenu.setOrder(menuBuilder.getOrder());
		metaMenu.setModule(configService.getModuleName());

		if (menuBuilder.getGroups() != null) {
			Set<Group> groups = new HashSet<Group>();
			groups.addAll(menuBuilder.getGroups());
			metaMenu.setGroups(groups);
		}

		String parent = menuBuilder.getParent();
		log.debug("Parent menu : {}", parent);
		if (menuMap.containsKey(parent)) {
			metaMenu.setParent(menuMap.get(parent));
		} else {
			MetaMenu parentMenu = metaMenuRepo.all()
					.filter("self.name = ?1", parent).fetchOne();
			log.debug("Parent menu searched: {} , menu found: {}", parent,
					parentMenu);
			metaMenu.setParent(parentMenu);
		}

		if (!menuBuilder.getIsParent()) {
			MetaAction action = getMetaAction(menuBuilder);
			menuBuilder.setActionGenerated(action);
			metaMenu.setAction(action);
		}

		metaMenu = metaMenuRepo.save(metaMenu);
		menuMap.put(name, metaMenu);

		generatatMetaMenu(menuIterator);

	}

	/**
	 * Create or Update MetaAction from menuBuilder record and save it.
	 * 
	 * @param menuBuilder
	 *            Source menuBuilder
	 * @return New or existing MetaAction
	 */
	@Transactional
	public MetaAction getMetaAction(MenuBuilder menuBuilder) {

		String actionName = menuBuilder.getName().replace("-", ".");

		MetaAction metaAction = metaActionRepo.all()
				.filter("self.name = ?1", actionName).fetchOne();

		if (metaAction == null) {
			metaAction = new MetaAction(actionName);
			metaAction.setType("action-view");
			metaAction.setModule(configService.getModuleName());
		}

		metaAction.setModel(menuBuilder.getModel());

		Action action = getAction(menuBuilder);
		String xml = XMLViews.toXml(action, true);
		metaAction.setXml(xml);

		return metaActionRepo.save(metaAction);

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

		StringWriter xmlWriter = new StringWriter();

		ObjectViews objectViews = new ObjectViews();

		objectViews.setMenus(menuItems);

		List<Action> actions = new ArrayList<Action>();
		actions.addAll(actionMap.values());
		objectViews.setActions(actions);

		XMLViews.marshal(objectViews, xmlWriter);

		FileWriter fileWriter = new FileWriter(menuFile);
		fileWriter.write(xmlWriter.toString());
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

		MetaMenu menu = menuBuilder.getMenuGenerated();
		if (menu != null) {
			metaMenuRepo.remove(menu);
		}

		MetaAction action = menuBuilder.getActionGenerated();
		if (action != null) {
			metaActionRepo.remove(action);
		}

		menuBuilderRepo.remove(menuBuilder);

	}

	private void deleteMenus() {

		if (menuItems != null) {
			Iterator<MenuItem> menuIter = menuItems.iterator();
			while (menuIter.hasNext()) {
				MenuItem menuItem = menuIter.next();
				if (deletedMenus.contains(menuItem.getName())) {
					menuIter.remove();
				}
			}
		}

	}
}
