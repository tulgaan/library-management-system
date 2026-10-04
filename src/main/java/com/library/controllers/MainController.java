package com.library.controllers;

import com.library.DBConnection;
import com.library.models.Book;
import com.library.models.BorrowRecord;
import com.library.models.Member;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class MainController {

    @FXML private TextField txtTitle;
    @FXML private TextField txtAuthor;
    @FXML private TextField txtIsbn;
    @FXML private TextField txtCategory;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtBookSearch;

    @FXML private TableView<Book> tableBooks;
    @FXML private TableColumn<Book, Integer> colBookId;
    @FXML private TableColumn<Book, String> colBookTitle;
    @FXML private TableColumn<Book, String> colBookAuthor;
    @FXML private TableColumn<Book, String> colBookIsbn;
    @FXML private TableColumn<Book, String> colBookCategory;
    @FXML private TableColumn<Book, Integer> colBookQuantity;
    @FXML private TableColumn<Book, Integer> colBookAvailable;

    @FXML private TextField txtMemberName;
    @FXML private TextField txtMemberPhone;
    @FXML private TextField txtMemberEmail;

    @FXML private TableView<Member> tableMembers;
    @FXML private TableColumn<Member, Integer> colMemberId;
    @FXML private TableColumn<Member, String> colMemberName;
    @FXML private TableColumn<Member, String> colMemberPhone;
    @FXML private TableColumn<Member, String> colMemberEmail;

    @FXML private TableView<BorrowRecord> tableBorrows;
    @FXML private TableColumn<BorrowRecord, Integer> colBorrowId;
    @FXML private TableColumn<BorrowRecord, String> colBorrowMember;
    @FXML private TableColumn<BorrowRecord, String> colBorrowBook;
    @FXML private TableColumn<BorrowRecord, LocalDate> colBorrowDate;
    @FXML private TableColumn<BorrowRecord, LocalDate> colDueDate;
    @FXML private TableColumn<BorrowRecord, LocalDate> colReturnDate;
    @FXML private TableColumn<BorrowRecord, String> colStatus;

    @FXML private Label lblTotalBooks;
    @FXML private Label lblBorrowed;
    @FXML private Label lblMembers;

    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private final ObservableList<Member> members = FXCollections.observableArrayList();
    private final ObservableList<BorrowRecord> borrows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupBookTable();
        setupMemberTable();
        setupBorrowTable();

        loadBooks("");
        loadMembers();
        loadBorrows();
        loadStatistics();

        tableBooks.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, selected) -> {
                    if (selected != null) {
                        txtTitle.setText(selected.getTitle());
                        txtAuthor.setText(selected.getAuthor());
                        txtIsbn.setText(selected.getIsbn());
                        txtCategory.setText(selected.getCategory());
                        txtQuantity.setText(String.valueOf(selected.getQuantity()));
                    }
                });

        tableMembers.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, selected) -> {
                    if (selected != null) {
                        txtMemberName.setText(selected.getName());
                        txtMemberPhone.setText(selected.getPhone());
                        txtMemberEmail.setText(selected.getEmail());
                    }
                });

        tableBorrows.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(BorrowRecord item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else if (item.isOverdue()) {
                    setStyle("-fx-background-color: #fecaca;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void setupBookTable() {
        colBookId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());

        colBookTitle.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));

        colBookAuthor.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));

        colBookIsbn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getIsbn()));

        colBookCategory.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));

        colBookQuantity.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantity()).asObject());

        colBookAvailable.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getAvailableQty()).asObject());
    }

    private void setupMemberTable() {
        colMemberId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());

        colMemberName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));

        colMemberPhone.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getPhone()));

        colMemberEmail.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
    }

    private void setupBorrowTable() {
        colBorrowId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());

        colBorrowMember.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getMemberName()));

        colBorrowBook.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getBookTitle()));

        colBorrowDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getBorrowDate()));

        colDueDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDueDate()));

        colReturnDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getReturnDate()));

        colStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
    }

    @FXML
    private void onAddBookClick() {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String isbn = txtIsbn.getText().trim();
        String category = txtCategory.getText().trim();
        String quantityText = txtQuantity.getText().trim();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || quantityText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Алдаа", "Бүх шаардлагатай талбарыг бөглөнө үү.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);

            if (quantity < 1) {
                showAlert(Alert.AlertType.WARNING, "Алдаа", "Тоо ширхэг 1-ээс их байх ёстой.");
                return;
            }

            String sql = "INSERT INTO books (title, author, isbn, category, quantity, available_qty) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, title);
                stmt.setString(2, author);
                stmt.setString(3, isbn);
                stmt.setString(4, category);
                stmt.setInt(5, quantity);
                stmt.setInt(6, quantity);
                stmt.executeUpdate();
            }

            clearBookFields();
            refreshAll();
            showAlert(Alert.AlertType.INFORMATION, "Амжилттай", "Шинэ ном амжилттай бүртгэгдлээ.");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Алдаа", "Тоо ширхэг тоон утга байх ёстой.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Алдаа", "Ном хадгалах үед алдаа гарлаа.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onUpdateBookClick() {
        Book selected = tableBooks.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Анхаар", "Засах номоо сонгоно уу.");
            return;
        }

        try {
            int newQuantity = Integer.parseInt(txtQuantity.getText().trim());
            int borrowedCount = selected.getQuantity() - selected.getAvailableQty();

            if (newQuantity < borrowedCount) {
                showAlert(Alert.AlertType.WARNING, "Алдаа",
                        "Одоогоор түрээслэгдсэн номын тооноос бага нийт тоо оруулах боломжгүй.");
                return;
            }

            int newAvailable = newQuantity - borrowedCount;

            String sql = """
                    UPDATE books
                    SET title=?, author=?, isbn=?, category=?, quantity=?, available_qty=?
                    WHERE id=?
                    """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, txtTitle.getText().trim());
                stmt.setString(2, txtAuthor.getText().trim());
                stmt.setString(3, txtIsbn.getText().trim());
                stmt.setString(4, txtCategory.getText().trim());
                stmt.setInt(5, newQuantity);
                stmt.setInt(6, newAvailable);
                stmt.setInt(7, selected.getId());
                stmt.executeUpdate();
            }

            clearBookFields();
            refreshAll();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Алдаа", "Ном шинэчлэх боломжгүй.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onDeleteBookClick() {
        Book selected = tableBooks.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Анхаар", "Устгах номоо сонгоно уу.");
            return;
        }

        if (selected.getAvailableQty() != selected.getQuantity()) {
            showAlert(Alert.AlertType.WARNING, "Анхаар",
                    "Түрээслэгдсэн номыг устгах боломжгүй.");
            return;
        }

        String sql = "DELETE FROM books WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();

            clearBookFields();
            refreshAll();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Алдаа",
                    "Энэ ном түрээсийн түүхтэй тул устгах боломжгүй.");
        }
    }

    @FXML
    private void onBookSearch() {
        loadBooks(txtBookSearch.getText().trim());
    }

    @FXML
    private void onAddMemberClick() {
        String name = txtMemberName.getText().trim();
        String phone = txtMemberPhone.getText().trim();
        String email = txtMemberEmail.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Алдаа", "Бүх талбарыг бөглөнө үү.");
            return;
        }

        String sql = "INSERT INTO members (name, phone, email) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.executeUpdate();

            clearMemberFields();
            refreshAll();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Алдаа", "Уншигч бүртгэх боломжгүй.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onUpdateMemberClick() {
        Member selected = tableMembers.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Анхаар", "Уншигчаа сонгоно уу.");
            return;
        }

        String sql = "UPDATE members SET name=?, phone=?, email=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, txtMemberName.getText().trim());
            stmt.setString(2, txtMemberPhone.getText().trim());
            stmt.setString(3, txtMemberEmail.getText().trim());
            stmt.setInt(4, selected.getId());
            stmt.executeUpdate();

            clearMemberFields();
            refreshAll();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Алдаа", "Уншигч шинэчлэх боломжгүй.");
        }
    }

    @FXML
    private void onDeleteMemberClick() {
        Member selected = tableMembers.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Анхаар", "Устгах уншигчаа сонгоно уу.");
            return;
        }

        String sql = "DELETE FROM members WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();

            clearMemberFields();
            refreshAll();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Алдаа",
                    "Энэ уншигч түрээсийн түүхтэй тул устгах боломжгүй.");
        }
    }

    @FXML
    private void onOpenRentDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/library/rent_dialog.fxml")
            );

            Parent root = loader.load();

            RentDialogController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Шинэ түрээс");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            controller.setStage(stage);
            stage.showAndWait();

            refreshAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onReturnBook() {
        BorrowRecord selected = tableBorrows.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Анхаар", "Буцаах бүртгэлээ сонгоно уу.");
            return;
        }

        if (!"Түрээслэсэн".equals(selected.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Анхаар", "Энэ ном аль хэдийн буцаагдсан байна.");
            return;
        }

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int bookId;

            String getBook = "SELECT book_id FROM borrow_records WHERE id=?";

            try (PreparedStatement stmt = conn.prepareStatement(getBook)) {
                stmt.setInt(1, selected.getId());

                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    throw new Exception("Book not found");
                }

                bookId = rs.getInt("book_id");
            }

            String updateBorrow = """
                    UPDATE borrow_records
                    SET return_date=?, status='Буцаасан'
                    WHERE id=?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(updateBorrow)) {
                stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
                stmt.setInt(2, selected.getId());
                stmt.executeUpdate();
            }

            String updateBook = """
                    UPDATE books
                    SET available_qty = available_qty + 1
                    WHERE id=?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(updateBook)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }

            conn.commit();

            refreshAll();
            showAlert(Alert.AlertType.INFORMATION, "Амжилттай", "Ном амжилттай буцаагдлаа.");

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ignored) {
            }

            showAlert(Alert.AlertType.ERROR, "Алдаа", "Буцаалт хийх боломжгүй.");
            e.printStackTrace();

        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void loadBooks(String search) {
        books.clear();

        String sql = """
                SELECT *
                FROM books
                WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?
                ORDER BY id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String keyword = "%" + search + "%";

            stmt.setString(1, keyword);
            stmt.setString(2, keyword);
            stmt.setString(3, keyword);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getInt("available_qty")
                ));
            }

            tableBooks.setItems(books);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMembers() {
        members.clear();

        String sql = "SELECT * FROM members ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                members.add(new Member(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email")
                ));
            }

            tableMembers.setItems(members);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBorrows() {
        borrows.clear();

        String sql = """
                SELECT br.id,
                       m.name AS member_name,
                       b.title AS book_title,
                       br.borrow_date,
                       br.due_date,
                       br.return_date,
                       br.status
                FROM borrow_records br
                JOIN members m ON br.member_id = m.id
                JOIN books b ON br.book_id = b.id
                ORDER BY br.id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                java.sql.Date returnDate = rs.getDate("return_date");

                borrows.add(new BorrowRecord(
                        rs.getInt("id"),
                        rs.getString("member_name"),
                        rs.getString("book_title"),
                        rs.getDate("borrow_date").toLocalDate(),
                        rs.getDate("due_date").toLocalDate(),
                        returnDate == null ? null : returnDate.toLocalDate(),
                        rs.getString("status")
                ));
            }

            tableBorrows.setItems(borrows);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement stmt =
                         conn.prepareStatement("SELECT COALESCE(SUM(quantity),0) total FROM books");
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    lblTotalBooks.setText(String.valueOf(rs.getInt("total")));
                }
            }

            try (PreparedStatement stmt =
                         conn.prepareStatement("SELECT COUNT(*) total FROM borrow_records WHERE status='Түрээслэсэн'");
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    lblBorrowed.setText(String.valueOf(rs.getInt("total")));
                }
            }

            try (PreparedStatement stmt =
                         conn.prepareStatement("SELECT COUNT(*) total FROM members");
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    lblMembers.setText(String.valueOf(rs.getInt("total")));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshAll() {
        loadBooks("");
        loadMembers();
        loadBorrows();
        loadStatistics();
    }

    private void clearBookFields() {
        txtTitle.clear();
        txtAuthor.clear();
        txtIsbn.clear();
        txtCategory.clear();
        txtQuantity.clear();
        tableBooks.getSelectionModel().clearSelection();
    }

    private void clearMemberFields() {
        txtMemberName.clear();
        txtMemberPhone.clear();
        txtMemberEmail.clear();
        tableMembers.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}