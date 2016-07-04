package com.axelor.studio.db.repo;

import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.repo.WkfRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StudioWkfRepository extends WkfRepository {

	@Inject
	ViewBuilderRepository viewBuilderRepo;

	@Inject
	MenuBuilderRepository menuBuilderRepo;

	/**
	 * Override to remove changes related with workflow. Like to remove buttons
	 * and status field from view and model.
	 */
	@Override
	public void remove(Wkf wkf) {

		ViewBuilder viewBuilder = wkf.getViewBuilder();

		if (viewBuilder != null) {
			viewBuilder.setClearWkf(true);
			viewBuilder.setEdited(true);
			saveViewBuilder(viewBuilder);
		}

		for (WkfNode wkfNode : wkf.getNodes()) {

			MenuBuilder statusMenu = wkfNode.getStatusMenu();
			if (statusMenu != null) {
				statusMenu.setDeleteMenu(true);
				statusMenu.setEdited(true);
				menuBuilderRepo.save(statusMenu);
			}

			MenuBuilder myStatusMenu = wkfNode.getMyStatusMenu();
			if (myStatusMenu != null) {
				myStatusMenu.setDeleteMenu(true);
				myStatusMenu.setEdited(true);
				menuBuilderRepo.save(myStatusMenu);
			}
		}

		super.remove(wkf);
	}

	@Transactional
	public void saveViewBuilder(ViewBuilder viewBuilder) {

		viewBuilderRepo.save(viewBuilder);

	}

	@Transactional
	public void saveMenu(MenuBuilder menuBuilder) {

		menuBuilderRepo.save(menuBuilder);

	}

}
