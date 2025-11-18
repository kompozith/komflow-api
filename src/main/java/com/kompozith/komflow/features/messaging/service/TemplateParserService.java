package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.messaging.entity.MessageVariable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TemplateParserService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Parse and replace variables in the message content for a specific contact
     */
    public String parseTemplate(String template, Contact contact) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        if (contact == null) {
            log.warn("Contact is null, cannot parse template variables");
            return template;
        }

        log.debug("Parsing template for contact ID: {}, person: {}", contact.getId(),
                 contact.getPerson() != null ? "loaded" : "null");

        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String variableKey = matcher.group(0); // e.g., "{{firstName}}"
            String variableName = matcher.group(1); // e.g., "firstName"

            MessageVariable variable = MessageVariable.fromKey(variableKey);
            if (variable == null) {
                log.warn("Unknown variable '{}' found in template", variableKey);
                throw new IllegalArgumentException("Unknown variable: " + variableKey);
            }

            try {
                String value = getFieldValue(contact, variable.getFieldPath());
                log.debug("Variable '{}' with path '{}' resolved to: '{}'",
                         variableKey, variable.getFieldPath(), value != null ? value : "null");
                result = result.replace(variableKey, value != null ? value : "");
            } catch (Exception e) {
                log.warn("Failed to get value for variable '{}' with path '{}': {}",
                        variableKey, variable.getFieldPath(), e.getMessage());
                result = result.replace(variableKey, "");
            }
        }

        return result;
    }

    /**
     * Get field value from contact using dot notation path
     */
    private String getFieldValue(Contact contact, String fieldPath) throws Exception {
        log.debug("Getting field value for path: {} on contact ID: {}", fieldPath, contact.getId());
        String[] pathParts = fieldPath.split("\\.");
        Object currentObject = contact;

        for (String part : pathParts) {
            if (currentObject == null) {
                log.debug("Current object is null for path part: {}", part);
                return null;
            }

            log.debug("Accessing field '{}' on object of type: {}", part, currentObject.getClass().getSimpleName());
            currentObject = getFieldValue(currentObject, part);
        }

        String result = currentObject != null ? currentObject.toString() : null;
        log.debug("Final result for path {}: {}", fieldPath, result);
        return result;
    }

    /**
     * Get field value from object, handling special cases like arrays and collections
     */
    private Object getFieldValue(Object object, String fieldName) throws Exception {
        Class<?> clazz = object.getClass();
        log.debug("Getting field '{}' from object of class: {}", fieldName, clazz.getSimpleName());

        // Handle array/collection access like phoneNumbers[0] or phoneNumbers[whatsapp]
        if (fieldName.contains("[")) {
            int bracketIndex = fieldName.indexOf("[");
            String actualFieldName = fieldName.substring(0, bracketIndex);
            String indexOrKey = fieldName.substring(bracketIndex + 1, fieldName.length() - 1);

            Field field = clazz.getDeclaredField(actualFieldName);
            field.setAccessible(true);
            Object fieldValue = field.get(object);
            log.debug("Field '{}' value: {}", actualFieldName, fieldValue);

            if (fieldValue instanceof List) {
                List<?> list = (List<?>) fieldValue;
                log.debug("List has {} elements", list.size());
                if (indexOrKey.equals("whatsapp")) {
                    // Find first WhatsApp-enabled phone
                    return list.stream()
                            .filter(phone -> {
                                try {
                                    Field isWhatsappField = phone.getClass().getDeclaredField("isWhatsapp");
                                    isWhatsappField.setAccessible(true);
                                    Object isWhatsapp = isWhatsappField.get(phone);
                                    return "true".equalsIgnoreCase(isWhatsapp != null ? isWhatsapp.toString() : null);
                                } catch (Exception e) {
                                    return false;
                                }
                            })
                            .findFirst()
                            .map(phone -> {
                                try {
                                    Field numberField = phone.getClass().getDeclaredField("number");
                                    numberField.setAccessible(true);
                                    return numberField.get(phone);
                                } catch (Exception e) {
                                    return null;
                                }
                            })
                            .orElse(null);
                } else {
                    // Regular index access
                    try {
                        int index = Integer.parseInt(indexOrKey);
                        if (index >= 0 && index < list.size()) {
                            Object item = list.get(index);
                            if (item != null && fieldName.endsWith(".number")) {
                                Field numberField = item.getClass().getDeclaredField("number");
                                numberField.setAccessible(true);
                                return numberField.get(item);
                            }
                            return item;
                        }
                    } catch (NumberFormatException e) {
                        // Not a number, treat as field access
                    }
                }
            }
            return null;
        }

        // Regular field access
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(object);
            log.debug("Field '{}' value: '{}' (type: {})", fieldName, value, value != null ? value.getClass().getSimpleName() : "null");

            // Special debug for Person fields
            if (clazz.getSimpleName().equals("Person") && (fieldName.equals("firstName") || fieldName.equals("lastName") || fieldName.equals("email"))) {
                log.debug("Person field '{}' accessed directly: {}", fieldName, value);
            }

            return value;
        } catch (NoSuchFieldException e) {
            log.warn("Field '{}' not found in class {}. Available fields: {}", fieldName, clazz.getSimpleName(),
                    java.util.Arrays.toString(clazz.getDeclaredFields()));
            throw e;
        } catch (Exception e) {
            log.warn("Error accessing field '{}' on class {}: {}", fieldName, clazz.getSimpleName(), e.getMessage());
            throw e;
        }
    }
}