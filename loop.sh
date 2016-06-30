# !/bin/sh
ulimit -n 65535

while :;
do
	java -server -XX:+UseG1GC -XX:MaxGCPauseMillis=25 -XX:ParallelGCThreads=2 -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -XX:+AlwaysPreTouch -XX:+RelaxAccessControlCheck -XX:+UseBiasedLocking -Xmx512m -Xms256m -cp ./m0nster-filter.jar io.m0nster.filter.Starter > stdout.log 2>&1
	[ $? -ne 1 ] && break
done