package com.axelor.apps.base.service.app;

import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnonymizeServiceImpl implements AnonymizeService {

  protected Logger LOG = LoggerFactory.getLogger(getClass());

  @Override
  public Object anonymizeValue(Object object, Property property) {
    Object value = null;
    if (property.getType() == PropertyType.STRING) {
      byte[] shaInBytes = hashString(object.toString());

      if (property.getMaxSize() != null && shaInBytes.length > (int) property.getMaxSize()) {
        value = bytesToHex(shaInBytes).substring(0, (int) property.getMaxSize());
      } else {
        value = bytesToHex(shaInBytes);
      }
    }
    return value;
  }

  protected byte[] hashString(String data) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-256");
      md.update(getSalt());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    }
    byte[] result = md.digest(data.getBytes(StandardCharsets.UTF_8));
    return result;
  }

  protected byte[] getSalt() throws NoSuchAlgorithmException {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return salt;
  }

  protected String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
