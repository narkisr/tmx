# Intro

A simple tmux session launcher

## Usage

Download the latest binary release from https://github.com/narkisr/tmx/releases/latest and add it to your path.

Create ~/.tmx.edn:

```clojure
{
 :profiles {
   :re-core {
     :root "/home/ronen/code/re-ops/re-core"
     :windows [
       {:cmd "lein repl"}
       {:cmd "sudo docker-compose up elasticsearch" :dir "/home/ronen/code/re-ops/re-dock"}
     ]
   }
 }
 :terminal "/usr/bin/gnome-terminal"
}
```

Launch a profile:

```bash
$ tmx start re-core
```

# Copyright and license

Copyright [2019] [Ronen Narkis]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
