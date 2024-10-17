package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Tag;

public interface TagService {
  void addMetaModelToTag(Tag tag, String fullName);
}
