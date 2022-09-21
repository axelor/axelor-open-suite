/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.studio.translation;

public interface ITranslation {

  public static final String EDITOR_CUSTOM_MODEL = /*$$(*/ "Custom model"; /*)*/
  public static final String EDITOR_CUSTOM_FIELD = /*$$(*/ "Custom field"; /*)*/
  public static final String EDITOR_STRING = /*$$(*/ "String"; /*)*/
  public static final String EDITOR_INTEGER = /*$$(*/ "Integer"; /*)*/
  public static final String EDITOR_PANEL = /*$$(*/ "Panel"; /*)*/
  public static final String EDITOR_BUTTON = /*$$(*/ "Button"; /*)*/
  public static final String EDITOR_DECIMAL = /*$$(*/ "Decimal"; /*)*/
  public static final String EDITOR_TIME = /*$$(*/ "Time"; /*)*/
  public static final String EDITOR_DATE = /*$$(*/ "Date"; /*)*/
  public static final String EDITOR_DATETIME = /*$$(*/ "Datetime"; /*)*/
  public static final String EDITOR_SEPARATOR = /*$$(*/ "Separator"; /*)*/
  public static final String EDITOR_BOOLEAN = /*$$(*/ "Boolean"; /*)*/
  public static final String EDITOR_M2O = /*$$(*/ "Many to one"; /*)*/
  public static final String EDITOR_M2M = /*$$(*/ "Many to many"; /*)*/
  public static final String EDITOR_JM2M = /*$$(*/ "Json many to many"; /*)*/
  public static final String EDITOR_JM2O = /*$$(*/ "Json many to one"; /*)*/
  public static final String EDITOR_JO2M = /*$$(*/ "Json one to many"; /*)*/
  public static final String EDITOR_O2M = /*$$(*/ "One to many"; /*)*/
  public static final String EDITOR_ONNEW = /*$$(*/ "On new"; /*)*/
  public static final String EDITOR_ONSAVE = /*$$(*/ "On save"; /*)*/
  public static final String EDITOR_DEFAULT = /*$$(*/ "Default value"; /*)*/
  public static final String EDITOR_SELECTION = /*$$(*/ "Selection"; /*)*/
  public static final String EDITOR_WIDGET = /*$$(*/ "Widget"; /*)*/
  public static final String EDITOR_HELP = /*$$(*/ "Help"; /*)*/
  public static final String EDITOR_REQUIRED = /*$$(*/ "Required"; /*)*/
  public static final String EDITOR_MIN_SIZE = /*$$(*/ "Min size"; /*)*/
  public static final String EDITOR_MAX_SIZE = /*$$(*/ "Max size"; /*)*/
  public static final String EDITOR_REGEX = /*$$(*/ "Regex"; /*)*/
  public static final String EDITOR_ONCHANGE = /*$$(*/ "On change"; /*)*/
  public static final String EDITOR_COLSPAN = /*$$(*/ "Colspan"; /*)*/
  public static final String EDITOR_NAMEFIELD = /*$$(*/ "Name field"; /*)*/
  public static final String EDITOR_ONCLICK = /*$$(*/ "On click"; /*)*/
  public static final String EDITOR_HIDDEN_GRID = /*$$(*/ "Hidden in grid"; /*)*/
  public static final String EDITOR_SHOW_IF = /*$$(*/ "Show if"; /*)*/
  public static final String EDITOR_REQUIRED_IF = /*$$(*/ "Required if"; /*)*/
  public static final String EDITOR_TYPE = /*$$(*/ "Type"; /*)*/
  public static final String EDITOR_META_MODEL = /*$$(*/ "Meta model"; /*)*/
  public static final String EDITOR_DOMAIN = /*$$(*/ "Domain"; /*)*/
  public static final String EDITOR_TARGET_JSON = /*$$(*/ "Target json model"; /*)*/
  public static final String EDITOR_SELECT_OPT = /*$$(*/ "Select option"; /*)*/
  public static final String EDITOR_PROP = /*$$(*/ "Properties"; /*)*/
  public static final String EDITOR_APP_NAME = /*$$(*/ "App name"; /*)*/
  public static final String EDITOR_SHOW_TITLE = /*$$(*/ "Show title"; /*)*/
  public static final String EDITOR_NAME_COLUMN = /*$$(*/ "Name column"; /*)*/
  public static final String EDITOR_CAN_COLLAPSE = /*$$(*/ "Can collapse"; /*)*/
  public static final String EDITOR_COLLAPSE_IF = /*$$(*/ "Collapse if"; /*)*/
  public static final String EDITOR_FIELD_OPTIONS = /*$$(*/ "Field options"; /*)*/
  public static final String EDITOR_UI_OPTIONS = /*$$(*/ "Ui options"; /*)*/
  public static final String EDITOR_IS_JSON_RELATIONAL_FIELD = /*$$(*/
      "Is json relational field"; /*)*/
  public static final String EDITOR_COMMON = /*$$(*/ "Common"; /*)*/
  public static final String EDITOR_PANELS = /*$$(*/ "Panels"; /*)*/
  public static final String EDITOR_TABS = /*$$(*/ "Tabs"; /*)*/
  public static final String EDITOR_SELECTION_TEXT = /*$$(*/ "Selection text"; /*)*/
  public static final String EDITOR_RELATIONAL_FIELDS = /*$$(*/ "Relational fields"; /*)*/
  public static final String EDITOR_PROMPT = /*$$(*/ "Prompt"; /*)*/
  public static final String EDITOR_VALID_IF = /*$$(*/ "Valid if"; /*)*/
  public static final String EDITOR_NAME_UNIQUE = /*$$(*/ "Name (unique)"; /*)*/
  public static final String EDITOR_UPDATE_SELECTION = /*$$(*/ "Update selection"; /*)*/
  public static final String EDITOR_MULTILINE = /*$$(*/ "Multiline"; /*)*/
  public static final String EDITOR_SIDEBAR = /*$$(*/ "Sidebar"; /*)*/
  public static final String EDITOR_ITEMSPAN = /*$$(*/ "Itemspan"; /*)*/

  public static final String BPM_APP_NAME = /*$$(*/ "value:BPM"; /*)*/
  public static final String PRODUCT_APP_NAME = /*$$(*/ "value:Product App"; /*)*/

  public static final String EDITOR_CSS = /*$$(*/ "Css"; /*)*/
  public static final String EDITOR_WIDGET_ATTRIBUTES = /*$$(*/ "Widget attributes"; /*)*/
  public static final String EDITOR_NO_OPTIONS = /*$$(*/ "No options"; /*)*/
  public static final String EDITOR_DATE_TIME = /*$$(*/ "Date time"; /*)*/
  public static final String EDITOR_TARGET_MODEL_REQUIRED = /*$$(*/ "Target model required"; /*)*/
  public static final String EDITOR_TARGET_JSON_MODEL_REQUIRED = /*$$(*/
      "Target json model required"; /*)*/
  public static final String EDITOR_ONE_TO_ONE = /*$$(*/ "One to one"; /*)*/
  public static final String EDITOR_FIRST_LETTER_MESSAGE = /*$$(*/
      "First Letter of the name should always be alphabet"; /*)*/
  public static final String REQUIRED = /*$$(*/ "required"; /*)*/
  public static final String EDITOR_ENTER_NAME_TITLE_ALERT = /*$$(*/
      "Please enter form name and title"; /*)*/
  public static final String EDITOR_FIELD_NAME_ALERT = /*$$(*/ "Field name should be unique"; /*)*/
  public static final String EDITOR_MODEL_NAME_ALERT = /*$$(*/ "Model name should be unique"; /*)*/
}
