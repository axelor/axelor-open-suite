package com.axelor.studio.db.repo;

import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.studio.db.MenuBuilder;
import com.google.inject.Inject;

public class MenuBuilderRepo extends MenuBuilderRepository {

	@Inject
	MetaMenuRepository metaMenuRepo;

	@Override
	public void remove(MenuBuilder menuBuilder) {

		MetaMenu metaMenu = menuBuilder.getMenuGenerated();
		metaMenu.setRemoveMenu(true);
		metaMenuRepo.save(metaMenu);

		super.remove(menuBuilder);
	}
}
