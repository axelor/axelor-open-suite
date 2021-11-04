package com.axelor.apps.tool.service;

import java.util.ArrayList;
import java.util.List;

public class ListToolServiceImpl implements ListToolService {

  @Override
  public <T> List<T> intersection(List<T> list1, List<T> list2) {
    List<T> list = new ArrayList<T>();

    for (T t : list1) {
      if (list2.contains(t)) {
        list.add(t);
      }
    }

    return list;
  }
}
