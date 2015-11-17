#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR
ulimit -v unlimited
mongod --dbpath /mnt/tmp/data
lein with-profile prod trampoline ring server-headless &

CHILD_PID=$!

trap "kill -9 $CHILD_PID" EXIT
wait $CHILD_PID
