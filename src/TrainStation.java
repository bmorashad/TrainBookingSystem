import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.*;

public class TrainStation extends Application {
    public final String STATION = "Colombo";
    private int boardFrom = -1;
    // 1 - arrived, -1 - not-arrived, 0 - not-booked
    private final int[] seatStat = new int[42];
    public final Passenger[] BOOKED_PASSENGERS = getBookedPassengers("../CWTEST@latest/data/cTob_booking_detail.txt", LocalDate.parse("2020-04-17"));

    PassengerQueue trainQueue = new PassengerQueue(21);
    Passenger[] waitingRoom = new Passenger[42];
    List<Passenger> lateComers = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        boolean exit = false;
        while (!exit){
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter option: ");
            String option = sc.nextLine();
//            String option = "a";
            switch (option.toLowerCase()) {
                case "w":
                    addArrivedPassengers();
                    break;
                case "a":
                    addToTrainQueue();
                    break;
                case "t":
                    makeTable();
                    break;
                case "q":
                    exit = true;
            }
        }
    }
    // "../CWTEST@latest/data/cTob_booking_detail.txt"
    public Passenger[] getBookedPassengers(String pathToFile, LocalDate date) {
        Passenger[] bookedPassengers = new Passenger[42];
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
                    bookedPassengers[seatNum-1] = passenger;
                    seatStat[seatNum-1] = -1;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }
        System.out.println(Arrays.toString(bookedPassengers));
        return bookedPassengers;
    }

    public void addArrivedPassengers() {
        System.out.println("tQ size: " + trainQueue.getSize());
        Stage stage = new Stage();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);
        grid.setHgap(10);

        Label lbl = new Label("Add arrived passengers to waiting room");
        lbl.setTextFill(Color.web("blue"));
        lbl.setStyle("-fx-font-size: 18px");
        GridPane.setConstraints(lbl, 0, 0);
        GridPane.setColumnSpan(lbl,6);
        grid.getChildren().add(lbl);

        Button add = new Button("Add");
        GridPane.setConstraints(add, 5, 9);
        grid.getChildren().add(add);

        Button quit = new Button("Quit");
        GridPane.setConstraints(quit, 6, 9);
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
        GridPane.setConstraints(greyInfo, 0, 2);
        GridPane.setColumnSpan(greyInfo, 2);
        grid.getChildren().add(greyInfo);

        Label green = new Label();
        green.setMinSize(15, 15);
        green.setMaxSize(15, 15);
        green.setStyle("-fx-background-color: #07b100");
        GridPane.setHalignment(green, HPos.LEFT);
//        GridPane.setColumnSpan(green, 2);
        GridPane.setConstraints(green, 6, 1);
        grid.getChildren().add(green);

        Label greenInfo = new Label("arrived");
        GridPane.setHalignment(greenInfo, HPos.LEFT);
        GridPane.setConstraints(greenInfo, 6, 2);
//        GridPane.setColumnSpan(greenInfo, 2);
        grid.getChildren().add(greenInfo);

        final List<Integer> toBeReserved = new ArrayList<>();
        for(int i = 0, c = 0, r = 2; i < BOOKED_PASSENGERS.length; i++) {
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
                seatBtn.setStyle("-fx-background-color: #07b100; -fx-text-fill: #fff");
            } else {
                seatBtn.setDisable(true);
                seatBtn.setStyle("-fx-background-color: #b1b1b1; -fx-text-fill: #333");
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
        System.out.println("boardFrom " + boardFrom);
        Random rd = new Random();
        int passengersToQueue = rd.nextInt(6) + 1;
        System.out.println(passengersToQueue + " Passengers can be added");
        boolean isConfirmed = confirm("Are you sure you want to add " + passengersToQueue + " passengers to queue");
        int totalAdded = 0;
        if(boardFrom != -1 || !lateComers.isEmpty()) {
            totalAdded = addLateComersToQueue(passengersToQueue);
//            int currentlyAdded = totalAdded;
            if (totalAdded < passengersToQueue && boardFrom < waitingRoom.length) {
                totalAdded = addToQueueFromWaitingRoom(totalAdded, passengersToQueue);
            }
            if(totalAdded == 0 && !trainQueue.isFull()) {
                System.out.println("No passengers were added");
            }
        } else if(boardFrom == waitingRoom.length-1){
            System.out.println("There are no passengers left to add");
        } else {
            System.out.println("There are no passengers in the waiting room to add");
        }
//        System.out.println(totalAdded);
    }

    public void showQueue() {
    }
    public void makeTable() {
        Passenger[] passengersInQueue = trainQueue.getQueue();
        System.out.println(Arrays.toString(passengersInQueue));
        bubbleSortArr(passengersInQueue);


        TableView<Passenger> tb = new TableView<>();

        TableColumn<Passenger, String> nameColumn = new TableColumn<>("Name");
//        nameColumn.setMinWidth(200);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
//        nameColumn.setSortable(false);

        TableColumn<Passenger, Integer> seatColumn = new TableColumn<>("Seat");
//        seatColumn.setMinWidth(200);
        seatColumn.setCellValueFactory(new PropertyValueFactory<>("seatNum"));
//        seatColumn.setSortable(false);

        TableColumn<Passenger, String> startStationColumn = new TableColumn<>("Start");
//        startStationColumn.setMinWidth(200);
        startStationColumn.setCellValueFactory(new PropertyValueFactory<>("startStation"));
//        startStationColumn.setSortable(false);

        TableColumn<Passenger, String> endStationColumn = new TableColumn<>("End");
//        endStationColumn.setMinWidth(200);
        endStationColumn.setCellValueFactory(new PropertyValueFactory<>("endStation"));
//        endStationColumn.setSortable(false);

        ObservableList<Passenger> passengers = FXCollections.observableArrayList();
        for(int i = 0; i < passengersInQueue.length - 1; i++) {
            passengers.add(passengersInQueue[i]);
            int currentSeat = passengersInQueue[i].getSeatNum();
            int nextSeat = passengersInQueue[i+1].getSeatNum();
            int upTo = nextSeat - currentSeat;
            for(int j = 1; j < upTo; j++) {
                if(seatStat[passengersInQueue[i].getSeatNum()+j-1] == -1) {
                    passengers.add(BOOKED_PASSENGERS[passengersInQueue[i].getSeatNum()+j - 1]);
                }
            }
        }
        if(passengersInQueue.length != 0) {
            passengers.add(passengersInQueue[passengersInQueue.length - 1]);
        }
        if(boardFrom-1 != passengersInQueue[passengersInQueue.length - 1].getSeatNum()) {
            int lastBoarded = passengersInQueue[passengersInQueue.length-1].getSeatNum();
            int lastInBoardingGrp = boardFrom;
            int difference = lastInBoardingGrp - lastBoarded;
            for(int i = 1; i < difference; i++) {
                if(seatStat[lastBoarded+i-1] == -1) {
                    passengers.add(BOOKED_PASSENGERS[lastBoarded+i-1]);
                }
            }
        }

        tb.setItems(passengers);
        tb.getColumns().addAll(nameColumn, seatColumn, startStationColumn, endStationColumn);

        tb.setRowFactory(tr -> new TableRow<Passenger>(){
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

        VBox vBox = new VBox();
        vBox.getChildren().add(tb);

        Scene scene = new Scene(vBox, 600, 600);
        Stage st = new Stage();
        st.setScene(scene);
        st.showAndWait();
    }




    //helper methods
    private boolean confirm(String message) {
        Scanner sc = new Scanner(System.in);
        System.out.println(message);
        String isConfirmed = sc.nextLine();
        if(isConfirmed.equalsIgnoreCase("y") || isConfirmed.equalsIgnoreCase("yes")) {
            return true;
        }
        return false;

    }
    private void setActionOnAdd(List<Integer> toBeReserved, Stage stage) {
        boolean isSeatSelected = !toBeReserved.isEmpty();
        if(isSeatSelected){
            for(int seatNum : toBeReserved) {
                if(seatNum-1 < boardFrom) {
                    lateComers.add(BOOKED_PASSENGERS[seatNum-1]);
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
        if (!clicked.contains("" + btn.getText())) {
            clicked.add(Integer.parseInt(btn.getText()));
            btn.setStyle("-fx-background-color: #5cff9d");
        } else {
            btn.setStyle("");
            clicked.remove(clicked.indexOf("" + btn.getText()));
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
            if (!lateComers.isEmpty()) {
                int i = 0;
                while (!lateComers.isEmpty() ) {
                    trainQueue.enqueue(lateComers.get(i));
                    seatStat[lateComers.get(i).getSeatNum() - 1] = 2;
                    lateComers.remove(i);
                    totalAdded += 1;
                    if (totalAdded >= passengersToQueue) {
                        System.out.println(totalAdded + " passengers were added successfully");
                        break;
                    }
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
        int alreadyAdded = totalAdded;
//        System.out.println(alreadyAdded);
//        System.out.println(remainingToAdd);
        try {
            for (int i = 0; i < remainingToAdd; i++) {
                System.out.println("iteration " + i);
                System.out.println("boardFrom " + (boardFrom));
//                System.out.println("alreadyAdded " + alreadyAdded);
                if (boardFrom == waitingRoom.length) {
                    System.out.println(alreadyAdded + "  passengers were added successfully");
                    break;
                }
                System.out.println(waitingRoom[boardFrom]);
                if (waitingRoom[boardFrom] != null) {
                    trainQueue.enqueue(waitingRoom[boardFrom]);
                    seatStat[waitingRoom[boardFrom].getSeatNum() - 1] = 2;
                    waitingRoom[boardFrom] = null;
                    alreadyAdded += 1;
                }
                boardFrom += 1;
                totalAdded += 1;
            }
            if(boardFrom != waitingRoom.length) {
                System.out.println(alreadyAdded + " passengers were added successfully");
            }
//            if(totalAdded < passengersToQueue) {
//                System.out.println(totalAdded + " Passengers were added since there's no passengers left to add");
//            }
        } catch (Exception e) {
            System.out.println(totalAdded + " Passengers added, since the queue is full");
            e.printStackTrace();
        }
        return alreadyAdded;
    }

    public <T extends Comparable<T>> T[] bubbleSortArr(T[] arr) {
        boolean isNoSorted = true;
        int inOrder = 0;
        while (isNoSorted) {
            boolean isSwapped = false;
            for(int i = 0; i < arr.length-inOrder-1; i++) {
                Integer currentNum = i;
                Integer nextNum = i+1;
                if(arr[currentNum].compareTo(arr[nextNum]) > 0) {
                    T temp = arr[currentNum];
                    arr[currentNum] = arr[nextNum];
                    arr[nextNum] = temp;
                    isSwapped = true;
                }
            }
            inOrder += 1;
            isNoSorted = isSwapped;
        }
        return arr;
    }


}
