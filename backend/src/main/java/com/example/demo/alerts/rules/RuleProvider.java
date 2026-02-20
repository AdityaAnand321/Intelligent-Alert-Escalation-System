package com.example.demo.alerts.rules;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.example.demo.alerts.model.SourceType;

@Component
public class RuleProvider {

    private final ResourceLoader resourceLoader;

    public RuleProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public RuleDefinition forSource(SourceType sourceType) {
        Map<String, RuleDefinition> rules = loadRules();
        return rules.get(keyFromSource(sourceType));
    }

    public Map<String, RuleDefinition> allRules() {
        return loadRules();
    }

    private Map<String, RuleDefinition> loadRules() {
        try {
            Resource resource = resourceLoader.getResource("classpath:rules.json");
            try (InputStream inputStream = resource.getInputStream()) {
                String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                JsonParser parser = JsonParserFactory.getJsonParser();
                Map<String, Object> root = parser.parseMap(json);
                Object rulesNode = root.get("rules");
                if (!(rulesNode instanceof Map<?, ?> rulesMap)) {
                    return Map.of();
                }

                Map<String, RuleDefinition> loaded = new HashMap<>();
                for (Map.Entry<?, ?> entry : rulesMap.entrySet()) {
                    if (!(entry.getKey() instanceof String key) || !(entry.getValue() instanceof Map<?, ?> valuesRaw)) {
                        continue;
                    }

                    Map<String, Object> values = new LinkedHashMap<>();
                    valuesRaw.forEach((k, v) -> {
                        if (k instanceof String str) {
                            values.put(str, v);
                        }
                    });

                    RuleDefinition rule = new RuleDefinition();
                    Object escalateIfCount = values.get("escalateIfCount");
                    Object windowMins = values.get("windowMins");
                    Object autoCloseIf = values.get("autoCloseIf");

                    if (escalateIfCount instanceof Number n) {
                        rule.setEscalateIfCount(n.intValue());
                    }
                    if (windowMins instanceof Number n) {
                        rule.setWindowMins(n.intValue());
                    }
                    if (autoCloseIf instanceof String s) {
                        rule.setAutoCloseIf(s);
                    }

                    loaded.put(key, rule);
                }
                return loaded;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load rules.json", ex);
        }
    }

    private String keyFromSource(SourceType sourceType) {
        return switch (sourceType) {
            case OVERSPEEDING -> "overspeed";
            case NEGATIVE_FEEDBACK -> "feedback_negative";
            case COMPLIANCE -> "compliance";
        };
    }
}
