#!/bin/bash

java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Client.RMIClient