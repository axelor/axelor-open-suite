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
package com.axelor.studio.service.data;

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
        "Help",
        "Help FR",
        "Panel Level",
        "Widget"
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

  public static final int NOTE = 0;
  public static final int MODULE = 1;
  public static final int IF_MODULE = 2;
  public static final int MODEL = 3;
  public static final int VIEW = 4;
  public static final int NAME = 5;
  public static final int TITLE = 6;
  public static final int TITLE_FR = 7;
  public static final int TYPE = 8;
  public static final int SELECT = 9;
  public static final int SELECT_FR = 10;
  public static final int MENU = 11;
  public static final int MENU_FR = 12;
  public static final int REQUIRED = 13;
  public static final int READONLY = 14;
  public static final int HIDDEN = 15;
  public static final int SHOW_IF = 16;
  public static final int IF_CONFIG = 17;
  public static final int FORMULA = 18;
  public static final int EVENT = 19;
  public static final int DOMAIN = 20;
  public static final int ON_CHANGE = 21;
  public static final int COLSPAN = 22;
  public static final int GRID = 23;
  public static final int HELP = 24;
  public static final int HELP_FR = 25;
  public static final int PANEL_LEVEL = 26;
  public static final int WIDGET = 27;

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
