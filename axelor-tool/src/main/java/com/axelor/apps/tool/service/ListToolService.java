package com.axelor.apps.tool.service;

import java.util.List;

public interface ListToolService {

  public <T> List<T> intersection(List<T> list1, List<T> list2);
}
