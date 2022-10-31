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
package com.axelor.studio.variables;

import java.util.Arrays;
import java.util.List;

public interface DataFormVariables {

  static final String MODEL_CODE = "modelCode";

  static final String RECORD_TO_CREATE = "recordToCreate";
  static final String ATTRS = "attrs";

  static final String ID = "id";
  static final String JSON_MODEL = "jsonModel";
  static final String MANY_TO_ONE = "many-to-one";
  static final String MANY_TO_MANY = "many-to-many";
  static final String JSON_MANY_TO_MANY = "json-many-to-many";
  static final String JSON_MANY_TO_ONE = "json-many-to-one";
  static final String CREATED_BY = "createdBy";
  static final String CONTENT_DISPOSITION = "Content-Disposition";
  static final String FILENAME = "filename";
  static final int START_INDEX = 10;
  static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
  static final String ALLOW_ALL = "*";
  static final String NULL = "NULL";
  static final String EMPTY = "EMPTY";
  static final String PATTERN = "pattern";
  static final String PLACEHOLDER = "placeholder";
  static final String SINGLE_QUOTE = "'";
  static final String SINGLE_QUOTE_HTML = "&#39;";
  static final String ITEMS = "items";
  static final String FIELDS = "fields";
  static final String NAME = "name";
  static final String DEFAULT_PATTERN = ".*";
  static final String DEFAULT_SUCCESS_MSG = "Submitted successfully";
  static final String DEFAULT_ERROR_MSG = "Error on Submit";
  static final String RESPONSE_FORMAT = "{\"message\":\"%s\",\"redirectUrl\":\"%s\"}";

  static final String REFERRED_ENTITY_IDENTIFIER = "$";
  static final String REFERRED_ENTITY_FIELD_IDENTIFER = "$#";

  static final String WIDGET_ATTRS = "widgetAttrs";

  static final String SUFFIX_SET = "Set";
  static final String TAG_SELECT = "TagSelect";
  static final String DATA_FORM_LINE = "dataFormLine";

  static final String REQUIRED = "required";

  static final String DOMAIN = "domain";
  static final String CAN_SELECT = "canSelect";
  static final String PANEL_EDITOR = "panelEditor";

  static final String WIDGET = "widget";

  static final List<String> VALID_TYPES =
      Arrays.asList("boolean", "date", "datetime", "decimal", "enum", "integer", "string", "time");

  static final String SHOW_IF =
      "($record.metaField.packageName == '%s' && $record.metaField.typeName == '%s') || $record.metaJsonField.targetModel == '%s'";
  static final String SHOW_IF_JSON = "$record.metaJsonField.targetJsonModel.name == '%s'";

  static final String INPUT_STRING =
      "<div class='col-6 top-buffer'><label class='form-check-label'>%s &nbsp;</label><br><input class='%s' type='%s' name='%s' %s/></div><br>\n";

  static final String SELECT_STRING_START =
      "<div class='col-6 top-buffer'><label>%s </label><select class='form-select' name='%s' %s>\n<option value=''>Please select your option</option>\n";

  static final String OPTION_STRING = "<option value='%s'>%s</option>\n";

  static final String SELECT_STRING_END = "</select></div><br>\n";

  static final String TEXT_AREA =
      "<div class='col-12 top-buffer'><label class='form-check-label'>%s &nbsp; </label><textarea class='form-control' name='%s' %s></textarea></div><br>\n";

  static final String FILE_INPUT =
      "<div class='col-6 top-buffer'><label>%s &nbsp;</label><br><input type='file' name='%s' accept='%s' onChange='fileCheck(event);' %s/><img style='display:none' id='%s' class='img-input'/></div><br>\n";

  static final String CREATE_BUTTON =
      "<div class='col-6 top-buffer'><label>%s &nbsp;<br></label><button type='button' class='btn btn-outline-dark col-12' name='$#%s' onClick='showEntityForm(event)'>Create</button></div><div style='border:1px solid gray; display:none;' class='col-12 top-buffer' id='$#%s'>";

  static final String CAPTCHA_BOX_DIV =
      "<div class='top-buffer' id='captcha'></div><button onclick='createCaptcha()' class='btn-outline-secondary' type='button'>&#8635;</button><input type='text' autocomplete='off' placeholder='Type the above texts here' id='captchaTextBox'/><p id='captcha-error-msg'></p>";

  static final String STEP_ATTRIBUTE = "step = '%s'";

  static final String END_DIV = "</div>";
  static final String WS_PUBLIC_DATA_FORM = "/ws/public/data-form";
  static final String MULTIPLE = "multiple";
  static final String JSON = "json";
  static final String MAX_LENGTH = "maxlength";
  static final String MAX = "max";
  static final String MIN_LENGTH = "minlength";
  static final String MIN = "min";
  static final String TEXT = "text";
  static final String NUMBER = "number";
  static final String CHECKBOX = "checkbox";
  static final String SET = "Set";
  static final String MANYTOMANY = "ManyToMany";
  static final String MANYTOONE = "ManyToOne";
  static final String ONETOONE = "OneToOne";
  static final String FORM_CONTROL = "form-control";
  static final String FORM_CHECKBOX = "form-checkbox";
  static final String DATE = "date";
  static final String DATE_TIME = "datetime-local";
  static final String TIME = "time";
  static final String IMAGE = "Image";
  static final String ACCEPT_ONLY_IMAGE_FILES = "image/*";
  static final String ACCEPT_ALL_FILES = "*";
  static final String ZERO = "0";
  static final String STEP_PATTERN = "0.%s1";
  static final String QUERY_EXCEPTION_NAMED_PARAMETER_FORMAT = "Named parameter .* not set";
  static final String QUERY_EXCEPTION_NAMED_PARAMETER_NOT_BOUND = "Named parameter not bound : .*";
  static final Double DEFAULT_STEP = 0.01;

  static final String TEMPLATE_FORM_NAME = "*FORM_NAME*";
  static final String TEMPLATE_FORM_ACTION = "*FORM_ACTION*";
  static final String TEMPLATE_FORM_CONTENT = "*FORM_CONTENT*";
  static final String TEMPLATE_MODEL_CODE = "*MODEL_CODE*";
  static final String TEMPLATE_RECORD_TO_CREATE = "*RECORD_TO_CREATE*";
  static final String TEMPLATE_LOCATION = "/form-builder/Template.html";
  static final String TEMPLATE_CAPTCHA_BOX = "*CAPTCHA_BOX*";
  static final String TEMPLATE_CAPTCHA_ATTEMPT = "*ATTEMPT_CAPTCHA*";
  static final String TEMPLATE_CAPTCHA_WRONG = "*WRONG_CAPTCHA*";

  static final String ATTEMPT_CAPTCHA_MESSAGE = /*$$(*/ "Please attempt the captcha" /*)*/;

  static final String WRONG_CAPTCHA_MESSAGE = /*$$(*/
      "Entered text did not match with the captcha" /*)*/;

  static final List<String> VALID_RELATIONSHIP_TYPES_META_MODEL =
      Arrays.asList(MANYTOMANY, MANYTOONE, ONETOONE);

  static final List<String> VALID_RELATIONSHIP_TYPES_JSON_MODEL =
      Arrays.asList(MANY_TO_MANY, MANY_TO_ONE, JSON_MANY_TO_MANY, JSON_MANY_TO_ONE);

  static final List<String> allowedForNumber =
      Arrays.asList("Integer", "integer", "Long", "long", "BigDecimal", "decimal");
  static final List<String> allowedForDate = Arrays.asList("date", "LocalDate");
  static final List<String> allowedForTime = Arrays.asList("time", "LocalTime");
  static final List<String> allowedForDateTime = Arrays.asList("datetime", "LocalDateTime");
  static final List<String> allowedForCheckbox = Arrays.asList("boolean", "Boolean");
}
