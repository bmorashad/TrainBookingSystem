import java.util.Arrays;

public class PassengerQueue {
    private final Passenger[] queue;
    private int maxStayInQueue;
    private int size;
    private int maxSize;
    private int head;
    private int tail;

    public PassengerQueue(int maxSize) {
        queue = new Passenger[maxSize];
        this.maxSize = maxSize;
    }
    // set logic in simulation
    public void setMaxStayInQueue(int sec) {
        maxStayInQueue = sec;
    }
    public int getMaxStayInQueue() {
        return maxStayInQueue;
    }
    public void enqueue(Passenger p) throws Exception {
       if(!isFull()) {
           queue[tail] = p;
           tail = (tail + 1) % queue.length;
           size++;
       } else {
           throw new Exception("Queue is full");
       }

    }
    public Passenger dequeue() {
       if(!isEmpty()) {
           Passenger removed = queue[head];
//           System.out.println(head);
           head = (head + 1) % queue.length;
//           System.out.println(head);
//           System.out.println(size);
           size--;
           return removed;
       }
       return null;
    }
    public Passenger delete(String fullName, int seatNum) {
        Passenger dltPassenger = null;
        for(int i = 0; i < size; i++) {
            // keep an eye, can improve prolly(binary search)
            int currentPassenger = (i+head)%maxSize;
            if(queue[currentPassenger].getSeatNum() == seatNum && queue[currentPassenger].getFullName() == fullName) {
                dltPassenger = queue[currentPassenger];
                for(int j = i; j < maxSize; j++) {
                    queue[j%maxSize] = queue[(j+1)%maxSize];
                    if(tail == (j+1)%maxSize) {
                        tail--;
                        break;
                    }
                }
                size--;
                break;
            }
        }
        return dltPassenger;
    }
    public boolean isFull() {
        if(size == queue.length) {
            return true;
        }
        return false;
    }
    public boolean isEmpty() {
        if(size == 0) {
            return true;
        }
        return false;
    }
    public int getSize() {
        return size;
    }
    public int getMaxSize() {
        return maxSize;
    }
    public void display() {
//        System.out.println(size);
        for(int i = 0; i < size; i++) {
            System.out.println(queue[(i+head) % maxSize].getFullName() + " " + queue[(i+head) % maxSize].getSeatNum());
        }
    }

}
