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
package com.axelor.apps.bankpayment.service.cfonb;

import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CfonbToolService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Procédure permettant de vérifier que la chaine de caractère ne contient que des entier
   *
   * @param s La chaine de caractère à tester
   * @param company Une société
   * @throws AxelorException
   */
  public void testDigital(String value, String zone) throws AxelorException {
    if (!StringTool.isDigital(value)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CFONB_TOOL_DIGITAL_ZONE_NOT_CORRECT),
          zone,
          value);
    }
  }

  /**
   * Procédure permettant de vérifier la longueur d'un enregistrement CFONB
   *
   * @param s Un enregistrement CFONB
   * @param company Une société
   * @param type Le type d'enregistrement :
   *     <ul>
   *       <li>0 = émetteur
   *       <li>1 = destinataire
   *       <li>2 = total
   *       <li>3 = entête
   *       <li>4 = détail
   *       <li>5 = fin
   *     </ul>
   *
   * @param size La longueur de l'enregistrement
   * @throws AxelorException
   */
  public void testLength(String s, int size) throws AxelorException {
    if (s.length() != size) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CFONB_TOOL_NB_OF_CHAR_PER_LINE),
          size);
    }
  }

  /**
   * Méthode permettant de mettre en majuscule et sans accent un CFONB
   *
   * @param cFONB
   * @return Le CFONB nettoyé
   */
  public List<String> toUpperCase(List<String> cFONB) {
    List<String> upperCase = new ArrayList<String>();
    for (String s : cFONB) {
      upperCase.add(StringTool.deleteAccent(s.toUpperCase()));
    }
    return upperCase;
  }

  /**
   * Méthode permettant de mettre en majuscule et sans accent un CFONB
   *
   * @param cFONB
   * @return Le CFONB nettoyé
   */
  public String toUpperCase(String record) {

    return StringTool.deleteAccent(record.toUpperCase());
  }

  public void checkFilled(String value, String numZone) throws AxelorException {

    if (Strings.isNullOrEmpty(value)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CFONB_TOOL_EMPTY_ZONE),
          numZone);
    }
  }

  public String normalizeNumber(BigDecimal amount) {

    return amount.setScale(2).toString().replace(".", "");
  }

  /** "M" = Obligatoire (Mandatory) */
  public final String STATUS_MANDATORY = "M";
  /** "O" = Optionnel (Optional) */
  public final String STATUS_OPTIONAL = "O";
  /**
   * "D" = Dépendant (Dependent), la condition de présence de la donnée est précisée dans les
   * tableaux de description des enregistrements
   */
  public final String STATUS_DEPENDENT = "D";
  /**
   * "N" = Non utilisée (zone que le CFONB se réserve le droit d'utiliser ultérieurement qui doit
   * alors être à blanc, ou zone non utilisée pour ce type de remise qui est alors ignorée par la
   * banque).
   */
  public final String STATUS_NOT_USED = "N";

  /** "AN" = alphanumérique */
  public final String FORMAT_ALPHA_NUMERIC = "AN";
  /** "N" = numérique */
  public final String FORMAT_NUMERIC = "N";
  /** "A" = Alphabétique */
  public final String FORMAT_ALPHA = "A";

  public String createZone(String numOfZone, String value, String status, String format, int length)
      throws AxelorException {

    String zone = value;

    /** la colonne "Statut" correspond au statut des données et peut prendre les valeurs : * */
    switch (status) {
      case STATUS_MANDATORY:
        this.checkFilled(zone, numOfZone);

        break;

      case STATUS_OPTIONAL:
        if (zone == null) {
          zone = "";
        }
        break;

      case STATUS_DEPENDENT:
        if (zone == null) {
          zone = "";
        }
        break;

      case STATUS_NOT_USED:
        zone = "";
        break;

      default:
        break;
    }

    /** la colonne "Format" correspond au format des données et peut prendre les valeurs : * */
    switch (format) {
      case FORMAT_ALPHA_NUMERIC:
        zone = StringTool.deleteAccent(zone);
        zone = StringTool.fillStringRight(zone, ' ', length);
        break;

      case FORMAT_NUMERIC:
        this.testDigital(zone, numOfZone);
        zone = StringTool.fillStringLeft(zone, '0', length);
        break;

      case FORMAT_ALPHA:
        zone = StringTool.deleteAccent(zone);
        zone = StringTool.fillStringRight(zone, ' ', length);
        break;

      default:
        break;
    }

    return zone;
  }

  public String createZone(String numOfZone, int value, String status, String format, int length)
      throws AxelorException {

    return this.createZone(numOfZone, Integer.toString(value), status, format, length);
  }

  public String createZone(
      String numOfZone, BigDecimal value, String status, String format, int length)
      throws AxelorException {

    return this.createZone(numOfZone, this.normalizeNumber(value), status, format, length);
  }

  public String readZone(
      String numOfZone, String lineContent, String status, String format, int position, int length)
      throws AxelorException {

    String zone = lineContent.substring(position - 1, position + length - 1);

    /** la colonne "Statut" correspond au statut des données et peut prendre les valeurs : * */
    switch (status) {
      case STATUS_MANDATORY:
        this.checkFilled(zone, numOfZone);

        break;

      case STATUS_OPTIONAL:
        if (zone.replaceAll(" ", "").isEmpty()) {
          return null;
        }
        break;

      case STATUS_DEPENDENT:
        if (zone.replaceAll(" ", "").isEmpty()) {
          return null;
        }
        break;

      case STATUS_NOT_USED:
        return null;

      default:
        break;
    }

    /** la colonne "Format" correspond au format des données et peut prendre les valeurs : * */
    switch (format) {
      case FORMAT_ALPHA_NUMERIC:
        zone = zone.trim();
        break;

      case FORMAT_NUMERIC:
        this.testDigital(zone.trim(), numOfZone);
        break;

      case FORMAT_ALPHA:
        zone = zone.trim();
        break;

      default:
        break;
    }

    log.debug(
        "Read zone : {}, status : {}, format : {}, position : {}, length : {}, result : {}",
        numOfZone,
        status,
        format,
        position,
        length,
        zone);

    return zone;
  }

  /**
   * Fonction permettant de créer le CFONB
   *
   * @param senderCFONB Un enregistrement 'émetteur'
   * @param recipientCFONB Un liste d'enregistrement 'destinataire'
   * @param totalCFONB Un enregistrement 'total'
   * @return Le CFONB
   */
  public List<String> createCFONBFile(
      String senderRecord, List<String> recipientRecord, String totalRecord) {
    // checker meme compte emetteur
    // checker meme type de virement
    // checker meme date de règlement

    List<String> cFONB = new ArrayList<String>();
    cFONB.add(senderRecord);
    cFONB.addAll(recipientRecord);
    cFONB.add(totalRecord);
    return cFONB;
  }
}
