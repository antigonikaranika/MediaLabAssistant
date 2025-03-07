import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class JsonHandler {

    private static final String BASE_PATH = "medialab/";
    private static final String TASKS_FILE = BASE_PATH + "tasks.json";
    private static final String CATEGORIES_FILE = BASE_PATH + "categories.json";
    private static final String PRIORITIES_FILE = BASE_PATH + "priorities.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void initialize(TaskManager taskManager) {
        try {
            taskManager.setTasks(loadTasks());
            taskManager.setCategories(loadCategories());
            taskManager.setPriorities(loadPriorities());
            System.out.println("Data loaded successfully from JSON files.");
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    private static <T> T loadFromFile(String filePath, Type typeOfT) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return GSON.fromJson(reader, typeOfT);
        } catch (IOException e) {
            throw new IOException("Error loading data from " + filePath + ": " + e.getMessage(), e);
        }
    }

    private static <T> void saveToFile(String filePath, T data) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            throw new IOException("Error saving data to " + filePath + ": " + e.getMessage(), e);
        }
    }

    public static List<Task> loadTasks() throws IOException {
        Type taskListType = new TypeToken<List<Task>>() {}.getType();
        return loadFromFile(TASKS_FILE, taskListType);
    }

    public static List<Category> loadCategories() throws IOException {
        Type categoryListType = new TypeToken<List<Category>>() {}.getType();
        return loadFromFile(CATEGORIES_FILE, categoryListType);
    }

    public static List<Priority> loadPriorities() throws IOException {
        Type priorityListType = new TypeToken<List<Priority>>() {}.getType();
        return loadFromFile(PRIORITIES_FILE, priorityListType);
    }

    public static void saveTasks(List<Task> tasks) throws IOException {
        saveToFile(TASKS_FILE, tasks);
    }

    public static void saveCategories(List<Category> categories) throws IOException {
        saveToFile(CATEGORIES_FILE, categories);
    }

    public static void savePriorities(List<Priority> priorities) throws IOException {
        saveToFile(PRIORITIES_FILE, priorities);
    }
}
