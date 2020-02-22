LEIN_SNAPSHOTS_IN_RELEASE=1 lein uberjar
cat bin/stub.sh target/tmx-0.2.1-standalone.jar > target/tmx && chmod +x target/tmx
