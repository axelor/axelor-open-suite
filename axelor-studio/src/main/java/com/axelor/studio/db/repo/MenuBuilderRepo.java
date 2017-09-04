package com.axelor.studio.db.repo;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.service.builder.MenuBuilderService;
import com.google.inject.Inject;

public class MenuBuilderRepo extends MenuBuilderRepository {
	
	private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
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
		
		log.debug("Removing menu: {}", metaMenu);
		if (metaMenu != null) {
			removeMetaMenu(metaMenu);
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

	private void removeMetaMenu(MetaMenu metaMenu) {
		
		List<MetaMenu> subMenus = metaMenuRepo.all().filter("self.parent = ?1", metaMenu).fetch();
		for (MetaMenu subMenu : subMenus) {
			subMenu.setParent(null);
		}
		List<MenuBuilder> subBuilders = all().filter("self.parentMenu = ?1", metaMenu).fetch();
		for (MenuBuilder subBuilder : subBuilders) {
			subBuilder.setParentMenu(null);
		}
		
		metaMenuRepo.remove(metaMenu);
	}
}
