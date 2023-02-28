package com.axelor.apps.tool.collection;

import java.util.Collections;
import java.util.Set;

public class SetUtils {

  private SetUtils() {}

  public static <T> Set<T> emptyIfNull(final Set<T> set) {
    return set == null ? Collections.<T>emptySet() : set;
  }

  public static <T> int size(final Set<T> set) {
    return set == null ? 0 : set.size();
  }
}
