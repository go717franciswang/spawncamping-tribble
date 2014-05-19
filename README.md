# http-delayed-job

## Setup
Start Mongodb in RHEL
```
mongod --dbpath /custom/path/to/data/dir
```

Configure FTP server
* Edit resource-test|prod/config.clj
* Mount ftp-dir to ftp-dir-path
```
mount --bind ftp-dir /var/ftp/gri/path
```

Start Delayed-job server
```sh
lein with-profile prod trampoline ring server-headless
```

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
