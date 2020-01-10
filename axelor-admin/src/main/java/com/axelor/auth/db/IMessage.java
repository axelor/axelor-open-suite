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
package com.axelor.auth.db;

public interface IMessage {

  /** Common messages * */
  public static final String IMPORT_OK = /*$$(*/ "Import completed successfully" /*)*/;

  public static final String ERR_IMPORT = /*$$(*/ "Error in import. Please check log." /*)*/;

  /** Permission assistant & group menu assistant* */
  public static final String BAD_FILE = /*$$(*/ "Bad import file" /*)*/;

  public static final String NO_HEADER = /*$$(*/ "No header row found" /*)*/;
  public static final String BAD_HEADER = /*$$(*/ "Bad header row:" /*)*/;
  public static final String NO_GROUP = /*$$(*/ "Groups not found: %s" /*)*/;
  public static final String NO_ROLE = /*$$(*/ "Roles not found: %s" /*)*/;
  public static final String NO_OBJECT = /*$$(*/ "Object not found: %s" /*)*/;
  public static final String ERR_IMPORT_WITH_MSG = /*$$(*/
      "Error in import: %s. Please check the server log" /*)*/;
  public static final String NO_MENU = /*$$(*/ "Menu not found: %s" /*)*/;
}
