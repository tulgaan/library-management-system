package com.library.controllers;

import com.library.DBConnection;
import com.library.models.Book;
import com.library.models.Member;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class RentDialogController {

    @FXML private ComboBox<Member> cmbMember;
    @FXML private ComboBox<Book> cmbBook;
    @FXML private DatePicker dpDueDate;

    private Stage stage;

    @FXML
    public void initialize() {
        dpDueDate.setValue(LocalDate.now().plusDays(14));
        loadMembers();
        loadBooks();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void loadMembers() {
        var list = FXCollections.<Member>observableArrayList();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement("SELECT * FROM members ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Member(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email")
                ));
            }

            cmbMember.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBooks() {
        var list = FXCollections.<Book>observableArrayList();

        String sql = "SELECT * FROM books WHERE available_qty > 0 ORDER BY title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getInt("available_qty")
                ));
            }

            cmbBook.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSaveClick() {
        Member member = cmbMember.getValue();
        Book book = cmbBook.getValue();
        LocalDate dueDate = dpDueDate.getValue();

        if (member == null || book == null || dueDate == null) {
            showAlert("Уншигч, ном болон буцаах өдрийг сонгоно уу.");
            return;
        }

        if (dueDate.isBefore(LocalDate.now())) {
            showAlert("Буцаах өдөр өнөөдрөөс өмнөх байж болохгүй.");
            return;
        }

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT available_qty FROM books WHERE id=? FOR UPDATE";

            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setInt(1, book.getId());

                ResultSet rs = stmt.executeQuery();

                if (!rs.next() || rs.getInt("available_qty") <= 0) {
                    conn.rollback();
                    showAlert("Энэ ном дууссан байна.");
                    return;
                }
            }

            String insertSql = """
                    INSERT INTO borrow_records
                    (member_id, book_id, borrow_date, due_date, status)
                    VALUES (?, ?, ?, ?, 'Түрээслэсэн')
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, member.getId());
                stmt.setInt(2, book.getId());
                stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                stmt.setDate(4, java.sql.Date.valueOf(dueDate));
                stmt.executeUpdate();
            }

            String updateSql = """
                    UPDATE books
                    SET available_qty = available_qty - 1
                    WHERE id=?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, book.getId());
                stmt.executeUpdate();
            }

            conn.commit();

            if (stage != null) {
                stage.close();
            }

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ignored) {
            }

            e.printStackTrace();
            showAlert("Түрээс бүртгэх үед алдаа гарлаа.");

        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    @FXML
    private void onCancelClick() {
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Анхаар");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
