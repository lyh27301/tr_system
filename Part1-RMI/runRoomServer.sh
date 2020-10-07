#!/bin/bash
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RoomServer.RoomRMIServer