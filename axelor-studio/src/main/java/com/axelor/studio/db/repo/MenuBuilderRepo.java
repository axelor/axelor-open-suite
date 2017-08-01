package com.axelor.studio.db.repo;

import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.service.builder.MenuBuilderService;
import com.google.inject.Inject;

public class MenuBuilderRepo extends MenuBuilderRepository{
	
	@Inject
	private MenuBuilderService menuBuilderService;
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private ActionBuilderRepo actionBuilderRepo;
	
	@Override
	public MenuBuilder save(MenuBuilder menuBuilder) {
		
		menuBuilder = super.save(menuBuilder);
		
		menuBuilderService.build(menuBuilder);
		
		return menuBuilder;
	}
	
	@Override
	public void remove (MenuBuilder menuBuilder) {
		
		MetaMenu metaMenu = metaMenuRepo.findByID("studio-" + menuBuilder.getName());
		
		if (metaMenu != null) {
			metaMenuRepo.remove(metaMenu);
		}
		
		ActionBuilder actionBuilder = menuBuilder.getActionBuilder();
		
		menuBuilder.setActionBuilder(null);
		if (actionBuilder != null) {
			try {
				actionBuilderRepo.remove(actionBuilder);
			}catch(Exception e) {
				
			}
		}
		
		super.remove(menuBuilder);
	}
}
