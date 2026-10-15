import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

interface LibraryOperations {
    void addBook(String title, String author, String issueDate);
    List<String> getAllBooks();
    void updateBook(int id, String title, String author, String issueDate);
    void deleteBook(int id);
    int getTotalBooksCount();
}

class DatabaseHelper implements LibraryOperations {
    private Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:library.db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
        return conn;
    }

    public void createTable() {
        // Naya table banaya hai taaki purane database se error na aaye
        String sql = "CREATE TABLE IF NOT EXISTS advanced_books ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " title TEXT NOT NULL,"
                + " author TEXT NOT NULL,"
                + " issue_date TEXT NOT NULL"
                + ");";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    @Override
    public void addBook(String title, String author, String issueDate) {
        String sql = "INSERT INTO advanced_books(title, author, issue_date) VALUES(?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, issueDate);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding book: " + e.getMessage());
        }
    }

    @Override
    public List<String> getAllBooks() {
        List<String> bookList = new ArrayList<>(); 
        String sql = "SELECT id, title, author, issue_date FROM advanced_books";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bookList.add("ID: " + rs.getInt("id") + " | " + rs.getString("title") + " by " + rs.getString("author") + " | Issued: " + rs.getString("issue_date"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching books: " + e.getMessage());
        }
        return bookList;
    }

    @Override
    public void updateBook(int id, String title, String author, String issueDate) {
        String sql = "UPDATE advanced_books SET title = ?, author = ?, issue_date = ? WHERE id = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, issueDate);
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating book: " + e.getMessage());
        }
    }

    @Override
    public void deleteBook(int id) {
        String sql = "DELETE FROM advanced_books WHERE id = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting book: " + e.getMessage());
        }
    }

    @Override
    public int getTotalBooksCount() {
        String sql = "SELECT COUNT(*) AS total FROM advanced_books";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("Error counting books: " + e.getMessage());
        }
        return 0;
    }
}

public class LibrarySystem extends JFrame {
    private JTextField titleField, authorField, idField, dateField;
    private JTextArea displayArea;
    private JLabel totalBooksLabel;
    private DatabaseHelper dbHelper;

    public LibrarySystem() {
        dbHelper = new DatabaseHelper();
        dbHelper.createTable(); 

        // UI Setup
        setTitle("Pro Library Management System");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(240, 248, 255)); // Light Blue Background

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(25, 25, 112)); // Midnight Blue
        JLabel headerLabel = new JLabel("Library Management System");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        inputPanel.setBackground(new Color(240, 248, 255));

        Font labelFont = new Font("Arial", Font.BOLD, 14);

        JLabel titleLabel = new JLabel("Book Title:");
        titleLabel.setFont(labelFont);
        titleField = new JTextField();
        inputPanel.add(titleLabel);
        inputPanel.add(titleField);

        JLabel authorLabel = new JLabel("Book Author:");
        authorLabel.setFont(labelFont);
        authorField = new JTextField();
        inputPanel.add(authorLabel);
        inputPanel.add(authorField);

        JLabel dateLabel = new JLabel("Issue Date (DD-MM-YYYY):");
        dateLabel.setFont(labelFont);
        dateField = new JTextField();
        inputPanel.add(dateLabel);
        inputPanel.add(dateField);

        JLabel idLabel = new JLabel("Book ID (For Update/Delete):");
        idLabel.setFont(labelFont);
        idField = new JTextField();
        inputPanel.add(idLabel);
        inputPanel.add(idField);

        JButton addButton = createStyledButton("Add Book", new Color(46, 139, 87));
        JButton updateButton = createStyledButton("Update Book", new Color(255, 165, 0));
        JButton deleteButton = createStyledButton("Delete Book", new Color(220, 20, 60));
        JButton viewButton = createStyledButton("View All", new Color(70, 130, 180));

        inputPanel.add(addButton);
        inputPanel.add(updateButton);
        inputPanel.add(deleteButton);
        inputPanel.add(viewButton);

        // Center Panel for Input and Display
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(240, 248, 255));
        centerPanel.add(inputPanel, BorderLayout.NORTH);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        displayArea.setBackground(new Color(255, 255, 240));
        displayArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        centerPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Footer Panel for Total Count
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(240, 248, 255));
        totalBooksLabel = new JLabel("Total Books in Library: " + dbHelper.getTotalBooksCount());
        totalBooksLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalBooksLabel.setForeground(new Color(25, 25, 112));
        footerPanel.add(totalBooksLabel);
        add(footerPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> {
            if(validateInputs(false)) {
                dbHelper.addBook(titleField.getText(), authorField.getText(), dateField.getText());
                showSuccess("Book Added Successfully!");
                refreshData();
            }
        });

        updateButton.addActionListener(e -> {
            if(validateInputs(true)) {
                dbHelper.updateBook(Integer.parseInt(idField.getText()), titleField.getText(), authorField.getText(), dateField.getText());
                showSuccess("Book Updated Successfully!");
                refreshData();
            }
        });

        deleteButton.addActionListener(e -> {
            if(!idField.getText().isEmpty()) {
                dbHelper.deleteBook(Integer.parseInt(idField.getText()));
                showSuccess("Book Deleted Successfully!");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(null, "Please enter Book ID to delete.");
            }
        });

        viewButton.addActionListener(e -> refreshData());
    }

    private void refreshData() {
        displayArea.setText("");
        List<String> books = dbHelper.getAllBooks();
        if(books.isEmpty()) {
            displayArea.append("No books found.\n");
        } else {
            for(String book : books) displayArea.append(book + "\n");
        }
        totalBooksLabel.setText("Total Books in Library: " + dbHelper.getTotalBooksCount());
        clearFields();
    }

    private void clearFields() {
        titleField.setText("");
        authorField.setText("");
        dateField.setText("");
        idField.setText("");
    }

    private boolean validateInputs(boolean requireId) {
        if(titleField.getText().isEmpty() || authorField.getText().isEmpty() || dateField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill Title, Author, and Date.");
            return false;
        }
        if(requireId && idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter Book ID.");
            return false;
        }
        return true;
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(null, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySystem().setVisible(true));
    }
}