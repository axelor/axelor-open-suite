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

package com.axelor.studio.service.data.importer;

import java.util.HashMap;
import java.util.Map;

import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.axelor.studio.service.data.TranslationService;
import com.axelor.studio.service.data.exporter.ExportMenu;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportMenu {
	
	private Integer parentMenuSeq = 0;
	
	private Map<Long, Integer> menuBuilderSeqMap = new HashMap<Long, Integer>();
	
	private Map<Long, Integer> menuSeqMap = new HashMap<Long, Integer>();
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private MenuBuilderRepository menuBuilderRepo;
	
	@Inject
	private TranslationService translationService;
	
	@Inject
	private ImportService importService;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	public void importMenus(DataReader reader, String key) throws AxelorException {
		if (reader == null) {
			return;
		}
		
		int totalLines = reader.getTotalLines(key);
		for (int rowNum = 0; rowNum < totalLines; rowNum++) {
			String[] row = reader.read(key, rowNum);
			if (row == null) {
				continue;
			}
			
			MetaModule module = importService.getModule(row[ExportMenu.MODULE], null);
			if (module == null) {
				continue;
			}
			
			String modelName = row[ExportMenu.OBJECT];
			MetaModel model = null;
			if (modelName != null) {
				model = metaModelRepo.findByName(modelName);
				if (model == null) {
					throw new AxelorException(I18n.get("Menu model not found for sheet: %s row: %s")
							, 1, key, rowNum + 1);
				}
			}
			
			createMenu(module, model, row, key, rowNum);
		}
		
	}
	
	@Transactional
	public void createMenu(MetaModule module, MetaModel  model, String[] row, String key, int rowNum) throws AxelorException {
		
		String name = row[ExportMenu.NAME];
		String title = getTitle(row[ExportMenu.TITLE], row[ExportMenu.TITLE_FR]);
		
		if (name == null && title == null) {
			throw new AxelorException(I18n.get("Menu name and title empty for sheet: %s row: %s")
					, 1, key, rowNum + 1);
		}
		
		if (name == null) {
			name = title;
		}
		
		name = Inflector.getInstance().dasherize(name);
		
		MenuBuilder menuBuilder = getMenuBuilder(name, module, model);
		menuBuilder.setTitle(title);
		
		String parentName = row[ExportMenu.PARENT];
		if (parentName != null) {
			menuBuilder = setParentMenu(menuBuilder, parentName);
		}
		
		menuBuilder.setIcon(row[ExportMenu.ICON]);
		menuBuilder.setIconBackground(row[ExportMenu.BACKGROUND]);
		menuBuilder.setDomain(row[ExportMenu.FILTER]);
		menuBuilder.setAction(row[ExportMenu.ACTION]);
		menuBuilder.setViews(row[ExportMenu.VIEWS]);
		
		String order = row[ExportMenu.ORDER];
		menuBuilder = setMenuOrder(menuBuilder, order);
		
		menuBuilderRepo.save(menuBuilder);
		
	}
	
	private String getTitle(String title, String titleFr) {
		
		if (title == null) {
			title = titleFr;
		}
		else if (titleFr != null) {
			translationService.addTranslation(title, titleFr, "fr");
		}
		
		return title;
	}
	
	private MenuBuilder getMenuBuilder(String name, MetaModule module, MetaModel model) {
		
		MenuBuilder menuBuilder = menuBuilderRepo
				.all()
				.filter("self.name = ?1 and self.metaModule.name = ?2" , name, module.getName()).fetchOne();
		if (menuBuilder == null) {
			menuBuilder = new MenuBuilder(name);
			menuBuilder.setMetaModule(module);
		}
		if (model != null) {
			menuBuilder.setMetaModel(model);
			menuBuilder.setIsParent(false);
		} else {
			menuBuilder.setMetaModel(null);
			menuBuilder.setIsParent(true);
		}
		
		menuBuilder.setEdited(true);
		
		
		return menuBuilder;
	}
	
	
	private MenuBuilder setParentMenu(MenuBuilder menuBuilder, String parentName) throws AxelorException {
		
		parentName = Inflector.getInstance().dasherize(parentName);
		MenuBuilder parent = menuBuilderRepo
				.all()
				.filter("self.name = ?1 and self.metaModule.name = ?2" ,
						parentName, menuBuilder.getMetaModule().getName()).fetchOne();
		if (parent != null) {
			menuBuilder.setMenuBuilder(parent);
		} else {
			MetaMenu parentMenu = metaMenuRepo.findByName(parentName);
			if (parentMenu != null) {
				menuBuilder.setMetaMenu(parentMenu);
			}
			else {
				throw new AxelorException(I18n.get("No parent menu found %s for menu %s"),
					1, parentName, menuBuilder.getTitle());
			}
		}
		
		return menuBuilder;
	}
	
	private MenuBuilder setMenuOrder(MenuBuilder menuBuilder, String order) {
		
		Integer seq = null;
		if (order != null) {
			try {
				seq = Integer.parseInt(order);
			}
			catch(NumberFormatException e) {
				seq = null;
			}
		}
		
		if (seq != null) {
			menuBuilder.setOrder(seq);
		}
		else {
			menuBuilder.setOrder(getMenuOrder(menuBuilder));
		}
		
		return menuBuilder;
	}
	
	private int getMenuOrder(MenuBuilder menuBuilder) {

		Integer seq = 0;
		MenuBuilder parent = menuBuilder.getMenuBuilder();
		if (parent != null) {
			Long menuId = parent.getId();
			if (menuBuilderSeqMap.containsKey(menuId)) {
				seq = menuBuilderSeqMap.get(menuId);
			}
			menuBuilderSeqMap.put(menuId, seq + 1);
			return seq; 
		}
		
		MetaMenu parentMenu = menuBuilder.getMetaMenu();
		if (parentMenu != null) {
			Long menuId = parentMenu.getId();
			if (menuSeqMap.containsKey(menuId)) {
				seq = menuSeqMap.get(menuId);
			}
			menuSeqMap.put(menuId, seq + 1);
			return seq; 
		}
		
		seq = parentMenuSeq;
		
		parentMenuSeq++;
		
		return seq;

	}
	
}
