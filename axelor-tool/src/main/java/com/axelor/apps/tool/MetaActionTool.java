/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool;

import com.axelor.meta.db.MetaAction;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.actions.Action;

public class MetaActionTool {

  /**
   * Creates a new {@code MetaAction} from the given action.<br>
   * This can be used for example to create a new menu entry with an {@code ActionView} generated
   * with user input.
   *
   * @param action The {@code Action} to be converted
   * @param name The {@code String} representing the name of the resulting {@code MetaAction}
   * @param type The {@code String} representing the type of the resulting {@code MetaAction}
   * @param module The {@code String} representing the name of the module that the resulting {@code
   *     MetaAction} should be attached to.
   * @return The {@code MetaAction} created from the given action
   */
  public static MetaAction actionToMetaAction(
      Action action, String name, String type, String module) {
    MetaAction metaAction = new MetaAction();

    metaAction.setModel(action.getModel());
    metaAction.setModule(module);
    metaAction.setName(name);
    metaAction.setType(type);
    metaAction.setXml(XMLViews.toXml(action, true));

    return metaAction;
  }
}
