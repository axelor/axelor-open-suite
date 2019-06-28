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
package com.axelor.studio.service;

import com.axelor.common.Inflector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommonService {

  public static final String[] HEADERS =
      new String[] {
        "Note",
        "View",
        "Type",
        "Name",
        "Position",
        "Title",
        "Title FR",
        "Selection",
        "Selection FR",
        "Wkf",
        "Menu",
        "Menu FR",
        "Context Field",
        "Context Field target",
        "context Field target name",
        "context Field title",
        "Context Field value",
        "Domain",
        "Enum Type",
        "Sequence",
        "Form View",
        "Grid View",
        "Help",
        "Hidden",
        "If Config",
        "Include If",
        "Max Size",
        "Min Size",
        "Name Field",
        "On change",
        "On click",
        "Formula",
        "Regex",
        "Readonly",
        "Required",
        "Show If",
        "Roles",
        "Precision",
        "Scale",
        "Value Expression",
        "Visible In Grid",
        "Widget",
        "Widget Attrs"
      };

  public static final String NOTE = "Note";
  public static final String VIEW = "View";
  public static final String TYPE = "Type";
  public static final String NAME = "Name";
  public static final String POSITION = "Position";
  public static final String TITLE = "Title";
  public static final String TITLE_FR = "Title FR";
  public static final String SELECT = "Selection";
  public static final String SELECT_FR = "Selection FR";
  public static final String WKF = "Wkf";
  public static final String MENU = "Menu";
  public static final String MENU_FR = "Menu FR";
  public static final String CONTEXT_FIELD = "Context Field";
  public static final String CONTEXT_FIELD_TARGET = "Context Field target";
  public static final String CONTEXT_FIELD_TARGET_NAME = "context Field target name";
  public static final String CONTEXT_FIELD_TITLE = "context Field title";
  public static final String CONTEXT_FIELD_VALUE = "Context Field value";
  public static final String DOMAIN = "Domain";
  public static final String ENUM_TYPE = "Enum Type";
  public static final String SEQUENCE = "Sequence";
  public static final String FORM_VIEW = "Form View";
  public static final String GRID_VIEW = "Grid View";
  public static final String HELP = "Help";
  public static final String HIDDEN = "Hidden";
  public static final String IF_CONFIG = "If Config";
  public static final String INCLUDE_IF = "Include If";
  public static final String MAX_SIZE = "Max Size";
  public static final String MIN_SIZE = "Min Size";
  public static final String NAME_FIELD = "Name Field";
  public static final String ON_CHANGE = "On change";
  public static final String ON_CLICK = "On click";
  public static final String FORMULA = "Formula";
  public static final String REGEX = "Regex";
  public static final String READONLY = "Readonly";
  public static final String REQUIRED = "Required";
  public static final String SHOW_IF = "Show If";
  public static final String ROLES = "Roles";
  public static final String PRECISION = "Precision";
  public static final String SCALE = "Scale";
  public static final String VALUE_EXPR = "Value Expression";
  public static final String VISIBLE_IN_GRID = "Visible In Grid";
  public static final String WIDGET = "Widget";
  public static final String WIDGET_ATTRS = "Widget Attrs";

  public static final String[] MENU_HEADERS =
      new String[] {
        "Notes",
        "Object",
        "Views",
        "Name",
        "Title",
        "Title FR",
        "Parent",
        "Order",
        "Icon",
        "Background",
        "Filters",
        "Action"
      };

  public static final String OBJECT = "Object";
  public static final String VIEWS = "Views";
  public static final String MENU_NAME = "Name";
  public static final String MENU_TITLE = "Title";
  public static final String MENU_TITLE_FR = "Title FR";
  public static final String PARENT = "Parent";
  public static final String ORDER = "Order";
  public static final String ICON = "Icon";
  public static final String BACKGROUND = "Background";
  public static final String FILTER = "Filters";
  public static final String ACTION = "Action";

  public static final List<String> FIELD_TYPES;

  static {
    List<String> types = new ArrayList<String>();
    types.add("string");
    types.add("integer");
    types.add("decimal");
    types.add("boolean");
    types.add("datetime");
    types.add("date");
    types.add("time");
    types.add("many-to-one");
    types.add("one-to-many");
    types.add("many-to-many");
    types.add("one-to-one");
    FIELD_TYPES = Collections.unmodifiableList(types);
  }

  public static final List<String> RELATIONAL_FIELD_TYPES;

  static {
    List<String> types = new ArrayList<String>();
    types.add("many-to-one");
    types.add("one-to-many");
    types.add("many-to-many");
    types.add("one-to-one");
    RELATIONAL_FIELD_TYPES = Collections.unmodifiableList(types);
  }

  public static final List<String> VIEW_ELEMENTS;

  static {
    List<String> elements = new ArrayList<String>();
    elements.add("onnew");
    elements.add("onsave");
    elements.add("panel");
    elements.add("button");
    elements.add("separator");
    VIEW_ELEMENTS = Collections.unmodifiableList(elements);
  }

  public static final List<String> POSITION_TYPES;

  static {
    List<String> types = new ArrayList<String>();
    types.add("before");
    types.add("after");
    types.add("replace");
    POSITION_TYPES = Collections.unmodifiableList(types);
  }

  public static final List<String> MODEL_TYPES = Arrays.asList(new String[] {"Real", "Custom"});

  public static final List<String> RELATIONAL_JSON_FIELD_TYPES =
      Arrays.asList(new String[] {"json-many-to-one", "json-one-to-many", "json-many-to-many"});

  public static final String[] WKF_HEADER =
      new String[] {
        "Name", "Model", "Json field", "Json", "Status", "Display type", "Xml", "App", "Description"
      };

  public static final String WKF_NAME = "Name";
  public static final String WKF_MODEL = "Model";
  public static final String WKF_JSON_FIELD = "Json field";
  public static final String WKF_JSON = "Json";
  public static final String WKF_STATUS = "Status";
  public static final String WKF_DISPLAY = "Display type";
  public static final String WKF_XML = "Xml";
  public static final String WKF_APP = "App";
  public static final String WKF_DESC = "Description";

  public static final String[] WKF_NODE_HEADER =
      new String[] {
        "Name",
        "Title",
        "Xml id",
        "Wkf",
        "Field",
        "Field model",
        "Sequence",
        "Start node",
        "End node",
        "Actions"
      };

  public static final String WKF_NODE_NAME = "Name";
  public static final String WKF_NODE_TITLE = "Title";
  public static final String WKF_NODE_XML = "Xml id";
  public static final String WKF_NODE_WKF = "Wkf";
  public static final String WKF_NODE_FIELD = "Field";
  public static final String WKF_NODE_FIELD_MODEL = "Field model";
  public static final String WKF_NODE_SEQ = "Sequence";
  public static final String WKF_NODE_START = "Start node";
  public static final String WKF_NODE_END = "End node";
  public static final String WKF_NODE_ACTIONS = "Actions";

  public static final String[] WKF_TRANSITION_HEADER =
      new String[] {
        "Name",
        "Xml id",
        "Button",
        "Button title",
        "Wkf",
        "Source node",
        "Target node",
        "Alert type",
        "Alert msg",
        "Success msg"
      };

  public static final String WKF_TRANS_NAME = "Name";
  public static final String WKF_TRANS_XML = "Xml id";
  public static final String WKF_TRANS_BUTTON = "Button";
  public static final String WKF_TRANS_BUTTON_TITLE = "Button title";
  public static final String WKF_TRANS_WKF = "Wkf";
  public static final String WKF_TRANS_SOURCE_NODE = "Source node";
  public static final String WKF_TRANS_TARGET_NODE = "Target node";
  public static final String WKF_TRANS_ALERT_TYPE = "Alert type";
  public static final String WKF_TRANS_ALERT_MSG = "Alert msg";
  public static final String WKF_TRANS_SUCCESS_MSG = "Success msg";

  public static final List<String> IGNORE_KEYS;

  static {
    List<String> keys = new ArrayList<String>();
    keys.add("Menu");
    keys.add("Wkf");
    keys.add("WkfNode");
    keys.add("WkfTransition");
    IGNORE_KEYS = Collections.unmodifiableList(keys);
  }

  public static final List<String> WKF_KEYS;

  static {
    List<String> keys = new ArrayList<String>();
    keys.add("Wkf");
    keys.add("WkfNode");
    keys.add("WkfTransition");
    WKF_KEYS = Collections.unmodifiableList(keys);
  }

  public final Inflector inflector = Inflector.getInstance();

  /**
   * Method to create field name from title if name of field is blank. It will simplify title and
   * make standard field name from it.
   *
   * @param title Title string to process.
   * @return Name created from title.
   */
  public String getFieldName(String title) {

    title = title.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("^[0-9]+", "");

    return inflector.camelize(inflector.simplify(title.trim()), true);
  }

  /**
   * Method create default view name from given modelName and viewType.
   *
   * @param modelName Model name.
   * @param viewType Type of view
   * @return View name.
   */
  public static String getDefaultViewName(String modelName, String viewType) {

    if (modelName.contains(".")) {
      String[] model = modelName.split("\\.");
      modelName = model[model.length - 1];
    }

    modelName =
        modelName
            .trim()
            .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
            .replaceAll("([a-z\\d])([A-Z])", "$1-$2")
            .toLowerCase();

    return modelName + "-" + viewType;
  }
}
