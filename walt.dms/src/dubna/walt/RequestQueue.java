/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dubna.walt;

//import java.util.LinkedList;
//import java.util.Queue;

/**
 *
 * @author serg
 */
public class RequestQueue {
//    private Queue<Query> queue = null;

    private int numRunning = 0;
//    private static final int MAX_RUNNING = 5;

    public RequestQueue() {
        System.out.println(" *********** RequestQueue - init");
//        queue = new LinkedList<Query>();
    }

    public void add(Query query) {
        System.out.println(numRunning + "  rq : RUN " + query.queryLabel + "; numRunning=" + numRunning);
//        System.out.println(numRunning + "  rq : ADD " + query.queryLabel + "; queue.size=" + queue.size() + "; numRunning=" + numRunning);
//        queue.add(query);
//        System.out.println(numRunning + "  rq : after ADD: queue.size=" + queue.size() + "; numRunning=" + numRunning);
//        runNext();
        runQuery(query);
    }

    public void runQuery(Query query) {
//            synchronized (this) {
//        System.out.println(numRunning + "  rq : runNext() queue.size=" + queue.size() + "; numRunning=" + numRunning);
        numRunning++;
        System.out.println("   rq : runQuery " + query.queryLabel + " / " + query);
        if (numRunning > 2) {
            try {
                System.gc();
                int i = (numRunning > 4)? 1000 : numRunning * 200;
                System.out.println("   rq : sleep " + i);
                Thread.sleep(i);
            } catch (InterruptedException ex) {
            }
        }
//        query.runQuery();
    }


/*
    public void runNext(){
            Query query = null;
//            synchronized (this) {
        System.out.println(numRunning + "  rq : runNext() queue.size=" + queue.size() + "; numRunning=" + numRunning);
//        if(numRunning < MAX_RUNNING) {
                query= queue.poll();
                if(query != null) {
                    numRunning++;
                    System.out.println("   rq : runQuery " +  query.queryLabel + " / " + query);
                    if(numRunning > 2) {
                        try {
                            Thread.sleep(numRunning*500);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
//            }
//            }
            if(query != null) {
                query.runQuery();
        }
    }
/**/
public void queryFinished(String queryLabel){
        synchronized (this) {
            System.out.println("***rq : queryFinished " +  queryLabel);
            if(--numRunning < 0 ) numRunning=0;
        }
//        runNext();
        
    }
}
