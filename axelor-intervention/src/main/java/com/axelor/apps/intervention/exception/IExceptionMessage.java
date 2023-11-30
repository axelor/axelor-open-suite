/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.intervention.exception;

public interface IExceptionMessage {

  String CSV_IMPORT_ERROR = /*$$(*/
      "There was an error when importing the CSV file %s. Please check your file." /*)*/;
  String EQUIP_IMPORT_1 = /*$$(*/
      "There is no existing equipment with the sequence %s. Skip." /*)*/;
  String EQUIP_IMPORT_2 = /*$$(*/ "Missing required code. Skip." /*)*/;
  String EQUIP_IMPORT_3 = /*$$(*/ "Missing required name. Skip." /*)*/;
  String EQUIP_IMPORT_4 = /*$$(*/ "Missing required type select. Skip." /*)*/;
  String EQUIP_IMPORT_7 = /*$$(*/
      "No existing contract found for the contract id %s. Null value passed." /*)*/;
  String EQUIP_IMPORT_8 = /*$$(*/
      "No existing equipment found for the sequence %s. Null value passed." /*)*/;
  String EQUIP_IMPORT_9 = /*$$(*/
      "No existing equipment family found for the code %s. Null value passed." /*)*/;

  String EQUIPMENT_MODEL_REMOVE_NOT_ALLOWED = /*$$(*/
      "Impossible to remove equipment model that has children's equipment models" /*)*/;
  String EQUIPMENT_REMOVE_NO_ALLOWED = /*$$(*/
      "Impossible to remove equipment that has children's equipment" /*)*/;

}
