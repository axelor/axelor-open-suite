package com.axelor.inject.servlet;

import com.axelor.inject.Beans;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jboss.weld.context.bound.BoundRequestContext;

public class ServletScopes {

  public static RequestScoper scopeRequest(Map<String, Object> storage) {
    return new RequestScoperImpl(storage);
  }

  private static class RequestScoperImpl implements RequestScoper {
    private final BoundRequestContext requestContext;
    private final Map<String, Object> requestDataStore;

    public RequestScoperImpl(Map<String, Object> requestDataStore) {
      final Map<String, Object> emptyMap = Collections.emptyMap();
      this.requestContext = Beans.get(BoundRequestContext.class);
      this.requestDataStore = requestDataStore != emptyMap ? requestDataStore : new HashMap<>();
    }

    @Override
    public CloseableScope open() {
      requestContext.associate(requestDataStore);
      requestContext.activate();

      return new CloseableScope() {
        @Override
        public void close() {
          try {
            requestContext.invalidate();
            requestContext.deactivate();
          } finally {
            requestContext.dissociate(requestDataStore);
          }
        }
      };
    }
  }
}
