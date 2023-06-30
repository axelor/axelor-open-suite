package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface DomainNameToolService<T> {

  public List<T> getEntitiesVithSameEmailAddress(
      String emailAddressName, List<Long> idListToExlude, String supplementaryFilter)
      throws AxelorException, ClassNotFoundException;

  public void set(T t);

  public T get();
}
