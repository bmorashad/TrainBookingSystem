import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TrainStation extends Application{
    private final String STATION = "Colombo";
    private int boardFrom = -1;
    private int lastBoarded = 0;
    // 1 - arrived, -1 - not-arrived, 0 - not-booked, 2 - in-trainQ, 3 - boarded
    private int[] seatStat = new int[42];
    private final Passenger[] BOOKED_PASSENGERS = new Passenger[42];

    private PassengerQueue trainQueue = new PassengerQueue(21);
    private Passenger[] waitingRoom = new Passenger[42];
    private List<Passenger> lateComers = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        LocalDate date = LocalDate.parse("2020-04-17");
        getBookedPassengers("../CWTEST@latest/data/cTob_booking_detail.txt", date);
        boolean exit = false;
        while (!exit){
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter option: ");
            String option = sc.nextLine();
//            String option = "t";
            switch (option.toLowerCase()) {
                case "w":
                    addArrivedPassengers();
                    break;
                case "a":
                    addToTrainQueue();
                    break;
                case "v":
                    visualize();
                    break;
                case "d":
                    deletePassengerFromQueue();
                    break;
                case "r":
                    runSimulation();
                    break;
                case "s":
                    saveToFile();
                    break;
                case "l":
                    loadFromFile();
                    break;
                case "q":
                    exit = true;
                default:
                    System.out.println("Enter a valid option(q to exit)");
            }
        }
    }
    // "../CWTEST@latest/data/cTob_booking_detail.txt"
    public void getBookedPassengers(String pathToFile, LocalDate date) {
        Scanner sc = null;
        try {
            File bookedPassengersDetail = new File(pathToFile);
            sc = new Scanner(bookedPassengersDetail);
            while(sc.hasNext()) {
                int seatNum = Integer.parseInt(sc.nextLine());
                LocalDate currentPassengerDate = LocalDate.parse(sc.nextLine());
                String name = sc.nextLine();
                String startStation = sc.nextLine();
                String endStation = sc.nextLine();
//                System.out.println(seatNum + "\t" + currentPassengerDate + "\t" + name + "\t" + startStation + " " + endStation);
                if(startStation.equals(STATION) && date.equals(currentPassengerDate)) {
                    Passenger passenger = new Passenger(name, seatNum);
                    passenger.setStartStation(startStation);
                    passenger.setEndStation(endStation);
                    BOOKED_PASSENGERS[seatNum-1] = passenger;
                    seatStat[seatNum-1] = -1;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }
        System.out.println(Arrays.toString(BOOKED_PASSENGERS));
    }

    public void addArrivedPassengers() {
        System.out.println("tQ size: " + trainQueue.getSize());
        Stage stage = new Stage();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);
        grid.setHgap(10);

//        grid.setGridLinesVisible(true);

        Text sceneHeading = new Text("Add arrived passengers to waiting room");
        sceneHeading.setStyle("-fx-font-family: Roboto;");
        sceneHeading.setFill(Color.valueOf("#2e4a7d"));
        sceneHeading.setFont(Font.font(null, FontWeight.BOLD, 20));
        GridPane.setConstraints(sceneHeading, 0, 0);
        GridPane.setColumnSpan(sceneHeading,7);
        GridPane.setMargin(sceneHeading, new Insets(0, 0, 10, 0));
        grid.getChildren().add(sceneHeading);

        Button add = new Button("Add");
        GridPane.setHalignment(add, HPos.RIGHT);
        GridPane.setConstraints(add, 4, 9);
        GridPane.setColumnSpan(add, 2);
        add.setMinWidth(60);
        add.getStyleClass().add("add");
        grid.getChildren().add(add);

        Button quit = new Button("Quit");
        GridPane.setConstraints(quit, 6, 9);
        quit.setMinWidth(50);
        quit.getStyleClass().add("quit");
        grid.getChildren().add(quit);

        Label grey = new Label();
        grey.setMinSize(15, 15);
        grey.setMaxSize(15, 15);
        grey.setStyle("-fx-background-color: #b8b8b8");
        GridPane.setHalignment(grey, HPos.LEFT);
        GridPane.setConstraints(grey, 0, 1);
        grid.getChildren().add(grey);

        Label greyInfo = new Label("not-booked");
        GridPane.setHalignment(greyInfo, HPos.LEFT);
        GridPane.setConstraints(greyInfo, 0, 1);
        GridPane.setColumnSpan(greyInfo, 2);
        GridPane.setMargin(greyInfo, new Insets(0, 0, 0, 20));
        grid.getChildren().add(greyInfo);

        Label green = new Label();
        green.setMinSize(15, 15);
        green.setMaxSize(15, 15);
        green.setStyle("-fx-background-color: #07b100");
        GridPane.setHalignment(green, HPos.LEFT);
        GridPane.setMargin(green, new Insets(0, 0, 0, 45));
        GridPane.setColumnSpan(green, 2);
        GridPane.setConstraints(green, 1, 1);
        grid.getChildren().add(green);

        Label greenInfo = new Label("arrived");
        GridPane.setHalignment(greenInfo, HPos.LEFT);
        GridPane.setConstraints(greenInfo, 1, 1);
        GridPane.setColumnSpan(greenInfo, 2);
        GridPane.setMargin(greenInfo, new Insets(0, 0, 0, 65));
        grid.getChildren().add(greenInfo);

        final List<Integer> toBeReserved = new ArrayList<>();
        for(int i = 0, c = 0, r = 1; i < BOOKED_PASSENGERS.length; i++) {
            if(i % 7 == 0) {
                r += 1;
                c = 0;
            }
            Button seatBtn = makeSeatButton(i+1, c, r);
            grid.getChildren().add(seatBtn);
            c++;
            if(BOOKED_PASSENGERS[i] != null && seatStat[i] == -1) {
                seatBtn.setOnAction(e -> {
                   setSeatButtonAction(seatBtn, toBeReserved);
                });
            } else if(seatStat[i] >= 1){
//                seatBtn.setStyle("-fx-background-color: #07b100; -fx-text-fill: #fff");
                    seatBtn.getStyleClass().clear();
                    seatBtn.getStyleClass().addAll("button", "arrived");
            } else {
                seatBtn.setDisable(true);
                seatBtn.setStyle("-fx-background-color: #b1b1b1; -fx-text-fill: #333; -fx-border-color: #555");
            }
        }
        add.setOnAction(e -> {
            setActionOnAdd(toBeReserved, stage);
        });
        quit.setOnAction(e -> {
            stage.close();
        });

        stage.setTitle("Add arrived passengers to waiting room");
        popGui(stage, grid, 600, 600);
    }

    public void addToTrainQueue() {
//        System.out.println("boardFrom " + boardFrom);
        Random rd = new Random();
        int passengersToQueue = rd.nextInt(6) + 1;
        System.out.println(passengersToQueue + " Passengers can be added");
        boolean isConfirmed = confirm("Are you sure you want to add " + passengersToQueue + " passengers to queue(y/n)");
        if(isConfirmed) {
//            int totalAdded = 0;
            if (boardFrom != -1 && boardFrom < BOOKED_PASSENGERS.length || !lateComers.isEmpty()) {
                int totalAdded = addLateComersToQueue(passengersToQueue);
//            int currentlyAdded = totalAdded;
                if (totalAdded < passengersToQueue) {
                    totalAdded = addToQueueFromWaitingRoom(totalAdded, passengersToQueue);
                }
                System.out.println(totalAdded + " Passengers were added");
            } else {
                System.out.println("There are no passengers in the waiting room to add");
            }
            visualize();
//            System.out.println(boardFrom);
        }
    }

//    public void view() {
//        visualize();
//    }

    public void deletePassengerFromQueue() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter name of the passenger: ");
        String name = sc.nextLine();
        System.out.println("Enter seat number of the passenger: ");
        int seatNum = getSeatNum();
        if(seatNum > 0) {
            boolean isConfirmed = confirm("Are sure you want to proceed(y/n)? ");
            if (isConfirmed) {
                Passenger deletedPassenger = trainQueue.delete(name, seatNum);
                if (deletedPassenger != null) {
                    seatStat[deletedPassenger.getSeatNum() - 1] = -1;
                    deletedPassenger.display();
                } else {
                    System.out.println("No such passengers there!");
                }
            }
        } else {
            System.out.println("Your attempts to enter seat number, reached maximum retries");
        }
    }

    public void saveToFile() {
        File file = new File("data/session-log/train-station-recent-sesstion.txt");
        FileWriter fw;
        if(boardFrom >= 0) {
            boolean isConfirmed = confirm("Are you sure, this will over-write most recent saved session?(y/n)");
            if(isConfirmed) {
                try {
                    fw = new FileWriter(file);
                    fw.write(lastBoarded + "\n");
                    List<Integer> secondsInQueueList = new ArrayList<>();
                    for (int i = 0; i < this.seatStat.length; i++) {
                        fw.write(seatStat[i] + ",");
                        if (this.seatStat[i] == 3) {
                            int secondsInQueue = BOOKED_PASSENGERS[i].getSecondsInQueue();
                            secondsInQueueList.add(secondsInQueue);
                        }
                    }
                    fw.write("\n" + boardFrom + "\n");
                    for (int i = 0; i < secondsInQueueList.size(); i++) {
                        fw.write(secondsInQueueList.get(i) + ",");
                    }
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else{
            System.out.println("No data to save!");
        }
    }

    public void loadFromFile() {
        File file = new File("data/session-log/train-station-recent-sesstion.txt");
        Scanner sc;
        int[] seatStat;
        int[] boardedSeconds = null;
        int[] boardedSeatNumArr = null;
        Passenger[] waitintRoom = new Passenger[42];
        List<Passenger> lateComers = new ArrayList<>();
        PassengerQueue trainQueue = new PassengerQueue(this.trainQueue.getMaxSize());
        int boardFrom;
        String seatStatString;
        int lastBoarded;
        String boardedSecondsString;
        try {
            sc = new Scanner(file);
            lastBoarded = Integer.parseInt(sc.nextLine());
            seatStatString = sc.nextLine();
            seatStat = getIntArrFromFile(seatStatString);
            boardFrom = Integer.parseInt(sc.nextLine());
            if(sc.hasNext()) {
                boardedSecondsString = sc.nextLine();
                boardedSeconds = getIntArrFromFile(boardedSecondsString);
                boardedSeatNumArr =new int[boardedSeconds.length];
            }
            boolean isValidStates = validateLoadedSeatStat(seatStat, lateComers, boardedSeatNumArr, waitintRoom, trainQueue);
            if(isValidStates) {
                this.seatStat = seatStat;
                this.waitingRoom = waitintRoom;
                this.boardFrom = boardFrom;
                this.lastBoarded =lastBoarded;
                this.lateComers = lateComers;
                this.trainQueue = trainQueue;
                setBoardedPassengersSecondsInQueue(boardedSeatNumArr, boardedSeconds);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private boolean validateLoadedSeatStat(int[] seatStat, List<Passenger> lateComers, int[] boardedSeatNumArr, Passenger[] waitingRoom, PassengerQueue trainQueue) throws Exception {
        boolean isValid = true;
        for(int i = 0, j = 0; i < seatStat.length; i++) {
            int stat = seatStat[i];
            System.out.println("i " + i);
            if(this.seatStat[i] != 0 && stat == 0 || this.seatStat[i] == 0 && stat != 0){
                System.out.println("Aborting! Mismatch with currently booked passengers detected...");
                isValid= false;
                break;
            }
            if(stat == 3) {
                boardedSeatNumArr[j] = i+1;
                j++;
            }
            else if(stat == 2) {
                trainQueue.enqueue(BOOKED_PASSENGERS[i]);
            } else if(stat == 1) {
                waitingRoom[i] = BOOKED_PASSENGERS[i];
            }
            if(i+1 <= boardFrom) {
                lateComers.add(BOOKED_PASSENGERS[i]);
            }
        }
        return isValid;
    }

    public void runSimulation() {
        ObservableList<Passenger> boardedPassengers = FXCollections.observableArrayList();
        int queueLen = trainQueue.getSize();
        if(queueLen > 0) {
            Passenger boardedPassenger;
            trainQueue.bubbleSortQueue();
            int secondsInQueue = getSecondsInQueue();
            int minSecondsInQueue = secondsInQueue;
            for (int i = 0; i < queueLen; i++) {
                boardedPassenger = trainQueue.dequeue();
                BOOKED_PASSENGERS[boardedPassenger.getSeatNum() - 1].setSecondsInQueue(secondsInQueue);
                seatStat[boardedPassenger.getSeatNum() - 1] = 3;
                secondsInQueue += getSecondsInQueue();
                boardedPassenger.display();
                boardedPassengers.add(boardedPassenger);
            }
            trainQueue.setMaxStayInQueue(secondsInQueue);
            lastBoarded = boardFrom;
            System.out.println(lastBoarded);
            float avgSecondsInQueue = secondsInQueue / (float) queueLen;
            float[] data = {queueLen, minSecondsInQueue, secondsInQueue, avgSecondsInQueue};
            System.out.println("----------------------Summary------------------------");
            System.out.println("Queue Length: " + queueLen);
            System.out.println("Min Stay: " + minSecondsInQueue);
            System.out.println("Max Stay: " + trainQueue.getMaxStayInQueue());
            System.out.println("Average Stay: " + avgSecondsInQueue);
            writeReportToFile("data/simulation-detail/", boardedPassengers, data);
            makeSimulationDetGUI(boardedPassengers, data);

        } else {
            System.out.println("No one in the queue!");
        }
    }

    public void visualize() {
        System.out.println("boardFrom " + boardFrom);
        System.out.println("lastBoarded " + lastBoarded);
        System.out.println("trainQ " + trainQueue.getSize());

        //Table Populating
        ObservableList<Passenger> passengersInQueue = getSeatsInTrainQueue();
        TableView<Passenger> trainQTable = makePassengerDetailTable(passengersInQueue, "Train Queue Is Empty", false);
        GridPane.setConstraints(trainQTable, 0, 2);

        HBox redInfo = makeTableRowColorInfo("#ffa485", "not-arrived");
        GridPane.setConstraints(redInfo, 0, 1);

        Pane captionQueueTable = makeTableCaption("Train Queue");
        GridPane.setConstraints(captionQueueTable, 0, 0);

        trainQTable.setRowFactory(tr -> new TableRow<Passenger>(){
            @Override
            public void updateItem(Passenger p, boolean empty) {
                super.updateItem(p, empty);
                if(p == null) {
                    setStyle("");
                } else if(seatStat[p.getSeatNum()-1] == -1) {
                    setStyle("-fx-background-color: #ffa485");
                } else {
                    setStyle("");
                }
            }
        });
//        trainQTable.sort();
        ObservableList<Passenger> allPassengers = getPassengersInWaitingRoom();
        TableView<Passenger> waitingRoomTable = makePassengerDetailTable(allPassengers, "No one in waiting room", false);
        GridPane.setConstraints(waitingRoomTable, 1, 2);

        HBox yelloInfo = makeTableRowColorInfo("#fff5ad", "late-arrival");
        GridPane.setConstraints(yelloInfo, 1, 1);

        Pane captionWRTable = makeTableCaption("Waiting Room");
        GridPane.setConstraints(captionWRTable, 1, 0);

        waitingRoomTable.setRowFactory(tr -> new TableRow<Passenger>(){
            @Override
            public void updateItem(Passenger p, boolean empty) {
                super.updateItem(p, empty);
                if(p == null) {
                    setStyle("");
                } else if (p.getSeatNum() <= boardFrom) {
                    setStyle("-fx-background-color: #fff5ad;");
                } else {
                    setStyle("");
                }
            }
        });

        ObservableList<Passenger> boarded = getBoardedPassengers();
        TableView<Passenger> boardePassengersTable = makePassengerDetailTable(boarded, "Train Queue Is Empty", true);
        GridPane.setConstraints(boardePassengersTable, 2, 2);

        Pane captionBoardedTable = makeTableCaption("Boarded Passengers");
        GridPane.setConstraints(captionBoardedTable, 2, 0);

        Button quit = new Button("Quit");
        GridPane.setConstraints(quit, 2, 4);
        GridPane.setHalignment(quit, HPos.RIGHT);
        quit.setMinWidth(70);
        quit.getStyleClass().add("quit");

        GridPane gp = new GridPane();
        gp.setHgap(20);
        gp.setVgap(10);
        GridPane.setFillHeight(trainQTable, false);
        gp.getChildren().addAll(quit, trainQTable, captionQueueTable, redInfo, waitingRoomTable, captionWRTable, yelloInfo, boardePassengersTable, captionBoardedTable);
        gp.setAlignment(Pos.CENTER);
//        gp.setGridLinesVisible(true);
//        gp.getColumnConstraints().add(new ColumnConstraints(100)); // column 0 is 100 wide

        Stage st = new Stage();
        st.setTitle("Visualize train queue and waiting room");
        quit.setOnAction(event -> {
            st.close();
        });
        popGui(st, gp, 1200, 600);
    }

    private int getSecondsInQueue() {
        Random r = new Random();
        int s1 = r.nextInt(6) + 1;
        int s2 = r.nextInt(6) + 1;
        int s3 = r.nextInt(6) + 1;
        int totalSeconds = s1 + s2 + s3;
        return totalSeconds;
    }
    private int[] getIntArrFromFile(String fileIntList) {
        List<Integer> intList = new ArrayList<>();
        int preSepI = -1;
        int sepI = fileIntList.indexOf(",");
        int i = 0;
        while(sepI != -1) {
            System.out.println("i " + i);
            int num = Integer.parseInt(fileIntList.substring(preSepI + 1, sepI));
            intList.add(num);
            preSepI = sepI;
            System.out.println("sepIB " + sepI);
            sepI = fileIntList.indexOf(",", sepI + 1);
            System.out.println("sepIA " + sepI);
            i++;
        }
        int[] intArr = new int[intList.size()];
        for(int j = 0; j < intList.size(); j++)  {
            intArr[j] = intList.get(j);
        }
        return intArr;
    }
    private void setBoardedPassengersSecondsInQueue(int[] seatArr, int[] seconds) {
        if(seatArr != null) {
            for (int i = 0; i < seatArr.length; i++) {
                BOOKED_PASSENGERS[seatArr[i] - 1].setSecondsInQueue(seconds[i]);
            }
        }
    }
    private void makeSimulationDetGUI(ObservableList<Passenger> passengers, float[] data) {
        TableView<Passenger> boardedPassengersTable = makePassengerDetailTable(passengers, "No one boarded", true);
        GridPane.setConstraints(boardedPassengersTable, 0, 1);

        Pane captionBoardedTable = makeTableCaption("Boarded Passengers(from recent queue)");
        GridPane.setConstraints(captionBoardedTable, 0, 0);

        Text summaryTitle = new Text("Queue Summary");
        GridPane.setConstraints(summaryTitle, 0, 0);
        GridPane.setMargin(summaryTitle, new Insets(25, 0, 25, 20));
        summaryTitle.setFill(Color.valueOf("#333"));
        summaryTitle.setUnderline(true);
        summaryTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 18));


        Text queueLen = makeSimulationSumDescription("Queue Len: ",(int) data[0]);
        Text minSec = makeSimulationSumDescription("Min Seconds in Queue: ",(int) data[1]);
        Text maxSec = makeSimulationSumDescription("Max Seconds in Queue: ",(int) data[2]);
        Text avgStay = makeSimulationSumDescription("Average Stay: ", data[3]);

        GridPane.setConstraints(queueLen,0, 1);
        GridPane.setConstraints(minSec,0, 2);
        GridPane.setConstraints(maxSec,0, 4);
        GridPane.setConstraints(avgStay,0, 5);

        GridPane sumBox = new GridPane();
        sumBox.setStyle("-fx-background-color: #fff");
        sumBox.setAlignment(Pos.CENTER);
        GridPane.setConstraints(sumBox, 1, 1);
        sumBox.getChildren().addAll(summaryTitle, queueLen, minSec, maxSec, avgStay);

        BarChart<String, Number> bc = makeBarChart(passengers);
        GridPane.setConstraints(bc, 2, 1);
        GridPane.setMargin(bc, new Insets(0, 0, 0, 20));


        Button quit = new Button("Quit");
        GridPane.setConstraints(quit, 2, 3);
        GridPane.setHalignment(quit, HPos.RIGHT);
        quit.setMinWidth(70);
        quit.getStyleClass().add("quit");

        GridPane gp = new GridPane();
        gp.setHgap(20);
        gp.setVgap(10);
//        for(int i = 1; i < 4; i++) {
//            gp.getRowConstraints().add(new RowConstraints(100));
//        }
        GridPane.setFillHeight(boardedPassengersTable, false);
        gp.getChildren().addAll(quit, boardedPassengersTable, captionBoardedTable, sumBox, bc);
        gp.setAlignment(Pos.CENTER);

        Stage st = new Stage();
        st.setTitle("Simulation report");
        quit.setOnAction(event -> {
            st.close();
        });
        popGui(st, gp, 1200, 600);
    }
    private Text makeSimulationSumDescription(String des, Number data) {
        Text description;
        if(data instanceof Float) {
            description = new Text(des + String.format("%.2f", data));
        } else {
            description = new Text(des + data);
        }
        GridPane.setMargin(description, new Insets(0, 0, 15, 20));
        description.setStyle("-fx-text-fill: #2c7d39");
        description.setFill(Color.valueOf("#2c7d39"));
        description.setFont(Font.font("Roboto", FontWeight.BOLD, 16));

        return description;
    }
    private BarChart<String, Number> makeBarChart(ObservableList<Passenger> passengers) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Simulation Summary");
//        bc.setCategoryGap(20);
//        bc.setBarGap(20);

        yAxis.setAnimated(true);
        xAxis.setLabel("Passenger");
        yAxis.setLabel("seconds");

        XYChart.Series series = new XYChart.Series();
        series.setName("Time in seconds taken by a passenger in his turn");
        series.getData().add(new XYChart.Data(passengers.get(0).getFullName() + " - " + passengers.get(0).getSeatNum(), passengers.get(0).getSecondsInQueue()));
        for(int i = 1; i < passengers.size(); i++) {
            series.getData().add(new XYChart.Data(passengers.get(i).getFullName() + " - " + passengers.get(i).getSeatNum(), passengers.get(i).getSecondsInQueue() - passengers.get(i-1).getSecondsInQueue()));
        }
        bc.getData().addAll(series);
        return bc;
    }
    private void writeReportToFile(String path, ObservableList<Passenger> boardedPassengers, float[] details) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        path += "simulation-report from " + dtf.format(now) + ".xml";

        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            Element root = dom.createElement("queue_simualtion");

            //SummaryTag
            Element summaryRootElement = dom.createElement("summary");

            e = dom.createElement("queue_length");
            e.appendChild(dom.createTextNode(Integer.toString((int) details[0])));
            summaryRootElement.appendChild(e);

            e = dom.createElement("minimum_seconds");
            e.appendChild(dom.createTextNode(Integer.toString((int) details[1])));
            summaryRootElement.appendChild(e);

            e = dom.createElement("maximum_seconds");
            e.appendChild(dom.createTextNode(Integer.toString((int) details[2])));
            summaryRootElement.appendChild(e);

            e = dom.createElement("average_seconds");
            e.appendChild(dom.createTextNode(Integer.toString((int) details[3])));
            summaryRootElement.appendChild(e);


            Element passengersRootElement = dom.createElement("passengers");
            for(Passenger p : boardedPassengers) {
                Element passenger = dom.createElement("passenger");

                Element name = dom.createElement("name");
                name.appendChild(dom.createTextNode(p.getFullName()));

                Element seat = dom.createElement("seat");
                seat.appendChild(dom.createTextNode(Integer.toString(p.getSeatNum())));

                Element startStation = dom.createElement("start_station");
                startStation.appendChild(dom.createTextNode((p.getStartStation())));

                Element endStation = dom.createElement("end_station");
                endStation.appendChild(dom.createTextNode((p.getEndStation())));

                Element secondsInQueue = dom.createElement("second");
                secondsInQueue.appendChild(dom.createTextNode(Integer.toString(p.getSecondsInQueue())));

                passenger.appendChild(name);passenger.appendChild(seat);passenger.appendChild(startStation);passenger.appendChild(endStation);
                passenger.appendChild(secondsInQueue);

                passengersRootElement.appendChild(passenger);
            }
            dom.appendChild(root);
            root.appendChild(passengersRootElement); root.appendChild(summaryRootElement);
            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//                tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(path)));

            } catch (TransformerException te) {
                te.printStackTrace();
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }




    private ObservableList<Passenger> getSeatsInTrainQueue() {
        ObservableList<Passenger> passengers = FXCollections.observableArrayList();
        Passenger[] passengersInQueue = trainQueue.getQueue();
//        int lastBoardedIndex = lastBoarded - 1;
        for(Passenger p : passengersInQueue) {
            passengers.add(p);
        }
        for(int i = lastBoarded; i < boardFrom; i++) {
            if(seatStat[i] == -1) {
                passengers.add(BOOKED_PASSENGERS[i]);
            } else {
                System.out.println("yes");
            }
        }
        bubbleSortArr(passengers);
        return passengers;
    }
    private ObservableList<Passenger> getPassengersInWaitingRoom() {
        ObservableList<Passenger> passengers = FXCollections.observableArrayList();
        if(boardFrom > -1) {
            for(Passenger p: waitingRoom) {
                if(p != null) {
                    passengers.add(p);
                }
            }
        }
        return passengers;
    }
    private ObservableList<Passenger> getBoardedPassengers() {
        ObservableList<Passenger> passengers = FXCollections.observableArrayList();
        for(int i=0; i<seatStat.length; i++) {
            if(seatStat[i] == 3) {
                passengers.add(BOOKED_PASSENGERS[i]);
            }
        }
        return passengers;
    }
    private TableView<Passenger> makePassengerDetailTable(ObservableList<Passenger> passengers, String placeHolder, boolean boarded) {
        //Table Populating
        TableView<Passenger> tb = new TableView<>();

        TableColumn<Passenger, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Passenger, Integer> seatColumn = new TableColumn<>("Seat");
        seatColumn.setCellValueFactory(new PropertyValueFactory<>("seatNum"));

        TableColumn<Passenger, String> startStationColumn = new TableColumn<>("Start");
        startStationColumn.setCellValueFactory(new PropertyValueFactory<>("startStation"));


        TableColumn<Passenger, String> endStationColumn = new TableColumn<>("End");
        endStationColumn.setCellValueFactory(new PropertyValueFactory<>("endStation"));


        TableColumn<Passenger, String> journeyColumn = new TableColumn<>("Journey");
        journeyColumn.getColumns().addAll(startStationColumn, endStationColumn);

        TableColumn<Passenger, Integer> secondsInQueueColumn = new TableColumn<>("Sec in queue");
        secondsInQueueColumn.setCellValueFactory(new PropertyValueFactory<>("secondsInQueue"));

        tb.setItems(passengers);
        Label lblPH = new Label(placeHolder);
        tb.setPlaceholder(lblPH);

        if(boarded) {
            tb.getColumns().addAll(nameColumn, seatColumn, journeyColumn, secondsInQueueColumn);
            return tb;
        }
        tb.getColumns().addAll(nameColumn, seatColumn, journeyColumn);
        return tb;

//        tb.setSelectionModel(null);//throws error when sorting

    }
    private Pane makeTableCaption(String caption) {
        HBox captionBox = new HBox();
        captionBox.setAlignment(Pos.CENTER_LEFT);
        Text title = new Text(caption);
        HBox.setMargin(title, new Insets(0, 0, 5, 0));
//        captionBox.setStyle("-fx-background-color: linear-gradient(to bottom, rgb(222,222,222) 16%, rgb(232,232,232) 79%); -fx-border-width: 1px 1px 0px 1px; -fx-border-color: #c3c3c3");
        title.setFill(Color.valueOf("#2e4a7d"));
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
        captionBox.getChildren().add(title);

        return captionBox;
    }
    private HBox makeTableRowColorInfo(String colorCode, String info) {
        Label color = new Label();
        color.setMinSize(15, 15);
        color.setMaxSize(15, 15);
        color.setStyle("-fx-background-color: " + colorCode);
        GridPane.setHalignment(color, HPos.LEFT);
        HBox.setMargin(color, new Insets(0, 0, 3, 0));
//        GridPane.setConstraints(yello, 1, 1);

        Label infoLabel = new Label(info);
        GridPane.setHalignment(infoLabel, HPos.LEFT);
        HBox.setMargin(infoLabel, new Insets(0, 0, 3,6));
//        GridPane.setConstraints(yelloInfo, 1, 1);

        HBox colorAndInfo = new HBox();
        colorAndInfo.getChildren().addAll(color, infoLabel);
        return colorAndInfo;
    }

    //helper methods
    private boolean confirm(String message) {
        Scanner sc = new Scanner(System.in);
        System.out.println(message);
        String isConfirmed = sc.nextLine();
        if(isConfirmed.equalsIgnoreCase("y") || isConfirmed.equalsIgnoreCase("yes")) {
            return true;
        }
        System.out.println("Abored!");
        return false;

    }
    private void setActionOnAdd(List<Integer> toBeReserved, Stage stage) {
        boolean isSeatSelected = !toBeReserved.isEmpty();
        if(isSeatSelected){
            for(int seatNum : toBeReserved) {
                if(seatNum-1 < boardFrom) {
                    lateComers.add(BOOKED_PASSENGERS[seatNum-1]);
                    waitingRoom[seatNum-1] = BOOKED_PASSENGERS[seatNum-1];
                } else {
                    waitingRoom[seatNum-1] = BOOKED_PASSENGERS[seatNum-1];
                }
                if(boardFrom < 0) {
                    boardFrom = 0;
                }
                seatStat[seatNum-1] = 1;
            }
            System.out.println(Arrays.toString(waitingRoom));
            stage.close();
        } else {
            throwErrorAlert("No seats are selected to book", "You must select atleast one \n seat to book");
        }
    }
    private static void setSeatButtonAction(Button btn, List<Integer> clicked) {
        if (!clicked.contains(Integer.parseInt(btn.getText()))) {
            clicked.add(Integer.parseInt(btn.getText()));
            btn.getStyleClass().clear();
            btn.getStyleClass().addAll("button", "clicked");
        } else {
            btn.getStyleClass().clear();
            btn.getStyleClass().add("button");
            clicked.remove(clicked.indexOf(Integer.parseInt(btn.getText())));
        }
    }
    private static Button makeSeatButton(int i, int c, int r) {
        Button seatBtn = new Button("" + i);
        seatBtn.setMinSize(50, 50);
        seatBtn.setStyle("-fx-font-size: 14px; -fx-font-family: 'Clear Sans';");
        GridPane.setConstraints(seatBtn, c, r);
        return seatBtn;
    }
    private static <T extends Pane> void popGui(Stage stage, T pane, double width, double height) {
        Scene scene = new Scene(pane, width, height);
        scene.getStylesheets().add("train-station.css");
        stage.setScene(scene);
        stage.showAndWait();
    }
    private static void throwErrorAlert(String heading, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.INFORMATION);
        errorAlert.setHeaderText(heading);
        errorAlert.setContentText(message);
        errorAlert.show();
    }
    private static void addToPassengerQueue(Passenger p, PassengerQueue queue, int totalAdded) {
        try {
            queue.enqueue(p);
        } catch (Exception e) {
        }
    }

    private int addLateComersToQueue(int passengersToQueue) {
        int totalAdded = 0;
        try {
            int i = 0;
            while (!lateComers.isEmpty() ) {
                trainQueue.enqueue(lateComers.get(i));
                seatStat[lateComers.get(i).getSeatNum() - 1] = 2;
                waitingRoom[lateComers.get(i).getSeatNum()-1] = null;
                lateComers.remove(i);
                totalAdded += 1;
                if (totalAdded >= passengersToQueue) {
//                    System.out.println(totalAdded + " passengers were added successfully");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(totalAdded + " Passengers added, since the queue is full");
            totalAdded = passengersToQueue;
//            e.printStackTrace();
        }
        return totalAdded;
    }
    private int addToQueueFromWaitingRoom(int totalAdded, int passengersToQueue) {
        int remainingToAdd = passengersToQueue - totalAdded;
        int boardFrom = this.boardFrom;
//        System.out.println(alreadyAdded);
//        System.out.println(remainingToAdd);
        try {
            for (int i = boardFrom; i < boardFrom + remainingToAdd; i++) {
//                System.out.println("iteration " + i);
//                System.out.println("boardFrom " + (boardFrom));
//                System.out.println("alreadyAdded " + alreadyAdded);
                if (this.boardFrom == waitingRoom.length) {
//                    System.out.println(alreadyAdded + "  passengers were added successfully");
                    break;
                }
//                System.out.println(waitingRoom[boardFrom]);
                if (waitingRoom[i] != null) {
                    trainQueue.enqueue(waitingRoom[i]);
                    System.out.println("boardFrom: " + boardFrom + " seatNum - 1: " + (waitingRoom[i].getSeatNum() - 1));
                    seatStat[i] = 2;
                    waitingRoom[i] = null;
                    totalAdded += 1;
                }
                this.boardFrom += 1;
            }
//            if(boardFrom != waitingRoom.length) {
//                System.out.println(alreadyAdded + " passengers were added successfully");
//            }
//            if(totalAdded < passengersToQueue) {
//                System.out.println(totalAdded + " Passengers were added since there's no passengers left to add");
//            }
        } catch (Exception e) {
            System.out.println(totalAdded + " Passengers added, since the queue is full");
            e.printStackTrace();
        }
        return totalAdded;
    }

    private int getSeatNum() {
        int seatNum = 0;
        Scanner sc = new Scanner(System.in);
        int retry = 0;
        int maxRetry = 2;
        while(retry <= maxRetry) {
            try {
                System.out.println("Enter seat number of the passenger: ");
                seatNum = sc.nextInt();
                if(seatNum > 42 || seatNum < 1) {
                    retry += 1;
                    System.out.println("Invalid seat number(1-42)");
//                    sc.next();
                } else {
                    return seatNum;
                }
            } catch (InputMismatchException e) {
                retry += 1;
                System.out.println("Only integers allowed");
                sc.next();
            }
        }
        return 0;
    }
    private <T extends Comparable<T>> List<T> bubbleSortArr(List<T> arr) {
        boolean isNoSorted = true;
        int inOrder = 0;
        while (isNoSorted) {
            boolean isSwapped = false;
            for(int i = 0; i < arr.size()-inOrder-1; i++) {
                Integer currentNum = i;
                Integer nextNum = i+1;
                if(arr.get(currentNum).compareTo(arr.get(nextNum)) > 0) {
                    Collections.swap(arr, currentNum, nextNum);
                    isSwapped = true;
                }
            }
            inOrder += 1;
            isNoSorted = isSwapped;
        }
        return arr;
    }


}
