package Server.MiddlewareServer;

import Server.Common.RMItem;
import Server.Common.Trace;
import Server.LockManager.DeadlockException;
import Server.LockManager.LockManager;
import Server.LockManager.TransactionLockObject;

import java.util.ArrayList;
import java.util.HashMap;

public class TransactionManager {
    private HashMap<Integer, Transaction> allTransactions = new HashMap<>();
    private HashMap<Integer, TransactionTimeout> timeoutHashMap = new HashMap<>();
    private LockManager lockManager = new LockManager();
    volatile private int idCounter = -1;

    synchronized public int createNewTransaction() {
        int xid = idCounter + 1;
        idCounter++;
        Transaction transaction = new Transaction(xid);
        allTransactions.put(xid, transaction);
        return xid;
    }

    public void addTimeout(int xid, TransactionTimeout timeout) {
        timeoutHashMap.put(xid, timeout);
    }

    public TransactionTimeout getTimeout(int xid) {
        return timeoutHashMap.get(xid);
    }


    public boolean existsTransaction(int xid){
        return allTransactions.containsKey(xid);
    }

    public boolean aquireLock(int xid, String key, TransactionLockObject.LockType lockType) throws DeadlockException {
        return lockManager.Lock(xid, key, lockType);
    }

    public void addBeforeImage(int xid, String key, RMItem remoteItem) {
        allTransactions.get(xid).insertDataHistory(key, remoteItem);
    }

    public HashMap<String, RMItem> getTransactionHistory(int xid){
        Transaction t = allTransactions.get(xid);
        return t.getDataHistory();
    }

    public boolean containsData(int xid, String objectKey){
        Transaction t = allTransactions.get(xid);
        return t.getDataHistory().containsKey(objectKey);
    }

    public boolean releaseLockAndRemoveTransaction (int xid) throws InvalidTransactionException {
        boolean success = lockManager.UnlockAll(xid);
        allTransactions.remove(xid);
        return success;
    }

    public Transaction getTransaction(int xid) {
        return  allTransactions.get(xid);
    }






}
