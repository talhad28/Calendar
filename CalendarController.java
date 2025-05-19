import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.DateFormatSymbols;
import java.util.*;

public class CalendarController {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private ComboBox<String> combMonth;

    @FXML
    private ComboBox<Integer> combYear;

    private final String[] months = new DateFormatSymbols().getMonths(); //gets all months
    private final String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private Calendar cal = Calendar.getInstance();
    HashMap<String, List<String>> eventMap = new HashMap<>();

    public void initialize(){
        int curMonth = cal.get(Calendar.MONTH);
        int curYear = cal.get(Calendar.YEAR);

        combMonth.getItems().addAll(Arrays.asList(months).subList(0, 12));
        combMonth.setValue(months[curMonth]); //set to current month at comboBox

        for(int i= curYear - 100; i< curYear + 100; i++){ //show 100 past years and 100 next years
            combYear.getItems().add(i);
        }
        combYear.setValue(curYear); //set to current year at comboBox

        showCalendar(curMonth, curYear); //show current month calendar at startup

    }

    @FXML
    void updateCalendar(ActionEvent event) {
        int month = combMonth.getSelectionModel().getSelectedIndex();
        int year = combYear.getValue();
        showCalendar(month, year);

    }

    public void showCalendar(int month, int year){

        cal.set(year, month, 1);
        int firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK) - 1;

        calendarGrid.getChildren().clear();
        calendarGrid.getRowConstraints().clear(); // ← clear old constraints

        // Set header row height
        RowConstraints headerRow = new RowConstraints();
        headerRow.setPrefHeight(20);
        headerRow.setMinHeight(20);
        headerRow.setMaxHeight(20);
        calendarGrid.getRowConstraints().add(headerRow); // row 0

        // Add day headers
        for (int i = 0; i < 7; i++) {
            VBox dayCell = new VBox();
            dayCell.setAlignment(Pos.TOP_LEFT);
            dayCell.setPrefSize(80, 20);
            dayCell.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");

            Label dayNumber = new Label(String.valueOf(days[i]));
            dayNumber.setStyle("-fx-font-weight: bold;");
            dayCell.getChildren().add(dayNumber);
            calendarGrid.add(dayCell, i, 0);
        }

        // calculate how many rows you need for the dates
        int totalCells = 31 + firstDayOfMonth;
        int weeks = (int)Math.ceil(totalCells / 7.0);

        // Add fixed height constraints for each week row
        for (int i = 0; i < weeks; i++) {
            RowConstraints weekRow = new RowConstraints();
            weekRow.setPrefHeight(80);
            weekRow.setMinHeight(80);
            weekRow.setMaxHeight(80);
            calendarGrid.getRowConstraints().add(weekRow);
        }

        // Add day buttons
        for (int day = 0; day < 31; day++) {
            Button dayCell = new Button();
            dayCell.setAlignment(Pos.TOP_LEFT);
            dayCell.setPrefSize(80, 80);
            dayCell.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
            dayCell.setText(""+(day+1));

            //calculate the button position on grid
            int col = (day + firstDayOfMonth) % 7;
            int row = (day + firstDayOfMonth) / 7 + 1;


            int finalDay = day + 1; //used to send in button's action
            dayCell.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openDayDialog(finalDay, month, year);
                }
            });

            calendarGrid.add(dayCell, col, row);
        }


    }

    public void openDayDialog(int day, int month, int year){
        Stage dialog = new Stage();
        dialog.setTitle("Events of " + day + "/" + month+1 + "/" + year);
        dialog.initModality(Modality.APPLICATION_MODAL); //blocks interaction with other windows until closed

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        String date = day + "/" + month + "/" + year; //used as a key for the hashmap

        List<String> events = eventMap.get(date) == null ? new ArrayList<>() : eventMap.get(date); //check if exist in hashmap

        ListView<String> eventListView = new ListView<>();
        eventListView.getItems().addAll(events); //adds all previous events to list view

        TextField newEventField = new TextField();
        newEventField.setPromptText("New Event");

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> {
            String newEvent = newEventField.getText().trim(); // Get input text
            if (!newEvent.isEmpty()) {
                eventListView.getItems().add(newEvent); // Add to ListView
                eventMap.putIfAbsent(date, new ArrayList<>()); // Ensure date key exists
                eventMap.get(date).add(newEvent); // Add event to the map
                newEventField.clear(); // Clear the input field
            }
        });

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setOnAction(e -> {
            String selected = eventListView.getSelectionModel().getSelectedItem(); // Get selected item
            if (selected != null) {
                eventListView.getItems().remove(selected); // Remove from ListView
                eventMap.get(date).remove(selected); // Remove from the map
            }
        });

        HBox buttons = new HBox(10, addBtn, deleteBtn);
        layout.getChildren().addAll(new Label("Events:"), eventListView, newEventField, buttons);

        Scene scene = new Scene(layout, 500, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

}
