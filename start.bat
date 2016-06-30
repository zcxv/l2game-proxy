@echo off
java -Xmx512mb -Xms256mb -XX:+UseG1GC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -XX:+AlwaysPreTouch -XX:+RelaxAccessControlCheck -XX:+UseBiasedLocking -XX:MaxGCPauseMillis=25 -XX:ParallelGCThreads=2 -cp ./m0nster-filter.jar io.m0nster.filter.Starter
@pause