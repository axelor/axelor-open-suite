package com.axelor.apps.base.interfaces;

public interface RecursiveModel<T extends RecursiveModel> {

  T getParent();
}
