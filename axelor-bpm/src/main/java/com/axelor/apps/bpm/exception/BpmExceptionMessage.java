package com.axelor.apps.bpm.exception;

public final class BpmExceptionMessage {

  private BpmExceptionMessage() {}

  public static final String MISSING_INPUT_LABEL = /*$$(*/ "Missing input label" /*)*/;

  public static final String MISSING_OUTPUT_LABEL = /*$$(*/ "Missing output label" /*)*/;

  public static final String INVALID_IMPORT_FILE = /*$$(*/ "Data file must be excel file" /*)*/;

  public static final String INVALID_HEADER = /*$$(*/ "Header is invalid in import file" /*)*/;

  public static final String EMPTY_OUTPUT_COLUMN = /*$$(*/
      "Output columns can't be empty in import file" /*)*/;
}
