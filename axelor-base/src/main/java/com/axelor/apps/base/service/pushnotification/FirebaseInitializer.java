package com.axelor.apps.base.service.pushnotification;

import com.axelor.app.AppSettings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import groovy.lang.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FirebaseInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(FirebaseInitializer.class);

  private boolean initialized = false;

  public FirebaseInitializer() {
    init();
  }

  private void init() {
    try {
      AppSettings settings = AppSettings.get();

      boolean enabled = Boolean.parseBoolean(settings.get("firebase.enabled", "false"));

      if (!enabled) {
        LOG.info("Firebase push notifications are disabled by configuration");
        return;
      }

      if (!FirebaseApp.getApps().isEmpty()) {
        initialized = true;
        LOG.info("Firebase already initialized");
        return;
      }

      String credentialsPath = settings.get("firebase.credentials.path");

      if (credentialsPath == null || credentialsPath.isBlank()) {
        LOG.error(
            "Firebase push notifications disabled: firebase.credentials.path is not configured");
        initialized = false;
        return;
      }

      FileInputStream serviceAccount = new FileInputStream(credentialsPath);

      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();

      FirebaseApp.initializeApp(options);
      initialized = true;

      LOG.info("Firebase successfully initialized");

    } catch (IOException e) {
      LOG.error("Failed to initialize Firebase, push disabled", e);
      initialized = false;
    }
  }

  public boolean isInitialized() {
    return initialized;
  }
}
