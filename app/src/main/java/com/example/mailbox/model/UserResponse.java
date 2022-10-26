package com.example.mailbox.model;

import java.util.List;

public class UserResponse {
    private String username;
    private String email;
    private List<Mailbox> mailboxes;

    public UserResponse(String username, String email, List<Mailbox> mailboxes) {
        this.username = username;
        this.email = email;
        this.mailboxes = mailboxes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Mailbox> getMailboxes() {
        return mailboxes;
    }

    public void setMailboxes(List<Mailbox> mailboxes) {
        this.mailboxes = mailboxes;
    }
}
