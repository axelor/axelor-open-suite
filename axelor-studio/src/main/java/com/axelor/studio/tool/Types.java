package com.axelor.studio.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Types {

  private Types() {
    throw new IllegalStateException("Utility class");
  }

  public static final String TYPE_BINARY = "Binary";
  public static final String TYPE_BOOLEAN = "Boolean";
  public static final String TYPE_INTEGER = "Integer";
  public static final String TYPE_LONG = "Long";
  public static final String TYPE_DECIMAL = "Integer";
  public static final String TYPE_STRING = "String";
  public static final String TYPE_TEXT = "Text";
  public static final String TYPE_DATE = "Date";
  public static final String TYPE_TIME = "Time";
  public static final String TYPE_DATETIME = "DateTime";
  public static final String TYPE_ENUM = "Enum";
  public static final String TYPE_REFERENCE = "Reference";
  public static final String TYPE_M2O = "M2O";
  public static final String TYPE_M2M = "M2M";

  private static Map<String, List<String>> widgetsByType;

  public static Map<String, List<String>> getWidgetsByType() {
    return widgetsByType;
  }

  static {
    addWidgetsToBoolean();
    addWidgetsToString();
    addWidgetsToNumericTypes();
    addWidgetsToSelectableTypes();

    addWidgetToType(TYPE_TEXT, Widgets.WIDGET_HTML);
    addWidgetToType(TYPE_TEXT, Widgets.WIDGET_CODE_EDITOR);
    addWidgetToType(TYPE_BINARY, Widgets.WIDGET_BINARY_LINK);
    addWidgetToType(TYPE_BINARY, Widgets.WIDGET_IMAGE);
    addWidgetToType(TYPE_M2O, Widgets.WIDGET_IMAGE); // M2O of MetaFile
    addWidgetToType(TYPE_M2O, Widgets.WIDGET_SUGGEST_BOX);
    addWidgetToType(TYPE_REFERENCE, Widgets.WIDGET_REF_SELECT);
    addWidgetToType(TYPE_M2M, Widgets.WIDGET_TAG_SELECT);
  }

  private static void addWidgetToType(String type, String widget) {
    widgetsByType.computeIfPresent(type, (t, w) -> widgetsByType.get(t)).add(widget);
    widgetsByType.computeIfAbsent(type, t -> new ArrayList<>()).add(widget);
  }

  private static void addWidgetsToBoolean() {
    addWidgetToType(TYPE_BOOLEAN, Widgets.WIDGET_INLINE_CHECKBOX);
    addWidgetToType(TYPE_BOOLEAN, Widgets.WIDGET_TOGGLE);
    addWidgetToType(TYPE_BOOLEAN, Widgets.WIDGET_BOOLEAN_SELECT);
    addWidgetToType(TYPE_BOOLEAN, Widgets.WIDGET_BOOLEAN_RADIO);
    addWidgetToType(TYPE_BOOLEAN, Widgets.WIDGET_BOOLEAN_SWITCH);
  }

  private static void addWidgetsToString() {
    addWidgetToType(TYPE_STRING, Widgets.WIDGET_EMAIL);
    addWidgetToType(TYPE_STRING, Widgets.WIDGET_URL);
    addWidgetToType(TYPE_STRING, Widgets.WIDGET_PASSWORD);
    addWidgetToType(TYPE_STRING, Widgets.WIDGET_IMAGE_LINK);
    addWidgetToType(TYPE_STRING, Widgets.WIDGET_HTML);
    addWidgetToType(TYPE_STRING, Widgets.WIDGET_CODE_EDITOR);
    // The String must have comma separated values and be related with a selection
    addWidgetToType(TYPE_STRING, Widgets.WIDGET_TAG_SELECT);
  }

  private static void addWidgetsToNumericTypes() {
    List<String> numericTypes = new ArrayList<>();
    numericTypes.add(TYPE_INTEGER);
    numericTypes.add(TYPE_LONG);
    numericTypes.add(TYPE_DECIMAL);

    for (String numericType : numericTypes) {
      addWidgetToType(numericType, Widgets.WIDGET_RELATIVE_TIME);
      addWidgetToType(numericType, Widgets.WIDGET_DURATION);
      addWidgetToType(numericType, Widgets.WIDGET_PROGRESS);
      addWidgetToType(numericType, Widgets.WIDGET_SELECT_PROGRESS);
    }
  }

  private static void addWidgetsToSelectableTypes() {
    List<String> selectableTypes = new ArrayList<>();
    selectableTypes.add(TYPE_BINARY);
    selectableTypes.add(TYPE_BOOLEAN);
    selectableTypes.add(TYPE_STRING);
    selectableTypes.add(TYPE_TEXT);
    selectableTypes.add(TYPE_INTEGER);
    selectableTypes.add(TYPE_LONG);
    selectableTypes.add(TYPE_DECIMAL);
    selectableTypes.add(TYPE_DATE);
    selectableTypes.add(TYPE_TIME);
    selectableTypes.add(TYPE_DATETIME);
    selectableTypes.add(TYPE_ENUM);

    for (String selectableType : selectableTypes) {
      addWidgetToType(selectableType, Widgets.WIDGET_IMAGE_SELECT);
      addWidgetToType(selectableType, Widgets.WIDGET_MULTI_SELECT);
      addWidgetToType(selectableType, Widgets.WIDGET_RADIO_SELECT);
      addWidgetToType(selectableType, Widgets.WIDGET_CHECKBOX_SELECT);
      addWidgetToType(selectableType, Widgets.WIDGET_NAV_SELECT);
    }
  }
}
