#!env python
"""
You'll need these versions to compile the server jars:
```bash
apt install openjdk-8-jdk openjdk-16-jdk openjdk-17-jdk
```

# Java 8: 1.9 -> 1.16
# Java 16: 1.17
# Java 17: 1.18+
"""
import os
import re
from datetime import datetime
from functools import lru_cache
from pathlib import Path

import requests


java_versions_needed = {
  '1_9_': 8,
  '1_10_': 8,
  '1_11_': 8,
  '1_12_': 8,
  '1_13_': 8,
  '1_14_': 8,
  '1_15_': 8,
  '1_16_': 8,
  '1_17_': 16,
  '1_18_': 17,
  '1_19_': 17,
  '1_20_': 17,
}
__root__ = Path(__file__).parent.parent.parent.resolve().absolute()


@lru_cache(1)
def available_versions() -> list[str]:
  content = requests.get("https://hub.spigotmc.org/versions/").text
  return re.findall(r'href="(\d+\.\d+(?:\.\d+)?)\.json"', content)


def find_version_to_build(directory_name: str) -> str:
  new_name = re.sub(r'v1_(\d+)_R(\d+)', r'1.\1.\2', directory_name)
  if new_name in available_versions():
    return new_name

  if new_name.endswith(".1"):
    new_name = new_name[:-2]
    if new_name in available_versions():
      return new_name

  raise Exception(f"{new_name} isn't known to spigot?")


for directory in __root__.glob('v1_*'):
  rev = find_version_to_build(directory.name)
  minor = directory.name.split('_')[1]
  java_version = java_versions_needed[minor]
  java_path = os.getenv(f'JAVA_HOME_{java_version}_X64') + '/bin/java'

# :for version in get_versions():
#   print(datetime.now(), "Trying to build:", version)
#
#     declare -x JAVA_HOME_11_X64="/usr/lib/jvm/temurin-11-jdk-amd64"
# declare -x JAVA_HOME_16_X64="/opt/hostedtoolcache/Java_Adopt_jdk/16.0.2-7/x64"
# declare -x JAVA_HOME_17_X64="/opt/hostedtoolcache/Java_Adopt_jdk/17.0.10-7/x64"
# declare -x JAVA_HOME_21_X64="/usr/lib/jvm/temurin-21-jdk-amd64"
# declare -x JAVA_HOME_8_X64="/opt/hostedtoolcache/Java_Adopt_jdk/8.0.402-6/x64"
#
#
#
# for version in $available_versions;
# do
#   echo "[$(date)] Trying to build: $version"
#
#   major=$(echo "$version" | cut -d '.' -f 2)
#   if [[ $major -lt 17 ]]; then
#     /usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar BuildTools.jar --remapped --rev "$version" > "log.$version.txt" 2>&1
#   else
#     /usr/lib/jvm/java-17-openjdk-amd64/bin/java -jar BuildTools.jar --remapped --rev "$version" > "log.$version.txt" 2>&1
#   fi;
#   echo "[$(date)] Result: $?"
# done
