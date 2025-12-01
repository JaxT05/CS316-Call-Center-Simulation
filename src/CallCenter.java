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

//                System.out.println("-----------------------");
//                for(int num : serveQueue) {
//                    System.out.print(num + ", ");
//                }
//                System.out.println("-----------------------");

                serveQueue.remove();
                serve(customerID);
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

    class SharedTest {
        static int count = 0;
    }

    /* The greeter class. */
    public static class Greeter implements Runnable {
        private LinkedList<Integer> waitQueue;
        private ReentrantLock waitQueueLock;
        private LinkedList<Integer> serveQueue;
        private ReentrantLock serveQueueLock;
        private Condition customerWaiting;
        private Condition customerService;

        static int test = 0;

        public Greeter(LinkedList<Integer> waitQueue, ReentrantLock waitQueueLock, LinkedList<Integer> serveQueue, ReentrantLock serveQueueLock, Condition customerWaiting, Condition customerService) {
            this.waitQueue = waitQueue;
            this.waitQueueLock = waitQueueLock;
            this.serveQueue = serveQueue;
            this.serveQueueLock = serveQueueLock;
            this.customerWaiting = customerWaiting;
            this.customerService = customerService;
        }

        //TODO: complete the Greeter class


        public void run() {
            waitQueueLock.lock();

            try {
                while(waitQueue.isEmpty()) {
                    customerWaiting.await();
                }

                int customerToGreet = waitQueue.remove();
                greet(customerToGreet);

                serveQueueLock.lock();
                serveQueue.add(customerToGreet);
//                System.out.println("Customer place in serve queue: " + serveQueue.size());
                customerService.signal();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                waitQueueLock.unlock();
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
        private LinkedList<Integer> waitQueue;
        private ReentrantLock waitQueueLock;
        private Condition customerWaiting;

        //Feel free to modify the constructor
        public Customer (int i, LinkedList<Integer> waitQueue, ReentrantLock waitQueueLock, Condition customerWaiting) {
            ID = i;
            this.waitQueue = waitQueue;
            this.waitQueueLock = waitQueueLock;
            this.customerWaiting = customerWaiting;
        }


        public void run() {
            waitQueueLock.lock();

            try {
                waitQueue.add(ID);

                System.out.println("Customer place in wait queue: " + ID);
                customerWaiting.signal();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                waitQueueLock.unlock();
            }
        }
    }

    /* Create the greeter and agents tasks first, and then create the customer tasks.
        to simulate a random interval between customer calls, sleep for a random period after creating each customer task. */
    public static void main(String[] args) throws InterruptedException {
    //TODO: complete the main method

        LinkedList<Integer> waitQueue = new LinkedList<>();
        LinkedList<Integer> serveQueue = new LinkedList<>();

        ReentrantLock waitQueueLock = new ReentrantLock();
        ReentrantLock serveQueueLock = new ReentrantLock();

        Condition customerWaiting = waitQueueLock.newCondition();
        Condition customerService = serveQueueLock.newCondition();

        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        for (int i = 0; i < NUMBER_OF_CUSTOMERS; i++) {
            executorService.submit(new Customer(i, waitQueue, waitQueueLock, customerWaiting));
            executorService.submit(new Greeter(waitQueue, waitQueueLock, serveQueue, serveQueueLock, customerWaiting, customerService));
            executorService.submit(new Agent(i, i, serveQueue, serveQueueLock, customerService));
        }

        executorService.shutdown();
    }

}
