package com.fergydanny.fergycommandblocker.model;

public class Matcher {
    private boolean matched;
    private String messageMatched;

    public Matcher(boolean matched, String messageMatched) {
        this.matched = matched;
        this.messageMatched = messageMatched;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public String getMessageMatched() {
        return messageMatched;
    }

    public void setMessageMatched(String messageMatched) {
        this.messageMatched = messageMatched;
    }
}
