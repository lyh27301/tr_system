package Server.MiddlewareServer;

import Server.Common.RMItem;

import java.util.HashMap;

public class Transaction {

    private int xid;
    //key: data object; value: before image
    private HashMap<String, RMItem> dataHistory;

    volatile private int timeCounter = 0;

    public Transaction(int xid){
        this.xid = xid;
        this.dataHistory = new HashMap<>();
    }

    public void increaseTimeCounter() {
        this.timeCounter = this.timeCounter + 1;
    }

    public void zeroTimeCounter() {
        this.timeCounter = 0;
    }

    public int getTimeCounter() {
        return timeCounter;
    }

    public void insertDataHistory(String key, RMItem item){
        this.dataHistory.put(key, item);
    }

    public HashMap<String, RMItem> getDataHistory(){
        return this.dataHistory;
    }

    public int getXid() {
        return this.xid;
    }
}
