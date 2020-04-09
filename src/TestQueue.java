// PassengerQueue Successfull
public class TestQueue {
    public static void main(String[] args) {
        PassengerQueue pq = new PassengerQueue(21);
        try {
            pq.enqueue(new Passenger("Arshad", 4));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            pq.enqueue(new Passenger("Ammar", 43));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i =0; i < 5; i++) {
            Passenger p = new Passenger("Rashad", 3);
            try {
                pq.enqueue(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        System.out.println(pq.getSize());
        try {
            pq.enqueue(new Passenger("Arshad", 4));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            pq.enqueue(new Passenger("Safa", 2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            pq.enqueue(new Passenger("Sara", 34));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(pq.getSize());
        pq.dequeue();
        System.out.println("After dequeue " + pq.getSize());
        pq.delete("Sara", 34);
        System.out.println("After delete " + pq.getSize());
        pq.dequeue();
        System.out.println("After dequeue " + pq.getSize());
        pq.display();

        System.out.println("Successfull");
    }
}
