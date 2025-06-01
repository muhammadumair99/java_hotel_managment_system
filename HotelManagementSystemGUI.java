import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class HotelManagementSystemGUI extends JFrame {
    private static final int MAX_ROOMS = 100;
    private static final int MAX_GUESTS = 100;
    private static final int MAX_RESERVATIONS = 100;
    private static final int MAX_USERS = 100;

    private static final String ROOMS_FILE = "rooms.txt";
    private static final String GUESTS_FILE = "guests.txt";
    private static final String RESERVATIONS_FILE = "reservations.txt";
    private static final String USERS_FILE = "users.txt";

    private static Room[] rooms = new Room[MAX_ROOMS];
    private static Guest[] guests = new Guest[MAX_GUESTS];
    private static Reservation[] reservations = new Reservation[MAX_RESERVATIONS];
    private static User[] users = new User[MAX_USERS];

    private static int roomCount = 0;
    private static int guestCount = 0;
    private static int reservationCount = 0;
    private static int userCount = 0;

    private String currentUserRole;

    public static void main(String[] args) {
        loadAllData();
        SwingUtilities.invokeLater(() -> new HotelManagementSystemGUI().setVisible(true));
    }

    public HotelManagementSystemGUI() {
        setTitle("Hotel Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginScreen();
    }

    private void loginScreen() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4, 2));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        JButton loginBtn = new JButton("Login");
        panel.add(loginBtn);

        add(panel, BorderLayout.CENTER);

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            User user = findUser(username, password);
            if (user != null) {
                currentUserRole = user.getRole();
                showMainMenu();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.");
            }
        });

        revalidate();
        repaint();
    }

    private void showMainMenu() {
        getContentPane().removeAll();
        setLayout(new GridLayout(0, 1));

        add(createButton("🏨 Room Management", e -> roomManagement()));
        add(createButton("👤 Guest Management", e -> guestManagement()));
        add(createButton("📅 Reservation Management", e -> reservationManagement()));

        if ("Admin".equalsIgnoreCase(currentUserRole)) {
            add(createButton("👥 User Management", e -> userManagement()));
        }

        add(createButton("Logout", e -> {
            currentUserRole = null;
            loginScreen();
        }));

        revalidate();
        repaint();
    }

    private JButton createButton(String title, ActionListener action) {
        JButton btn = new JButton(title);
        btn.addActionListener(action);
        return btn;
    }

    // --- Room Management ---
    private void roomManagement() {
        JFrame frame = new JFrame("Room Management");
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Number", "Type", "Rate", "Occupied"}, 0);
        for (int i = 0; i < roomCount; i++) {
            Room r = rooms[i];
            model.addRow(new Object[]{r.getRoomNumber(), r.getType(), r.getDailyRate(), r.isOccupied()});
        }

        JTable table = new JTable(model);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton addBtn = new JButton("Add Room");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");

        bottom.add(addBtn);
        bottom.add(editBtn);
        bottom.add(deleteBtn);
        frame.add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            JTextField numberField = new JTextField();
            JTextField typeField = new JTextField();
            JTextField rateField = new JTextField();
            Object[] fields = {"Room Number:", numberField, "Type:", typeField, "Daily Rate:", rateField};
            int result = JOptionPane.showConfirmDialog(frame, fields, "Add Room", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                int number = Integer.parseInt(numberField.getText());
                String type = typeField.getText();
                double rate = Double.parseDouble(rateField.getText());
                if (roomCount < MAX_ROOMS) {
                    rooms[roomCount++] = new Room(number, type, rate);
                    saveRoomsToFile();
                    frame.dispose();
                    roomManagement();
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                for (int j = selectedRow; j < roomCount - 1; j++) {
                    rooms[j] = rooms[j + 1];
                }
                rooms[--roomCount] = null;
                saveRoomsToFile();
                frame.dispose();
                roomManagement();
            }
        });

        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Room r = rooms[selectedRow];
                JTextField typeField = new JTextField(r.getType());
                JTextField rateField = new JTextField(String.valueOf(r.getDailyRate()));
                Object[] fields = {"Type:", typeField, "Daily Rate:", rateField};
                int result = JOptionPane.showConfirmDialog(frame, fields, "Edit Room", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    r.setType(typeField.getText());
                    r.setDailyRate(Double.parseDouble(rateField.getText()));
                    saveRoomsToFile();
                    frame.dispose();
                    roomManagement();
                }
            }
        });

        frame.setVisible(true);
    }

    // --- Guest Management ---
    private void guestManagement() {
        JFrame frame = new JFrame("Guest Management");
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Name", "ID", "Contact", "Room No"}, 0);
        for (int i = 0; i < guestCount; i++) {
            Guest g = guests[i];
            model.addRow(new Object[]{g.getName(), g.getId(), g.getContact(), g.getRoomNumber()});
        }

        JTable table = new JTable(model);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton checkInBtn = new JButton("Check-In");
        JButton checkOutBtn = new JButton("Check-Out");
        JButton searchBtn = new JButton("Search");

        bottom.add(checkInBtn);
        bottom.add(checkOutBtn);
        bottom.add(searchBtn);
        frame.add(bottom, BorderLayout.SOUTH);

        checkInBtn.addActionListener(e -> {
            JTextField name = new JTextField();
            JTextField id = new JTextField();
            JTextField contact = new JTextField();
            JTextField roomField = new JTextField();
            Object[] fields = {"Name:", name, "ID:", id, "Contact:", contact, "Room No:", roomField};
            int result = JOptionPane.showConfirmDialog(frame, fields, "Check-In", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                int roomNum = Integer.parseInt(roomField.getText());
                Room r = findRoom(roomNum);
                if (r != null && !r.isOccupied() && guestCount < MAX_GUESTS) {
                    guests[guestCount++] = new Guest(name.getText(), id.getText(), contact.getText(), roomNum);
                    r.setOccupied(true);
                    saveGuestsToFile();
                    saveRoomsToFile();
                    frame.dispose();
                    guestManagement();
                } else {
                    JOptionPane.showMessageDialog(frame, "Room invalid or occupied.");
                }
            }
        });

        checkOutBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int roomNum = guests[selectedRow].getRoomNumber();
                for (int j = selectedRow; j < guestCount - 1; j++) guests[j] = guests[j + 1];
                guests[--guestCount] = null;
                Room r = findRoom(roomNum);
                if (r != null) r.setOccupied(false);
                saveGuestsToFile();
                saveRoomsToFile();
                frame.dispose();
                guestManagement();
            }
        });

        searchBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog("Enter Guest Name or ID:");
            for (int i = 0; i < guestCount; i++) {
                Guest g = guests[i];
                if (g.getName().equalsIgnoreCase(input) || g.getId().equalsIgnoreCase(input)) {
                    JOptionPane.showMessageDialog(frame, "Guest Found: " + g.getName() + " in Room " + g.getRoomNumber());
                    return;
                }
            }
            JOptionPane.showMessageDialog(frame, "Guest not found.");
        });

        frame.setVisible(true);
    }

    // --- Reservation Management ---
    private void reservationManagement() {
        JFrame frame = new JFrame("Reservation Management");
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Name", "Room No", "Check-In", "Check-Out"}, 0);
        for (int i = 0; i < reservationCount; i++) {
            Reservation r = reservations[i];
            model.addRow(new Object[]{r.getGuestName(), r.getRoomNumber(), r.getCheckInDate(), r.getCheckOutDate()});
        }

        JTable table = new JTable(model);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton addBtn = new JButton("Reserve");
        JButton cancelBtn = new JButton("Cancel");

        bottom.add(addBtn);
        bottom.add(cancelBtn);
        frame.add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            JTextField name = new JTextField();
            JTextField room = new JTextField();
            JTextField in = new JTextField();
            JTextField out = new JTextField();
            Object[] fields = {"Guest Name:", name, "Room No:", room, "Check-In:", in, "Check-Out:", out};
            int result = JOptionPane.showConfirmDialog(frame, fields, "Reserve", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                int roomNum = Integer.parseInt(room.getText());
                Room r = findRoom(roomNum);
                if (r != null && !r.isOccupied() && reservationCount < MAX_RESERVATIONS) {
                    reservations[reservationCount++] = new Reservation(name.getText(), roomNum, in.getText(), out.getText());
                    r.setOccupied(true);
                    saveReservationsToFile();
                    saveRoomsToFile();
                    frame.dispose();
                    reservationManagement();
                }
            }
        });

        cancelBtn.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected >= 0) {
                int roomNum = reservations[selected].getRoomNumber();
                for (int j = selected; j < reservationCount - 1; j++) reservations[j] = reservations[j + 1];
                reservations[--reservationCount] = null;
                Room r = findRoom(roomNum);
                if (r != null) r.setOccupied(false);
                saveReservationsToFile();
                saveRoomsToFile();
                frame.dispose();
                reservationManagement();
            }
        });

        frame.setVisible(true);
    }

    // --- User Management ---
    private void userManagement() {
        JFrame frame = new JFrame("User Management");
        frame.setSize(500, 300);
        frame.setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Username", "Role"}, 0);
        for (int i = 0; i < userCount; i++) {
            User u = users[i];
            model.addRow(new Object[]{u.getUsername(), u.getRole()});
        }

        JTable table = new JTable(model);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton removeBtn = new JButton("Remove");

        bottom.add(addBtn);
        bottom.add(removeBtn);
        frame.add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            JTextField username = new JTextField();
            JTextField password = new JTextField();
            JTextField role = new JTextField();
            Object[] fields = {"Username:", username, "Password:", password, "Role (Admin/User):", role};
            int result = JOptionPane.showConfirmDialog(frame, fields, "Add User", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION && userCount < MAX_USERS) {
                users[userCount++] = new User(username.getText(), password.getText(), role.getText());
                saveUsersToFile();
                frame.dispose();
                userManagement();
            }
        });

        removeBtn.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected >= 0) {
                for (int j = selected; j < userCount - 1; j++) users[j] = users[j + 1];
                users[--userCount] = null;
                saveUsersToFile();
                frame.dispose();
                userManagement();
            }
        });

        frame.setVisible(true);
    }

    // --- Backend Logic and File Handling ---
    private static Room findRoom(int roomNum) {
        for (int i = 0; i < roomCount; i++)
            if (rooms[i].getRoomNumber() == roomNum) return rooms[i];
        return null;
    }

    private static User findUser(String username, String password) {
        for (int i = 0; i < userCount; i++) {
            if (users[i].getUsername().equals(username) && users[i].getPassword().equals(password))
                return users[i];
        }
        return null;
    }

    private static void loadAllData() {
        loadRoomsFromFile();
        loadGuestsFromFile();
        loadReservationsFromFile();
        loadUsersFromFile();
    }

    private static void saveRoomsToFile() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(ROOMS_FILE))) {
            for (int i = 0; i < roomCount; i++) {
                Room r = rooms[i];
                w.write(r.getRoomNumber() + "," + r.getType() + "," + r.getDailyRate() + "," + r.isOccupied());
                w.newLine();
            }
        } catch (IOException ignored) {}
    }

    private static void loadRoomsFromFile() {
        try (BufferedReader r = new BufferedReader(new FileReader(ROOMS_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",");
                Room room = new Room(Integer.parseInt(p[0]), p[1], Double.parseDouble(p[2]));
                room.setOccupied(Boolean.parseBoolean(p[3]));
                rooms[roomCount++] = room;
            }
        } catch (IOException ignored) {}
    }

    private static void saveGuestsToFile() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(GUESTS_FILE))) {
            for (int i = 0; i < guestCount; i++) {
                Guest g = guests[i];
                w.write(g.getName() + "," + g.getId() + "," + g.getContact() + "," + g.getRoomNumber());
                w.newLine();
            }
        } catch (IOException ignored) {}
    }

    private static void loadGuestsFromFile() {
        try (BufferedReader r = new BufferedReader(new FileReader(GUESTS_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",");
                guests[guestCount++] = new Guest(p[0], p[1], p[2], Integer.parseInt(p[3]));
            }
        } catch (IOException ignored) {}
    }

    private static void saveReservationsToFile() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(RESERVATIONS_FILE))) {
            for (int i = 0; i < reservationCount; i++) {
                Reservation res = reservations[i];
                w.write(res.getGuestName() + "," + res.getRoomNumber() + "," + res.getCheckInDate() + "," + res.getCheckOutDate());
                w.newLine();
            }
        } catch (IOException ignored) {}
    }

    private static void loadReservationsFromFile() {
        try (BufferedReader r = new BufferedReader(new FileReader(RESERVATIONS_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",");
                reservations[reservationCount++] = new Reservation(p[0], Integer.parseInt(p[1]), p[2], p[3]);
            }
        } catch (IOException ignored) {}
    }

    private static void saveUsersToFile() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (int i = 0; i < userCount; i++) {
                User u = users[i];
                w.write(u.getUsername() + "," + u.getPassword() + "," + u.getRole());
                w.newLine();
            }
        } catch (IOException ignored) {}
    }

    private static void loadUsersFromFile() {
        try (BufferedReader r = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",");
                users[userCount++] = new User(p[0], p[1], p[2]);
            }
        } catch (IOException ignored) {}
    }

    // --- Data Classes ---
    static class Room {
        private int roomNumber;
        private String type;
        private double dailyRate;
        private boolean occupied;

        public Room(int num, String t, double rate) {
            roomNumber = num;
            type = t;
            dailyRate = rate;
        }

        public int getRoomNumber() { return roomNumber; }
        public String getType() { return type; }
        public double getDailyRate() { return dailyRate; }
        public boolean isOccupied() { return occupied; }

        public void setType(String t) { type = t; }
        public void setDailyRate(double r) { dailyRate = r; }
        public void setOccupied(boolean o) { occupied = o; }
    }

    static class Guest {
        private String name, id, contact;
        private int roomNumber;

        public Guest(String n, String i, String c, int r) {
            name = n; id = i; contact = c; roomNumber = r;
        }

        public String getName() { return name; }
        public String getId() { return id; }
        public String getContact() { return contact; }
        public int getRoomNumber() { return roomNumber; }
    }

    static class Reservation {
        private String guestName, checkInDate, checkOutDate;
        private int roomNumber;

        public Reservation(String g, int r, String in, String out) {
            guestName = g; roomNumber = r; checkInDate = in; checkOutDate = out;
        }

        public String getGuestName() { return guestName; }
        public int getRoomNumber() { return roomNumber; }
        public String getCheckInDate() { return checkInDate; }
        public String getCheckOutDate() { return checkOutDate; }
    }

    static class User {
        private String username, password, role;

        public User(String u, String p, String r) {
            username = u; password = p; role = r;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getRole() { return role; }
    }
}
