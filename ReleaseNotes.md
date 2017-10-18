# v0.2 (10/20/2017)
# Release Notes

## Notable Changes
The Barcelona Release (v 0.2) of the Virtual Device micro service includes the following:
* POM changes for appropriate repository information for distribution/repos management, checkstyle plugins, etc.
* Added Dockerfile for creation of micro service targeted for ARM64
* Consolidated Docker properties files to common directory

## Bug Fixes
* Fixed Device equality logic
* Added check for service existence after initialization to Base Service
* Fixed scheduling configuration when running from source
* Fixed Maven Spring plugin environment
* Fixed Maven plugin entry
* Removed OS specific file path for logging file 

 - [#22](https://github.com/edgexfoundry/device-virtual/pull/22) - Remove staging plugin contributed by Jeremy Phelps ([JPWKU](https://github.com/JPWKU))
 - [#21](https://github.com/edgexfoundry/device-virtual/pull/21) - Adds null check in BaseService contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#20](https://github.com/edgexfoundry/device-virtual/pull/20) - Fixes Maven artifact dependency path contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#19](https://github.com/edgexfoundry/device-virtual/pull/19) - added staging and snapshots repos to pom along with nexus staging mav… contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#18](https://github.com/edgexfoundry/device-virtual/pull/18) - Added support for aarch64 arch contributed by ([feclare](https://github.com/feclare))
 - [#17](https://github.com/edgexfoundry/device-virtual/pull/17) - removed device manager from app properties urls contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#16](https://github.com/edgexfoundry/device-virtual/pull/16) - Fix spring-boot-maven-plugin issue in pom.xml contributed by ([MartinXuEdx](https://github.com/MartinXuEdx))
 - [#15](https://github.com/edgexfoundry/device-virtual/pull/15) - Fixes device comparison logic contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#14](https://github.com/edgexfoundry/device-virtual/pull/14) - Adds Docker build capability contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#13](https://github.com/edgexfoundry/device-virtual/pull/13) - Adds LF Nexus Repo for artifacts contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#11](https://github.com/edgexfoundry/device-virtual/pull/11) - Cleans up Attributes Class contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#10](https://github.com/edgexfoundry/device-virtual/pull/10) - Fixes scheduling when running from source contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#9](https://github.com/edgexfoundry/device-virtual/pull/9) - Fixes Log File Path contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#8](https://github.com/edgexfoundry/device-virtual/issues/8) - Log File Path not Platform agnostic
 - [#7](https://github.com/edgexfoundry/device-virtual/issues/7) - TEST ISSUE - PHILIP
 - [#6](https://github.com/edgexfoundry/device-virtual/pull/6) - Add distributionManagement for artifact storage contributed by Andrew Grimberg ([tykeal](https://github.com/tykeal))
 - [#5](https://github.com/edgexfoundry/device-virtual/pull/5) - Fixes plugin lifecycle maven errors contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#4](https://github.com/edgexfoundry/device-virtual/pull/4) - Fixes default schedule provisioning for docker contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#3](https://github.com/edgexfoundry/device-virtual/pull/3) - Fixes virtual device service default schedule initialization contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#2](https://github.com/edgexfoundry/device-virtual/pull/2) - Fix virtual device service Docker host name contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#1](https://github.com/edgexfoundry/device-virtual/pull/1) - Contributed Project Fuse source code contributed by Tyler Cox ([trcox](https://github.com/trcox))
