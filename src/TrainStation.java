import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.*;

public class TrainStation extends Application {
    private final String STATION = "Colombo";
    private int boardFrom = -1;
    private int lastBoarded = 0;
    // 1 - arrived, -1 - not-arrived, 0 - not-booked
    private final int[] seatStat = new int[42];
    public final Passenger[] BOOKED_PASSENGERS = getBookedPassengers("../CWTEST@latest/data/cTob_booking_detail.txt", LocalDate.parse("2020-04-17"));

    private PassengerQueue trainQueue = new PassengerQueue(21);
    private Passenger[] waitingRoom = new Passenger[42];
    private List<Passenger> lateComers = new ArrayList<>();

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
//            String option = "t";
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

    public void deletePassengerFromQueue() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter name of the passenger: ");
        String name = sc.nextLine();
        System.out.println("Enter seat number of the passenger: ");
        int seatNum = sc.nextInt();
        Passenger deletedPassenger = trainQueue.delete(name, seatNum);
        if(deletedPassenger != null) {
            seatStat[deletedPassenger.getSeatNum()-1] = -1;
            deletedPassenger.display();
        } else {
            System.out.println("No such passengers there!");
        }
    }

    public void runSimulation() {

    }


    public void showQueue() {
    }
    public void makeTable() {
        System.out.println(boardFrom);
        System.out.println("trainQ " + trainQueue.getSize());

        //Table Populating
        ObservableList<Passenger> passengersInQueue = getSeatsInTrainQueue();
        TableView<Passenger> trainQTable = makePassengerDetailTable(passengersInQueue, "Train Queue Is Empty");
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
        TableView<Passenger> waitingRoomTable = makePassengerDetailTable(allPassengers, "No one in waiting room");
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

        ObservableList<Passenger> boarded = getSeatsInTrainQueue();
        TableView<Passenger> boardePassengersTable = makePassengerDetailTable(passengersInQueue, "Train Queue Is Empty");
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
    private ObservableList<Passenger> getSeatsInTrainQueue() {
        ObservableList<Passenger> passengers = FXCollections.observableArrayList();
        int lastBoardedIndex = lastBoarded - 1;
        for(int i = 1; i <= boardFrom; i++) {
            if(seatStat[lastBoardedIndex+i] == 2 || seatStat[lastBoardedIndex+i] == -1) {
                passengers.add(BOOKED_PASSENGERS[lastBoardedIndex+i]);
            } else {
                System.out.println("yes");
            }
        }
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
    private TableView<Passenger> makePassengerDetailTable(ObservableList<Passenger> passengers, String placeHolder) {
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

        tb.setItems(passengers);
        tb.getColumns().addAll(nameColumn, seatColumn, journeyColumn);

        Label lblPH = new Label(placeHolder);
        tb.setPlaceholder(lblPH);
//        tb.setSelectionModel(null);//throws error when sorting

        return tb;
    }
    private Pane makeTableCaption(String caption) {
        Text title = new Text(caption);
        HBox captionBox = new HBox();
        captionBox.setAlignment(Pos.CENTER_LEFT);
//        captionBox.setStyle("-fx-background-color: linear-gradient(to bottom, rgb(222,222,222) 16%, rgb(232,232,232) 79%); -fx-border-width: 1px 1px 0px 1px; -fx-border-color: #c3c3c3");
        title.setFill(Color.valueOf("#2e4a7d"));
        title.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
        HBox.setMargin(title, new Insets(0, 0, 5, 0));
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
            if (!lateComers.isEmpty()) {
                int i = 0;
                while (!lateComers.isEmpty() ) {
                    trainQueue.enqueue(lateComers.get(i));
                    seatStat[lateComers.get(i).getSeatNum() - 1] = 2;
                    waitingRoom[lateComers.get(i).getSeatNum()-1] = null;
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
//                System.out.println("iteration " + i);
//                System.out.println("boardFrom " + (boardFrom));
//                System.out.println("alreadyAdded " + alreadyAdded);
                if (boardFrom == waitingRoom.length) {
                    System.out.println(alreadyAdded + "  passengers were added successfully");
                    break;
                }
//                System.out.println(waitingRoom[boardFrom]);
                if (waitingRoom[boardFrom] != null) {
                    trainQueue.enqueue(waitingRoom[boardFrom]);
                    System.out.println("boardFrom: " + boardFrom + " seatNum - 1: " + (waitingRoom[boardFrom].getSeatNum() - 1));
                    seatStat[boardFrom] = 2;
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
