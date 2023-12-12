import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;




public class EventCalendarManagement extends JFrame {
    private List<Event> events = new ArrayList<>();
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, detailsButton, editDetailsButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventCalendarManagement().setVisible(true));
    }

    public EventCalendarManagement() {
        setTitle("Event Calendar Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        initComponents();
        loadEventsFromDatabase();
    }

    private void initComponents() {
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Number");
        tableModel.addColumn("Event Name");
        tableModel.addColumn("Event Date");
        tableModel.addColumn("Event Details");

        eventTable = new JTable(tableModel);
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(50);

        addButton = new JButton("Add Event");
        editButton = new JButton("Edit Event");
        deleteButton = new JButton("Delete Event");
        detailsButton = new JButton("Event Details");
        editDetailsButton = new JButton("Edit Details");

        setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(addButton);
        controlPanel.add(editButton);
        controlPanel.add(deleteButton);
        controlPanel.add(detailsButton);
        controlPanel.add(editDetailsButton);

        add(new JScrollPane(eventTable), BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Event newEvent = createNewEvent();
                if (newEvent != null) {

                    try {
                        Connection connection = new DatabaseConnector().getConnection();
                        PreparedStatement statement = connection.prepareStatement("INSERT INTO events (name, event_date, details) VALUES (?, ?, ?)");
                        statement.setString(1, newEvent.getName());
                        statement.setDate(2, new java.sql.Date(newEvent.getDate().getTime()));
                        statement.setString(3, newEvent.getDetails());
                        statement.executeUpdate();
                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    events.add(newEvent);
                    Collections.sort(events);
                    updateEventTable();
                }
            }
        });

        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Event selectedEvent = getSelectedEvent();
                if (selectedEvent != null) {
                    editEvent(selectedEvent);
                    try {
                        Connection connection = new DatabaseConnector().getConnection();
                        PreparedStatement statement = connection.prepareStatement("UPDATE events SET name=?, event_date=?, details=? WHERE id=?");
                        statement.setString(1, selectedEvent.getName());
                        statement.setDate(2, new java.sql.Date(selectedEvent.getDate().getTime()));
                        statement.setString(3, selectedEvent.getDetails());
                        statement.setInt(4, selectedEvent.getId());
                        statement.executeUpdate();
                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    Collections.sort(events);
                    updateEventTable();
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Event selectedEvent = getSelectedEvent();
                if (selectedEvent != null) {
                    try {
                        Connection connection = new DatabaseConnector().getConnection();
                        PreparedStatement statement = connection.prepareStatement("DELETE FROM events WHERE id=?");
                        statement.setInt(1, selectedEvent.getId()); // Assuming 'id' is the primary key in the events table
                        statement.executeUpdate();
                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    events.remove(selectedEvent);
                    updateEventTable();
                }
            }
        });

        detailsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Event selectedEvent = getSelectedEvent();
                if (selectedEvent != null) {
                    showEventDetails(selectedEvent);
                }
            }
        });

        editDetailsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Event selectedEvent = getSelectedEvent();
                if (selectedEvent != null) {
                    editEventDetails(selectedEvent);
                    try {
                        Connection connection = new DatabaseConnector().getConnection();
                        PreparedStatement statement = connection.prepareStatement("UPDATE events SET details=? WHERE id=?");
                        statement.setString(1, selectedEvent.getDetails());
                        statement.setInt(2, selectedEvent.getId());
                        statement.executeUpdate();
                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    Collections.sort(events);
                    updateEventTable();
                }
            }
        });
    }

    private Event createNewEvent() {
        String name = JOptionPane.showInputDialog(this, "Enter event name:");
        if (name != null && !name.trim().isEmpty()) {
            String dateStr = JOptionPane.showInputDialog(this, "Enter event date (dd-MM-yyyy):");
            if (isValidDate(dateStr)) {
                String details = JOptionPane.showInputDialog(this, "Enter event details (optional):");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    Date date = dateFormat.parse(dateStr);
                    return new Event(-1,name, date, details);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "An error occurred while parsing the date. Event not added.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid date format. Event not added.");
            }
        }
        return null;
    }

    private boolean isValidDate(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false);

        try {
            dateFormat.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private Event getSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow != -1) {
            int selectedEventIndex = (int) eventTable.getValueAt(selectedRow, 0) - 1;
            if (selectedEventIndex >= 0 && selectedEventIndex < events.size()) {
                return events.get(selectedEventIndex);
            }
        }
        return null;
    }

    private void editEvent(Event event) {
        String newName = JOptionPane.showInputDialog(this, "Enter new event name:", event.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            String newDateStr = JOptionPane.showInputDialog(this, "Enter new event date (dd-MM-yyyy):", event.getDateStr());
            if (isValidDate(newDateStr)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    Date newDate = dateFormat.parse(newDateStr);
                    event.setName(newName);
                    event.setDate(newDate);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "An error occurred while parsing the date. Event not edited.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid date format. Event not edited.");
            }
        }
    }

    private void editEventDetails(Event event) {
        String newDetails = JOptionPane.showInputDialog(this, "Edit event details:", event.getDetails());
        if (newDetails != null) {
            event.setDetails(newDetails);
            updateEventTable();
        }
    }

    private void showEventDetails(Event event) {
        JTextArea textArea = new JTextArea();
        textArea.setText("Event Name: " + event.getName() + "\nEvent Date: " + event.getDateStr() +
                "\nEvent Details:\n" + wrapText(event.getDetails(), 60)); 

        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane);
    }

    private String wrapText(String input, int maxLineLength) {
        String[] words = input.split(" ");
        StringBuilder result = new StringBuilder();

        int lineLength = 0;
        for (String word : words) {
            if (lineLength + word.length() <= maxLineLength) {
                result.append(word).append(" ");
                lineLength += word.length() + 1;
            } else {
                result.append("\n").append(word).append(" ");
                lineLength = word.length() + 1;
            }
        }

        return result.toString();
    }

    private void updateEventTable() {
        tableModel.setRowCount(0);
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            tableModel.addRow(new Object[]{i + 1, event.getName(), event.getDateStr(), event.getDetails()});
        }
    }

    public class Event implements Comparable<Event> {
        private int id;
        private String name;
        private Date date;
        private String details;

        public Event(int id, String name, Date date, String details) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.details = details;
        }

        // Getters and setters for the id field
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public String getDateStr() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            return dateFormat.format(date);
        }

        @Override
        public String toString() {
            return "Event: " + name + " Date: " + getDateStr() + " Details: " + details;
        }

        @Override
        public int compareTo(Event otherEvent) {
            return this.date.compareTo(otherEvent.date);
        }
    }

    private void loadEventsFromDatabase() {
        try {
            Connection connection = new DatabaseConnector().getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT id, name, event_date, details FROM events");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                Date date = resultSet.getDate("event_date");
                String details = resultSet.getString("details");

                Event event = new Event(id, name, date, details);
                events.add(event);
            }

            connection.close();
            updateEventTable(); // Update the table after loading events
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
