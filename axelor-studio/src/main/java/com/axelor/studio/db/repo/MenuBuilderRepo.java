package com.axelor.studio.db.repo;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.MetaStore;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.service.StudioMetaService;
import com.axelor.studio.service.builder.MenuBuilderService;
import com.google.inject.Inject;

public class MenuBuilderRepo extends MenuBuilderRepository {
	
	private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private MenuBuilderService menuBuilderService;
	
	@Inject
	private ActionBuilderRepo actionBuilderRepo;
	
	@Inject
	private StudioMetaService metaService;
	
	@Override
	public MenuBuilder save(MenuBuilder menuBuilder) {
		
		if (menuBuilder.getName() == null) {
			menuBuilder.setName("studio-menu-" + menuBuilder.getId());
		}
		
		if (menuBuilder.getActionBuilder() != null) {
			menuBuilder.getActionBuilder().setMenuAction(true);
		}
		
		menuBuilder = super.save(menuBuilder);
		
		menuBuilderService.build(menuBuilder);
		
		return menuBuilder;
	}
	
	@Override
	public MenuBuilder copy(MenuBuilder menuBuilder, boolean deep) {
		
		ActionBuilder actionBuilder = menuBuilder.getActionBuilder();
		menuBuilder.setActionBuilder(null);
		
		menuBuilder = super.copy(menuBuilder, deep);
		
		if (actionBuilder != null) {
			menuBuilder.setActionBuilder(actionBuilderRepo.copy(actionBuilder, deep));
		}
		
		return menuBuilder;
	}
	
	@Override
	public void remove (MenuBuilder menuBuilder) {
		
		metaService.removeMetaMenu(menuBuilder.getName());
		
		ActionBuilder actionBuilder = menuBuilder.getActionBuilder();
		
		menuBuilder.setActionBuilder(null);
		if (actionBuilder != null) {
			try {
				actionBuilderRepo.remove(actionBuilder);
			}catch(Exception e) {
			}
		}
		
		MetaStore.clear();
		
		super.remove(menuBuilder);
	}

	
}
