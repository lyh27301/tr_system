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
    private LockManager lockManager = new LockManager();

    public void createNewTransaction(int xid) {
        Transaction transaction = new Transaction(xid);
        allTransactions.put(xid, transaction);
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






}
