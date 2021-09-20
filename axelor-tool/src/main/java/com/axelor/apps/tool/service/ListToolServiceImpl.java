package com.axelor.apps.tool.service;

import com.axelor.apps.tool.module.ToolModule;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(ToolModule.PRIORITY)
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
