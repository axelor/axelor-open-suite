package com.axelor.studio.service.module;

import com.axelor.common.FileUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.studio.exception.IExceptionMessage;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleInstallService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String[] ENV_VARS = new String[] {"CATALINA_HOME", "JAVA_HOME"
        //			, "PGHOME"
      };

  private Map<String, String> ENV = new HashMap<String, String>();

  @Inject private ModuleImportService moduleImportService;

  @Inject
  public ModuleInstallService() {

    //    	AppSettings settings = AppSettings.get();
    //		String url = settings.get("db.default.url", "").replaceAll(".*?//", "");
    ENV.putAll(System.getenv());
    //        ENV.put("PGPORT", url.substring(url.indexOf(":") + 1, url.lastIndexOf("/")));
    //        ENV.put("PGDATABASE", url.substring(url.lastIndexOf("/") + 1));
    //        ENV.put("PGUSER", settings.get("db.default.user"));
    //        ENV.put("PGPASSWORD", settings.get("db.default.password"));

    String resource = this.getClass().getClassLoader().getResource("").getFile();
    File file = new File(resource);
    ENV.put("CATALINA_APP", file.getParentFile().getParentFile().getAbsolutePath());
    ENV.put(
        "GRADLE_OPTS",
        "-Dfile.encoding=utf-8 -Djava.io.tmpdir=" + System.getProperty("java.io.tmpdir"));
  }

  public String buildApp() throws AxelorException {

    setServerEnv();

    String logText = null;

    File sourceDir = moduleImportService.getSourceDir();
    String script = "gradlew";
    if (SystemUtils.IS_OS_WINDOWS) {
      script = "gradlew.bat";
    }

    String scriptPath = new File(sourceDir, script).getAbsolutePath();
    log.debug("Script path: {}", scriptPath);

    String command = scriptPath + " -x test clean build";
    StringBuffer result = new StringBuffer();

    int exitStatus = execute(sourceDir, command, result);
    log.debug("Exit status: {}", exitStatus);
    if (exitStatus != 0) {
      logText = result.toString();
    }

    return logText;
  }

  public void restartServer(boolean reset, File logFile) throws AxelorException {

    setServerEnv();

    String warPath = getWarPath();

    try {
      String scriptPath = getRestartScriptPath();
      ProcessBuilder processBuilder;
      if (reset) {
        processBuilder = new ProcessBuilder(scriptPath, warPath, "reset");
      } else {
        processBuilder = new ProcessBuilder(scriptPath, warPath);
      }
      processBuilder.environment().putAll(ENV);
      processBuilder.redirectErrorStream(true);
      processBuilder.redirectOutput(logFile);
      //			processBuilder.redirectError(logFile);
      processBuilder.start();
    } catch (IOException e) {
      throw new AxelorException(e, TraceBackRepository.TYPE_TECHNICAL);
    }
  }

  private String getWarPath() throws AxelorException {

    File warDir = FileUtils.getFile(moduleImportService.getSourceDir(), "build", "libs");
    log.debug("War directory path: {}", warDir.getAbsolutePath());
    if (!warDir.exists()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.NO_BUILD_DIR));
    }

    for (File file : warDir.listFiles()) {
      if (file.getName().endsWith(".war")) {
        return file.getAbsolutePath();
      }
    }

    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.NO_BUILD_DIR));
  }

  private String getRestartScriptPath() throws IOException, FileNotFoundException {

    String ext = "sh";
    if (SystemUtils.IS_OS_WINDOWS) {
      ext = "bat";
    }
    InputStream stream = this.getClass().getResourceAsStream("/script/RestartServer." + ext);
    File script = File.createTempFile("RestartServer", "." + ext);
    script.setExecutable(true);
    FileOutputStream out = new FileOutputStream(script);
    IOUtils.copy(stream, out);
    out.close();

    return script.getAbsolutePath();
  }

  private void setServerEnv() throws AxelorException {

    String path = ENV.get("PATH");
    if (path == null) {
      path = "";
    }

    if (SystemUtils.IS_OS_WINDOWS) {
      String winPath = ENV.get("Path");
      if (!path.contains(winPath)) {
        path += File.pathSeparator + winPath;
      }
    }

    for (String var : ENV_VARS) {
      path = setPath(path, var);
    }

    ENV.put("PATH", path);
  }

  private String setPath(String path, String var) throws AxelorException {

    String envVar = ENV.get(var);
    if (envVar == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.NO_ENVIROMENT_VARIABLE),
          var);
    }

    String binPath;
    if (envVar.endsWith(File.separator)) {
      binPath = envVar + "bin";
    } else {
      binPath = envVar + File.separator + "bin";
    }

    if (!path.contains(binPath)) {
      path += File.pathSeparator + binPath;
    }

    return path;
  }

  public int execute(File workingDir, String command, StringBuffer result) {

    final CommandLine commandLine = CommandLine.parse(command);

    final Executor executor = new DefaultExecutor();
    executor.setWorkingDirectory(workingDir);

    OutStream out = new OutStream(result);

    PumpStreamHandler streams = new PumpStreamHandler(out);
    executor.setStreamHandler(streams);

    int status = 0;
    try {
      status = executor.execute(commandLine, ENV);
    } catch (IOException e) {
      status = 1;
      e.printStackTrace();
    }

    return status;
  }

  class OutStream extends OutputStream {

    public StringBuffer buffer = null;

    public OutStream(StringBuffer buffer) {
      this.buffer = buffer;
    }

    @Override
    public void write(int b) throws IOException {

      buffer.append((char) b);
    }
  }
}
