import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SplitScreen extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        makeGUI();
    }
    public void makeGUI() {
        SplitPane sp = new SplitPane();
        GridPane gp1 = new GridPane();
        GridPane gp2 = new GridPane();
//        gp1.setAlignment(Pos.CENTER);
//        gp2.setAlignment(Pos.CENTER);
        Label red = new Label();
        red.setMaxSize(10, 10);
        red.setMinSize(10, 10);
        red.setStyle("-fx-background-color: red");
        Label lbl1 = new Label("not arrived");
        GridPane.setConstraints(red, 1, 0);
        GridPane.setConstraints(lbl1, 2, 0);
        gp1.setHgap(5);
//        gp1.setGridLinesVisible(true);
        gp1.getChildren().addAll(red, lbl1);
        gp2.getChildren().add(new Button("Hello there!"));
        VBox leftControl  = new VBox();
        leftControl.setAlignment(Pos.CENTER);
        leftControl.getChildren().add(gp1);
        VBox rightControl = new VBox();
        rightControl.getChildren().add(gp2);
        sp.getItems().addAll(leftControl, rightControl);
        Stage st = new Stage();
        st.setScene(new Scene(sp, 500, 500));
        st.show();
    }
    public void makeTable() {
        TableView<Passenger> tb = new TableView<>();

//        ObservableList<Passenger> = new
    }
}
