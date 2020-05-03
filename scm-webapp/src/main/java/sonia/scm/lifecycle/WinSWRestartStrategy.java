package sonia.scm.lifecycle;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * A {@link RestartStrategy} which can be used if scm-manager was started as windows
 * service with WinSW.
 *
 * @see <a href="https://github.com/winsw/winsw/blob/master/doc/selfRestartingService.md">Self-restarting Windows services</a>
 */
class WinSWRestartStrategy extends RestartStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(WinSWRestartStrategy.class);

  static final String ENV_EXECUTABLE = "WINSW_EXECUTABLE";

  @Override
  @SuppressWarnings("java:S2142")
  protected void executeRestart(InjectionContext context) {
    String executablePath = System.getenv(ENV_EXECUTABLE);
    try {
      int rs = execute(executablePath);
      if (rs != 0) {
        LOG.error("winsw {} returned status code {}", executablePath, rs);
      }
    } catch (IOException | InterruptedException e) {
      LOG.error("failed to execute winsw at {}", executablePath, e);
    }
    LOG.error("scm-manager is in an unrecoverable state, we will now exit the java process");
    System.exit(1);
  }

  private int execute(String executablePath) throws InterruptedException, IOException {
    return new ProcessBuilder(executablePath, "restart!").start().waitFor();
  }

  static boolean isSupported(Map<String, String> environment) {
    String executablePath = environment.get(ENV_EXECUTABLE);
    if (Strings.isNullOrEmpty(executablePath)) {
      return false;
    }
    File exe = new File(executablePath);
    return exe.exists();
  }
}
