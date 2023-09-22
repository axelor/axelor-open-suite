package com.axelor.apps.base.service.signature;

import com.axelor.apps.base.AxelorException;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public interface SignatureService {
  byte[] sign(InputStream inputStream, Certificate[] certificateChain, PrivateKey signingKey)
      throws AxelorException, IOException;
}
