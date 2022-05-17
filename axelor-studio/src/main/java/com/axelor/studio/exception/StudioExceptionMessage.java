package com.axelor.studio.exception;

public final class StudioExceptionMessage {

  private StudioExceptionMessage() {}

  /** Check if app builder code is not conflicting with existing app. */
  public static final String APP_BUILDER_1 = /*$$(*/
      "Please provide unique code. The code '%s' is already used" /*)*/;

  /** Check if chart name doesn't contains any space. */
  public static final String CHART_BUILDER_1 = /*$$(*/ "The name must not contain spaces" /*)*/;

  public static final String CANNOT_ALTER_NODES = /*$$(*/
      "Can't alter nodes for real existing selection field" /*)*/;
}
