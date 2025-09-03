/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.app.AppSettings;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.crypto.BytesEncryptor;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class BankOrderEncryptionServiceImpl implements BankOrderEncryptionService {

  protected MetaFiles metaFiles;
  protected AppBankPaymentService appBankPaymentService;

  @Inject
  public BankOrderEncryptionServiceImpl(
      MetaFiles metaFiles, AppBankPaymentService appBankPaymentService) {
    this.metaFiles = metaFiles;
    this.appBankPaymentService = appBankPaymentService;
  }

  /**
   * Encrypt bank order file according to axelor-config.properties settings
   *
   * @param file
   * @return
   * @throws IOException
   */
  @Override
  public File encryptFile(File file) throws AxelorException {
    BytesEncryptor encryptor = getEncryptor();
    try {
      byte[] encrypt = encryptor.encrypt(Files.readAllBytes(file.toPath()));

      Files.write(file.toPath(), encrypt, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

      return file;

    } catch (IOException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_DECRYPT_ERROR));
    }
  }

  @Override
  public void checkInputPassword(String password) throws AxelorException {
    String encryptPassword = checkAndGetEncryptionPassword();
    if (!password.equals(encryptPassword)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_ENCRYPTION_INCORRECT_PASSWORD));
    }
  }

  @Override
  public byte[] getDecryptedBytes(File bankOrderFile) throws AxelorException {
    BytesEncryptor encryptor = getEncryptor();
    try {
      return encryptor.decrypt(Files.readAllBytes(bankOrderFile.toPath()));
    } catch (IOException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_DECRYPT_ERROR));
    }
  }

  protected BytesEncryptor getEncryptor() throws AxelorException {
    AppSettings appSettings = AppSettings.get();
    String algorithm = appSettings.get("encryption.algorithm");
    String encryptPassword = checkAndGetEncryptionPassword();

    if (encryptPassword == null || encryptPassword.isEmpty()) {
      return null;
    }
    BytesEncryptor encryptor;

    if ("GCM".equalsIgnoreCase(algorithm)) {
      encryptor = BytesEncryptor.gcm(encryptPassword);
    } else {
      encryptor = BytesEncryptor.cbc(encryptPassword);
    }

    return encryptor;
  }

  /**
   * Check if the file is encrypted
   *
   * @param bankOrderGeneratedFile
   * @return
   * @throws IOException
   */
  @Override
  public boolean isFileEncrypted(MetaFile bankOrderGeneratedFile) throws AxelorException {
    if (bankOrderGeneratedFile == null) {
      return false;
    }
    File bankOrderFile = MetaFiles.getPath(bankOrderGeneratedFile).toFile();
    BytesEncryptor encryptor = getEncryptor();

    if (encryptor == null) {
      return false;
    }

    try {
      return encryptor.isEncrypted(Files.readAllBytes(bankOrderFile.toPath()));
    } catch (IOException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_DECRYPT_ERROR));
    }
  }

  @Override
  public String checkAndGetEncryptionPassword() throws AxelorException {
    String encryptPassword = AppSettings.get().get("encryption.bankorder.password");

    // throw exception if enableBankOrderFileEncryption is true and password is null or empty
    if (appBankPaymentService.getAppBankPayment().getEnableBankOrderFileEncryption()
        && (encryptPassword == null || encryptPassword.isEmpty())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_ENCRYPTION_NO_PASSWORD));
    }

    // if enableBankOrderFileEncryption is false, return either password if exists or null/empty
    // value
    return encryptPassword;
  }

  @Override
  public File encryptUploadedBankOrderFile(MetaFile bankOrderMetaFile) throws AxelorException {
    File bankOrderFile = MetaFiles.getPath(bankOrderMetaFile).toFile();
    return encryptFile(bankOrderFile);
  }
}
