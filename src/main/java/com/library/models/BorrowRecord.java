package com.library.models;

import java.time.LocalDate;

public class BorrowRecord {

    private int id;
    private String memberName;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;

    public BorrowRecord(int id, String memberName, String bookTitle,
                        LocalDate borrowDate, LocalDate dueDate,
                        LocalDate returnDate, String status) {
        this.id = id;
        this.memberName = memberName;
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOverdue() {
        return "Түрээслэсэн".equals(status)
                && dueDate != null
                && dueDate.isBefore(LocalDate.now());
    }
}