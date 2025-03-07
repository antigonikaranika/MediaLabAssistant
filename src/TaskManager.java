import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;
import java.util.HashSet;

public class TaskManager {
    private List<Task> tasks;
    private List<Category> categories;
    private List<Priority> priorities;

    public TaskManager() {
        this.tasks = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.priorities = new ArrayList<>();
    }


    public String addTask(Task task, List<String> userReminders) {
        task.setStatus("Open");

        task.setDefaultReminders();

        Set<String> uniqueReminders = new HashSet<>(task.getReminders());

        for (String reminderDate : userReminders) {
            try {
                if (!uniqueReminders.contains(reminderDate)) {
                    task.addReminder(reminderDate);
                    uniqueReminders.add(reminderDate);
                }
            } catch (IllegalArgumentException e) {
                return "Error: Wrong reminder format! Please use yyyy-MM-dd.";
            }
        }

        tasks.add(task);
        return "Task successfully created!";
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String updateTask(Task task, String title, String description, String category,
                             String priority, String deadline, String status, List<String> newReminders) {

        if (!isValidDate(deadline)) {
            return "Error: Wrong date format for deadline. Please use YYYY-MM-DD.";
        }

        task.setTitle(title);
        task.setDescription(description);
        task.setCategory(category);
        task.setPriority(priority);
        task.setDeadline(deadline);
        task.setStatus(status);
        Set<String> alreadyUsed = new HashSet<>();

        if (!"Completed".equalsIgnoreCase(status)) {
            task.clearReminders();
            for (String reminderDate : newReminders) {

                if (!isValidDate(reminderDate)) {
                    return "Error: Wrong date format for reminder. Please use YYYY-MM-DD.";
                }
                if (!alreadyUsed.add(reminderDate)) {
                    return "Error: There is already a reminder with the same date '" + reminderDate + "'.";
                }

                try {
                    task.addReminder(reminderDate);
                    alreadyUsed.add(reminderDate);
                } catch (IllegalArgumentException e) {
                    return "Error adding reminder: " + e.getMessage();
                }
            }
        } else {

            task.clearReminders();
            task.setStatus("Completed");
            return "Task marked as completed. All reminders removed.";
        }

        return "Task updated successfully!";
        }

    public void checkAndUpdateTaskStatuses() {
        LocalDate currentDate = LocalDate.now();
        for (Task task : tasks) {
            if (!task.getStatus().equals("Completed") && LocalDate.parse(task.getDeadline()).isBefore(currentDate)) {
                task.setStatus("Delayed");
            }
        }
    }

    public void addCategory(Category category) {

        if (categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase(category.getName()))) {
            System.out.println("Error: Category '" + category.getName() + "' already exists.");
            return;
        }

        categories.add(category);
        System.out.println("Category '" + category.getName() + "' added successfully.");
    }


    public void removeCategory(Category category) {
        if (categories.contains(category)) {

            List<Task> tasksToRemove = tasks.stream()
                    .filter(t -> t.getCategory().equals(category.getName()))
                    .toList();

            for (Task task : tasksToRemove) {
                removeTask(task);
            }

            categories.remove(category);
        }
    }

    public void renameCategory(Category cat, String newName) {
        String oldName = cat.getName();
        cat.setName(newName);

        for (Task t : tasks) {
            if (t.getCategory().equals(oldName)) {
                t.setCategory(newName);
            }
        }
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void addPriority(Priority priority) {
        if (priorities.stream().anyMatch(p -> p.getName().equalsIgnoreCase(priority.getName()))) {
            System.out.println("Error: Priority '" + priority.getName() + "' already exists.");
            return;
        }
        priorities.add(priority);
        System.out.println("Priority '" + priority.getName() + "' added successfully.");
    }


    public void removePriority(String priorityName) {
        if (priorityName.equals("Default")) {
            System.out.println("The 'Default' priority cannot be removed.");
            return;
        }

        Priority priorityToRemove = priorities.stream()
                .filter(priority -> priority.getName().equals(priorityName))
                .findFirst()
                .orElse(null);

        if (priorityToRemove != null) {
            priorities.remove(priorityToRemove);
            tasks.stream()
                    .filter(task -> task.getPriority().equals(priorityName))
                    .forEach(task -> task.setPriority("Default"));
        }
    }

    public List<Priority> getPriorities() {
        return priorities;
    }

    public void renamePriority(String oldName, String newName) {
        if (oldName.equals("Default")) {
            System.out.println("Error: The 'Default' priority cannot be renamed.");
            return;
        }

        boolean nameExists = priorities.stream()
                .anyMatch(priority -> priority.getName().equalsIgnoreCase(newName));

        if (nameExists) {
            System.out.println("Error: A priority with the name '" + newName + "' already exists.");
            return;
        }

        Priority priorityToRename = priorities.stream()
                .filter(priority -> priority.getName().equalsIgnoreCase(oldName))
                .findFirst()
                .orElse(null);

        if (priorityToRename != null) {
            priorityToRename.setName(newName);

            for (Task task : tasks) {
                if (task.getPriority().equalsIgnoreCase(oldName)) {
                    task.setPriority(newName);
                }
            }
            System.out.println("Priority renamed from '" + oldName + "' to '" + newName + "'.");
        } else {
            System.out.println("Error: Priority '" + oldName + "' not found.");
        }
    }

    public List<Task> searchTasks(String title, String category, String priority) {
        return tasks.stream()
                .filter(task -> (title == null || task.getTitle().contains(title)) &&
                        (category == null || task.getCategory().equalsIgnoreCase(category)) &&
                        (priority == null || task.getPriority().equalsIgnoreCase(priority)))
                .collect(Collectors.toList());
    }

    public int countDelayedTasks() {
        return (int) tasks.stream()
                .filter(task -> "Delayed".equalsIgnoreCase(task.getStatus()))
                .count();
    }

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void setPriorities(List<Priority> priorities) {
        this.priorities = priorities;
    }

}
