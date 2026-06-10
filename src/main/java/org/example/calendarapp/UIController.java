package org.example.calendarapp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.time.temporal.TemporalAdjusters;

public class UIController implements Initializable {

    // ---------- plus button / create task pane ----------
    @FXML private Button plusButton;
    @FXML private AnchorPane createNewTaskPane;
    @FXML private Button toDoExitButton;
    @FXML private Button createButton;

    // ---------- color picker ----------
    @FXML private Button selectColorButton;
    @FXML private Button selectColorExitButton;
    @FXML private Pane selectColorPane;
    @FXML private Button applyButton;
    @FXML private ToggleGroup colorToggle;

    // ---------- form fields ----------
    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private DatePicker datePicker1;

    //----- main date picker------
    @FXML private DatePicker datePicker;

    // ---------- spinners ----------
    @FXML private Spinner<Integer> hourSpinner1;
    @FXML private Spinner<Integer> minuteSpinner1;
    @FXML private Spinner<Integer> hourSpinner2;
    @FXML private Spinner<Integer> minuteSpinner2;
    @FXML private Label hourLabel1;
    @FXML private Label minuteLabel1;
    @FXML private Label hourLabel2;
    @FXML private Label minuteLabel2;
    @FXML private ToggleGroup time1;
    @FXML private ToggleGroup time2;

    // ---------- weekly grid ----------
    @FXML private AnchorPane weeklyGrid;

    // ---------- view task pane ----------
    @FXML private AnchorPane viewTaskPane;
    @FXML private Label viewTaskTitle;
    @FXML private Label viewTaskType;
    @FXML private Label viewTaskDescription;
    @FXML private Label viewTaskDate;
    @FXML private Label viewTaskTime;
    @FXML private Pane viewTaskColorBox;
    @FXML private Button completeButton;
    @FXML private Button eraseButton;
    @FXML private Button viewTaskExitButton;

    private Label currentTaskLabel = null;


    // ---------- spinner value factories ----------
    private SpinnerValueFactory<Integer> hourSpin1;
    private SpinnerValueFactory<Integer> minuteSpin1;
    private SpinnerValueFactory<Integer> hourSpin2;
    private SpinnerValueFactory<Integer> minuteSpin2;

    private int hourNum1, minuteNum1, hourNum2, minuteNum2;

    // stores the color the user picked (default blue)
    private String selectedColor = "#9FD5FF";

    // -------------------------------------------------------
    //  PLUS BUTTON
    // -------------------------------------------------------
    @FXML
    protected void onPlusButtonClick() {
        createNewTaskPane.setVisible(true);
        createNewTaskPane.toFront();
    }

    @FXML
    protected void onToDoExitButtonClick() {
        createNewTaskPane.setVisible(false);
    }

    // -------------------------------------------------------
    //  CREATE BUTTON — main logic
    // -------------------------------------------------------
    private Label createTaskCard(CalendarEntry entry, int col) {
        double HOUR_HEIGHT = 50.0;
        double COL_WIDTH   = 100.0;

        // Convert hours to 24-hour format if needed for your math
        int startHour24 = entry.getStartHour() + (entry.isStartPM() && entry.getStartHour() != 12 ? 12 : 0);
        if (!entry.isStartPM() && entry.getStartHour() == 12) startHour24 = 0;

        int endHour24 = entry.getEndHour() + (entry.isEndPM() && entry.getEndHour() != 12 ? 12 : 0);
        if (!entry.isEndPM() && entry.getEndHour() == 12) endHour24 = 0;

        // Your exact positioning math
        double topY    = startHour24 * HOUR_HEIGHT;
        double height  = (endHour24 - startHour24) * HOUR_HEIGHT;
        if (height < 10) height = 10;

        double leftX   = col * COL_WIDTH + 2;
        double width   = COL_WIDTH - 4;

        // Build the task label
        Label taskLabel = new Label(entry.getTitle());
        taskLabel.setLayoutX(leftX);
        taskLabel.setLayoutY(topY);
        taskLabel.setPrefWidth(width);
        taskLabel.setPrefHeight(height);

        // Tag it so our date-picker clear routine can find it later
        taskLabel.setId("taskLabel");

        taskLabel.setStyle(
                "-fx-background-color: " + entry.getColor() + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 2 4 2 4;" +
                        "-fx-font-size: 11px;" +
                        "-fx-wrap-text: true;"
        );

        // Setup the click handler right here
        taskLabel.setOnMouseClicked(e -> openViewTaskPane(entry, taskLabel));

        return taskLabel;
    }

    @FXML
    protected void onCreateButtonClick() {

        // 1. read title
        String title = titleField.getText();
        if (title == null || title.isBlank()) title = "(no title)";

        // 2. read date → figure out which column (0=Mon … 6=Sun)
        LocalDate date = datePicker1.getValue();
        if (date == null) date = LocalDate.now();
        int col = getDayColumn(date.getDayOfWeek());

        // 3. read start time
        int startHour12 = hourSpinner1.getValue();
        int startMin    = minuteSpinner1.getValue();
        ToggleButton amPm1 = (ToggleButton) time1.getSelectedToggle();
        boolean startPM = amPm1 != null && amPm1.getText().equals("PM");
        double startHour24 = convertToHour24(startHour12, startMin, startPM);

        // 4. read end time
        int endHour12 = hourSpinner2.getValue();
        int endMin    = minuteSpinner2.getValue();
        ToggleButton amPm2 = (ToggleButton) time2.getSelectedToggle();
        boolean endPM = amPm2 != null && amPm2.getText().equals("PM");
        double endHour24 = convertToHour24(endHour12, endMin, endPM);

        // 5. store entry
        CalendarEntry entry = new CalendarEntry(
                title,
                descriptionField.getText(),
                date,
                startHour12, startMin, startPM,
                endHour12,   endMin,   endPM,
                selectedColor
        );


        CalendarEntry.addEvent(entry);
        Label taskLabel = createTaskCard(entry, col);

        // Get today's date
        LocalDate viewedDate = datePicker.getValue();

        if (viewedDate != null) {
            LocalDate startOfWeek = viewedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek   = viewedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            boolean isThisWeek = !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);

            if (isThisWeek) {
                weeklyGrid.getChildren().add(taskLabel);
            }
        }

        // 9. close and reset
        createNewTaskPane.setVisible(false);
        titleField.clear();
        descriptionField.clear();
        datePicker1.setValue(null);
    }


    // create a method that shows tasks based on date picker

    @FXML
    protected void updateWeeklyView(){
        LocalDate selectedDate = datePicker.getValue();

        if (selectedDate == null){
            return;} // if date is not selected
        LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek   = selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        // clearing weeklyGridView
        weeklyGrid.getChildren().removeIf(node -> node instanceof Label);

        // 2. Loop through your list
        for (CalendarEntry entry : CalendarEntry.getEntryList()) {
               LocalDate entryDate = entry.getDate();
            // 3. If the entry matches the selected date
            if (!entryDate.isBefore(startOfWeek) && !entryDate.isAfter(endOfWeek)) {

                int col = getDayColumn(entryDate.getDayOfWeek());

                Label perfectTaskLabel = createTaskCard(entry, col);

                weeklyGrid.getChildren().add(perfectTaskLabel);
            }
        }


    }

    // -------------------------------------------------------
    //  COLOR PICKER
    // -------------------------------------------------------
    @FXML
    protected void onSelectColorButtonClick() {
        selectColorPane.setVisible(true);
        selectColorPane.toFront();
    }

    @FXML
    protected void onSelectColorExitButtonClick() {
        selectColorPane.setVisible(false);
    }

    @FXML
    protected void onSelectApplyButton() {
        ToggleButton selected = (ToggleButton) colorToggle.getSelectedToggle();
        if (selected != null) {
            // pull the color straight from the button's inline style
            String style = selected.getStyle();
            // style looks like "-fx-background-color: #FF9898;"
            String color = style.replace("-fx-background-color:", "").replace(";", "").trim();
            selectedColor = color;
            selectColorButton.setStyle(
                    "-fx-background-color: " + selectedColor + ";" +
                            "-fx-border-color: #8EABE8;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;"
            );
        }
        selectColorPane.setVisible(false);

    }

    @FXML
    protected void onViewTaskExitButtonClick() {
        viewTaskPane.setVisible(false);
        //viewtaskpane is what shows task info
    }

    @FXML
    protected void onCompleteButtonClick() {
        if (currentTaskLabel != null) {
            weeklyGrid.getChildren().remove(currentTaskLabel);
        }
        viewTaskPane.setVisible(false);
    }

    @FXML
    protected void onEraseButtonClick() {
        if (currentTaskLabel != null) {
            weeklyGrid.getChildren().remove(currentTaskLabel);
        }
        viewTaskPane.setVisible(false);
    }

    private void openViewTaskPane(CalendarEntry entry, Label taskLabel) {
        currentTaskLabel = taskLabel;
        viewTaskTitle.setText(entry.getTitle());
        viewTaskDescription.setText(entry.getDescription());
        viewTaskDate.setText(entry.getDate().toString());
        viewTaskTime.setText(entry.getTimeString());
        viewTaskColorBox.setStyle("-fx-background-color: " + entry.getColor() + "; -fx-background-radius: 4;");
        viewTaskPane.setVisible(true);
        viewTaskPane.toFront();
    }

    // -------------------------------------------------------
    //  SPINNERS
    // -------------------------------------------------------
    protected void spinMethod() {
        hourSpin1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 12);
        hourSpinner1.setValueFactory(hourSpin1);

        minuteSpin1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        minuteSpinner1.setValueFactory(minuteSpin1);

        hourSpin2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 12);
        hourSpinner2.setValueFactory(hourSpin2);

        minuteSpin2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        minuteSpinner2.setValueFactory(minuteSpin2);
    }

    public void displayTime() {
        hourNum1 = hourSpinner1.getValue();
        minuteNum1 = minuteSpinner1.getValue();
        hourLabel1.setText(String.valueOf(hourNum1));
        minuteLabel1.setText(String.valueOf(minuteNum1));

        hourNum2 = hourSpinner2.getValue();
        minuteNum2 = minuteSpinner2.getValue();
        hourLabel2.setText(String.valueOf(hourNum2));
        minuteLabel2.setText(String.valueOf(minuteNum2));
    }

    // -------------------------------------------------------
    //  HELPERS
    // -------------------------------------------------------

    /**
     * Maps a day of week to the grid column index.
     * Mon=0, Tue=1, Wed=2, Thu=3, Fri=4, Sat=5, Sun=6
     */
    private int getDayColumn(DayOfWeek day) {
        return switch (day) {
            case MONDAY    -> 0;
            case TUESDAY   -> 1;
            case WEDNESDAY -> 2;
            case THURSDAY  -> 3;
            case FRIDAY    -> 4;
            case SATURDAY  -> 5;
            case SUNDAY    -> 6;
        };
    }

    /**
     * Converts a 12-hour clock value + AM/PM into a grid row (0–23).
     * Row 0 = 12AM, Row 1 = 1AM, ... Row 12 = 12PM, Row 13 = 1PM ...
     */
    /**
     * Converts 12-hour time + AM/PM into a decimal 24-hour value.
     * e.g. 7:30 AM → 7.5,  1:15 PM → 13.25
     */
    private double convertToHour24(int hour12, int minutes, boolean isPM) {
        int hour24;
        if (!isPM) {
            hour24 = (hour12 == 12) ? 0 : hour12;
        } else {
            hour24 = (hour12 == 12) ? 13 : hour12 + 12;
        }
        return hour24 + (minutes / 60.0);
    }

    // -------------------------------------------------------
    //  INITIALIZE
    // -------------------------------------------------------
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        spinMethod();
    }
}