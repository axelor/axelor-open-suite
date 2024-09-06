package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.meta.db.MetaFile;
import java.io.File;
import java.io.IOException;

public interface BankOrderEncryptionService {
  File encryptFile(File file) throws IOException;

  MetaFile getDecryptedFile(MetaFile bankOrderGeneratedFile, String password) throws IOException;

  MetaFile getDecryptedFile(MetaFile bankOrderGeneratedFile) throws IOException;

  boolean isFileEncrypted(MetaFile bankOrderGeneratedFile) throws IOException;
}
