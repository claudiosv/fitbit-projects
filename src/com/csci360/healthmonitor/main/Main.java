package com.csci360.healthmonitor.main;

//TODO: redesign classes and packages
/*
Reorganise into folders/packages
Rename some files
Consider GRASP principles more
Make builders and factories
Be more clear about Memento and Observer pattern
Implement State pattern for UI screens
Apply more MVC i.e. screens and parts need to be seen as views
 */
//TODO: unit testing
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {
    //UI Needs to have access to our Classes
    public static User user = new User();
    public static StepHistory stepHistory = new StepHistory();
    public static SleepHistory sleepHistory = new SleepHistory();
    public static HeartHistory heartHistory = new HeartHistory();
    public static Sync sync = new Sync(user);
    public static Timer timer = new Timer();
    public Scene scene;
    public Pane test;
    public Button btnTest;
    private BorderPane border;

    public static void main(String args[]) {
        launch(args);
    }

    public GridPane addSleepTracker() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(0, 5, 0, 5));

        // Category in column 2, row 1
        Label category = new Label();
        category.setWrapText(true);
        category.setMaxWidth(113);
        category.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        grid.add(category, 0, 0, 3, 1);
        if (sleepHistory.sleepTracker.startSleepTracker()) {
            Runnable testRun = new Runnable() {
                public void run() {
                    try {
                        while (!sleepHistory.sleepTracker.isSleeping()) {
                            System.out.println("waiting to fall asleep");
                            Platform.runLater(() -> category.setText("Waiting to fall asleep"));
                            Thread.sleep(500);
                        }
                        while (sleepHistory.sleepTracker.isSleeping()) {
                            System.out.println("waiting to wake up");
                            Platform.runLater(() -> category.setText("Waiting to wake up"));
                            Thread.sleep(500);
                        }

                        Platform.runLater(() -> category.setText("Saving sleep times"));
                        sleepHistory.addSleepTimes(sleepHistory.sleepTracker.saveNightsSleep());

                        NightsSleep lastNight = sleepHistory.getSleepTimes(0);
                        System.out.printf("Fell Asleep At: %s\n", lastNight.getSleepTime());
                        System.out.printf("Woke Up At: %s\n", lastNight.getWakeTime());
                        System.out.printf("Seconds Asleep: %d\n", lastNight.secondsAsleep());

                        Platform.runLater(() -> category.setText("Fell Asleep At: " + lastNight.getSleepTime() + "\nWoke Up At: " + lastNight.getWakeTime() + "\nSeconds Asleep: " + lastNight.secondsAsleep()));
                    } catch (InterruptedException e) {
                        //thread interrupted, display error
                    }
                }
            };

            Thread testThread = new Thread(testRun);
            testThread.start();
        }

        return grid;
    }

    public Pane addHeartMonitor() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(16, 16, 16, 32));

        // Category in column 2, row 1

        Button btn1 = new Button();
        Image imageDecline = new Image(getClass().getResourceAsStream("003-heartbeat.png"));
        ImageView imageView = new ImageView(imageDecline);
        imageView.setFitHeight(32);
        imageView.setFitWidth(32);
        btn1.setGraphic(imageView);

        btn1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                border.setCenter(addActiveHeartMonitor());

            }
        });
        GridPane.setHalignment(btn1, HPos.CENTER);
        GridPane.setValignment(btn1, VPos.CENTER);
        grid.add(btn1, 0, 1);

        return grid;
    }

    public GridPane addActiveHeartMonitor() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(0, 5, 0, 5));

        // Category in column 2, row 1
        Label category = new Label();
        category.setWrapText(true);

        category.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        Button btnStop = new Button();
        Image imageStop = new Image(getClass().getResourceAsStream("002-stop.png"));
        ImageView imageViewStop = new ImageView(imageStop);
        imageViewStop.setFitHeight(16);
        imageViewStop.setFitWidth(16);
        btnStop.setGraphic(imageViewStop);

        btnStop.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                heartHistory.heartMonitor.stopHeart();
                border.setCenter(addStoppedHeartMonitor());
            }
        });

        Button btnReset = new Button();
        Image imageReset = new Image(getClass().getResourceAsStream("001-reload.png"));
        ImageView imageViewReset = new ImageView(imageReset);
        imageViewReset.setFitHeight(16);
        imageViewReset.setFitWidth(16);
        btnReset.setGraphic(imageViewReset);

        btnReset.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                heartHistory.heartMonitor.resetHeart();
            }
        });

        grid.add(category, 0, 0, 3, 1);
        grid.add(btnStop, 0, 2);
        grid.add(btnReset, 1, 2);

        if (heartHistory.heartMonitor.startHeart()) {
            heartHistory.heartMonitor.bpmProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> category.setText(String.format("%.0f bpm", newValue)));
            });
        }

        return grid;
    }

    public GridPane addStoppedHeartMonitor() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(0, 5, 0, 5));

        Label category = new Label(String.format("%.0f bpm", heartHistory.heartMonitor.bpmProperty().get()));
        category.setWrapText(true);
        category.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        category.setMaxWidth(113);
        category.setVisible(true);

        // Category in column 2, row 1
        Button btnSave = new Button();
        Image imageSave = new Image(getClass().getResourceAsStream("003-save.png"));
        ImageView imageViewSave = new ImageView(imageSave);
        imageViewSave.setFitHeight(16);
        imageViewSave.setFitWidth(16);
        btnSave.setGraphic(imageViewSave);

        btnSave.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                heartHistory.addHeartRate(heartHistory.heartMonitor.saveHeartRate());
                heartHistory.heartMonitor.resetHeart();
                category.setText("Saved successfully");
            }
        });
        grid.add(category, 0, 0);
        grid.add(btnSave, 0, 1);
        return grid;

    }

    public GridPane addSyncData() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(0, 5, 0, 5));

        // Category in column 2, row 1
        Label category = new Label();
        category.setWrapText(true);
        category.setMaxWidth(113);

        category.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        if (sync.addCompanion()) {
            Runnable syncRunnable = new Runnable() {
                public void run() {
                    try {
                        System.out.println("Starting the sync process");
                        Platform.runLater(() -> category.setText("Starting the sync process"));
                        sync.syncData("Empty Data to send");

                        Thread.sleep(2550);
                        System.out.println("Sync process completed");
                        Platform.runLater(() -> category.setText("Sync process completed"));

                        Platform.runLater(() -> category.setText("Username: " + user.username + "\nGender: " + user.getGender() +
                                "\nBirthday: " + user.getBirthday() + "\nHeight: " + user.getHeight() + "\nWeight: " + user.getWeight()));


                        System.out.println("User: " + user);
                        System.out.printf("Username: %s\n", user.username);
                        System.out.printf("Gender: %s\n", user.getGender());
                        System.out.printf("Birthday: %s\n", user.getBirthday());
                        System.out.printf("Height: %d\n", user.getHeight());
                        System.out.printf("Weight: %d\n", user.getWeight());
                    } catch (InterruptedException e) {
                        //thread interrupted, display sync error
                    }
                }
            };

            Thread syncThread = new Thread(syncRunnable);
            syncThread.start();
        }
        grid.add(category, 0, 0);


        return grid;
    }

    public GridPane addTimer() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(0, 5, 0, 5));

        // Category in column 2, row 1

        Button btnStop = new Button();
        Label category = new Label("100 secs");
        Slider slider = new Slider();
        slider.setMin(1);
        slider.setMax(300);
        slider.setValue(100);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(10);
        slider.setMaxWidth(100);
        slider.setMinWidth(100);
        slider.valueProperty().addListener((obs, oldval, newVal) ->
                slider.setValue(newVal.intValue()));
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {

            category.setText(String.format("%.0f secs", newValue));

        });
        category.setWrapText(true);

        category.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        Button btn1 = new Button();
        Image imageDecline = new Image(getClass().getResourceAsStream("002-play-button.png"));
        ImageView imageView = new ImageView(imageDecline);
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        btn1.setGraphic(imageView);
        timer.remainingTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() <= 0) {
                Platform.runLater(() -> {border.setCenter(addFinishedTimer());
                btnStop.setVisible(false);
                slider.setVisible(true);});
            } else {
                Platform.runLater(() -> category.setText(String.format("%d secs", newValue.intValue())));
            }
        });
        btn1.setOnAction(event -> {
                    timer.startTimer((int) slider.getValue());
                    slider.setVisible(false);
                    btnStop.setVisible(true);
                }
        );


        Image imageStop = new Image(getClass().getResourceAsStream("002-stop.png"));
        ImageView imageViewStop = new ImageView(imageStop);
        imageViewStop.setFitHeight(16);
        imageViewStop.setFitWidth(16);
        btnStop.setGraphic(imageViewStop);

        btnStop.setOnAction(event -> {
                    timer.remainingTimeProperty().setValue(0);
                }
        );
        btnStop.setVisible(false);
        grid.add(slider, 0, 1);
        grid.add(btn1, 0, 2);
        grid.add(btnStop, 0, 2);
        grid.add(category, 0, 0, 3, 1);


        return grid;
    }

    public GridPane addFinishedTimer() {
        btnTest.setVisible(true);
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(0, 5, 0, 5));

        // Category in column 2, row 1


        Label category = new Label("Timer completed");

        category.setMaxWidth(113);
        category.setWrapText(true);

        category.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        grid.add(category, 0, 0);


        return grid;
    }

    public GridPane addStepCounter() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(16, 16, 16, 32));

        // Category in column 2, row 1

        Button btn1 = new Button();
        Image imageDecline = new Image(getClass().getResourceAsStream("002-play-button.png"));
        ImageView imageView = new ImageView(imageDecline);
        imageView.setFitHeight(32);
        imageView.setFitWidth(32);
        btn1.setGraphic(imageView);

        btn1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                border.setCenter(addActiveStepCounter());

            }
        });
        GridPane.setHalignment(btn1, HPos.CENTER);
        GridPane.setValignment(btn1, VPos.CENTER);
        grid.add(btn1, 0, 1);


        return grid;
    }

    public GridPane addActiveStepCounter() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(0, 5, 0, 5));

        // Category in column 2, row 1
        Label category = new Label("0 steps");
        category.setWrapText(true);

        category.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        Button btnStop = new Button();
        Image imageStop = new Image(getClass().getResourceAsStream("002-stop.png"));
        ImageView imageViewStop = new ImageView(imageStop);
        imageViewStop.setFitHeight(16);
        imageViewStop.setFitWidth(16);
        btnStop.setGraphic(imageViewStop);

        btnStop.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                stepHistory.stepCounter.stopCounter();
                border.setCenter(addStoppedStepCounter());
            }
        });

        Button btnReset = new Button();
        Image imageReset = new Image(getClass().getResourceAsStream("001-reload.png"));
        ImageView imageViewReset = new ImageView(imageReset);
        imageViewReset.setFitHeight(16);
        imageViewReset.setFitWidth(16);
        btnReset.setGraphic(imageViewReset);

        btnReset.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                stepHistory.stepCounter.resetSteps();
            }
        });


        grid.add(category, 0, 0, 3, 1);
        grid.add(btnStop, 0, 2);
        grid.add(btnReset, 1, 2);

        if (stepHistory.stepCounter.startCounter()) {
            stepHistory.stepCounter.numStepsProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> category.setText(String.format("%d steps", newValue.intValue())));
            });
        }

        return grid;
    }

    public GridPane addStoppedStepCounter() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(0, 5, 0, 5));

        // Category in column 2, row 1
        Button btnSave = new Button();
        Image imageSave = new Image(getClass().getResourceAsStream("003-save.png"));
        ImageView imageViewSave = new ImageView(imageSave);
        imageViewSave.setFitHeight(16);
        imageViewSave.setFitWidth(16);
        btnSave.setGraphic(imageViewSave);

        Label category = new Label(String.format("%d steps", stepHistory.stepCounter.getNumSteps()));
        category.setMaxWidth(113);
        category.setWrapText(true);

        category.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        btnSave.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                stepHistory.addDailyCount(stepHistory.stepCounter.saveDailyCount());
                category.setText("Saved successfully");
            }
        });
        grid.add(category, 0, 0);
        grid.add(btnSave, 0, 1);
        return grid;
    }

    public GridPane addHomeScreen() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(0, 5, 0, 5));

        // Category in column 2, row 1


        Button btn1 = new Button();
        //btn1.setStyle("-fx-background-color: #e5e5e5;");
        // btn1.setText("T");
        Image imageTimer = new Image(getClass().getResourceAsStream("007-stopwatch.png"));
        ImageView imageViewTimer = new ImageView(imageTimer);
        imageViewTimer.setFitHeight(16);
        imageViewTimer.setFitWidth(16);
        btn1.setGraphic(imageViewTimer);
        btn1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                border.setCenter(addTimer());
                btnTest.setVisible(true);
            }
        });
        grid.add(btn1, 0, 1);

        Button btn2 = new Button();
        Image imageHeartMonitor = new Image(getClass().getResourceAsStream("003-heartbeat.png"));
        ImageView imageViewHeartMonitor = new ImageView(imageHeartMonitor);
        imageViewHeartMonitor.setFitHeight(16);
        imageViewHeartMonitor.setFitWidth(16);
        btn2.setGraphic(imageViewHeartMonitor);
        //btn2.setText("H");
        btn2.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                border.setCenter(addHeartMonitor());
                btnTest.setVisible(true);
            }
        });
        grid.add(btn2, 2, 1);

        Button btn3 = new Button();
        //btn3.setText("ST");
        Image imageStepTracker = new Image(getClass().getResourceAsStream("004-stais.png"));
        ImageView imageViewStepTracker = new ImageView(imageStepTracker);
        imageViewStepTracker.setFitHeight(16);
        imageViewStepTracker.setFitWidth(16);
        btn3.setGraphic(imageViewStepTracker);
        btn3.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                border.setCenter(addStepCounter());
                btnTest.setVisible(true);
            }
        });
        grid.add(btn3, 1, 2);

        Button btn4 = new Button();
        //btn4.setText("Z");
        Image imageSleepTracker = new Image(getClass().getResourceAsStream("001-sleep.png"));
        ImageView imageViewSleepTracker = new ImageView(imageSleepTracker);
        imageViewSleepTracker.setFitHeight(16);
        imageViewSleepTracker.setFitWidth(16);
        btn4.setGraphic(imageViewSleepTracker);
        btn4.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                border.setCenter(addSleepTracker());
                btnTest.setVisible(true);
            }
        });
        grid.add(btn4, 0, 3);

        Button btn5 = new Button();
        //btn5.setText("SY");
        Image imageSync = new Image(getClass().getResourceAsStream("002-synchronization-arrows.png"));
        ImageView imageViewSync = new ImageView(imageSync);
        imageViewSync.setFitHeight(16);
        imageViewSync.setFitWidth(16);
        btn5.setGraphic(imageViewSync);
        btn5.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                border.setCenter(addSyncData());
                btnTest.setVisible(true);
            }
        });
        grid.add(btn5, 2, 3);

        BackgroundFill myBF = new BackgroundFill(Color.BLACK, new CornerRadii(1),
                new Insets(0.0, 0.0, 0.0, 0.0));// or null for the padding
//then you set to your node or container or layout
        grid.setBackground(new Background(myBF));

        return grid;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        //StackPane root = new StackPane();
        //root.getChildren().add(addHomeScreen());
        border = new BorderPane();
        HBox hbox = new HBox();
        btnTest = new Button();
        btnTest.setVisible(false);
        Image imageBack = new Image(getClass().getResourceAsStream("001-back.png"));
        ImageView imageViewBack = new ImageView(imageBack);
        imageViewBack.setFitHeight(12);
        imageViewBack.setFitWidth(12);
        btnTest.setGraphic(imageViewBack);
        btnTest.setStyle(
                "-fx-min-width: 16px; " +
                        "-fx-min-height: 16px; " +
                        "-fx-max-width: 16px; " +
                        "-fx-max-height: 16px;"
        );
        btnTest.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                border.setCenter(addHomeScreen());
                btnTest.setVisible(false);
            }
        });
        Label category = new Label("10:00 AM");

        CalendarObservable cal = new CalendarObservable();
        cal.currentTime.addListener((observable, oldValue, newValue) -> {

            Platform.runLater(() ->
                    category.setText(newValue)
            );

        });
        category.setText(cal.currentTime.get());
        //category.setAlignment(Pos.TOP_RIGHT);
        //btnTest.setAlignment(Pos.TOP_LEFT);
        hbox.setPadding(new Insets(2, 2, 2, 2));
        Region reg = new Region();
        reg.setPrefWidth(20);
        hbox.getChildren().add(btnTest);
        hbox.getChildren().add(reg);
        hbox.getChildren().add(category);

        border.setTop(hbox);
        border.setCenter(addHomeScreen());

        Pane mainBox = new Pane();
        Image watch = new Image(getClass().getResourceAsStream("watch.jpg"));
        BackgroundImage backgroundImage = new BackgroundImage(watch, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        mainBox.setBackground(new Background(backgroundImage));


        test = new Pane();
        BackgroundFill myBF = new BackgroundFill(Color.BLACK, new CornerRadii(0),
                new Insets(0.0, 0.0, 0.0, 0.0));// or null for the padding
//then you set to your node or container or layout
        test.setBackground(new Background(myBF));
        test.setMaxSize(113, 113);
        test.setMinSize(113, 113);
        test.setMaxSize(116, 116);
        test.setMinSize(116, 116);

        test.setLayoutX(17);
        test.setLayoutY(85);

        test.getChildren().add(border);
        mainBox.getChildren().add(test);
        scene = new Scene(mainBox, 150, 286);
        scene.getStylesheets().add
                (getClass().getResource("button.css").toExternalForm());
        primaryStage.setTitle("FitBit Not a Simulator");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            stepHistory.stepCounter.stopCounter();
            sleepHistory.sleepTracker.stopSleepTracker();
            heartHistory.heartMonitor.stopHeart();
            timer.stopTimer();
            System.exit(0);
        });
        primaryStage.show();
    }
}