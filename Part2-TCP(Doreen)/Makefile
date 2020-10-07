all: CarServer FlightServer RoomServer CustomerServer MiddlewareServer TCPClient

CarServer: Server/Common/*.java Server/CarServer/*.java
	javac Server/Common/*.java Server/CarServer/*.java

FlightServer: Server/Common/*.java Server/FlightServer/*.java
	javac Server/Common/*.java Server/FlightServer/*.java

RoomServer: Server/Common/*.java Server/RoomServer/*.java
	javac Server/Common/*.java Server/RoomServer/*.java

CustomerServer: Server/Common/*.java Server/CustomerServer/*.java
	javac Server/Common/*.java Server/CustomerServer/*.java

MiddlewareServer: Server/Common/*.java Server/MiddlewareServer/*.java
	javac Server/Common/*.java Server/MiddlewareServer/*.java

TCPClient: Server/Common/*.java Client/TCP_Client.java
	javac Server/Common/*.java Client/TCP_Client.java


clean:
	rm -f Server/Common/*.class
	rm -f Server/CarServer/*.class
	rm -f Server/FlightServer/*.class
	rm -f Server/RoomServer/*.class
	rm -f Server/MiddlewareServer/*.class
	rm -f Client/TCP_Client.class
	rm -f Server/CustomerServer/*.class