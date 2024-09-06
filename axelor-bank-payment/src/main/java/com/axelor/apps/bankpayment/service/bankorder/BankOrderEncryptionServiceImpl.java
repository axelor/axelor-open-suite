package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.app.AppSettings;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.crypto.BytesEncryptor;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BankOrderEncryptionServiceImpl implements BankOrderEncryptionService {

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
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_DECRYPT_ERROR),
          e.getMessage());
    }
  }

  /**
   * Decrypt bank order file according to axelor-config.properties settings with password checking
   *
   * @param bankOrderGeneratedFile
   * @param password
   * @return
   * @throws IOException
   */
  @Override
  public MetaFile getDecryptedFile(MetaFile bankOrderGeneratedFile, String password)
      throws AxelorException {
    String encryptPassword = AppSettings.get().get("encryption.bankorder.password");
    if (!password.equals(encryptPassword)) {
      return null;
    } else {
      return getDecryptedFile(bankOrderGeneratedFile);
    }
  }

  /**
   * Encrypt bank order file according to axelor-config.properties settings
   *
   * @param bankOrderGeneratedFile
   * @return
   * @throws IOException
   */
  @Override
  public MetaFile getDecryptedFile(MetaFile bankOrderGeneratedFile) throws AxelorException {

    File bankOrderFile = MetaFiles.getPath(bankOrderGeneratedFile).toFile();
    String fileName = bankOrderFile.getName();
    String prefix =
        fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
    String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";

    BytesEncryptor encryptor = getEncryptor();

    try {

      byte[] decrypt = encryptor.decrypt(Files.readAllBytes(bankOrderFile.toPath()));

      Path tempFilePath = MetaFiles.createTempFile(prefix, suffix);
      Files.write(tempFilePath, decrypt, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

      return Beans.get(MetaFiles.class).upload(tempFilePath.toFile());
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_DECRYPT_ERROR),
          e.getMessage());
    }
  }

  protected BytesEncryptor getEncryptor() {
    AppSettings appSettings = AppSettings.get();
    String algorithm = appSettings.get("encryption.algorithm");
    String encryptPassword = appSettings.get("encryption.bankorder.password");

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
    try {
      return encryptor.isEncrypted(Files.readAllBytes(bankOrderFile.toPath()));
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_DECRYPT_ERROR),
          e.getMessage());
    }
  }
}
