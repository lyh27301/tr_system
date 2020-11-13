package Server.MiddlewareServer;

import Server.Common.Message;
import Server.Common.Trace;

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
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {}
            transaction.increaseTimeCounter();
        } while(transaction.getTimeCounter() < 10);
        // No new incoming commands for 30 seconds, abort the transaction.
        int xid = transaction.getXid();
        try {

            if (handler.abort(xid)) {
                MiddlewareServer.addTimeoutTransaction(xid);
                Trace.info("Timeout! Transaction-" + xid + " is aborted");

            } else {
                Trace.info("Timeout! Transaction-" + xid + " failed to abort");
            }
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        }


    }
}
