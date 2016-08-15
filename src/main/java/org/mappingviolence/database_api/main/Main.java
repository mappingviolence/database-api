package org.mappingviolence.database_api.main;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mappingviolence.database_api.server.Server;

import com.google.common.primitives.Ints;

public class Main {
  private final String[] args;

  private static final int DEFAULT_PORT = 4567;
  private int port;

  private Main(String[] args) {
    this.args = args;
  }

  public static void main(String[] args) {
    if (args.length > 0 && "--debug".equals(args[0])) {
      new Main(args).run(true);
    } else {
      new Main(args).run(false);
    }
  }

  private void run(boolean debug) {
    if (debug) {
      Logger.getGlobal().log(Level.WARNING, "DEBUG");
    } else {
      if (args.length == 2 && "-p".equals(args[0]) && Ints.tryParse(args[1]) != null) {
        port = Integer.valueOf(args[1]);
      } else {
        port = DEFAULT_PORT;
      }

      Server server = new Server(port);
      server.start();
    }
  }
}
