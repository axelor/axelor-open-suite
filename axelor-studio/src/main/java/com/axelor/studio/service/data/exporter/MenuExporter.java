package com.axelor.studio.service.data.exporter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.View;
import com.axelor.studio.service.data.TranslationService;
import com.google.inject.Inject;

public class MenuExporter {
	
	public static final String[] MENU_HEADERS = new String[] {
		"Notes",
		"Module",
		"Object",
		"Views",
		"Name",
		"Title",
		"Title FR",
		"Parent",
		"Order",
		"Icon",	
		"Background",
		"Filters",
		"Action"
	};
	
	public static final int MODULE = 1;
	public static final int OBJECT = 2;
	public static final int VIEWS = 3;
	public static final int NAME = 4;
	public static final int TITLE = 5;
	public static final int TITLE_FR = 6;
	public static final int PARENT = 7;
	public static final int ORDER = 8;
	public static final int ICON = 9;
	public static final int BACKGROUND = 10;
	public static final int FILTER = 11;
	public static final int ACTION = 12;
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private TranslationService translationService;
	
	List<MetaMenu> menus = new ArrayList<MetaMenu>();
	
	public void export(DataWriter writer, List<String> modules) {		
		
		menus = new ArrayList<MetaMenu>();

		getMenus(modules);
		
		writeMenus(writer);
	}
	
	public List<MetaMenu> getMenus(List<String> modules) {
		
		if (!menus.isEmpty()) {
			return menus;
		}
		
		menus = new ArrayList<MetaMenu>();
		
		List<MetaMenu> parentMenus = metaMenuRepo.all()
				.filter("self.left = true "
						+ "and self.module in ?1 "
						+ "and self.action is null "
						+ "and self.parent is null "
						, modules)
				.order("order").order("id").fetch();
		
		addMenu(parentMenus.iterator());
		
		return menus;

	}
	
	private void addMenu(Iterator<MetaMenu> menuIter) {
		
		if (!menuIter.hasNext()) return;
		
		MetaMenu menu = menuIter.next();
		menus.add(menu);
		
		List<MetaMenu> subMenus = metaMenuRepo.all()
				.filter("self.parent = ?1" ,menu)
				.order("order").order("id").fetch();
		
		addMenu(subMenus.iterator());
		
		addMenu(menuIter);
		
	}
	
	private void writeMenus(DataWriter writer) {
		
		String[] values = new String[MENU_HEADERS.length];
		values = MENU_HEADERS;
		
		writer.write("Menu", null, values);
		
		for (MetaMenu menu : menus) {
			values = new String[MENU_HEADERS.length];
			
			values[MODULE] = menu.getModule();
			values[NAME] = menu.getName();
			values[PARENT] = menu.getParent() != null ? menu.getParent().getName() : null;
			values[ORDER] = menu.getOrder() != null ? menu.getOrder().toString() : null;
			values[ICON] = menu.getIcon();
			values[BACKGROUND] = menu.getIconBackground();
			values[TITLE] = menu.getTitle();
			values[TITLE_FR] = translationService.getTranslation(menu.getTitle(), "fr");
			
			MetaAction action = menu.getAction();
			if (action != null && action.getType().equals("action-view")) {
				values = addAction(values, action);
			}
			
			writer.write("Menu", null, values);
		}
	}
	
	private String[] addAction(String[] values, MetaAction metaAction) {
		
		values[ACTION] = metaAction.getName();
		values[OBJECT] = getModelName(metaAction.getModel());
		
		try {
			ObjectViews objectViews = XMLViews.fromXML(metaAction.getXml());
			ActionView action = (ActionView) objectViews.getActions().get(0);	
			values[FILTER] = action.getDomain();
			List<View> views = action.getViews();
			if (views == null) {
				return values;
			}
			for (View view : views) {
				String name = view.getName();
				if (name == null) {
					continue;
				}
				if (values[VIEWS] == null) {
					values[VIEWS] = view.getName();
				}
				else {
					values[VIEWS] += "," + view.getName();
				}
			}
		} catch (JAXBException e) {
		}
		
		
		return values;
	}
	
	private String getModelName(String name) {
		
		if (name == null) {
			return name;
		}
		
		String[] names = name.split("\\.");
		
		return names[names.length-1];
	}

	
	

}
