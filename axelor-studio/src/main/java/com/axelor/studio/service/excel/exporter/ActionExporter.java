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
package com.axelor.studio.service.excel.exporter;

import com.axelor.meta.db.MetaAction;
import java.util.ArrayList;
import java.util.List;

public class ActionExporter {

  public static final String[] ACTION_HEADERS =
      new String[] {"Notes", "Object", "Views", "Name", "Title", "Title FR", "Action"};

  public static final int OBJECT = 1;
  public static final int VIEWS = 2;
  public static final int NAME = 3;
  public static final int TITLE = 4;
  public static final int TITLE_FR = 5;
  public static final int ACTION = 6;

  List<MetaAction> actions = null;

  public void export(String moduleName, DataWriter writer) {
    actions = new ArrayList<>();
  }
}
