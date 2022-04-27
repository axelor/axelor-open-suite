package com.axelor.apps.base.service.app;

import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnonymizeServiceImpl implements AnonymizeService {

  protected Logger LOG = LoggerFactory.getLogger(getClass());

  protected MetaSelectRepository metaSelectRepository;
  protected MetaSelectItemRepository metaSelectItemRepository;

  @Inject
  public AnonymizeServiceImpl(
      MetaSelectRepository metaSelectRepository,
      MetaSelectItemRepository metaSelectItemRepository) {
    this.metaSelectRepository = metaSelectRepository;
    this.metaSelectItemRepository = metaSelectItemRepository;
  }

  @Override
  public Object anonymizeValue(Object object, Property property) {

    if (property.getSelection() != null) {
      return getSelectionFirstValue(property.getSelection());
    }

    switch (property.getType()) {
      case TEXT:
      case STRING:
        byte[] shaInBytes = hashString(object.toString());

        if (property.getMaxSize() != null && shaInBytes.length > (int) property.getMaxSize()) {
          return bytesToHex(shaInBytes).substring(0, (int) property.getMaxSize());
        } else {
          return bytesToHex(shaInBytes);
        }

      case LONG:
      case DOUBLE:
        return 0;

      case INTEGER:
        return BigInteger.ZERO;

      case DECIMAL:
        return BigDecimal.ZERO;

      case DATE:
        return LocalDate.of(1970, 1, 1);

      case TIME:
        return LocalTime.of(0, 0, 0);

      case DATETIME:
        return LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.of(0, 0, 0));

      case BINARY:
      default:
        return null;
    }
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

  private String getSelectionFirstValue(String selection) {
    MetaSelect metaSelect = metaSelectRepository.findByName(selection);
    if (metaSelect == null) {
      return null;
    }

    MetaSelectItem metaSelectItem =
        metaSelectItemRepository.all().filter("self.select.id = ?1", metaSelect.getId()).fetchOne();

    if (metaSelectItem != null) {
      return metaSelectItem.getValue();
    } else {
      return null;
    }
  }
}
