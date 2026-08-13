package cafe.woden.ircclient.irc.matrix;

import cafe.woden.ircclient.irc.ircv3.Ircv3RuntimeCatalogs;

/** Explicit application-provider fixture for Matrix adapter tests. */
final class MatrixIrcv3TestSupport {

  private MatrixIrcv3TestSupport() {}

  static MatrixIrcv3RuntimeSupport applicationClasspathRuntimeSupport() {
    return new MatrixIrcv3RuntimeSupport(Ircv3RuntimeCatalogs.applicationClasspath());
  }
}
