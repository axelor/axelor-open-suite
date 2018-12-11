/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.creator.view;

import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;

public class AbstractViewCreatoryFactory {

  public static final String VIEW_NOT_SUPPORTED = /*$$(*/ "View is not supported" /*)*/;

  public AbstractViewCreator getViewCreator(String type, boolean autoCreate)
      throws AxelorException {

    switch (type) {
      case "form":
        //			return new FormCreator(autoCreate);
      case "grid":
        return new GridCreator();
      case "kanban":
        return new KanbanCreator();
      case "calendar":
        return new CalendarCreator();
    }

    throw new AxelorException(TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(VIEW_NOT_SUPPORTED));
  }
}
