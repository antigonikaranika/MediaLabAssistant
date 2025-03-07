import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Bounds;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Optional;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import java.util.Comparator;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.util.stream.Collectors;
import javafx.application.Platform;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class Main extends Application {

    private TaskManager taskManager;
    private Label totalTasksLabel;
    private Label completedTasksLabel;
    private Label delayedTasksLabel;
    private Label upcomingTasksLabel;
    private ListView<HBox> taskListView;
    private ListView<HBox> remindersListView;
    private VBox categoryContainer;
    private VBox priorityContainer;

    @Override
    public void start(Stage primaryStage) {
        taskManager = new TaskManager();
        JsonHandler.initialize(taskManager);

        taskManager.checkAndUpdateTaskStatuses();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        double padding = screenBounds.getHeight() * 0.01;
        VBox upperSpace = new VBox(padding);
        upperSpace.setStyle("-fx-padding: " + padding + ";");
        upperSpace.setAlignment(Pos.CENTER);

        // Create labels for statistics
        totalTasksLabel = new Label("Total Tasks: 0");
        completedTasksLabel = new Label("Completed Tasks: 0");
        delayedTasksLabel = new Label("Delayed Tasks: 0");
        upcomingTasksLabel = new Label("Upcoming Tasks in Next 7 Days: 0");

        double fontSize = screenBounds.getHeight() * 0.02;
        totalTasksLabel.setStyle("-fx-font-size: " + fontSize + "px;");
        completedTasksLabel.setStyle("-fx-font-size: " + fontSize + "px;");
        delayedTasksLabel.setStyle("-fx-font-size: " + fontSize + "px;");
        upcomingTasksLabel.setStyle("-fx-font-size: " + fontSize + "px;");

        upperSpace.getChildren().addAll(totalTasksLabel, completedTasksLabel, delayedTasksLabel, upcomingTasksLabel);

        Separator separator = new Separator();
        separator.setStyle("-fx-border-width: 3px; -fx-border-color: black;");
        upperSpace.getChildren().add(separator);

        // Tasks per Category
        VBox lowerSpace = new VBox(10);
        lowerSpace.setAlignment(Pos.CENTER);
        lowerSpace.setPadding(new Insets(10));
        lowerSpace.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-width: 1px;");

        Label tasksTitle = new Label("Tasks per Category");
        tasksTitle.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-weight: bold;");
        lowerSpace.getChildren().add(tasksTitle);

        taskListView = new ListView<>();
        taskListView.setPrefHeight(100);
        taskListView.setFocusTraversable(true);
        taskListView.setMouseTransparent(false);
        lowerSpace.getChildren().add(taskListView);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button createTaskButton = new Button("Create Task");
        buttonBox.getChildren().add(createTaskButton);
        lowerSpace.getChildren().add(buttonBox);

        taskListView.getItems().setAll(getTasksByCategory());
        taskListView.setPrefHeight(taskListView.getItems().size() * 30);

        createTaskButton.setOnAction(e -> showTaskCreationDialog(taskListView));


        // Categories
        Label categoriesLabel= new Label("Categories");
        categoriesLabel.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-weight: bold;");
        lowerSpace.getChildren().add(categoriesLabel);

        categoryContainer = new VBox(10);
        categoryContainer.setPadding(new Insets(10));
        categoryContainer.setStyle("-fx-border-color: #bbb; -fx-border-width: 1px; -fx-background-color: #f9f9f9;");
        refreshCategories(categoryContainer);
        lowerSpace.getChildren().add(categoryContainer);

        Button addCategoryButton = new Button("Add Category");
        addCategoryButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Category");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter new category name:");

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);

            TextField inputField = dialog.getEditor();
            inputField.textProperty().addListener((obs, oldVal, newVal) -> {
                boolean nameExists = taskManager.getCategories().stream()
                        .anyMatch(c -> c.getName().equalsIgnoreCase(newVal));
                okButton.setDisable(newVal.trim().isEmpty() || nameExists);
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String newCategoryName = result.get().trim();
                if (!newCategoryName.isEmpty()) {
                    taskManager.addCategory(new Category(newCategoryName));
                    refreshTaskDisplay();
                    showSuccessAlert("Success", "Category added successfully!");
                }
            }
        });

        lowerSpace.getChildren().add(addCategoryButton);

        // Priorities
        Label prioritiesLabel= new Label("Priorities");
        prioritiesLabel.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-weight: bold;");

        lowerSpace.getChildren().add(prioritiesLabel);

        priorityContainer = new VBox(10);
        priorityContainer.setPadding(new Insets(10));
        priorityContainer.setStyle("-fx-border-color: #bbb; -fx-border-width: 1px; -fx-background-color: #f9f9f9;");
        refreshPriorityList(priorityContainer);
        lowerSpace.getChildren().add(priorityContainer);

        Button addPriorityButton = new Button("Add Priority");
        addPriorityButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Priority");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter new priority name:");

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);

            TextField inputField = dialog.getEditor();

            inputField.textProperty().addListener((obs, oldVal, newVal) -> {
                boolean nameExists = taskManager.getPriorities().stream()
                        .anyMatch(p -> p.getName().equalsIgnoreCase(newVal.trim()));
                okButton.setDisable(newVal.trim().isEmpty() || nameExists);
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String newPriorityName = result.get().trim();
                if (!newPriorityName.isEmpty()) {
                    taskManager.addPriority(new Priority(newPriorityName));
                    refreshTaskDisplay();
                    showSuccessAlert("Success", "Priority added successfully!");
                }
            }
        });

        lowerSpace.getChildren().add(addPriorityButton);

        // Reminders
        remindersListView = new ListView<>();
        remindersListView.setMouseTransparent(false);
        remindersListView.setFocusTraversable(false);

        remindersListView.setPrefHeight(0);
        remindersListView.setMaxHeight(Region.USE_COMPUTED_SIZE);

        Label remindersLabel = new Label("Reminders");
        remindersLabel.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-weight: bold;");

        lowerSpace.getChildren().add(remindersLabel);

        lowerSpace.getChildren().add(remindersListView);

        Button createReminderButton = new Button("Create Reminder");
        createReminderButton.setOnAction(e -> showNewReminderDialog());
        lowerSpace.getChildren().add(createReminderButton);

        refreshRemindersListView();

        // Search bar for title, category, and priority
        Label searchLabel = new Label("Search Tasks");
        searchLabel.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-weight: bold;");

        VBox searchBox = new VBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Title search field
        TextField titleSearchField = new TextField();
        titleSearchField.setPromptText("Enter title");

        // Category search field
        TextField categorySearchField = new TextField();
        categorySearchField.setPromptText("Enter category");

        // Priority search field
        TextField prioritySearchField = new TextField();
        prioritySearchField.setPromptText("Enter priority");

        Button searchButton = new Button("Search");

        searchBox.getChildren().addAll(
                new Label("Search by Title:"), titleSearchField,
                new Label("Search by Category:"), categorySearchField,
                new Label("Search by Priority:"), prioritySearchField,
                searchButton,
                new Label("Search Results:")
        );

        VBox searchResultsContainer = new VBox(10);
        searchResultsContainer.setPadding(new Insets(10));

        searchButton.setOnAction(e -> {
            String title = titleSearchField.getText() != null ? titleSearchField.getText().trim().toLowerCase() : "";
            String category = categorySearchField.getText() != null ? categorySearchField.getText().trim().toLowerCase() : "";
            String priority = prioritySearchField.getText() != null ? prioritySearchField.getText().trim().toLowerCase() : "";

            List<Task> searchResults = taskManager.searchTasks(null, null, null).stream()
                    .filter(task ->
                            (title.isEmpty() || task.getTitle().toLowerCase().contains(title)) &&
                                    (category.isEmpty() || task.getCategory().toLowerCase().contains(category)) &&
                                    (priority.isEmpty() || task.getPriority().toLowerCase().contains(priority))
                    ).toList();

            searchResultsContainer.getChildren().clear();

            for (int i = 0; i < searchResults.size(); i++) {
                Task task = searchResults.get(i);
                HBox taskBox = new HBox(20);
                taskBox.setAlignment(Pos.CENTER_LEFT);
                taskBox.setPadding(new Insets(10));
                taskBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-background-color: " +
                        (i % 2 == 0 ? "#f9f9f9;" : "#e6e6e6;"));

                VBox taskInfoBox = new VBox(3);
                taskInfoBox.setPadding(new Insets(5));

                Label titleLabel = new Label("Title: " + task.getTitle());
                Label priorityLabel = new Label("Priority: " + task.getPriority());
                Label categoryLabel = new Label("Category: " + task.getCategory());
                Label deadlineLabel = new Label("Deadline: " + task.getDeadline());
                Label statusLabel = new Label("Status: " + task.getStatus());

                titleLabel.setStyle("-fx-font-weight: bold;");
                priorityLabel.setStyle("-fx-text-fill: #555;");
                categoryLabel.setStyle("-fx-text-fill: #555;");
                deadlineLabel.setStyle("-fx-text-fill: #555;");

                taskInfoBox.getChildren().addAll(titleLabel, priorityLabel, categoryLabel, deadlineLabel, statusLabel);
                taskBox.getChildren().add(taskInfoBox);

                searchResultsContainer.getChildren().add(taskBox);
            }

            Platform.runLater(() -> {
                Stage stage = (Stage) searchResultsContainer.getScene().getWindow();
                stage.sizeToScene();
            });

        });

        lowerSpace.getChildren().add(searchLabel);
        lowerSpace.getChildren().add(searchBox);


        VBox root = new VBox();


        root.getChildren().addAll(upperSpace, separator, lowerSpace);
        root.getChildren().add(searchResultsContainer);
        ScrollPane scrollPane = new ScrollPane(root);
        Scene scene = new Scene(scrollPane, screenBounds.getWidth(), screenBounds.getHeight());
        separator.prefWidthProperty().bind(scene.widthProperty());
        scrollPane.prefHeightProperty().bind(scene.heightProperty());
        scrollPane.prefWidthProperty().bind(scene.widthProperty());

        primaryStage.setTitle("Medialab Assistant");
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshTaskDisplay();

        if (taskManager.countDelayedTasks() > 0) {
            showDelayedTasksPopup(taskManager.countDelayedTasks());
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                JsonHandler.saveTasks(taskManager.getTasks());
                JsonHandler.saveCategories(taskManager.getCategories());
                JsonHandler.savePriorities(taskManager.getPriorities());
                System.out.println("Data saved on exit.");
            } catch (IOException e) {
                System.out.println("Error saving data on exit: " + e.getMessage());
            }
        }));
    }

    private void showTaskCreationDialog(ListView<HBox> taskListView) {
        Stage dialog = new Stage();
        dialog.setTitle("Create New Task");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setPadding(new Insets(10));

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Task Description");

        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Select Category");
        categoryComboBox.setItems(FXCollections.observableArrayList(
                taskManager.getCategories().stream().map(Category::getName).toList()
        ));

        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.setPromptText("Select Priority");
        priorityComboBox.setItems(FXCollections.observableArrayList(
                taskManager.getPriorities().stream().map(Priority::getName).toList()
        ));

        TextField deadlineField = new TextField();
        deadlineField.setPromptText("Deadline (yyyy-MM-dd)");

        TextField remindersField = new TextField();
        remindersField.setPromptText("Reminders (comma-separated dates) (optional)");

        Button submitButton = new Button("Create");
        submitButton.setDisable(true);

        ChangeListener<String> validationListener = (obs, oldVal, newVal) -> {
            boolean isValid = !titleField.getText().trim().isEmpty() &&
                    categoryComboBox.getValue() != null &&
                    priorityComboBox.getValue() != null &&
                    isValidDateFormat(deadlineField.getText().trim());

            submitButton.setDisable(!isValid);
        };

        titleField.textProperty().addListener(validationListener);
        deadlineField.textProperty().addListener(validationListener);
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validationListener.changed(null, null, null));
        priorityComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validationListener.changed(null, null, null));

        submitButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            String category = categoryComboBox.getValue();
            String priority = priorityComboBox.getValue();
            String deadline = deadlineField.getText().trim();
            String remindersText = remindersField.getText().trim();

            if (!isValidDateFormat(deadline)) {
                showErrorAlert("Error", "Invalid deadline format. Please use yyyy-MM-dd.");
                return;
            }

            List<String> userReminders = new ArrayList<>();
            if (!remindersText.isEmpty()) {
                String[] reminderDates = remindersText.split(",");
                for (String reminder : reminderDates) {
                    if (!isValidDateFormat(reminder.trim())) {
                        showErrorAlert("Error", "Invalid reminder date format. Please use yyyy-MM-dd.");
                        return;
                    }
                    userReminders.add(reminder.trim());
                }
            }

            try {
                Task newTask = new Task(title, description, category, priority, deadline, "Open");
                String resultMessage = taskManager.addTask(newTask, userReminders);

                if (resultMessage.startsWith("Error")) {
                    showErrorAlert("Error", resultMessage);
                    return;
                }

                taskListView.getItems().setAll(getTasksByCategory());
                taskListView.setPrefHeight(taskListView.getItems().size() * 30);
                updateTaskStatistics();
                refreshTaskDisplay();
                showSuccessAlert("Success", resultMessage);
                dialog.close();
            } catch (Exception ex) {
                showErrorAlert("Error", "Failed to create task: " + ex.getMessage());
            }
        });

        dialogVBox.getChildren().addAll(
                new Label("Enter Task Details"),
                titleField, descriptionField,
                new Label("Select Category:"), categoryComboBox,
                new Label("Select Priority:"), priorityComboBox,
                deadlineField, remindersField, submitButton
        );

        Scene dialogScene = new Scene(dialogVBox, 400, 400);
        dialog.setScene(dialogScene);
        dialog.show();
    }


    private boolean isValidDateFormat(String date) {
        try {
            LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return !parsedDate.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        alert.showAndWait();
    }

    private List<HBox> getTasksByCategory() {
        List<HBox> taskDisplayList = new ArrayList<>();

        for (Category category : taskManager.getCategories()) {
            Label categoryLabel = new Label("Category: " + category.getName());
            categoryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #003366;");
            taskDisplayList.add(new HBox(categoryLabel));

            for (Task task : taskManager.getTasks()) {
                if (task.getCategory().equals(category.getName())) {
                    Label taskLabel = new Label("  - " + task.getTitle() + " (" + task.getStatus() + ")");

                    Button detailsButton = new Button("View Details");
                    Button editButton = new Button("Edit");
                    Button removeButton = new Button("Delete");

                    removeButton.setOnAction(e -> {
                        Alert confirm = new Alert(
                                Alert.AlertType.CONFIRMATION,
                                "Are you sure you want to delete this task?",
                                ButtonType.YES, ButtonType.NO
                        );
                        confirm.setTitle("Confirm Deletion");
                        confirm.setHeaderText(null);

                        Optional<ButtonType> result = confirm.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.YES) {
                            taskManager.removeTask(task);
                            refreshTaskDisplay();
                            showSuccessAlert("Success", "Task deleted successfully!");
                        }
                    });
                    removeButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");

                    detailsButton.setOnAction(e -> showTaskDetailsDialog(task));
                    editButton.setOnAction(e -> {
                        showEditTaskDialog(task);
                        refreshTaskDisplay();
                    });

                    HBox taskRow = new HBox(10, taskLabel, detailsButton, editButton, removeButton);
                    taskRow.setStyle("-fx-background-color: #f0fff0; -fx-border-color: #ccc; -fx-border-radius: 3px;");
                    taskRow.setAlignment(Pos.CENTER_LEFT);
                    taskDisplayList.add(taskRow);
                }
            }
        }
        return taskDisplayList;
    }

    private void showTaskDetailsDialog(Task task) {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Task Details");

        VBox detailsLayout = new VBox(10);
        detailsLayout.setPadding(new Insets(10));
        detailsLayout.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Title: " + task.getTitle());
        Label descriptionLabel = new Label("Description: " + task.getDescription());
        Label categoryLabel = new Label("Category: " + task.getCategory());
        Label priorityLabel = new Label("Priority: " + task.getPriority());
        Label deadlineLabel = new Label("Deadline: " + task.getDeadline());
        Label statusLabel = new Label("Status: " + task.getStatus());
        Label remindersLabel = new Label("Reminders: " + String.join(", ", task.getReminders()));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> detailsStage.close());

        detailsLayout.getChildren().addAll(titleLabel, descriptionLabel, categoryLabel, priorityLabel,
                deadlineLabel, statusLabel, remindersLabel, closeButton);

        Scene scene = new Scene(detailsLayout, 300, 250);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    private void showEditTaskDialog(Task task) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Modify task details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(task.getTitle());
        TextField descriptionField = new TextField(task.getDescription());

        ComboBox<String> categoryComboBox = new ComboBox<>(FXCollections.observableArrayList(
                taskManager.getCategories().stream().map(Category::getName).toList()
        ));
        categoryComboBox.setValue(task.getCategory());

        ComboBox<String> priorityComboBox = new ComboBox<>(FXCollections.observableArrayList(
                taskManager.getPriorities().stream().map(Priority::getName).toList()
        ));
        priorityComboBox.setValue(task.getPriority());

        ComboBox<String> statusComboBox = new ComboBox<>();

        if ("Delayed".equalsIgnoreCase(task.getStatus())) {
            statusComboBox.setItems(FXCollections.observableArrayList("Completed"));
        } else {
            statusComboBox.setItems(FXCollections.observableArrayList("Open", "In Progress", "Postponed", "Completed"));
        }
        statusComboBox.setValue(task.getStatus());

        TextField deadlineField = new TextField(task.getDeadline());

        VBox remindersBox = new VBox(5);
        updateRemindersUI(remindersBox, task, dialog);

        grid.addRow(0, new Label("Title:"), titleField);
        grid.addRow(1, new Label("Description:"), descriptionField);
        grid.addRow(2, new Label("Category:"), categoryComboBox);
        grid.addRow(3, new Label("Priority:"), priorityComboBox);
        grid.addRow(4, new Label("Status:"), statusComboBox);
        grid.addRow(5, new Label("Deadline:"), deadlineField);
        grid.addRow(6, new Label("Reminders:"), remindersBox);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        deadlineField.setDisable("Completed".equalsIgnoreCase(task.getStatus()));

        statusComboBox.valueProperty().addListener((obs, oldStatus, newStatus) -> {

            deadlineField.setDisable("Completed".equalsIgnoreCase(newStatus));

            if ("Completed".equalsIgnoreCase(newStatus)) {
                remindersBox.getChildren().clear();
            } else {
                updateRemindersUI(remindersBox, task, dialog);
            }

            Platform.runLater(() -> {
                Stage dialogStage = (Stage) remindersBox.getScene().getWindow();
                dialogStage.sizeToScene();
            });
        });

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                List<String> reminders = new ArrayList<>(task.getReminders());

                String newDeadline = deadlineField.getText().trim();
                LocalDate deadlineDate = LocalDate.parse(newDeadline, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate today = LocalDate.now();

                String selectedStatus = statusComboBox.getValue();

                if ("Completed".equalsIgnoreCase(task.getStatus())) {

                } else if ("Delayed".equalsIgnoreCase(task.getStatus())) {

                    if (deadlineDate.isAfter(today)) {
                        selectedStatus = "Open";
                    }
                } else {

                    if (deadlineDate.isBefore(today)) {
                        selectedStatus = "Delayed";
                    }
                }

                String result = taskManager.updateTask(
                        task,
                        titleField.getText().trim(),
                        descriptionField.getText().trim(),
                        categoryComboBox.getValue(),
                        priorityComboBox.getValue(),
                        newDeadline,
                        selectedStatus,
                        reminders
                );

                if (result.startsWith("Error")) {
                    showErrorAlert("Task Update Failed", result);
                } else {
                    showSuccessAlert("Task Updated", result);
                    refreshTaskDisplay();
                    refreshRemindersListView();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void updateRemindersUI(VBox remindersBox, Task task, Dialog<ButtonType> parentDialog) {
        remindersBox.getChildren().clear();

        boolean isEditable = !"Completed".equalsIgnoreCase(task.getStatus()) && !"Delayed".equalsIgnoreCase(task.getStatus());

        for (String reminder : new ArrayList<>(task.getReminders())) {
            HBox row = new HBox(10);
            TextField reminderField = new TextField(reminder);
            reminderField.setEditable(isEditable);

            if (isEditable) {
                Button editButton = new Button("Edit");
                editButton.setOnAction(e -> {
                    String newDate = reminderField.getText().trim();
                    String result = task.updateReminder(reminder, newDate);
                    if (result.startsWith("Error")) {
                        showErrorAlert("Invalid Reminder", result);
                    } else {
                        showSuccessAlert("Reminder Updated", result);
                        updateRemindersUI(remindersBox, task, parentDialog);
                    }
                });

                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction(e -> {
                    task.removeReminder(reminder);
                    updateRemindersUI(remindersBox, task, parentDialog);
                });

                row.getChildren().addAll(reminderField, editButton, deleteButton);
            } else {

                row.getChildren().add(reminderField);
            }

            remindersBox.getChildren().add(row);
        }

        if (isEditable) {
            Button addReminderButton = new Button("Add Reminder");
            addReminderButton.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("New Reminder");
                dialog.setHeaderText("Enter new reminder date (yyyy-MM-dd):");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String date = result.get().trim();
                    String addResult = task.addReminder(date);
                    if (addResult.startsWith("Error")) {
                        showErrorAlert("Invalid Reminder", addResult);
                    } else {
                        showSuccessAlert("Reminder Added", addResult);
                        updateRemindersUI(remindersBox, task, parentDialog);

                        Platform.runLater(() -> {
                            Stage dialogStage = (Stage) remindersBox.getScene().getWindow();
                            dialogStage.sizeToScene();
                        });
                    }
                }
            });
            remindersBox.getChildren().add(addReminderButton);
        }
    }

    private void refreshCategories(VBox categoryContainer) {
        categoryContainer.getChildren().clear();

        for (Category category : taskManager.getCategories()) {
            HBox categoryBox = new HBox(15);
            categoryBox.setAlignment(Pos.CENTER_LEFT);
            categoryBox.setPadding(new Insets(10));
            categoryBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-background-color: #f0f8ff; -fx-border-radius: 5px;");

            Label categoryLabel = new Label(category.getName());
            categoryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> showRenameCategoryDialog(category));

            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm Delete");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete the category: " + category.getName() + "?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    taskManager.removeCategory(category);
                    refreshCategories(categoryContainer);
                    refreshTaskDisplay();
                }
            });
            deleteButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");

            categoryBox.getChildren().addAll(categoryLabel, editButton, deleteButton);
            categoryContainer.getChildren().add(categoryBox);
        }
    }

    private void showRenameCategoryDialog(Category cat) {
        TextInputDialog dialog = new TextInputDialog(cat.getName());
        dialog.setTitle("Rename Category");
        dialog.setHeaderText(null);
        dialog.setContentText("New Category Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get().trim();
            if (!newName.isEmpty()) {

                taskManager.renameCategory(cat, newName);

                refreshTaskDisplay();
                showSuccessAlert("Success", "Task renamed successfully!");
            }
        }
    }

    private void refreshTaskDisplay() {
        taskListView.getItems().setAll(getTasksByCategory());
        taskListView.setPrefHeight(taskListView.getItems().size() * 30);

        refreshCategories(categoryContainer);
        refreshRemindersListView();
        refreshPriorityList(priorityContainer);
        updateTaskStatistics();
    }

    private void refreshRemindersListView() {
        LocalDate today = LocalDate.now();

        for (Task task : taskManager.getTasks()) {
            if (task.getStatus().equalsIgnoreCase("Completed")) {
                task.getReminders().clear();
            } else {
                task.setReminders(task.getReminders().stream()
                        .filter(reminder -> isReminderValid(reminder, today))
                        .collect(Collectors.toList()));
            }
        }

        remindersListView.getItems().clear();

        List<HBox> sortedReminders = taskManager.getTasks().stream()
                .flatMap(task -> task.getReminders().stream()
                        .map(reminder -> new ReminderEntry(reminder, task)))
                .sorted(Comparator.comparing(ReminderEntry::getDate))
                .map(entry -> {
                    Label reminderLabel = new Label(entry.reminderDate + " → " + entry.task.getTitle());
                    reminderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #804000;");

                    Button editButton = new Button("Edit");
                    editButton.setOnAction(e -> {
                        showEditReminderDialog(entry.task, entry.reminderDate);
                        refreshTaskDisplay();
                    });

                    Button deleteButton = new Button("Delete");
                    deleteButton.setOnAction(e -> {
                        entry.task.getReminders().remove(entry.reminderDate);
                        refreshRemindersListView();
                        refreshTaskDisplay();
                        showSuccessAlert("Reminder Deleted", "The reminder was successfully removed.");
                    });
                    deleteButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
                    HBox row = new HBox(10, reminderLabel, editButton, deleteButton);
                    row.setAlignment(Pos.CENTER_LEFT);
                    return row;
                })
                .collect(Collectors.toList());

        remindersListView.getItems().addAll(sortedReminders);
        remindersListView.setPrefHeight(sortedReminders.size() * 30);
    }

    private void showEditReminderDialog(Task task, String oldReminderDate) {
        TextInputDialog dialog = new TextInputDialog(oldReminderDate);
        dialog.setTitle("Edit Reminder");
        dialog.setHeaderText("Update reminder for: " + task.getTitle());
        dialog.setContentText("Enter new reminder date (yyyy-MM-dd):");

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        TextField inputField = dialog.getEditor();

        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(!isValidDateFormat(newVal.trim()));
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newReminderDate = result.get().trim();

            if (!isValidDateFormat(newReminderDate)) {
                showErrorAlert("Invalid Date", "Please enter a valid date in yyyy-MM-dd format.");
                return;
            }

            List<String> reminders = task.getReminders();

            if (reminders.contains(newReminderDate)) {
                showErrorAlert("Duplicate Reminder", "A reminder for this date already exists for this task.");
                return;
            }

            reminders.remove(oldReminderDate);
            reminders.add(newReminderDate);

            refreshRemindersListView();
            showSuccessAlert("Reminder Updated", "Reminder updated successfully for " + task.getTitle());
        }
    }

    private void showNewReminderDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Reminder");
        dialog.setHeaderText("Select a task and enter a reminder date");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> taskDropdown = new ComboBox<>();
        taskDropdown.setPromptText("Select a Task");
        taskDropdown.setItems(FXCollections.observableArrayList(
                taskManager.getTasks().stream()
                        .filter(task -> !task.getStatus().equalsIgnoreCase("Completed") && !task.getStatus().equalsIgnoreCase("Delayed"))
                        .map(Task::getTitle)
                        .toList()
        ));

        TextField dateField = new TextField();
        dateField.setPromptText("yyyy-MM-dd");

        grid.add(new Label("Task:"), 0, 0);
        grid.add(taskDropdown, 1, 0);
        grid.add(new Label("Reminder Date:"), 0, 1);
        grid.add(dateField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        taskDropdown.valueProperty().addListener((obs, oldVal, newVal) ->
                okButton.setDisable(newVal == null || dateField.getText().trim().isEmpty() || !isValidDateFormat(dateField.getText().trim()))
        );

        dateField.textProperty().addListener((obs, oldVal, newVal) ->
                okButton.setDisable(taskDropdown.getValue() == null || newVal.trim().isEmpty() || !isValidDateFormat(newVal.trim()))
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String selectedTaskTitle = taskDropdown.getValue();
                String reminderDate = dateField.getText().trim();

                if (!isValidDateFormat(reminderDate)) {
                    showErrorAlert("Invalid Date", "Please enter a valid future date in the format yyyy-MM-dd.");
                    return null;
                }

                Task selectedTask = taskManager.getTasks().stream()
                        .filter(task -> task.getTitle().equals(selectedTaskTitle))
                        .findFirst()
                        .orElse(null);

                if (selectedTask != null) {

                    if (selectedTask.getReminders().contains(reminderDate)) {
                        showErrorAlert("Duplicate Reminder", "A reminder for this date already exists for this task.");
                        return null;
                    }

                    selectedTask.addReminder(reminderDate);
                    refreshRemindersListView();
                    showSuccessAlert("Reminder Created", "New reminder set for " + selectedTaskTitle + " on " + reminderDate);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private boolean isReminderValid(String reminder, LocalDate today) {
        try {
            LocalDate reminderDate = LocalDate.parse(reminder, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return !reminderDate.isBefore(today);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static class ReminderEntry {
        String reminderDate;
        Task task;

        ReminderEntry(String reminderDate, Task task) {
            this.reminderDate = reminderDate;
            this.task = task;
        }

        public LocalDate getDate() {
            try {
                return LocalDate.parse(reminderDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                return LocalDate.MAX;
            }
        }
    }

    private void refreshPriorityList(VBox priorityContainer) {
        priorityContainer.getChildren().clear();

        for (Priority priority : taskManager.getPriorities()) {
            HBox priorityBox = new HBox(15);
            priorityBox.setAlignment(Pos.CENTER_LEFT);
            priorityBox.setPadding(new Insets(10));
            priorityBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-background-color: #f8f0ff; -fx-border-radius: 5px;");

            Label priorityLabel = new Label(priority.getName());
            priorityLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            priorityBox.getChildren().add(priorityLabel);

            if (!priority.getName().equals("Default")) {
                Button editButton = new Button("Edit");
                editButton.setOnAction(e -> showRenamePriorityDialog(priority));

                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction(e -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirm Delete");
                    alert.setHeaderText(null);
                    alert.setContentText("Are you sure you want to delete the priority: " + priority.getName() + "?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        taskManager.removePriority(priority.getName());
                        refreshTaskDisplay();
                    }
                });
                deleteButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");

                priorityBox.getChildren().addAll(editButton, deleteButton);
            }

            priorityContainer.getChildren().add(priorityBox);
        }
    }

    private void showRenamePriorityDialog(Priority priority) {
        if (priority.getName().equals("Default")) {
            showErrorAlert("Error", "The 'Default' priority cannot be renamed.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(priority.getName());
        dialog.setTitle("Rename Priority");
        dialog.setHeaderText(null);
        dialog.setContentText("New Priority Name:");

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        TextField inputField = dialog.getEditor();
        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean nameExists = taskManager.getPriorities().stream()
                    .anyMatch(p -> p.getName().equalsIgnoreCase(newVal));
            okButton.setDisable(newVal.trim().isEmpty() || nameExists);
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get().trim();
            if (!newName.isEmpty()) {
                taskManager.renamePriority(priority.getName(), newName);
                refreshTaskDisplay();
                showSuccessAlert("Success", "Priority renamed successfully!");
            }
        }
    }

    private void updateTaskStatistics() {
        int totalTasks = taskManager.getTasks().size();
        int completedTasks = (int) taskManager.getTasks().stream().filter(task -> "Completed".equalsIgnoreCase(task.getStatus())).count();
        int delayedTasks = taskManager.countDelayedTasks();

        int upcomingTasks = (int) taskManager.getTasks().stream()
                .filter(task -> {
                    LocalDate deadline = LocalDate.parse(task.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDate currentDate = LocalDate.now();
                    return deadline.isAfter(currentDate) && deadline.isBefore(currentDate.plusDays(7));
                }).count();

        totalTasksLabel.setText("Total Tasks: " + totalTasks);
        completedTasksLabel.setText("Completed Tasks: " + completedTasks);
        delayedTasksLabel.setText("Delayed Tasks: " + delayedTasks);
        upcomingTasksLabel.setText("Upcoming Tasks in Next 7 Days: " + upcomingTasks);
    }

    private void showDelayedTasksPopup(int delayedTasksCount) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Delayed Tasks Notification");
        alert.setHeaderText("You have " + delayedTasksCount + " delayed task(s)!");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
