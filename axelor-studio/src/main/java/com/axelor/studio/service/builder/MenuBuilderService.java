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

import java.util.HashSet;
import java.util.Set;

import com.axelor.auth.db.Group;
import com.axelor.auth.db.Role;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.studio.db.MenuBuilder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MenuBuilderService {
	
	@Inject
	private ActionBuilderService actionBuilderService;
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Transactional
	public MetaMenu build(MenuBuilder builder) {
		
		MetaMenu menu = createMenu(builder);
		if (builder.getActionBuilder() != null) {
			menu.setAction(actionBuilderService.build(builder.getActionBuilder()));
		}
		
		return metaMenuRepo.save(menu);
	
	}

	private MetaMenu createMenu(MenuBuilder builder) {
		
		String xmlId = "studio-" + builder.getName();
		MetaMenu menu = metaMenuRepo.findByID(xmlId);
		
		if (menu == null) {
			menu = new MetaMenu(builder.getName());
			menu.setXmlId(xmlId);
		}
		
		menu.setTitle(builder.getTitle());
		menu.setIcon(builder.getIcon());
		menu.setIconBackground(builder.getIconBackground());
		menu.setOrder(builder.getOrder());
		menu.setParent(builder.getParentMenu());
		
		if (builder.getGroups() != null) {
			Set<Group> groups = new HashSet<Group>();
			groups.addAll(builder.getGroups());
			menu.setGroups(groups);
		}
		
		if (builder.getRoles() != null) {
			Set<Role> roles = new HashSet<Role>();
			roles.addAll(builder.getRoles());
			menu.setRoles(roles);
		}
		
		menu.setConditionToCheck(builder.getConditionToCheck());
		menu.setModuleToCheck(builder.getModuleToCheck());
		menu.setLeft(builder.getLeft());
		menu.setTop(builder.getTop());
		menu.setHidden(builder.getHidden());
		menu.setMobile(builder.getMobile());
		
		menu.setTag(builder.getTag());
		menu.setTagCount(builder.getTagCount());
		menu.setTagGet(builder.getTagGet());
		menu.setTagStyle(builder.getTagStyle());
		
		menu.setLink(builder.getLink());
		
		return menu;
	}
	
}
