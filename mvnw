#!/bin/sh
set -e

if [ -n "$MAVEN_HOME" ] && [ -x "$MAVEN_HOME/bin/mvn" ]; then
  exec "$MAVEN_HOME/bin/mvn" "$@"
fi

if [ -n "$M2_HOME" ] && [ -x "$M2_HOME/bin/mvn" ]; then
  exec "$M2_HOME/bin/mvn" "$@"
fi

if [ -x "/c/Program Files/NetBeans-23/netbeans/java/maven/bin/mvn.cmd" ]; then
  exec "/c/Program Files/NetBeans-23/netbeans/java/maven/bin/mvn.cmd" "$@"
fi

if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi

echo "Maven was not found. Set MAVEN_HOME or install Maven/NetBeans bundled Maven." >&2
exit 1
