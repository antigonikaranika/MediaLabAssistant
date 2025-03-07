import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task with title, description, category, priority, deadline, status, and reminders.
 */
public class Task {
    private String title;
    private String description;
    private String category;
    private String priority;
    private String deadline;
    private String status;
    private List<String> reminders;

    /**
     * Constructs a Task with specified attributes.
     * @param title The title of the task.
     * @param description The description of the task.
     * @param category The category of the task.
     * @param priority The priority of the task.
     * @param deadline The deadline of the task in format "yyyy-MM-dd".
     * @param status The status of the task.
     */
    public Task(String title, String description, String category, String priority, String deadline, String status) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
        this.status = status;
        this.reminders = new ArrayList<>();
    }

    /**
     * Gets the title of the task.
     * @return The title.
     */
    public String getTitle() { return title; }

    /**
     * Sets the title of the task.
     * @param title The new title.
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Gets the description of the task.
     * @return The description.
     */
    public String getDescription() { return description; }

    /**
     * Sets the description of the task.
     * @param description The new description.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Gets the category of the task.
     * @return The category.
     */
    public String getCategory() { return category; }

    /**
     * Sets the category of the task.
     * @param category The new category.
     */
    public void setCategory(String category) { this.category = category; }

    /**
     * Gets the priority of the task.
     * @return The priority.
     */
    public String getPriority() { return priority; }

    /**
     * Sets the priority of the task.
     * @param priority The new priority.
     */
    public void setPriority(String priority) { this.priority = priority; }

    /**
     * Gets the deadline of the task.
     * @return The deadline in format "yyyy-MM-dd".
     */
    public String getDeadline() { return deadline; }

    /**
     * Sets the deadline of the task.
     * @param deadline The new deadline in format "yyyy-MM-dd".
     */
    public void setDeadline(String deadline) { this.deadline = deadline; }

    /**
     * Gets the status of the task.
     * @return The status.
     */
    public String getStatus() { return status; }

    /**
     * Sets the status of the task.
     * @param status The new status.
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Gets the list of reminders for the task.
     * @return The list of reminders.
     */
    public List<String> getReminders() {
        return reminders;
    }

    /**
     * Sets the reminders for the task.
     * @param newReminders The new list of reminders.
     */
    public void setReminders(List<String> newReminders) {
        this.reminders = new ArrayList<>(newReminders);
    }

    /**
     * Sets default reminders relative to the deadline.
     * Adds reminders 1 day, 1 week, and 1 month before the deadline if applicable.
     */
    public void setDefaultReminders() {
        LocalDate deadline = LocalDate.parse(this.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate currentDate = LocalDate.now();

        if (deadline.isAfter(currentDate.plusDays(1))) {
            addReminder(deadline.minusDays(1).toString());
        }
        if (deadline.isAfter(currentDate.plusWeeks(1))) {
            addReminder(deadline.minusWeeks(1).toString());
        }
        if (deadline.isAfter(currentDate.plusMonths(1))) {
            addReminder(deadline.minusMonths(1).toString());
        }
    }

    /**
     * Adds a reminder to the task.
     * @param reminderDate The date of the reminder in format "yyyy-MM-dd".
     * @return A message indicating success or describing an error.
     */
    public String addReminder(String reminderDate) {
        try {
            LocalDate reminder = LocalDate.parse(reminderDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate deadline = LocalDate.parse(this.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate currentDate = LocalDate.now();

            if (reminder.isBefore(currentDate)) {
                return "Error: Reminder date must be after the current date.";
            }

            if (reminder.isAfter(deadline)) {
                return "Error: Reminder date must be before the deadline.";
            }

            if (reminders.contains(reminderDate)) {
                return "Error: Reminder already exists for this date.";
            }

            reminders.add(reminderDate);
            return "Reminder added successfully.";
        } catch (DateTimeParseException e) {
            return "Error: Invalid date format for reminder. Please use yyyy-MM-dd.";
        }
    }

    /**
     * Updates an existing reminder.
     * @param oldDate The existing reminder date.
     * @param newDate The new reminder date.
     * @return A message indicating success or describing an error.
     */
    public String updateReminder(String oldDate, String newDate) {
        if (!reminders.contains(oldDate)) {
            return "Error: Reminder not found.";
        }

        String addResult = addReminder(newDate);

        if (addResult.startsWith("Error")) {
            return addResult;
        }

        reminders.remove(oldDate);
        return "Reminder updated successfully.";
    }

    /**
     * Removes a reminder from the task.
     * @param reminderDate The reminder date to remove.
     * @return A message indicating success or describing an error.
     */
    public String removeReminder(String reminderDate) {
        if (!reminders.contains(reminderDate)) {
            return "Error: Reminder not found.";
        }
        reminders.remove(reminderDate);
        return "Reminder removed successfully.";
    }

    /**
     * Clears all reminders for the task.
     */
    public void clearReminders() {
        this.reminders.clear();
    }

    /**
     * Returns a string representation of the task.
     * @return A string with all task attributes.
     */
    @Override
    public String toString() {
        return "Task [title=" + title + ", description=" + description + ", category=" + category +
                ", priority=" + priority + ", deadline=" + deadline + ", status=" + status +
                ", reminders=" + reminders + "]";
    }

    /**
     * Compares this task to another object for equality.
     * @param obj The object to compare.
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Task other)) return false;
        return Objects.equals(title, other.title) &&
                Objects.equals(description, other.description) &&
                Objects.equals(reminders, other.reminders);
    }

    /**
     * Computes the hash code for this task.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, description, reminders);
    }
}
