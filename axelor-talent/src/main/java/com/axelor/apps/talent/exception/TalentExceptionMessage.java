package com.axelor.apps.talent.exception;

public final class TalentExceptionMessage {

  private TalentExceptionMessage() {}

  public static final String INVALID_DATE_RANGE = /*$$(*/
      "Invalid dates. From date must be before to date." /*)*/;

  public static final String INVALID_TR_DATE = /*$$(*/
      "Training dates must be under training session date range." /*)*/;

  public static final String NO_EVENT_GENERATED = /*$$(*/
      "No Training register is generated because selected employees don't have any user." /*)*/;
}
