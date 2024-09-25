package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.io.File;

public interface BankOrderEncryptionService {
  File encryptFile(File file) throws AxelorException;

  MetaFile getDecryptedFile(MetaFile bankOrderGeneratedFile, String password)
      throws AxelorException;

  MetaFile getDecryptedFile(MetaFile bankOrderGeneratedFile) throws AxelorException;

  boolean isFileEncrypted(MetaFile bankOrderGeneratedFile) throws AxelorException;

  String checkAndGetEncryptionPassword() throws AxelorException;
}
