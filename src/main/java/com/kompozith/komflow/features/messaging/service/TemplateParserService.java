package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.messaging.entity.Message;
import com.kompozith.komflow.features.messaging.entity.MessageVariable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TemplateParserService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    private static final DateTimeFormatter EVENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z", Locale.ENGLISH);
    private static final DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    private static final DateTimeFormatter EVENT_HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final ZoneId GMT_ZONE = ZoneId.of("GMT");

    @Value("${app.public-event-base-url:}")
    private String publicEventBaseUrl;

    /**
     * Parse and replace variables in the message content for a specific contact
     */
    public String parseTemplate(String template, Contact contact) {
        return parseTemplate(template, contact, null, null);
    }

    /**
     * Parse and replace variables in the message content for a specific contact with event time context.
     */
    public String parseTemplate(String template, Contact contact, Instant eventInstantUtc) {
        return parseTemplate(template, contact, null, eventInstantUtc);
    }

    public String parseTemplate(String template, Contact contact, Message message, Instant eventInstantUtc) {
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
            MessageVariable variable = MessageVariable.fromKey(variableKey);
            if (variable == null) {
                log.warn("Unknown variable '{}' found in template", variableKey);
                throw new IllegalArgumentException("Unknown variable: " + variableKey);
            }

            try {
                String value;
                if (variable == MessageVariable.EVENT_LOCAL_TIME
                        || variable == MessageVariable.EVENT_END_LOCAL_TIME
                        || variable == MessageVariable.EVENT_TITLE
                        || variable == MessageVariable.EVENT_START_DATE
                        || variable == MessageVariable.EVENT_START_TIME
                        || variable == MessageVariable.EVENT_END_DATE
                        || variable == MessageVariable.EVENT_END_TIME
                        || variable == MessageVariable.EVENT_LOCATION
                        || variable == MessageVariable.EVENT_TIMEZONE
                        || variable == MessageVariable.EVENT_SUBTITLE
                        || variable == MessageVariable.EVENT_ADDRESS
                        || variable == MessageVariable.EVENT_MEETING_URL
                        || variable == MessageVariable.EVENT_PUBLIC_URL) {
                    value = resolveEventVariable(variable, contact, message, eventInstantUtc);
                } else {
                    value = getFieldValue(contact, variable.getFieldPath());
                }
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

    private String resolveEventVariable(MessageVariable variable, Contact contact, Message message, Instant eventInstantUtc) {
        if (variable == MessageVariable.EVENT_LOCAL_TIME) {
            Instant instantToUse = eventInstantUtc;
            if (instantToUse == null && message != null && message.getFirstEvent() != null) {
                instantToUse = message.getFirstEvent().getStartAt();
            }
            return resolveEventLocalTime(contact, instantToUse);
        }
        if (variable == MessageVariable.EVENT_END_LOCAL_TIME) {
            Instant instantToUse = null;
            if (message != null && message.getFirstEvent() != null) {
                instantToUse = message.getFirstEvent().getEndAt();
                if (instantToUse == null) {
                    instantToUse = message.getFirstEvent().getStartAt();
                }
            }
            if (instantToUse == null) {
                instantToUse = eventInstantUtc;
            }
            return resolveEventLocalTime(contact, instantToUse);
        }

        if (message == null || message.getFirstEvent() == null) {
            return "";
        }

        return switch (variable) {
            case EVENT_TITLE -> safeString(message.getFirstEvent().getTitle());
            case EVENT_START_DATE -> formatEventDate(message.getFirstEvent().getStartAt(), resolveContactZoneId(contact, message.getFirstEvent().getTimezone()));
            case EVENT_START_TIME -> formatEventTime(message.getFirstEvent().getStartAt(), resolveContactZoneId(contact, message.getFirstEvent().getTimezone()));
            case EVENT_END_DATE -> formatEventDate(message.getFirstEvent().getEndAt(), resolveContactZoneId(contact, message.getFirstEvent().getTimezone()));
            case EVENT_END_TIME -> formatEventTime(message.getFirstEvent().getEndAt(), resolveContactZoneId(contact, message.getFirstEvent().getTimezone()));
            case EVENT_LOCATION -> safeString(message.getFirstEvent().getLocation());
            case EVENT_TIMEZONE -> resolveTimezoneLocationLabel(contact, message.getFirstEvent().getTimezone());
            case EVENT_SUBTITLE -> safeString(message.getFirstEvent().getSubtitle());
            case EVENT_ADDRESS -> safeString(message.getFirstEvent().getAddress());
            case EVENT_MEETING_URL -> safeString(message.getFirstEvent().getMeetingUrl());
            case EVENT_PUBLIC_URL -> resolvePublicEventUrl(message);
            default -> "";
        };
    }

    private String resolvePublicEventUrl(Message message) {
        if (message == null || message.getFirstEvent() == null) {
            return "";
        }

        String slug = safeString(message.getFirstEvent().getSlug()).trim();
        if (slug.isEmpty()) {
            return "";
        }

        String baseUrl = publicEventBaseUrl == null ? "" : publicEventBaseUrl.trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        if (baseUrl.isEmpty()) {
            return "/event/" + slug;
        }

        return baseUrl + "/event/" + slug;
    }

    private String formatEventDate(Instant instant, ZoneId zoneId) {
        if (instant == null) {
            return "";
        }
        return EVENT_DATE_FORMATTER.format(instant.atZone(zoneId));
    }

    private String formatEventTime(Instant instant, ZoneId zoneId) {
        if (instant == null) {
            return "";
        }
        return EVENT_HOUR_FORMATTER.format(instant.atZone(zoneId));
    }

    private String resolveEventLocalTime(Contact contact, Instant eventInstantUtc) {
        if (eventInstantUtc == null) {
            return "";
        }
        ZoneId zoneId = resolveContactZoneId(contact, null);
        return EVENT_TIME_FORMATTER.format(eventInstantUtc.atZone(zoneId));
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private ZoneId resolveZoneId(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return GMT_ZONE;
        }
        try {
            return ZoneId.of(timezone.trim());
        } catch (Exception e) {
            return GMT_ZONE;
        }
    }

    private ZoneId resolveContactZoneId(Contact contact, String fallbackTimezone) {
        if (contact != null && contact.getPerson() != null) {
            String contactTimezone = contact.getPerson().getTimezone();
            if (contactTimezone != null && !contactTimezone.isBlank()) {
                try {
                    return ZoneId.of(contactTimezone.trim());
                } catch (Exception e) {
                    log.warn("Invalid contact timezone '{}' for contact {}. Trying fallback timezone.",
                            contactTimezone, contact.getId());
                }
            }
        }

        if (fallbackTimezone != null && !fallbackTimezone.isBlank()) {
            try {
                return ZoneId.of(fallbackTimezone.trim());
            } catch (Exception e) {
                log.warn("Invalid fallback timezone '{}'. Falling back to GMT.", fallbackTimezone);
            }
        }

        return GMT_ZONE;
    }

    private String resolveTimezoneLocationLabel(Contact contact, String fallbackTimezone) {
        String city = null;
        String country = null;

        if (contact != null && contact.getPerson() != null) {
            city = safeString(contact.getPerson().getCity()).trim();
            country = safeString(contact.getPerson().getCountry()).trim();
        }

        if (!city.isEmpty() && !country.isEmpty()) {
            return city + ", " + country;
        }
        if (!city.isEmpty()) {
            return city;
        }
        if (!country.isEmpty()) {
            return country;
        }

        ZoneId zoneId = resolveContactZoneId(contact, fallbackTimezone);
        String zoneIdText = zoneId.getId(); // e.g. America/New_York
        String[] segments = zoneIdText.split("/");
        if (segments.length >= 2) {
            String location = segments[segments.length - 1].replace('_', ' ');
            return location + " (" + zoneIdText + ")";
        }
        return zoneIdText;
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
            if (clazz.getSimpleName().equals("Person") && (fieldName.equals("firstName") || fieldName.equals("lastName") || fieldName.equals("email") || fieldName.equals("language") || fieldName.equals("country") || fieldName.equals("city") || fieldName.equals("timezone"))) {
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
