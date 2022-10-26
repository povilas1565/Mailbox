package com.example.mailbox.model;

import java.util.List;

public class Mailbox {
    private Long mailboxId;
    private boolean newMail;
    private String name;
    private List<String> mailHistory;
    private boolean attemptedDeliveryNoticePresent;
    private Double battery;

    public Mailbox(Long mailboxId,
                   boolean newMail,
                   String name,
                   List<String> mailHistory,
                   boolean attemptedDeliveryNoticePresent,
                   Double battery) {
        this.mailboxId = mailboxId;
        this.newMail = newMail;
        this.name = name;
        this.mailHistory = mailHistory;
        this.attemptedDeliveryNoticePresent = attemptedDeliveryNoticePresent;
        this.battery = battery;
    }

    public Long getMailboxId() {
        return mailboxId;
    }

    public void setMailboxId(Long mailboxId) {
        this.mailboxId = mailboxId;
    }

    public boolean isNewMail() {
        return newMail;
    }

    public void setNewMail(boolean newMail) {
        this.newMail = newMail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMailHistory() {
        return mailHistory;
    }

    public void setMailHistory(List<String> mailHistory) {
        this.mailHistory = mailHistory;
    }

    public boolean isAttemptedDeliveryNoticePresent() {
        return attemptedDeliveryNoticePresent;
    }

    public void setAttemptedDeliveryNoticePresent(boolean attemptedDeliveryNoticePresent) {
        this.attemptedDeliveryNoticePresent = attemptedDeliveryNoticePresent;
    }

    public Double getBattery() {
        return battery;
    }

    public void setBattery(Double battery) {
        this.battery = battery;
    }
}
