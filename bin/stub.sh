#!/usr/bin/env bash
MYSELF=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"
java=java
if test -n "$JAVA_HOME"; then
    java="$JAVA_HOME/bin/java"
fi

if [[ "$1" == "start" ]] ; then
  out=`java $java_args -jar $MYSELF "$@"`
  retVal=$?
  if [[ $retVal -ne 0 ]]; then
    echo $out
    exit $retVal
  elif [[ "$out" != "" ]] ; then
   tmux attach -t $out
  fi
else
   java $java_args -jar $MYSELF "$@"
fi

exit 0
