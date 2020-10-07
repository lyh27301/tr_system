#!/bin/bash

java Server/CarServer/CarTCPServer > /dev/null 2>&1
java Server/FlightServer/FlightTCPServer > /dev/null 2>&1
java Server/RoomServer/RoomTCPServer > /dev/null 2>&1
java Server/CustomerServer/CustomerTCPServer > /dev/null 2>&1




