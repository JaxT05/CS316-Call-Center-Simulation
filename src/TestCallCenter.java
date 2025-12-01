/*
    You can import any additional package here.
 */

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class TestCallCenter {

    /*
       Total number of customers that each agent will serve in this simulation.
       (Note that an agent can only serve one customer at a time.)
     */
    private static final int CUSTOMERS_PER_AGENT = 5;

    /*
       Total number of agents.
     */
    private static final int NUMBER_OF_AGENTS = 3;

    /*
       Total number of customers to create for this simulation.
     */
    private static final int NUMBER_OF_CUSTOMERS = NUMBER_OF_AGENTS * CUSTOMERS_PER_AGENT;

    /*
      Number of threads to use for this simulation.
     */
    private static final int NUMBER_OF_THREADS = 10;

    /*
        Create the greeter and agents tasks first, and then create the customer tasks.
        to simulate a random interval between customer calls, sleep for a random period after creating each customer task.
     */
    public static void main(String[] args) {
        //TODO: complete the main method

        LinkedList<Integer> waitQueue = new LinkedList<>();
        LinkedList<Integer> serveQueue = new LinkedList<>();

        ReentrantLock waitQueueLock = new ReentrantLock();
        ReentrantLock serveQueueLock = new ReentrantLock();

        Condition customerWaiting = waitQueueLock.newCondition();
        Condition customerService = serveQueueLock.newCondition();

        try (ExecutorService es = Executors.newFixedThreadPool(NUMBER_OF_THREADS)) {
            es.submit(new Greeter(waitQueue, serveQueue, waitQueueLock, serveQueueLock, customerWaiting, customerService));

            for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
                es.submit(new Agent(i, waitQueue, serveQueue, waitQueueLock, serveQueueLock, customerService));
            }

            for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
                es.submit(new Customer(i, waitQueue, waitQueueLock, customerWaiting));
            }

        }
    }

    /*
       The Agent class.
     */
    public static class Agent implements Runnable {
        //TODO: complete the agent class
        //The ID of the agent
        private final int ID;

        private final LinkedList<Integer> waitQueue;
        private final LinkedList<Integer> serveQueue;

        private final ReentrantLock waitQueueLock;
        private final ReentrantLock serveQueueLock;

        private final Condition customerService;

        private int num_served = 0;

        //Feel free to modify the constructor
        public Agent(int i, LinkedList<Integer> waitQueue, LinkedList<Integer> serveQueue, ReentrantLock waitQueueLock, ReentrantLock serveQueueLock, Condition customerService) {
            ID = i;
            this.waitQueue = waitQueue;
            this.serveQueue = serveQueue;
            this.waitQueueLock = waitQueueLock;
            this.serveQueueLock = serveQueueLock;
            this.customerService = customerService;
        }
        /*
        Your implementation must call the method below to serve each customer.
        Do not modify this method.
         */

        @Override
        public void run() {
            while (num_served < CUSTOMERS_PER_AGENT) {
                serveQueueLock.lock();

                try {
                    while (serveQueue.isEmpty()) {
                        customerService.await();
                    }

                    num_served++;

                    int customerID = serveQueue.remove();
                    serve(customerID);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    serveQueueLock.unlock();
                }
            }

        }

        public void serve(int customerID) {
            System.out.println("Agent " + ID + " is serving customer " + customerID);
            try {
                /*
                   Simulate busy serving a customer by sleeping for a random amount of time.
                */
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        The greeter class.
     */
    public static class Greeter implements Runnable {
        //TODO: complete the Greeter class

        private static int num_served = 0;
        private final LinkedList<Integer> waitQueue;
        private final LinkedList<Integer> serveQueue;
        private final ReentrantLock waitQueueLock;
        private final ReentrantLock serveQueueLock;
        private final Condition customerWaiting;
        private final Condition customerService;

        public Greeter(LinkedList<Integer> waitQueue, LinkedList<Integer> serveQueue, ReentrantLock waitQueueLock, ReentrantLock serveQueueLock, Condition customerWaiting, Condition customerService) {
            this.waitQueue = waitQueue;
            this.serveQueue = serveQueue;
            this.waitQueueLock = waitQueueLock;
            this.serveQueueLock = serveQueueLock;
            this.customerWaiting = customerWaiting;
            this.customerService = customerService;
        }

        @Override
        public void run() {
            while (num_served < NUMBER_OF_CUSTOMERS) {
                waitQueueLock.lock();
                serveQueueLock.lock();

                try {
                    while (waitQueue.isEmpty()) {
                        customerWaiting.await();
                    }

                    int customerToGreet = waitQueue.remove();
                    greet(customerToGreet);

                    num_served++;

                    serveQueue.add(customerToGreet);

                    customerService.signal();

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    waitQueueLock.unlock();
                    serveQueueLock.unlock();
                }
            }
        }

        /*
                Your implementation must call the method below to serve each customer.
                Do not modify this method.
                 */
        public void greet(int customerID) {
            System.out.println("Greeting customer " + customerID);
            try {
                    /*
                    Simulate busy serving a customer by sleeping for a random amount of time.
                    */
                sleep(ThreadLocalRandom.current().nextInt(10, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        The customer class.
     */
    public static class Customer implements Runnable {
        //TODO: complete the Customer class
        //The ID of the customer.
        private final int ID;
        private final LinkedList<Integer> waitQueue;
        private final ReentrantLock waitQueueLock;
        private final Condition waitQueueAvailable;

        //Feel free to modify the constructor
        public Customer(int i, LinkedList<Integer> waitQueue, ReentrantLock waitQueueLock, Condition waitQueueAvailable) {
            ID = i;

            this.waitQueue = waitQueue;
            this.waitQueueLock = waitQueueLock;
            this.waitQueueAvailable = waitQueueAvailable;
        }

        @Override
        public void run() {
            waitQueueLock.lock();

            try {
                waitQueue.add(ID);

                System.out.println("Waiting to greet customer: " + ID);

                waitQueueAvailable.signal();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                waitQueueLock.unlock();
            }

        }
    }

}
