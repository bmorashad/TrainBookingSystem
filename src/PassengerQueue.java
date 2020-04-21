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

    public void setMaxStayInQueue(int sec) {
        maxStayInQueue = sec;
    }
    public int getMaxStayInQueue() {
        return maxStayInQueue;
    }
    public int getMaxSize() {
        return maxSize;
    }
    public int getSize() {
        return size;
    }
    public Passenger[] getQueue() {
        Passenger[] queue = new Passenger[size];
        for(int i = 0; i < size; i++) {
            queue[i] = this.queue[i+head%maxSize];
        }
        return queue;
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
        bubbleSortQueue();
        if(!isEmpty()) {
            int indexOfSeatNum = findSeat(seatNum);
            if (indexOfSeatNum >= 0) {
                if (queue[indexOfSeatNum].getFullName().equalsIgnoreCase(fullName)) {
                    dltPassenger = queue[indexOfSeatNum];
                    for (int j = 0; j < size; j++) {
                        queue[(j + indexOfSeatNum) % maxSize] = queue[(j + indexOfSeatNum + 1) % maxSize];
                    }
                    tail--;
                    size--;
                }
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
    public  void bubbleSortQueue() {
        boolean isNoSorted = true;
        int inOrder = 0;
        while (isNoSorted) {
            boolean isSwapped = false;
            for(int i = 0; i < size-inOrder-1; i++) {
                Integer currentNum = (head+i)%maxSize;
                Integer nextNum = (head+i+1)%maxSize;
                if(queue[currentNum].compareTo(queue[nextNum]) > 0) {
                    swapPassengers(currentNum, nextNum);
                    isSwapped = true;
                }
            }
            inOrder += 1;
            isNoSorted = isSwapped;
        }
    }
    public void display() {
//        System.out.println(size);
        for(int i = 0; i < size; i++) {
            System.out.println(queue[(i+head) % maxSize].getFullName() + " " + queue[(i+head) % maxSize].getSeatNum());
        }
    }

    // HELPER METHODS

    private int findSeat(int seatNum) {
        int search_index;
        int from = head;
        int to = tail-1;
        do {
            if(to >= from) {
                search_index = (to - from)  / 2 + from;
            } else {
                search_index = (((maxSize - from) + to)/2 + from) % maxSize;
            }
            if(queue[search_index].getSeatNum() == seatNum) {
                return search_index;
            }
            if (seatNum > queue[search_index].getSeatNum()) {
                from = (search_index+1)%maxSize;
            } else {
                to = ((search_index-1) + maxSize) % maxSize;
            }
        } while ((from - to) != 1 && (to - from) != maxSize-1);
        return -1;
    }
    private void swapPassengers(int first, int second) {
        Passenger temp = queue[first];
        queue[first] = queue[second];
        queue[second] = temp;
    }
}
