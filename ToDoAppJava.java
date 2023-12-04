import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ToDoAppJava {
    private List<TaskInfo> tasks = new ArrayList<>();
    private JTextArea taskEntry;
    private JComboBox<String> priorityComboBox;
    private JTextField dueDateField;
    private JList<String> taskList;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ToDoAppJava().createAndShowGUI();
        });
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Enhanced To-Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        taskEntry = new JTextArea(5, 30);
        panel.add(taskEntry);

        String[] priorityOptions = {"High", "Medium", "Low"};
        priorityComboBox = new JComboBox<>(priorityOptions);
        panel.add(priorityComboBox);

        JLabel dueDateLabel = new JLabel("Due Date:");
        panel.add(dueDateLabel);

        dueDateField = new JTextField("No Due Date");
        panel.add(dueDateField);

        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });
        panel.add(addButton);

        taskList = new JList<>();
        panel.add(new JScrollPane(taskList));

        JButton detailsButton = new JButton("Task Details");
        detailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTaskDetails();
            }
        });
        panel.add(detailsButton);

        JButton deleteButton = new JButton("Delete Task");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTask();
            }
        });
        panel.add(deleteButton);

        JButton saveButton = new JButton("Save Tasks");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTasks();
            }
        });
        panel.add(saveButton);

        JButton loadButton = new JButton("Load Tasks");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTasks();
            }
        });
        panel.add(loadButton);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private void addTask() {
        String taskText = taskEntry.getText();
        String priority = (String) priorityComboBox.getSelectedItem();
        String dueDate = dueDateField.getText();

        if (!taskText.isEmpty()) {
            TaskInfo taskInfo = new TaskInfo(taskText, priority, dueDate, false);
            tasks.add(taskInfo);
            updateTaskList();
            clearEntryFields();
        } else {
            JOptionPane.showMessageDialog(null, "Task text cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateTaskList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (int i = 0; i < tasks.size(); i++) {
            TaskInfo taskInfo = tasks.get(i);
            String taskStr = String.format("%d. %s - Priority: %s - Due Date: %s - Completed: %s",
                    i + 1, taskInfo.getTask(), taskInfo.getPriority(), taskInfo.getDueDate(), taskInfo.isCompleted());
            model.addElement(taskStr);
        }
        taskList.setModel(model);
    }

    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this task?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tasks.remove(selectedIndex);
                updateTaskList();
            }
        }
    }

    private void clearEntryFields() {
        taskEntry.setText("");
        priorityComboBox.setSelectedIndex(2); // Default to "Low"
        dueDateField.setText("No Due Date");
    }

    private void showTaskDetails() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            TaskInfo taskInfo = tasks.get(selectedIndex);
            String details = String.format("Task: %s\nPriority: %s\nDue Date: %s\nCompleted: %s",
                    taskInfo.getTask(), taskInfo.getPriority(), taskInfo.getDueDate(), taskInfo.isCompleted());
            JOptionPane.showMessageDialog(null, details, "Task Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveTasks() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                JSONArray jsonArray = new JSONArray();
                for (TaskInfo taskInfo : tasks) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Task", taskInfo.getTask());
                    jsonObject.put("Priority", taskInfo.getPriority());
                    jsonObject.put("Due Date", taskInfo.getDueDate());
                    jsonObject.put("Completed", taskInfo.isCompleted());
                    jsonArray.add(jsonObject);
                }
                writer.write(jsonArray.toJSONString());
                writer.flush();
                JOptionPane.showMessageDialog(null, "Tasks saved successfully.", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error saving tasks: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadTasks() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileReader reader = new FileReader(file)) {
                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(reader);

                tasks.clear();
                for (Object obj : jsonArray) {
                    JSONObject jsonObject = (JSONObject) obj;
                    String task = (String) jsonObject.get("Task");
                    String priority = (String) jsonObject.get("Priority");
                    String dueDate = (String) jsonObject.get("Due Date");
                    boolean completed = (boolean) jsonObject.get("Completed");
                    tasks.add(new TaskInfo(task, priority, dueDate, completed));
                }

                updateTaskList();
                JOptionPane.showMessageDialog(null, "Tasks loaded successfully.", "Load Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | ParseException e) {
                JOptionPane.showMessageDialog(null, "Error loading tasks: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class TaskInfo {
        private String task;
        private String priority;
        private String dueDate;
        private boolean completed;

        public TaskInfo(String task, String priority, String dueDate, boolean completed) {
            this.task = task;
            this.priority = priority;
            this.dueDate = dueDate;
            this.completed = completed;
        }

        public String getTask() {
            return task;
        }

        public String getPriority() {
            return priority;
        }

        public String getDueDate() {
            return dueDate;
        }

        public boolean isCompleted() {
            return completed;
        }
    }
}
