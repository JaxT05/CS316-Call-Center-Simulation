/* You can import any additional package here. */
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class CallCenter {

    /* Total number of customers that each agent will serve in this simulation.
       (Note that an agent can only serve one customer at a time.) */
    private static final int CUSTOMERS_PER_AGENT = 5;

    /* Total number of agents. */
    private static final int NUMBER_OF_AGENTS = 3;

    /* Total number of customers to create for this simulation. */
    private static final int NUMBER_OF_CUSTOMERS = NUMBER_OF_AGENTS * CUSTOMERS_PER_AGENT;

    /* Number of threads to use for this simulation. */
    private static final int NUMBER_OF_THREADS = 10;

    /* The Agent class. */
    public static class Agent implements Runnable {
    //TODO: complete the agent class
        //The ID of the agent
        private final int ID;
        private final int customerID;
        private final LinkedList<Integer> serveQueue;
        private final ReentrantLock serveQueueLock;
        private final Condition customerService;

        //Feel free to modify the constructor
        public Agent(int i, int customerID, LinkedList<Integer> serveQueue, ReentrantLock serveQueueLock, Condition customerService) {
            ID = i;
            this.customerID = customerID;
            this.serveQueue = serveQueue;
            this.serveQueueLock = serveQueueLock;
            this.customerService = customerService;
        }

        public void run() {
            serveQueueLock.lock();
            try {
                while(serveQueue.isEmpty()) {
                    customerService.await();
                }
                serveQueue.remove();
                serve(customerID);
                //signal ? ? ??
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                serveQueueLock.unlock();
            }
        }
        /*
        Your implementation must call the method below to serve each customer.
        Do not modify this method.
         */
        public void serve(int customerID) {
            System.out.println("Agent " + ID + " is serving customer " + customerID);
            try {
                /* Simulate busy serving a customer by sleeping for a random amount of time. */
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /* The greeter class. */
    public static class Greeter implements Runnable {
        //TODO: complete the Greeter class


        public void run() {
            serveQueueLock.lock();
            try {
                while(serveQueue.isEmpty()) {
                    customerService.await();
                }
                waitQueue.remove();
                greet(customerID);
                serveQueue.add(customerID);
                System.out.println("Customer place in serve queue: " + serveQueue.size());
                //signal ? ? ??
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                serveQueueLock.unlock();
            }

        }

     /* Your implementation must call the method below to serve each customer.
        You can modify this method. -> add another print statement to print the customer */
        public void greet(int customerID) {
            System.out.println("Greeting customer " + customerID);
                try {
                    /* Simulate busy serving a customer by sleeping for a random amount of time. */
                    sleep(ThreadLocalRandom.current().nextInt(10, 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
            }
        }
    }

    /* The customer class. */
    public static class Customer implements Runnable {
        //TODO: complete the Customer class
        //The ID of the customer.
        private final int ID;

        //Feel free to modify the constructor
        public Customer (int i){
            ID = i;
        }


        public void run() {

        }
    }

    /* Create the greeter and agents tasks first, and then create the customer tasks.
        to simulate a random interval between customer calls, sleep for a random period after creating each customer task. */
    public static void main(String[] args){
    //TODO: complete the main method

        LinkedList<Integer> waitQueue = new LinkedList<>();
        LinkedList<Integer> serveQueue = new LinkedList<>();

        ReentrantLock waitQueueLock = new ReentrantLock();
        ReentrantLock serveQueueLock = new ReentrantLock();

        Condition customerWaiting = waitQueueLock.newCondition();
        Condition customerService = serveQueueLock.newCondition();


        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
            executorService.submit(new Customer(i));
            executorService.submit(new Greeter());
            executorService.submit(new Agent());
        }
        executorService.shutdown();
    }

}
