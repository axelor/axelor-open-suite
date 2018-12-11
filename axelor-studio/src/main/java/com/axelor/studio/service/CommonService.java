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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonService {

  public static final String[] HEADERS =
      new String[] {
        "Note",
        "Module",
        "If Module",
        "Object",
        "View",
        "Name",
        "Title",
        "Title FR",
        "Type",
        "Selection",
        "Selection FR",
        "Menu",
        "Menu FR",
        "Required",
        "Readonly",
        "Hidden",
        "Show if",
        "If config",
        "Formula",
        "Event",
        "Domain",
        "On change",
        "Colspan",
        "Grid",
        "Doc",
        "Doc FR",
        "Panel Level",
        "Widget",
        "Help"
      };

  public static final String[] HELP_HEADERS =
      new String[] {
        "Note",
        "Module",
        "Object",
        "View",
        "Name",
        "Title",
        "Title FR",
        "Type",
        "Menu",
        "Menu FR",
        "Doc",
        "Doc FR",
        "Help"
      };

  public static final Map<String, String> FIELD_TYPES;

  static {
    Map<String, String> map = new HashMap<String, String>();
    map.put("char", "string");
    map.put("html", "string");
    map.put("text", "string");
    map.put("url", "string");
    map.put("long", "long");
    map.put("date", "date");
    map.put("datetime", "datetime");
    map.put("time", "time");
    map.put("duration", "integer");
    map.put("select", "integer");
    map.put("multiselect", "string");
    map.put("binary", "binary");
    map.put("m2o", "many-to-one");
    map.put("o2m", "one-to-many");
    map.put("m2m", "many-to-many");
    map.put("o2o", "one-to-one");
    map.put("boolean", "boolean");
    map.put("int", "integer");
    map.put("decimal", "decimal");
    map.put("file", "many-to-one");
    FIELD_TYPES = Collections.unmodifiableMap(map);
  }

  public static final Map<String, String> VIEW_ELEMENTS;

  static {
    Map<String, String> map = new HashMap<String, String>();
    map.put("panel", "panel");
    map.put("panelbook", "panelbook");
    map.put("panelside", "panelside");
    map.put("paneltab", "paneltab");
    map.put("button", "button");
    map.put("wizard", "wizard");
    map.put("error", "error");
    map.put("note", "note");
    map.put("label", "label");
    map.put("warn", "warn");
    map.put("menubar", "menubar");
    map.put("stream", "stream");
    map.put("menubar.item", "menubar");
    map.put("dashlet", "dashlet");
    map.put("general", "general");
    map.put("empty", "empty");
    map.put("onsave", "onsave");
    map.put("onnew", "onnew");
    map.put("onload", "onload");
    map.put("colspan", "colspan");
    map.put("spacer", "spacer");
    VIEW_ELEMENTS = Collections.unmodifiableMap(map);
  }

  public static final List<String> IGNORE_TYPES;

  static {
    List<String> types = new ArrayList<String>();
    types.add("general");
    types.add("tip");
    types.add("warn");
    types.add("note");
    types.add("empty");
    IGNORE_TYPES = Collections.unmodifiableList(types);
  }

  public static final List<String> NON_MODEL;

  static {
    List<String> types = new ArrayList<String>();
    types.add("Menu");
    types.add("Actions");
    types.add("MissingFields");
    types.add("Modules");
    NON_MODEL = Collections.unmodifiableList(types);
  }

  public static final Map<String, String> FR_MAP;

  static {
    Map<String, String> map = new HashMap<String, String>();
    map = new HashMap<String, String>();
    map.put("chaine", "char");
    map.put("tableau", "o2m");
    map.put("entier", "int");
    map.put("fichier", "file");
    map.put("bouton", "button");
    map.put("Note", "general");
    map.put("case à cocher", "boolean");
    map.put("Astuce", "tip");
    map.put("Attention", "warn");
    FR_MAP = Collections.unmodifiableMap(map);
  }

  public static final String NOTE = "Note";
  public static final String MODULE = "Module";
  public static final String IF_MODULE = "If module";
  public static final String MODEL = "Object";
  public static final String VIEW = "View";
  public static final String NAME = "Name";
  public static final String TITLE = "Title";
  public static final String TITLE_FR = "Title FR";
  public static final String TYPE = "Type";
  public static final String SELECT = "Selection";
  public static final String SELECT_FR = "Selection FR";
  public static final String MENU = "Menu";
  public static final String MENU_FR = "Menu FR";
  public static final String REQUIRED = "Required";
  public static final String READONLY = "Readonly";
  public static final String HIDDEN = "Hidden";
  public static final String SHOW_IF = "Show If";
  public static final String IF_CONFIG = "If config";
  public static final String FORMULA = "Formula";
  public static final String EVENT = "Event";
  public static final String DOMAIN = "Domain";
  public static final String ON_CHANGE = "On change";
  public static final String COLSPAN = "Colspan";
  public static final String GRID = "Grid";
  public static final String DOC = "Doc";
  public static final String DOC_FR = "Doc FR";
  public static final String PANEL_LEVEL = "Panel Level";
  public static final String WIDGET = "Widget";
  public static final String HELP = "Help";

  public static final Map<String, String> RELATIONAL_TYPES;

  static {
    Map<String, String> map = new HashMap<String, String>();
    map = new HashMap<String, String>();
    map.put("o2m", "OneToMany");
    map.put("m2m", "ManyToMany");
    map.put("m2o", "ManyToOne");
    map.put("file", "ManyToOne");
    map.put("o2o", "OneToOne");
    RELATIONAL_TYPES = Collections.unmodifiableMap(map);
  }

  public static final String[] MODULE_HEADERS =
      new String[] {"Module", "Depends", "Title", "Version", "Description", "Parent view priority"};

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
}
