public class Passenger implements Comparable<Passenger>{
    private String fullName;
    private int seatNum;
    private String startStation;
    private String endStation;
    private int secondsInQueue;

    public Passenger(String fullName, int seatNum) {
        this.fullName = fullName;
        this.seatNum = seatNum;
    }

    public void setStartStation(String startStation) {
        this.startStation = startStation;
    }

    public void setEndStation(String endStation) {
        this.endStation = endStation;
    }

    public String getStartStation() {
        return startStation;
    }

    public String getEndStation() {
        return endStation;
    }

    public String getFullName() {
        return fullName;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public int getSecondsInQueue() {
        return secondsInQueue;
    }

    public void setFullName(String fullName) {
       this.fullName = fullName;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }

    public void setSecondsInQueue(int sec) {
        secondsInQueue = sec;
//        if(maxSecondsInQueue < secondsInQueue) {
//            maxSecondsInQueue = secondsInQueue;
//        }
    }

    public void display() {
        if(secondsInQueue > 0) {
            System.out.println(fullName + "\t" + seatNum + "\t" + startStation + ", " + endStation + "\t" + secondsInQueue + "S");
        } else {
            System.out.println(fullName + "\t" + seatNum + "\t" + startStation + ", " + endStation);
        }
    }

    @Override
    public int compareTo(Passenger other) {
        return Integer.compare(this.seatNum, other.seatNum);
    }
}
