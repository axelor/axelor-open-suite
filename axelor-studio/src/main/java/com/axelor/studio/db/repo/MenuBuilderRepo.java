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

public class MenuBuilderRepo extends MenuBuilderRepository {


	@Override
	public void remove(MenuBuilder menuBuilder) {

		if (menuBuilder.getMenuGenerated() != null) {
			menuBuilder.getMenuGenerated().setRemoveMenu(true);
		}

		super.remove(menuBuilder);
	}
	
	@Override
	public MenuBuilder save(MenuBuilder menuBuilder) {
		
		if (menuBuilder.getActionBuilder() != null) {
			menuBuilder.getActionBuilder().setEdited(true);
		}
		
		return super.save(menuBuilder);
	}
}
