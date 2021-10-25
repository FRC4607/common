# 4607 Common Library

This repository contains code for FRC 4607 robots that is kept the same yearly.

# Running Tests
Before you run tests, you will need to set the environment variable `CSVDIR` so that the logger tests have a place to put their log files. This can be done in powershell with
```
$env:CSVDIR = "C:/path/to/any/directory"
[Environment]::SetEnvironmentVariable("CSVDIR", $env:CSVDIR, [System.EnvironmentVariableTarget]::User)
```
Any windows open will need to be restarted for the change to take effect. Note that `CSVDIR` must not end with a trailing slash.