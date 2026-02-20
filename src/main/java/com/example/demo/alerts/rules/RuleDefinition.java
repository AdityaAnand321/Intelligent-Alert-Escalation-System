package com.example.demo.alerts.rules;

public class RuleDefinition {
    private Integer escalateIfCount;
    private Integer windowMins;
    private String autoCloseIf;

    public Integer getEscalateIfCount() {
        return escalateIfCount;
    }

    public void setEscalateIfCount(Integer escalateIfCount) {
        this.escalateIfCount = escalateIfCount;
    }

    public Integer getWindowMins() {
        return windowMins;
    }

    public void setWindowMins(Integer windowMins) {
        this.windowMins = windowMins;
    }

    public String getAutoCloseIf() {
        return autoCloseIf;
    }

    public void setAutoCloseIf(String autoCloseIf) {
        this.autoCloseIf = autoCloseIf;
    }
}
