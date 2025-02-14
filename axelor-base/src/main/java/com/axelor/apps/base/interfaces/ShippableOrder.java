package com.axelor.apps.base.interfaces;

import com.axelor.apps.base.db.TemporaryLineHolder;

public interface ShippableOrder {
  TemporaryLineHolder getTemporaryLineHolder();
}
