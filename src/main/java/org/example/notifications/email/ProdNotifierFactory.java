package org.example.notifications.email;

public class ProdNotifierFactory implements INotifierFactory {
    private final SendGridAdapter sendGrid;
    public ProdNotifierFactory(SendGridAdapter sendGrid) { this.sendGrid = sendGrid; }
    public EmailNotifier createEmailNotifier() { return new EmailNotifier(sendGrid); }
}
