
# MediaLab Assistant

## Description

**MediaLab Assistant** is a task management application built with **JavaFX**. The application allows users to:

- Create, edit, and delete tasks.
- Manage categories and priorities.
- Set and manage task deadlines and reminders.
- Persist data using **JSON files** for tasks, categories, and priorities.

This project was developed using **IntelliJ IDEA Community Edition** and relies on **Maven** for dependency management and project structure.

---

## Project Structure

The project directory contains:

```
MediaLabAssistant/
├── pom.xml                 # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── Main.java            # Entry point (JavaFX GUI)
│   │   │   ├── Task.java             # Task entity
│   │   │   ├── Category.java         # Category entity
│   │   │   ├── Priority.java         # Priority entity
│   │   │   ├── TaskManager.java      # Business logic (task management)
│   │   │   ├── JsonHandler.java      # Handles reading/writing JSON files
│   │   ├── resources/
├── medialab/
│   ├── tasks.json        # Tasks data
│   ├── categories.json   # Categories data
│   ├── priorities.json   # Priorities data
```

---

## Prerequisites

### Required Software
- Java Development Kit (JDK) 17 or later (tested with OpenJDK 23.0.2)
- IntelliJ IDEA Community Edition (recommended)
- Maven (automatically included with IntelliJ)

---

## Setup and Run Instructions

### 1️ Install IntelliJ IDEA Community Edition
Download and install IntelliJ from:  
 [https://www.jetbrains.com/idea/download](https://www.jetbrains.com/idea/download)

---

### 2️⃣ Download and Extract MediaLabAssistant
Download the project zip file: **`MediaLabAssistant.zip`**  
Extract it to a folder of your choice.

---

### 3️⃣ Open the Project in IntelliJ
- Open IntelliJ.
- Select **"Open"**.
- Choose the **`MediaLabAssistant`** folder.
- IntelliJ will detect the `pom.xml` and automatically configure the project as a **Maven project**.

---

### 4️⃣ Reload Maven Project
- Open the **Maven** panel in IntelliJ (usually on the right-hand side).
- Click the **Reload All Maven Projects** button (refresh icon).

---

### 5️⃣ Add Run Configuration
To run the application, you need to set up a **Maven Run Configuration** in IntelliJ:

- Go to **Run > Edit Configurations**.
- Click the **+** button and select **Maven**.
- Set:
    - Name: `Run MediaLabAssistant`
    - Working Directory: point to the project root (where `pom.xml` is located)
    - Command Line: `clean javafx:run`
- Apply and OK.

---

### 6️⃣ Run the Project
- Select your new `Run MediaLabAssistant` configuration from the top-right dropdown.
- Press **Run** .

---

## Data Files (JSON)
All data is stored in `medialab/`, including:
- `tasks.json`: Stores all tasks.
- `categories.json`: Stores all categories.
- `priorities.json`: Stores all priorities.

---

## Dependencies (Handled by Maven)

| Library        | Version |
|----------------|---------|
| JavaFX Controls | 21.0.1 |
| JavaFX FXML     | 21.0.1 |
| Gson            | 2.10.1 |

---

##  Application Features Summary

| Feature | Description |
|---|---|
| Create Tasks | Set title, description, category, priority, deadline, and reminders. |
| Edit Tasks | Update task details (some restrictions apply for completed/delayed tasks). |
| Delete Tasks | Remove unwanted tasks. |
| Manage Categories | Add, edit, delete categories. |
| Manage Priorities | Add, edit, delete priorities. |
| Automatic Status Update | Tasks automatically become "Delayed" if overdue at startup. |
| Popup Notifications | Alerts for overdue tasks at startup. |
| Reminder Management | Automatic and custom reminders for each task. |
| Search | Search tasks by title, category, priority. |

There are some screenshots of the application in the file "report.pdf".

---

## Developed For
This project was developed as part of the **Multimedia Technology** course at the **National Technical University of Athens (NTUA)** by:

** Antigoni Maria Karanika **  

