import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class TestFileLocation {
    public static void main(String[] args) {
//        File bookedPassengersDetail;
//        Scanner sc = null;
//        try {
//            bookedPassengersDetail = new File("../CWTEST@latest/data/cTob_booking_detail.txt");
//            sc = new Scanner(bookedPassengersDetail);
//            while(sc.hasNext()) {
//                int seatNum = Integer.parseInt(sc.nextLine());
//                LocalDate date = LocalDate.parse(sc.nextLine());
//                String name = sc.nextLine();
//                String startStation = sc.nextLine();
//                String endStation = sc.nextLine();
//                System.out.println(seatNum + "\t" + date + "\t" + name + "\t" + startStation + " " + endStation);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            sc.close();
//        }
        File file = new File("test.txt");
        try{
            FileOutputStream out = new FileOutputStream(file, true);
            ObjectOutputStream oout = new ObjectOutputStream(out);
//            oout.reset();
            oout.writeObject(new TestPassenger("Rashad", 33));
            oout.writeObject(new TestPassenger("Arshad", 1));
            oout.writeObject(new TestPassenger("Ammar", 3));
            oout.writeObject(new TestPassenger("Rashad", 42));
            oout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<TestPassenger> testpassengers = new ArrayList<>();
        try {
            FileInputStream out = new FileInputStream("test.txt");
            ObjectInputStream in = new ObjectInputStream(out);
            while (true) {
//                System.out.println("hi");
                TestPassenger passenger = (TestPassenger) in.readObject();
                testpassengers.add(passenger);
            }
        } catch (EOFException e) {
            for(TestPassenger p : testpassengers) {
                System.out.println(p.getFullName() + "\t" + p.getSeatNum());
            }
            System.out.println("-----------End--------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            FileOutputStream out = new FileOutputStream(file, true);
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.reset();
            oout.writeObject(new TestPassenger("Man", 33));
            oout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        testpassengers = new ArrayList<>();
        try {
            FileInputStream out = new FileInputStream("test.txt");
            ObjectInputStream in = new ObjectInputStream(out);
            while (true) {
//                System.out.println("hi");
                TestPassenger passenger = (TestPassenger) in.readObject();
                testpassengers.add(passenger);
            }
        } catch (EOFException e) {
            for(TestPassenger p : testpassengers) {
                System.out.println(p.getFullName() + "\t" + p.getSeatNum());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
