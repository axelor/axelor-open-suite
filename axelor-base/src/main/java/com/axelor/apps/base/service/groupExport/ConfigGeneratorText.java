package com.axelor.apps.base.service.groupExport;

public interface ConfigGeneratorText {

  public static final String CONFIG_START = /*$$(*/
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
          + "<csv-inputs xmlns=\"http://axelor.com/xml/ns/data-import\"\r\n"
          + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
          + "  xsi:schemaLocation=\"http://axelor.com/xml/ns/data-import http://axelor.com/xml/ns/data-import/data-import_5.3.xsd\">\n\n" /*)*/;

  public static final String CONFIG_END = /*$$(*/ "</csv-inputs>" /*)*/;
}
