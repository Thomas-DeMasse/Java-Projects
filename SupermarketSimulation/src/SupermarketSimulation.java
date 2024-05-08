

import java.util.PriorityQueue;
import java.util.Random;

public class SupermarketSimulation {

    private int serviceTime;
    private int nextArrival;
    private int firstArrival;
    private int customerID = 1;
    private int inQueue = 1;
    private int maxInQueue = 1;
    private int customersServiced = 0;
    private int wait;
    private int longestWait = 0;


    public static void main(String[] args){
        SupermarketSimulation sim = new SupermarketSimulation();
        Random rand = new Random();
        PriorityQueue<Customer> line = new PriorityQueue<>();

        sim.firstArrival = (rand.nextInt(3)+1); //determine time of first arrival
        sim.nextArrival = rand.nextInt(3)+1; //time of next arrival
        sim.serviceTime = rand.nextInt(3)+1; //service time of first arrival
        line.add(new Customer(sim.customerID, sim.firstArrival)); //add first customer to queue
        System.out.println("Customer" + sim.customerID + " entered queue."); //announce first customer has been queued

        for(int minute = sim.firstArrival; minute <= 720;
            minute++){
            if(sim.nextArrival == 0){
                sim.customerID++;
                sim.inQueue++;
                line.add(new Customer(sim.customerID, minute)); //add new customer to queue

                System.out.println("Customer " + sim.customerID + " entered queue at " + minute + "."); //announce new customer has been added
                System.out.println("There are now " + sim.inQueue + " customers in line."); //state q size

                sim.nextArrival = rand.nextInt(4)+1; //schedule next arrival
            }
            if(!line.isEmpty() && sim.serviceTime == 0){ //if there is a customer in line and service time has completed dequeue
                sim.wait = (minute-line.peek().getArrivalTime());
                System.out.println("Customer " + line.peek().getCustomerID() +  " has been serviced."); //announce that current customer has been serviced
                System.out.println("They waited " + sim.wait + " minutes."); //state how long they waited
                line.poll(); //remove customer from queue
                sim.inQueue--;
                sim.customersServiced++;
                sim.serviceTime = rand.nextInt(4)+1; //determine service time of next customer
                System.out.println("There are now " + sim.inQueue + " customers in line."); //state q size
            }
            else if(line.isEmpty()){//if there is no customer in line reset service time and continue
                sim.serviceTime = rand.nextInt(3)+1;
                sim.nextArrival--;
                continue;
            }
            if(sim.wait > sim.longestWait){//check if last customers wait time was longer than previous maximum
                sim.longestWait = sim.wait;
            }
            if(sim.inQueue > sim.maxInQueue){//check if number of customers in queue is larger than previous maximum
                sim.maxInQueue = sim.inQueue;
            }
            sim.nextArrival--;
            sim.serviceTime--;
        }
        System.out.println("\nThe day has ended. " + sim.customersServiced + " customers were serviced.");
        System.out.println("The longest wait time of any customer was " + sim.longestWait + " minutes.");
        System.out.println("The largest number of customers in queue was " + sim.maxInQueue + ".");
    }
}