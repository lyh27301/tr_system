all: java.policy compile-car-server compile-middleware-server

CarServer: java.policy compile-car-server

MiddlewareServer: java.policy compile-middleware-servermake

java.policy: FORCE
	@echo "Creating server java policy"
	@echo "grant codebase \"file:`pwd`/\" {" > java.policy
	@echo "permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy
FORCE: ;

CarInterface.jar: Server/CarServer/ICarManager.java
	@echo "Compiling car server interface"
	javac Server/CarServer/ICarManager.java
	jar cvf CarInterface.jar Server/CarServer/ICarManager.class

compile-car-server: Server/CarServer/*.java Server/Common/*.java
	javac Server/CarServer/*.java Server/Common/*.java

compile-middleware-server: Server/MiddlewareServer/*.java Server/Common/*.java
	javac Server/MiddlewareServer/*.java Server/Common/*.java

RMIInterface.jar: Server/Interface/IResourceManager.java
	@echo "Compiling RMI server interface"
	javac Server/Interface/IResourceManager.java
	jar cvf RMIInterface.jar Server/Interface/IResourceManager.class

compile-server-rmi: Server/RMI/*.java Server/Interface/IResourceManager.java Server/Common/*.java
	javac Server/RMI/*.java Server/Interface/IResourceManager.java Server/Common/*.java

clean:
	rm -f Server/Interface/*.class Server/Common/*.class Server/RMI/*.class Server/CarServer/*.class Server/MiddlewareServer/*.class
	rm -f CarInterface.jar
	rm -f RMIInterface.jar
	rm -f java.policy 
