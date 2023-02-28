package com.axelor.apps.tool.collection;

import java.util.Collections;
import java.util.List;

public class ListUtils {

  private ListUtils() {}

  public static <T> List<T> emptyIfNull(final List<T> list) {
    return list == null ? Collections.<T>emptyList() : list;
  }

  public static <T> int size(final List<T> list) {
    return list == null ? 0 : list.size();
  }
}
