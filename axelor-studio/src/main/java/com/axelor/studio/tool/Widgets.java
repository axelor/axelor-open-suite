package com.axelor.studio.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Widgets {

  private Widgets() {
    throw new IllegalStateException("Utility class");
  }

  public static final String WIDGET_INLINE_CHECKBOX = "InlineCheckbox";
  public static final String WIDGET_TOGGLE = "Toggle";
  public static final String WIDGET_BOOLEAN_SELECT = "BooleanSelect";
  public static final String WIDGET_BOOLEAN_RADIO = "BooleanRadio";
  public static final String WIDGET_BOOLEAN_SWITCH = "BooleanSwitch";
  public static final String WIDGET_EMAIL = "Email";
  public static final String WIDGET_URL = "URL";
  public static final String WIDGET_PASSWORD = "Password";
  public static final String WIDGET_HTML = "Html";
  public static final String WIDGET_CODE_EDITOR = "CodeEditor";
  public static final String WIDGET_RELATIVE_TIME = "RelativeTime";
  public static final String WIDGET_DURATION = "Duration";
  public static final String WIDGET_PROGRESS = "Progress";
  public static final String WIDGET_SELECT_PROGRESS = "SelectProgress";
  public static final String WIDGET_IMAGE_SELECT = "ImageSelect";
  public static final String WIDGET_MULTI_SELECT = "MultiSelect";
  public static final String WIDGET_RADIO_SELECT = "RadioSelect";
  public static final String WIDGET_CHECKBOX_SELECT = "CheckboxSelect";
  public static final String WIDGET_NAV_SELECT = "NavSelect";
  public static final String WIDGET_IMAGE_LINK = "ImageLink";
  public static final String WIDGET_IMAGE = "Image";
  public static final String WIDGET_BINARY_LINK = "BinaryLink";
  public static final String WIDGET_SUGGEST_BOX = "SuggestBox";
  public static final String WIDGET_REF_SELECT = "RefSelect";
  public static final String WIDGET_TAG_SELECT = "TagSelect";

  private static Map<String, List<String>> typesByWidget;

  public static Map<String, List<String>> getTypesByWidget() {
    return typesByWidget;
  }

  static {
    addBooleanWidgets();
    addStringWidgets();
    addNumericWidgets();
    addSelectWidgets();

    addTypeToWidget(WIDGET_HTML, Types.TYPE_TEXT);
    addTypeToWidget(WIDGET_CODE_EDITOR, Types.TYPE_TEXT);
    addTypeToWidget(WIDGET_BINARY_LINK, Types.TYPE_BINARY);
    addTypeToWidget(WIDGET_IMAGE, Types.TYPE_BINARY);
    addTypeToWidget(WIDGET_IMAGE, Types.TYPE_M2O); // M2O of MetaFile
    addTypeToWidget(WIDGET_SUGGEST_BOX, Types.TYPE_M2O);
    addTypeToWidget(WIDGET_REF_SELECT, Types.TYPE_REFERENCE);
    addTypeToWidget(WIDGET_TAG_SELECT, Types.TYPE_M2M);
  }

  private static void addTypeToWidget(String widget, String type) {
    typesByWidget.computeIfPresent(widget, (w, t) -> typesByWidget.get(w)).add(type);
    typesByWidget.computeIfAbsent(widget, w -> new ArrayList<>()).add(type);
  }

  private static void addBooleanWidgets() {
    addTypeToWidget(WIDGET_INLINE_CHECKBOX, Types.TYPE_BOOLEAN);
    addTypeToWidget(WIDGET_TOGGLE, Types.TYPE_BOOLEAN);
    addTypeToWidget(WIDGET_BOOLEAN_SELECT, Types.TYPE_BOOLEAN);
    addTypeToWidget(WIDGET_BOOLEAN_RADIO, Types.TYPE_BOOLEAN);
    addTypeToWidget(WIDGET_BOOLEAN_SWITCH, Types.TYPE_BOOLEAN);
  }

  private static void addStringWidgets() {
    addTypeToWidget(WIDGET_EMAIL, Types.TYPE_STRING);
    addTypeToWidget(WIDGET_URL, Types.TYPE_STRING);
    addTypeToWidget(WIDGET_PASSWORD, Types.TYPE_STRING);
    addTypeToWidget(WIDGET_IMAGE_LINK, Types.TYPE_STRING);
    addTypeToWidget(WIDGET_HTML, Types.TYPE_STRING);
    addTypeToWidget(WIDGET_CODE_EDITOR, Types.TYPE_STRING);
    // The String must have comma separated values and be related with a selection
    addTypeToWidget(WIDGET_TAG_SELECT, Types.TYPE_STRING);
  }

  private static void addNumericWidgets() {
    List<String> numericTypes = new ArrayList<>();
    numericTypes.add(Types.TYPE_INTEGER);
    numericTypes.add(Types.TYPE_LONG);
    numericTypes.add(Types.TYPE_DECIMAL);

    for (String numericType : numericTypes) {
      addTypeToWidget(WIDGET_RELATIVE_TIME, numericType);
      addTypeToWidget(WIDGET_DURATION, numericType);
      addTypeToWidget(WIDGET_PROGRESS, numericType);
      addTypeToWidget(WIDGET_SELECT_PROGRESS, numericType);
    }
  }

  private static void addSelectWidgets() {
    List<String> selectableTypes = new ArrayList<>();
    selectableTypes.add(Types.TYPE_BINARY);
    selectableTypes.add(Types.TYPE_BOOLEAN);
    selectableTypes.add(Types.TYPE_STRING);
    selectableTypes.add(Types.TYPE_TEXT);
    selectableTypes.add(Types.TYPE_INTEGER);
    selectableTypes.add(Types.TYPE_LONG);
    selectableTypes.add(Types.TYPE_DECIMAL);
    selectableTypes.add(Types.TYPE_DATE);
    selectableTypes.add(Types.TYPE_TIME);
    selectableTypes.add(Types.TYPE_DATETIME);
    selectableTypes.add(Types.TYPE_ENUM);

    for (String selectableType : selectableTypes) {
      addTypeToWidget(WIDGET_IMAGE_SELECT, selectableType);
      addTypeToWidget(WIDGET_MULTI_SELECT, selectableType);
      addTypeToWidget(WIDGET_RADIO_SELECT, selectableType);
      addTypeToWidget(WIDGET_CHECKBOX_SELECT, selectableType);
      addTypeToWidget(WIDGET_NAV_SELECT, selectableType);
    }
  }
}
