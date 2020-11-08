package Client;

import Server.Common.Message;
import Server.Common.Trace;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ClientTest implements Runnable{


    static int middlewarePort = 6116;
    static String middlewareHost = "localhost";
    public static int numberOfClients = 1;
    public long startTime = 0;
    public double throughput = 1.0;
    public long[] times = new long[50];
    public static void main (String[] args) throws Exception{

        if (args.length > 0) middlewareHost = args[0];

        long startTime = 5*1000 + System.currentTimeMillis();
        try {
            ClientTest[] c = new ClientTest[numberOfClients];
            Thread[] thread = new Thread[numberOfClients];
            for (int i = 0; i < numberOfClients; i++) {
                c[i] = new ClientTest();
                c[i].startTime = startTime;
                if (i == 0)
                    setupDB();
                thread[i] = new Thread(c[i]);
                thread[i].start();
            }

            for (int i = 0; i < numberOfClients; i++) {
                thread[i].join();
            }
            System.out.println("Response Time\n\n");
            for (int i = 0; i < numberOfClients; i++) {
                for (int j = 0; j < c[i].times.length; j++) {
                        System.out.print(c[i].times[j]);
                        if (j!= c[i].times.length - 1)
                            System.out.print(",");
                }
                    System.out.println();
            }
            System.out.println();

        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }


    @Override
    public void run() {
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName(middlewareHost);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Socket s = null;
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;

        try {
            s = new Socket(ip, middlewarePort);
            inputStream = new ObjectInputStream(s.getInputStream());
            outputStream = new ObjectOutputStream(s.getOutputStream());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int waitTime = (int)((1000 * numberOfClients) / throughput);
        long variation = 30;

        while (System.currentTimeMillis() < startTime){}

        for (int i = (int)Thread.currentThread().getId()*200; i < (int)Thread.currentThread().getId()*200 + 150; i++) {
            double l = Math.random();
            int v;
            if (l < 0.5)
                v = waitTime - ((int)(variation*Math.random()));
            else
                v = waitTime + ((int)(variation*Math.random()));
            try {
                long rt = oneRMTransaction(inputStream,outputStream);
                if (i >= (int)Thread.currentThread().getId()*200 + 100)
                    times[i - ((int)Thread.currentThread().getId()*200 + 100)] = rt;
                if ((int)(v - rt) < 0)
                    continue;
                Thread.sleep((int) (v - rt));
            } catch(Exception e){}
        }
        try {
            inputStream.close();
            outputStream.close();
            Trace.info("Server connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setupDB() throws Exception {
        InetAddress ip = InetAddress.getByName(middlewareHost);
        Socket s = new Socket(ip, middlewarePort);

        ObjectInputStream inputStream = new ObjectInputStream(s.getInputStream());
        ObjectOutputStream outputStream = new ObjectOutputStream(s.getOutputStream());
        outputStream.flush();
        System.out.println("Start setup");
        try {
            outputStream.writeObject(new Message("Start"));
            String response = ((Message)inputStream.readObject()).getMessageText();
            System.out.println(response);
            int xid = Integer.valueOf(response);
            for (int i = 1; i <= 100; i++) {
                outputStream.writeObject(new Message("AddFlight,"+ xid +","+i+",1000,"+String.valueOf(500+i)));
                response = ((Message)inputStream.readObject()).getMessageText();
                System.out.println(response);
                outputStream.writeObject(new Message("AddCars,"+ xid +","+"Montreal"+i+",1000,"+String.valueOf(200+i)));
                response = ((Message)inputStream.readObject()).getMessageText();
                System.out.println(response);
                outputStream.writeObject(new Message("AddRooms,"+ xid +","+"Montreal"+i+",1000,"+String.valueOf(300+i)));
                response = ((Message)inputStream.readObject()).getMessageText();
                System.out.println(response);
            }
            for (int i = 1; i <= 500; i++) {
                outputStream.writeObject(new Message("AddCustomerID,"+ xid +","+i));
                response = ((Message)inputStream.readObject()).getMessageText();
                System.out.println(response);
            }
            outputStream.writeObject(new Message("Commit,"+xid));
            response = ((Message)inputStream.readObject()).getMessageText();
            System.out.println(response);
        } catch(Exception e){
            System.out.println(e.toString());
            System.exit(-1);
        }
        inputStream.close();
        outputStream.close();
        Trace.info("Server connection closed");
        System.out.println("End setup");
    }
    private long oneRMTransaction(ObjectInputStream inputStream, ObjectOutputStream outputStream) throws Exception{
        long startTime = System.currentTimeMillis();
        int key = (int)(Math.random()*100 + 1);
        int customerID = (int)(Math.random()*500 + 1);
        outputStream.writeObject(new Message("Start"));
        String response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        int xid = Integer.valueOf(response);
        outputStream.writeObject(new Message("QueryFlight,"+ xid +","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveFlight,"+ xid +","+customerID+","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        key = (int)(Math.random()*100 + 1);
        customerID = (int)(Math.random()*500 + 1);
        outputStream.writeObject(new Message("QueryFlight,"+ xid +","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveFlight,"+ xid +","+customerID+","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        key = (int)(Math.random()*100 + 1);
        customerID = (int)(Math.random()*500 + 1);
        outputStream.writeObject(new Message("QueryFlight,"+ xid +","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveFlight,"+ xid +","+customerID+","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        outputStream.writeObject(new Message("Commit,"+xid));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        long responseTime = System.currentTimeMillis() - startTime;
        return responseTime;
    }

    private long threeRMTransaction(ObjectInputStream inputStream, ObjectOutputStream outputStream) throws Exception{
        long startTime = System.currentTimeMillis();
        int key = (int)(Math.random()*100 + 1);
        int customerID = (int)(Math.random()*500 + 1);
        outputStream.writeObject(new Message("Start"));
        String response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        int xid = Integer.valueOf(response);
        outputStream.writeObject(new Message("QueryFlight,"+ xid +","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveFlight,"+ xid +","+customerID+","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        outputStream.writeObject(new Message("QueryCars,"+ xid +","+"Montreal"+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveCar,"+ xid +","+customerID+","+"Montreal"+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        outputStream.writeObject(new Message("QueryRooms,"+ xid +","+"Montreal"+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveRoom,"+ xid +","+customerID+","+"Montreal"+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        outputStream.writeObject(new Message("Commit,"+xid));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        long responseTime = System.currentTimeMillis() - startTime;
        return responseTime;
    }
}

