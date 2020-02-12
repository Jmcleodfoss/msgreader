#!/bin/bash

# Go through each MSG file in the directory $input_dir, processing them by running various msg class main functions on them.
#
# Run as:
# 	extras/test-msg.sh
#
# results are placed in $results_dir

# Locations used
declare input_dir=test-msg-files
declare results_dir=test-output

# Output file for tracking / timing
declare stats=$results_dir/stats.txt

declare version=0.0-SNAPSHOT

# Jar files

if [ -n "$OS" ] && [ "$OS" = "Windows_NT" ]; then
	declare cp=msg\\target\\msg-$version.jar
else
	declare cp=msg/target/msg-$version.jar
fi


GetTestDirectory() {
	declare temp=$(basename "$1")
	echo "$results_dir/${temp%.pst}"
}

TestModule() {
	declare class=$1
	shift

	declare output=$(GetTestDirectory "$1")/${class#io/github/jmcleodfoss/*/*}.out
	echo "
"`date +%H:%M:%S`": starting $class test" >> $stats
	java $options -cp "$cp" $class "$@" > "$output"
	echo `date +%H:%M:%S`": done $class test" >> $stats
}

TestMSGFile() {
	declare msg="$1"
	declare output_dir=$(GetTestDirectory "$msg")
	if [ ! -d "$output_dir" ]; then
		mkdir "$output_dir"
	fi
	echo "Testing $msg; output directory $output_dir" >> $stats

	TestModule io.github.jmcleodfoss.msg.DIFAT "$msg"
	TestModule io.github.jmcleodfoss.msg.Directory "$msg"
	TestModule io.github.jmcleodfoss.msg.DirectoryEntry "$msg"
	TestModule io.github.jmcleodfoss.msg.FAT "$msg"
	TestModule io.github.jmcleodfoss.msg.Header "$msg"
	TestModule io.github.jmcleodfoss.msg.MiniFAT "$msg"
}

rm -rf $results_dir
if [ ! -d $results_dir ]; then
	mkdir $results_dir
fi

echo "Starting tests at " `date +%H:%M:%S` > $stats


# Tests done on each pst file
for msg in "$input_dir"/*.msg; do
	TestMSGFile "$msg"
done

echo "Ending tests at " `date +%H:%M:%S` >> $stats

declare result=`grep -e java\.lang\..*Exception -e [a-n]Exception $results_dir/*/*.out`
if [[ $? -gt 0 ]]; then
	echo "Errors found\n$result"
fi
