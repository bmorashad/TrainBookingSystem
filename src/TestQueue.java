import java.util.ArrayList;
import java.util.Arrays;

// PassengerQueue Successfull
public class TestQueue {
    static int[] arr = new int[8];
    public static void addToArr() {
        for(int i = 0; i < 8; i++) {
            arr[i] = i+1;
        }

    }
    public static int binarySearch(int[] arr, int searchFor, int index, boolean isFirstHalf) {
        int currentIndex = arr.length / 2;
        if(isFirstHalf) {
            index += currentIndex-1;
        } else {
            index -= currentIndex-1;
        }
        if(arr[currentIndex] == searchFor) {
            return index;
        } else {
            int[] subArr;
            isFirstHalf = true;
            if(searchFor > arr[currentIndex]) {
                isFirstHalf = false;
                subArr = Arrays.copyOfRange(arr, currentIndex, arr.length);
            } else {
                subArr = Arrays.copyOfRange(arr, 0, currentIndex);
            }
            return binarySearch(subArr, searchFor, index, isFirstHalf);
        }
    }

    public static void main(String[] args) {
////        PassengerQueue pq = new PassengerQueue(21);
////        try {
////            pq.enqueue(new Passenger("Arshad", 4));
////        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            pq.enqueue(new Passenger("Ammar", 43));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        for(int i =0; i < 5; i++) {
//            Passenger p = new Passenger("Rashad", 3);
//            try {
//                pq.enqueue(p);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
////        System.out.println(pq.getSize());
//        try {
//            pq.enqueue(new Passenger("Arshad", 4));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            pq.enqueue(new Passenger("Safa", 2));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            pq.enqueue(new Passenger("Sara", 34));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(pq.getSize());
//        pq.dequeue();
//        System.out.println("After dequeue " + pq.getSize());
//        pq.delete("Sara", 34);
//        System.out.println("After delete " + pq.getSize());
//        pq.dequeue();
//        System.out.println("After dequeue " + pq.getSize());
//        pq.display();
//
//        System.out.println("Successfull");
        addToArr();
        System.out.println(binarySearch(arr, 8, 0, true));
    }
}
