package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.io.File;

public interface BankOrderEncryptionService {
  File encryptFile(File file) throws AxelorException;

  void checkInputPassword(String password) throws AxelorException;

  MetaFile getDecryptedFile(MetaFile bankOrderGeneratedFile, String password)
      throws AxelorException;

  MetaFile getDecryptedFile(MetaFile bankOrderGeneratedFile) throws AxelorException;

  byte[] getDecryptedBytes(File bankOrderFile) throws AxelorException;

  boolean isFileEncrypted(MetaFile bankOrderGeneratedFile) throws AxelorException;

  String checkAndGetEncryptionPassword() throws AxelorException;
}
