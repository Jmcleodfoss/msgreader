#!/bin/bash
# Retrieve pstreader GUID.java and adjust the package name
curl https://raw.githubusercontent.com/Jmcleodfoss/pstreader/master/pst/src/main/java/io/github/jmcleodfoss/GUID.java | sed '
	1{
		i // DO NOT EDIT THIS FILE
		i // Automatically generated by msgreader/extras/getGUID_java.sh
		i // Any changes must be made to that file.
		i package io.github.jmcleodfoss.msg;
		d
		n
	}
	' > GUID.java