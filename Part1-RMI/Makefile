all: java.policy compile-client compile-car-server compile-flight-server compile-room-server compile-customer-server compile-middleware-server

CarServer: java.policy compile-car-server
RoomServer: java.policy compile-room-server
FlightServer: java.policy compile-flight-server
CustomerServer: java.policy compile-customer-server

MiddlewareServer: java.policy compile-middleware-server
Client: java.policy compile-client


java.policy: FORCE
	@echo "Creating server java policy"
	@echo "grant codebase \"file:`pwd`/\" {" > java.policy
	@echo "permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy
FORCE: ;

compile-client: Client Server/Common/*.java Server/Interface/*.java
	javac Client/*.java Server/Common/*.java Server/Interface/*.java

compile-car-server: Server/CarServer/*.java Server/Common/*.java Server/Interface/*.java
	javac Server/CarServer/*.java Server/Common/*.java Server/Interface/*.java

compile-flight-server: Server/FlightServer/*.java Server/Common/*.java Server/Interface/*.java
	javac Server/FlightServer/*.java Server/Common/*.java Server/Interface/*.java

compile-room-server: Server/RoomServer/*.java Server/Common/*.java Server/Interface/*.java
	javac Server/RoomServer/*.java Server/Common/*.java Server/Interface/*.java

compile-customer-server: Server/CustomerServer/*.java Server/Common/*.java Server/Interface/*.java
	javac Server/CustomerServer/*.java Server/Common/*.java Server/Interface/*.java

compile-middleware-server: Server/MiddlewareServer/*.java Server/Common/*.java Server/Interface/*.java
	javac Server/MiddlewareServer/*.java Server/Common/*.java Server/Interface/*.java

clean:
	rm -f Server/Interface/*.class Server/Common/*.class
	rm -f Server/CarServer/*.class Server/FlightServer/*.class Server/RoomServer/*.class Server/CustomerServer/*.class
	rm -f java.policy
	rm -f Client/*.class
	rm -f Server/MiddlewareServer/*.class
