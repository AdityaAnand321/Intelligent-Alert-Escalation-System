package com.example.demo.alerts.rules;

import java.util.HashMap;
import java.util.Map;

public class RuleConfig {
    private Map<String, RuleDefinition> rules = new HashMap<>();

    public Map<String, RuleDefinition> getRules() {
        return rules;
    }

    public void setRules(Map<String, RuleDefinition> rules) {
        this.rules = rules;
    }
}
