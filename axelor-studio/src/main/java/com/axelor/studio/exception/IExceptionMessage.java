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
package com.axelor.studio.exception;

public interface IExceptionMessage {

  /** Check if app builder code is not conflicting with existing app. */
  static final String APP_BUILDER_1 = /*$$(*/
      "Please provide unique code. The code '%s' is already used" /*)*/;

  /** Check if chart name doesn't contains any space. */
  static final String CHART_BUILDER_1 = /*$$(*/ "Name must not contains space" /*)*/;

  /** Message to display on click of edit icon of node or transition if workflow is not saved. */
  static final String WKF_1 = /*$$(*/ "Workflow is not saved" /*)*/;

  /** Message to display if model specified on ViewBuilder is not found on database * */
  static final String MODEL_NOT_FOUND = /*$$(*/ "Model '%s' is not found" /*)*/;

  /** Field not found on model * */
  static final String FIELD_NOT_FOUND = /*$$(*/ "Field '%s' is not found on model %s" /*)*/;

  /** No fields define on view builders * */
  static final String PLEASE_ADD_FIELDS = /*$$(*/ "Please add fields" /*)*/;

  /** No module related views or models to export * */
  static final String NO_MODULE_DATA = /*$$(*/ "No module related views or model found" /*)*/;

  /** Invalid zip * */
  static final String INVALID_ZIP = /*$$(*/
      "Uploaded file is not a zip file, please upload only zip file" /*)*/;

  /** Invalid module zip * */
  static final String INVALID_MODULE_ZIP = /*$$(*/
      "Uploaded zip file name is not a valid module name" /*)*/;

  /** Invalid zip entry * */
  static final String INVALID_ZIP_ENTRY = /*$$(*/ "Invalid zip entry: %s" /*)*/;

  /** Source direcotry not found * */
  static final String NO_SOURCE_DIR = /*$$(*/ "Source directory not configured" /*)*/;

  /** Module imported successfully * */
  static final String MODULE_IMPORTED = /*$$(*/
      "Module imported successfully, please restart server to install it" /*)*/;

  /** No build directory found * */
  static final String NO_BUILD_DIR = /*$$(*/
      "Error in application build. No build directory found" /*)*/;

  /** Set environment variables to build app and restart server */
  static final String NO_ENVIROMENT_VARIABLE = /*$$(*/ "Please set %s environment variable" /*)*/;

  /** Error in application build please check log */
  static final String BUILD_LOG_CHECK = /*$$(*/
      "Error in application build. Please check the log file" /*)*/;
}
