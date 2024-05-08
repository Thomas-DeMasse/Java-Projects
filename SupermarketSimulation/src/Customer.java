public class Customer implements Comparable<Customer> {

    private int arrivalTime;
    private int customerID;

    public Customer(int customerID, int arrivalTime) {
        this.customerID = customerID;
        this.arrivalTime = arrivalTime;
    }

    @Override
    public int compareTo(Customer o) {
        return o.customerID > this.customerID ? -1 : 1;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getCustomerID() {
        return customerID;
    }
}
