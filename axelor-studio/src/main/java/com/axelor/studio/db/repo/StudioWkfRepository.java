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
	 * Overridden to remove changes related with workflow. Like to remove buttons
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
				saveMenu(statusMenu);
			}

			MenuBuilder myStatusMenu = wkfNode.getMyStatusMenu();
			if (myStatusMenu != null) {
				myStatusMenu.setDeleteMenu(true);
				myStatusMenu.setEdited(true);
				saveMenu(myStatusMenu);
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
