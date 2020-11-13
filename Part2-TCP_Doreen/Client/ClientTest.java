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
    static int numberOfClients = 1;
    public long startTimeOfClient = 0;
    static double throughput = 100;
    public long[] data = new long[50];
    public static void main (String[] args) throws Exception{

        if (args.length > 0) middlewareHost = args[0];

        long startTime = 5*1000 + System.currentTimeMillis();
        try {
            ClientTest[] c = new ClientTest[numberOfClients];
            Thread[] thread = new Thread[numberOfClients];
            for (int i = 0; i < numberOfClients; i++) {
                c[i] = new ClientTest();
                c[i].startTimeOfClient = startTime;
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
                for (int j = 0; j < c[i].data.length; j++) {
                    System.out.print(c[i].data[j]);
                    if (j!= c[i].data.length - 1)
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
        Socket s;
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
        long variation = 31;

        while (System.currentTimeMillis() < startTimeOfClient){}

        for (int i = (int)Thread.currentThread().getId()*200; i < (int)Thread.currentThread().getId()*200 + 150; i++) {
            int interval;
            if (Math.random() < 0.5)
                interval = waitTime - ((int)(variation*Math.random()));
            else
                interval = waitTime + ((int)(variation*Math.random()));
            try {
                long rt = oneRMTransaction(inputStream,outputStream);
                if (i >= (int)Thread.currentThread().getId()*200 + 100)
                    data[i - ((int)Thread.currentThread().getId()*200 + 100)] = rt;
                System.out.println("sleep" + (int)(interval - rt));
                if ((int)(interval - rt) < 0)
                    continue;
                Thread.sleep((int) (interval - rt));
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
            Message msg = (Message)inputStream.readObject();
            String response = msg.getMessageText();
            System.out.println(response);
            int xid = (int)(msg.getMessageObject());
            System.out.println(xid);
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

            outputStream.writeObject(new Message("Start"));
            msg = (Message)inputStream.readObject();
            response = msg.getMessageText();
            System.out.println(response);
            xid = (int)(msg.getMessageObject());
            System.out.println(xid);
            outputStream.writeObject(new Message("QueryFlight,"+ xid +","+"1"));
            response = ((Message)inputStream.readObject()).getMessageText();
            System.out.println(response);
            outputStream.writeObject(new Message("QueryCars,"+ xid +","+"Montreal"+"1"));
            response = ((Message)inputStream.readObject()).getMessageText();
            System.out.println(response);
            outputStream.writeObject(new Message("QueryRooms,"+ xid +","+"Montreal"+"1"));
            response = ((Message)inputStream.readObject()).getMessageText();
            System.out.println(response);
            outputStream.writeObject(new Message("Commit,"+xid));
            response = ((Message)inputStream.readObject()).getMessageText();
            System.out.println(response);
            outputStream.writeObject(new Message("Quit"));
//            response = ((Message)inputStream.readObject()).getMessageText();
            System.out.println("Good bye!");
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
        long startTxn = System.currentTimeMillis();
        int key = (int)(Math.random()*100 + 1);
        int customerID = (int)(Math.random()*500 + 1);
        outputStream.writeObject(new Message("Start"));
        Message msg = (Message)inputStream.readObject();
        String response = msg.getMessageText();
        System.out.println(response);
        int xid = (int)(msg.getMessageObject());
        System.out.println(xid);
        outputStream.writeObject(new Message("QueryFlight,"+ xid +","+key));
        response = ((Message)inputStream.readObject()).getMessageText();

        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveFlight,"+ xid +","+customerID+","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
//        key = (int)(Math.random()*100 + 1);
//        customerID = (int)(Math.random()*500 + 1);
        outputStream.writeObject(new Message("QueryFlight,"+ xid +","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveFlight,"+ xid +","+customerID+","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
//        key = (int)(Math.random()*100 + 1);
//        customerID = (int)(Math.random()*500 + 1);
        outputStream.writeObject(new Message("QueryFlight,"+ xid +","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveFlight,"+ xid +","+customerID+","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
        outputStream.writeObject(new Message("Commit,"+xid));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        long responseTime = System.currentTimeMillis() - startTxn;
        return responseTime;
    }

    private long threeRMTransaction(ObjectInputStream inputStream, ObjectOutputStream outputStream) throws Exception{
        long startTxn = System.currentTimeMillis();
        int key = (int)(Math.random()*100 + 1);
        int customerID = (int)(Math.random()*500 + 1);
        outputStream.writeObject(new Message("Start"));
        Message msg = (Message)inputStream.readObject();
        String response = msg.getMessageText();
        System.out.println(response);
        int xid = (int)(msg.getMessageObject());
        System.out.println(xid);
        outputStream.writeObject(new Message("QueryFlight,"+ xid +","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveFlight,"+ xid +","+customerID+","+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
        outputStream.writeObject(new Message("QueryCars,"+ xid +","+"Montreal"+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveCar,"+ xid +","+customerID+","+"Montreal"+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
        outputStream.writeObject(new Message("QueryRooms,"+ xid +","+"Montreal"+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
        outputStream.writeObject(new Message("ReserveRoom,"+ xid +","+customerID+","+"Montreal"+key));
        response = ((Message)inputStream.readObject()).getMessageText();
        if(response.equals("The transaction "+xid+" is deadlocked:"+"Transaction-"+xid+" is aborted. Please try again later."))
        {
            System.out.println(response);
            long responseTime = System.currentTimeMillis() - startTxn;
            return responseTime;
        }
        System.out.println(response);
        outputStream.writeObject(new Message("Commit,"+xid));
        response = ((Message)inputStream.readObject()).getMessageText();
        System.out.println(response);
        long responseTime = System.currentTimeMillis() - startTxn;
        return responseTime;
    }
}