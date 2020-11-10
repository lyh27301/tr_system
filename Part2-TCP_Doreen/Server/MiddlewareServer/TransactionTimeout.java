package Server.MiddlewareServer;

import Server.Common.Message;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TransactionTimeout implements Runnable {
    TransactionManager manager;
    Transaction transaction = null;
    MiddlewareClientHandler handler;
    int xid;

    public TransactionTimeout(int xid, TransactionManager manager, MiddlewareClientHandler handler) {
        this.manager = manager;
        this.handler = handler;
        this.xid = xid;
    }

    @Override
    public void run() {
        do {
            transaction = manager.getTransaction(xid);
            if(transaction == null) continue;
            System.out.println("while loop of transaction-"+ xid +" starts - counter=" + transaction.getTimeCounter());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {}
            transaction.increaseTimeCounter();
        } while(transaction.getTimeCounter() < 10);
        // No new incoming commands for 30 seconds, abort the transaction.
        int xid = transaction.getXid();
        try {
            System.out.println("abort xid=" + xid + " starts");

            if (handler.abort(xid)) {
                handler.clientOutputStream.writeObject(new Message("Timeout! Transaction-" + xid + " is aborted"));
            } else {
                handler.clientOutputStream.writeObject(new Message("Timeout! However, failed to abort Transaction-" + xid));
            }
            System.out.println("abort xid=ends");
            Message message = new Message("Reason: Timeout.");
            message.setMessageObject(Integer.valueOf(xid));
            handler.clientOutputStream.writeObject(message);
        } catch (InvalidTransactionException | IOException e) {
            e.printStackTrace();
        }


    }
}
