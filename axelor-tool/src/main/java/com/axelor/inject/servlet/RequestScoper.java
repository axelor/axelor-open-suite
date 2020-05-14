package com.axelor.inject.servlet;

import java.io.Closeable;

public interface RequestScoper {
  CloseableScope open();

  public interface CloseableScope extends Closeable {
    @Override
    void close();
  }
}
