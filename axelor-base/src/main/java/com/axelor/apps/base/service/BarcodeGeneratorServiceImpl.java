/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Singleton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BarcodeGeneratorServiceImpl implements BarcodeGeneratorService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public InputStream createBarCode(
      String serialno, BarcodeTypeConfig barcodeTypeConfig, boolean isPadding)
      throws AxelorException {

    if (serialno != null && barcodeTypeConfig != null) {
      BarcodeFormat barcodeFormat = null;
      switch (barcodeTypeConfig.getName()) {
        case "AZTEC":
          barcodeFormat = BarcodeFormat.AZTEC;
          break;

        case "CODABAR":
          barcodeFormat = BarcodeFormat.CODABAR;
          serialno = checkTypeForCodabar(serialno, barcodeFormat);
          break;

        case "CODE_39":
          barcodeFormat = BarcodeFormat.CODE_39;
          serialno = checkTypeForCode39(serialno, barcodeFormat);
          break;

        case "CODE_128":
          barcodeFormat = BarcodeFormat.CODE_128;
          break;

        case "DATA_MATRIX":
          barcodeFormat = BarcodeFormat.DATA_MATRIX;
          break;

        case "EAN_8":
          barcodeFormat = BarcodeFormat.EAN_8;
          serialno = checkTypeForEan8(serialno, barcodeFormat, isPadding);
          break;

        case "ITF":
          barcodeFormat = BarcodeFormat.ITF;
          serialno = checkTypeForItf(serialno, barcodeFormat, isPadding);
          break;

        case "PDF_417":
          barcodeFormat = BarcodeFormat.PDF_417;
          serialno = checkTypeForPdf417(serialno, barcodeFormat, isPadding);
          break;

        case "QR_CODE":
          barcodeFormat = BarcodeFormat.QR_CODE;
          break;

        case "UPC_A":
          barcodeFormat = BarcodeFormat.UPC_A;
          serialno = checkTypeForUpca(serialno, barcodeFormat, isPadding);
          break;

        case "EAN_13":
          barcodeFormat = BarcodeFormat.EAN_13;
          serialno = checkTypeForEan13(serialno, barcodeFormat, isPadding);
          break;

        default:
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.BARCODE_GENERATOR_9));
      }
      return generateBarcode(serialno, barcodeTypeConfig, barcodeFormat);
    }
    return null;
  }

  public InputStream generateBarcode(
      String serialno, BarcodeTypeConfig barcodeTypeConfig, BarcodeFormat barcodeFormat) {

    final MultiFormatWriter writer = new MultiFormatWriter();
    int height = barcodeTypeConfig.getHeight();
    int width = barcodeTypeConfig.getWidth();
    BitMatrix bt;
    try {
      bt = writer.encode(serialno, barcodeFormat, width, height);
      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      int[] pixels = new int[width * height];
      int index = 0;
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          pixels[index++] = bt.get(x, y) ? Color.BLACK.hashCode() : Color.WHITE.hashCode();
        }
      }
      image.setRGB(0, 0, width, height, pixels, 0, width);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ImageIO.write(image, "png", out);
      return new ByteArrayInputStream(out.toByteArray());
    } catch (WriterException | IOException e) {
      LOG.trace(e.getMessage(), e);
    }
    return null;
  }

  // accepts only number with variable length
  public String checkTypeForCodabar(String serialno, BarcodeFormat barcodeFormat)
      throws AxelorException {

    if (isNumber(serialno)) {
      return serialno;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.BARCODE_GENERATOR_3),
        serialno,
        barcodeFormat,
        null);
  }

  // accepts variable length of alphanumeric input but alphabet in upperCase
  // only
  public String checkTypeForCode39(String serialno, BarcodeFormat barcodeFormat)
      throws AxelorException {

    if (serialno.equals(serialno.toUpperCase())) {
      return serialno;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.BARCODE_GENERATOR_4),
        serialno,
        barcodeFormat,
        null);
  }

  // accepts only number with even length
  public String checkTypeForItf(String serialno, BarcodeFormat barcodeFormat, Boolean isPadding)
      throws AxelorException {

    if (isPadding) {
      if ((serialno.length() % 2) != 0) {
        serialno += "1";
      }
    }
    if (isNumber(serialno) && (serialno.length() % 2) == 0) {
      return serialno;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.BARCODE_GENERATOR_2),
        serialno,
        barcodeFormat,
        null);
  }

  // accepts alphanumeric input with min and max length limit
  public String checkTypeForPdf417(String serialno, BarcodeFormat barcodeFormat, Boolean isPadding)
      throws AxelorException {

    if (isPadding) {
      serialno = addCharPaddingBits(4, serialno, barcodeFormat);
    }
    // return serialno only if it is strictly alphanumeric
    if (serialno.length() == 4 || serialno.length() == 5) {
      if (isAlphanumeric(serialno)) {
        return serialno;
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.BARCODE_GENERATOR_6),
          serialno,
          barcodeFormat);
    }
    // return serialno if it is only number or only alphabets
    else if (serialno.length() == 7 || serialno.length() == 8 || serialno.length() == 9) {
      if (isNumber(serialno)) {
        return serialno;
      } else if (isCharacter(serialno)) {
        return serialno;
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.BARCODE_GENERATOR_8),
          serialno,
          barcodeFormat);
    } else if (serialno.length() > 3 && serialno.length() < 12) {
      return serialno;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.BARCODE_GENERATOR_5),
        serialno,
        barcodeFormat,
        3,
        12);
  }

  // accepts only number with fixed length of input
  public String checkTypeForUpca(String serialno, BarcodeFormat barcodeFormat, Boolean isPadding)
      throws AxelorException {

    if (isPadding) {
      serialno = addPaddingBits(11, serialno, barcodeFormat);
    }
    serialno = checkTypeForFixedLength(serialno, barcodeFormat, 11);
    return serialno;
  }

  // accepts only number with fixed length of input
  public String checkTypeForEan8(String serialno, BarcodeFormat barcodeFormat, Boolean isPadding)
      throws AxelorException {

    if (isPadding) {
      serialno = addPaddingBits(7, serialno, barcodeFormat);
    }
    serialno = checkTypeForFixedLength(serialno, barcodeFormat, 7);
    return serialno;
  }

  // accepts only number with fixed length of input
  public String checkTypeForEan13(String serialno, BarcodeFormat barcodeFormat, Boolean isPadding)
      throws AxelorException {

    if (isPadding) {
      serialno = addPaddingBits(12, serialno, barcodeFormat);
    }
    serialno = checkTypeForFixedLength(serialno, barcodeFormat, 12);
    return serialno;
  }

  // type check for the barcodes which accept only numbers as input with fixed
  // length like EAN_8, EAN_13, UPC_A
  public String checkTypeForFixedLength(
      String serialno, BarcodeFormat barcodeFormat, int barcodeLength) throws AxelorException {

    if (isNumber(serialno) && barcodeLength == serialno.length()) {
      return serialno;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.BARCODE_GENERATOR_1),
        serialno,
        barcodeFormat,
        barcodeLength);
  }

  public boolean isNumber(String s) {

    try {
      Long.parseLong(s);
    } catch (NumberFormatException e) {
      return false;
    } catch (NullPointerException e) {
      return false;
    }
    return true;
  }

  public boolean isAlphanumeric(String s) {

    String PATTERN = "([A-Za-z]+[0-9]|[0-9]+[A-Za-z])[A-Za-z0-9]*";
    Pattern pattern = Pattern.compile(PATTERN);
    if (pattern.matcher(s).matches()) {
      return true;
    }
    return false;
  }

  public boolean isCharacter(String s) {

    char[] charArr = s.toCharArray();
    for (char c : charArr) {
      if (!Character.isLetter(c)) {
        return false;
      }
    }
    return true;
  }

  // add padding bits for the barcodes which accepts input with fixed length
  // like EAN_8, EAN_13, UPC_A
  public String addPaddingBits(int barcodeLength, String serialno, BarcodeFormat barcodeFormat)
      throws AxelorException {

    int paddingbits;
    int serialnoLength = serialno.length();
    if (serialnoLength < barcodeLength) {
      paddingbits = barcodeLength - serialnoLength;
      for (int i = 0; i < paddingbits; i++) {
        serialno += "1";
      }
      return serialno;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.BARCODE_GENERATOR_7),
        serialno,
        barcodeFormat,
        barcodeLength);
  }

  // add char padding bits for the barcode like PDF_417
  public String addCharPaddingBits(int barcodeLength, String serialno, BarcodeFormat barcodeFormat)
      throws AxelorException {

    int paddingbits;
    int serialnoLength = serialno.length();
    if (serialnoLength < barcodeLength) {
      paddingbits = barcodeLength - serialnoLength;
      for (int i = 0; i < paddingbits; i++) {
        serialno += "a";
      }
      return serialno;
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.BARCODE_GENERATOR_7),
        serialno,
        barcodeFormat,
        barcodeLength);
  }
}
