package com.axelor.apps.tool.collection;

import java.util.Collection;
import java.util.Collections;

public class CollectionUtils {

  private CollectionUtils() {}

  public static <T> Collection<T> emptyIfNull(final Collection<T> collection) {
    return collection == null ? Collections.<T>emptyList() : collection;
  }

  public static <T> int size(final Collection<T> collection) {
    return collection == null ? 0 : collection.size();
  }

  public static <T> boolean isEmpty(Collection<T> collection) {
    return (collection == null || collection.isEmpty());
  }

  public static <T> boolean isNotEmpty(Collection<T> collection) {
    return !isEmpty(collection);
  }
}
