package com.kompozith.komflow.features.contact.service;

import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.dto.ContactDetailsDto;
import com.kompozith.komflow.features.contact.dto.ContactDto;
import com.kompozith.komflow.features.contact.dto.ContactImportResultDto;
import com.kompozith.komflow.features.contact.dto.ContactWithTagCountDto;
import com.kompozith.komflow.features.contact.dto.CreateContactDto;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.mapper.ContactMapper;
import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.core.service.BaseService;
import com.kompozith.komflow.features.personnel.dto.CreatePersonDto;
import com.kompozith.komflow.features.personnel.dto.CreatePhoneNumberDto;
import com.kompozith.komflow.features.personnel.entity.Person;
import com.kompozith.komflow.features.personnel.entity.PhoneNumber;
import com.kompozith.komflow.features.personnel.repository.PersonRepository;
import com.kompozith.komflow.features.personnel.repository.PhoneNumberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl extends BaseService implements ContactService {

    private static final List<String> EXPORT_HEADERS = List.of(
            "First Name",
            "Last Name",
            "Email",
            "Phone Number",
            "Language",
            "Country",
            "City",
            "Timezone",
            "Enabled",
            "Tag IDs",
            "Created At"
    );

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final PersonRepository personRepository;
    private final TagRepository tagRepository;
    private final PhoneNumberRepository phoneNumberRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public ContactDto create(CreateContactDto createContactDto) {
        Contact contact = contactMapper.createContactDtoToContact(createContactDto);

        Person person = resolvePersonForCreate(createContactDto);
        contact.setPerson(person);

        if (createContactDto.getTagIds() != null && !createContactDto.getTagIds().isEmpty()) {
            contact.setTags(new HashSet<>(tagRepository.findAllById(createContactDto.getTagIds())));
        }

        return contactMapper.contactToContactDto(contactRepository.save(contact));
    }

    @Override
    public List<ContactDto> findAll() {
        return contactRepository.findAll().stream().map(contactMapper::contactToContactDto).collect(Collectors.toList());
    }

    @Override
    public Page<ContactWithTagCountDto> findAll(Pageable pageable, String search, Boolean enabled, Instant createdAtFrom, Instant createdAtTo, String tagIds) {
        String normalizedTagIds = normalizeTagIds(tagIds);
        return contactRepository.findWithFiltersAndTagCount(search, enabled, createdAtFrom, createdAtTo, normalizedTagIds, pageable);
    }

    @Override
    public ContactDetailsDto findById(Long id) {
        Contact contact = contactRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new ObjectNotFoundException(Contact.class.getSimpleName(), id));
        return contactMapper.contactToContactDetailsDto(contact);
    }

    @Override
    public ContactDto update(Long id, CreateContactDto createContactDto) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Contact.class.getSimpleName(), id));

        Contact alreadyExistedContact = contactRepository.findByPersonId(createContactDto.getPersonId()).orElse(null);

        if (createContactDto.getPersonId() != null) {
            if (alreadyExistedContact != null && !alreadyExistedContact.getId().equals(contact.getId())) {
                throw new ObjectExistException(Contact.class.getSimpleName(), "personId", createContactDto.getPersonId().toString());
            }
        }

        contact.setEnabled(createContactDto.isEnabled());
        contact.setLastMessageReceivedAt(createContactDto.getLastMessageReceivedAt());

        if (createContactDto.getPersonId() != null) {
            Person person = personRepository.findById(createContactDto.getPersonId())
                    .orElseThrow(() -> new ObjectNotFoundException(Person.class.getSimpleName(), createContactDto.getPersonId()));
            contact.setPerson(person);
        }

        if (createContactDto.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(createContactDto.getTagIds()));
            contact.setTags(tags);
        }

        Contact updatedContact = contactRepository.save(contact);
        return contactMapper.contactToContactDto(updatedContact);
    }

    @Override
    public void delete(Long id) {
        contactRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Contact.class.getSimpleName(), id));

        contactRepository.deleteById(id);
    }

    @Override
    public byte[] exportContacts(String format, String search, Boolean enabled, Instant createdAtFrom, Instant createdAtTo, String tagIds) {
        String normalizedFormat = format == null ? "csv" : format.trim().toLowerCase(Locale.ROOT);
        if (!"csv".equals(normalizedFormat) && !"xlsx".equals(normalizedFormat)) {
            throw new IllegalArgumentException("Unsupported format. Use csv or xlsx.");
        }

        String normalizedTagIds = normalizeTagIds(tagIds);
        Page<ContactWithTagCountDto> page = contactRepository.findWithFiltersAndTagCount(
                search,
                enabled,
                createdAtFrom,
                createdAtTo,
                normalizedTagIds,
                Pageable.unpaged()
        );

        List<Long> ids = page.getContent().stream()
                .map(ContactWithTagCountDto::getId)
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            return "xlsx".equals(normalizedFormat) ? buildXlsx(List.of()) : buildCsv(List.of());
        }

        Map<Long, Contact> contactsById = contactRepository.findAllByIdInWithAssociations(ids).stream()
                .collect(Collectors.toMap(Contact::getId, contact -> contact));

        List<Contact> orderedContacts = ids.stream()
                .map(contactsById::get)
                .filter(contact -> contact != null)
                .toList();

        return "xlsx".equals(normalizedFormat) ? buildXlsx(orderedContacts) : buildCsv(orderedContacts);
    }

    @Override
    public ContactImportResultDto importContacts(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The import file is empty.");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        List<Map<String, String>> rows;

        try {
            if (originalName.endsWith(".csv")) {
                rows = parseCsvRows(file.getInputStream());
            } else if (originalName.endsWith(".xlsx")) {
                rows = parseXlsxRows(file.getInputStream());
            } else {
                throw new IllegalArgumentException("Unsupported file type. Use .csv or .xlsx.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read import file.");
        }

        int imported = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        reseedContactSequenceIfPossible();

        for (int i = 0; i < rows.size(); i++) {
            int lineNumber = i + 2;
            try {
                CreateContactDto dto = mapRowToCreateContact(rows.get(i));
                ImportHandlingResult result = processImportedContact(dto);
                if (result == ImportHandlingResult.CREATED) {
                    imported++;
                } else if (result == ImportHandlingResult.UPDATED) {
                    updated++;
                } else {
                    skipped++;
                }
            } catch (ObjectExistException ex) {
                skipped++;
            } catch (Exception ex) {
                failed++;
                errors.add("Line " + lineNumber + ": " + extractErrorMessage(ex));
            }
        }

        return new ContactImportResultDto(imported, updated, skipped, failed, errors);
    }

    private Person resolvePersonForCreate(CreateContactDto createContactDto) {
        if (createContactDto.getPersonId() != null) {
            if (contactRepository.findByPersonId(createContactDto.getPersonId()).isPresent()) {
                throw new ObjectExistException(Contact.class.getSimpleName(), "personId", createContactDto.getPersonId().toString());
            }

            return personRepository.findById(createContactDto.getPersonId())
                    .orElseThrow(() -> new ObjectNotFoundException("Person", createContactDto.getPersonId()));
        }

        CreatePersonDto personDto = createContactDto.getPerson();
        if (personDto == null) {
            throw new IllegalArgumentException("contact.person.selection.invalid");
        }

        if (personRepository.findByEmail(personDto.getEmail()).isPresent()) {
            throw new ObjectExistException(Person.class.getSimpleName(), "email", personDto.getEmail());
        }

        Person person = new Person();
        person.setEmail(personDto.getEmail());
        person.setFirstName(personDto.getFirstName());
        person.setLastName(personDto.getLastName());
        person.setLanguage(normalizeLanguage(personDto.getLanguage()));
        person.setCountry(personDto.getCountry());
        person.setCity(personDto.getCity());
        person.setTimezone(personDto.getTimezone());

        if (createContactDto.getPhoneNumbers() != null && !createContactDto.getPhoneNumbers().isEmpty()) {
            List<PhoneNumber> phoneNumbers = new ArrayList<>();
            for (CreatePhoneNumberDto phoneDto : createContactDto.getPhoneNumbers()) {
                if (phoneDto.getNumber() == null || phoneDto.getNumber().isBlank()) {
                    continue;
                }

                phoneNumberRepository.findByNumber(phoneDto.getNumber()).ifPresent(existing -> {
                    throw new IllegalArgumentException("Phone number already exists: " + phoneDto.getNumber());
                });

                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setNumber(phoneDto.getNumber());
                phoneNumber.setIsWhatsapp(phoneDto.getIsWhatsapp() != null ? phoneDto.getIsWhatsapp().toString() : "false");
                phoneNumber.setPerson(person);
                phoneNumbers.add(phoneNumber);
            }

            person.setPhoneNumbers(phoneNumbers);
        }

        return personRepository.save(person);
    }

    private String normalizeTagIds(String tagIds) {
        if (tagIds == null) {
            return null;
        }
        String trimmed = tagIds.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private byte[] buildCsv(List<Contact> contacts) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join(",", EXPORT_HEADERS)).append("\n");

        for (Contact contact : contacts) {
            List<String> columns = List.of(
                    nullSafe(contact.getPerson().getFirstName()),
                    nullSafe(contact.getPerson().getLastName()),
                    nullSafe(contact.getPerson().getEmail()),
                    getFirstPhoneNumber(contact),
                    nullSafe(contact.getPerson().getLanguage()),
                    nullSafe(contact.getPerson().getCountry()),
                    nullSafe(contact.getPerson().getCity()),
                    nullSafe(contact.getPerson().getTimezone()),
                    String.valueOf(contact.isEnabled()),
                    contact.getTags() == null
                            ? ""
                            : contact.getTags().stream().map(Tag::getId).sorted().map(String::valueOf).collect(Collectors.joining(";")),
                    contact.getCreatedAt() == null ? "" : contact.getCreatedAt().toString()
            );

            builder.append(columns.stream().map(this::escapeCsv).collect(Collectors.joining(","))).append("\n");
        }

        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildXlsx(List<Contact> contacts) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Contacts");

            Row header = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.size(); i++) {
                header.createCell(i).setCellValue(EXPORT_HEADERS.get(i));
            }

            int rowIndex = 1;
            for (Contact contact : contacts) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(nullSafe(contact.getPerson().getFirstName()));
                row.createCell(1).setCellValue(nullSafe(contact.getPerson().getLastName()));
                row.createCell(2).setCellValue(nullSafe(contact.getPerson().getEmail()));
                row.createCell(3).setCellValue(getFirstPhoneNumber(contact));
                row.createCell(4).setCellValue(nullSafe(contact.getPerson().getLanguage()));
                row.createCell(5).setCellValue(nullSafe(contact.getPerson().getCountry()));
                row.createCell(6).setCellValue(nullSafe(contact.getPerson().getCity()));
                row.createCell(7).setCellValue(nullSafe(contact.getPerson().getTimezone()));
                row.createCell(8).setCellValue(contact.isEnabled());
                row.createCell(9).setCellValue(
                        contact.getTags() == null
                                ? ""
                                : contact.getTags().stream().map(Tag::getId).sorted().map(String::valueOf).collect(Collectors.joining(";"))
                );
                row.createCell(10).setCellValue(contact.getCreatedAt() == null ? "" : contact.getCreatedAt().toString());
            }

            for (int i = 0; i < EXPORT_HEADERS.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to generate XLSX export.");
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String getFirstPhoneNumber(Contact contact) {
        if (contact.getPerson() == null || contact.getPerson().getPhoneNumbers() == null || contact.getPerson().getPhoneNumbers().isEmpty()) {
            return "";
        }

        return contact.getPerson().getPhoneNumbers().stream()
                .map(PhoneNumber::getNumber)
                .filter(number -> number != null && !number.isBlank())
                .findFirst()
                .orElse("");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        boolean containsSpecial = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return containsSpecial ? "\"" + escaped + "\"" : escaped;
    }

    private List<Map<String, String>> parseCsvRows(InputStream inputStream) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return rows;
            }

            List<String> headers = parseCsvLine(headerLine).stream()
                    .map(this::normalizeHeader)
                    .toList();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> values = parseCsvLine(line);
                rows.add(toRowMap(headers, values));
            }
        }

        return rows;
    }

    private List<Map<String, String>> parseXlsxRows(InputStream inputStream) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return rows;
            }

            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return rows;
            }

            List<String> headers = new ArrayList<>();
            for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
                Cell cell = headerRow.getCell(cellIndex);
                headers.add(normalizeHeader(formatter.formatCellValue(cell)));
            }

            int firstDataRow = sheet.getFirstRowNum() + 1;
            for (int rowIndex = firstDataRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                List<String> values = new ArrayList<>();
                for (int cellIndex = 0; cellIndex < headers.size(); cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    values.add(formatter.formatCellValue(cell));
                }

                boolean allEmpty = values.stream().allMatch(value -> value == null || value.trim().isEmpty());
                if (allEmpty) {
                    continue;
                }

                rows.add(toRowMap(headers, values));
            }
        }

        return rows;
    }

    private Map<String, String> toRowMap(List<String> headers, List<String> values) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            String value = i < values.size() ? values.get(i) : "";
            row.put(header, value == null ? "" : value.trim());
        }
        return row;
    }

    private List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        tokens.add(current.toString().trim());
        return tokens;
    }

    private CreateContactDto mapRowToCreateContact(Map<String, String> row) {
        String email = firstNonBlank(
                row.get("email"),
                row.get("emailaddress")
        );

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }

        CreatePersonDto person = new CreatePersonDto(
                email,
                firstNonBlank(row.get("firstname"), row.get("first_name")),
                firstNonBlank(row.get("lastname"), row.get("last_name")),
                normalizeLanguage(row.get("language")),
                firstNonBlank(row.get("country"), row.get("countryname")),
                firstNonBlank(row.get("city"), row.get("town")),
                firstNonBlank(row.get("timezone"), row.get("time_zone"), row.get("tz"))
        );

        CreateContactDto dto = new CreateContactDto();
        dto.setEnabled(parseBooleanWithDefault(row.get("enabled"), true));
        dto.setPerson(person);

        String phoneValue = firstNonBlank(
                row.get("phonenumber"),
                row.get("phone"),
                row.get("phone_number")
        );

        if (phoneValue != null && !phoneValue.isBlank()) {
            dto.setPhoneNumbers(List.of(new CreatePhoneNumberDto(phoneValue, false)));
        }

        List<Long> tagIds = parseLongList(firstNonBlank(row.get("tagids"), row.get("tag_ids")));
        if (!tagIds.isEmpty()) {
            dto.setTagIds(tagIds);
        }

        return dto;
    }

    private List<Long> parseLongList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        String[] tokens = value.split("[,;\\s]+");
        List<Long> result = new ArrayList<>();

        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                result.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ignored) {
                // Ignore invalid tag ID tokens.
            }
        }

        return result;
    }

    private boolean parseBooleanWithDefault(String value, boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized)) {
            return true;
        }

        if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized) || "n".equals(normalized)) {
            return false;
        }

        return defaultValue;
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }

        return header.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    private String normalizeLanguage(String rawLanguage) {
        if (rawLanguage == null || rawLanguage.isBlank()) {
            return null;
        }

        String normalized = rawLanguage.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("fr")) {
            return "fr";
        }
        if (normalized.startsWith("en")) {
            return "en";
        }

        return normalized;
    }

    private String extractErrorMessage(Exception exception) {
        Throwable current = exception;
        while (current.getCause() != null) {
            current = current.getCause();
        }

        String message = current.getMessage();
        return message == null || message.isBlank() ? "Unexpected error" : message;
    }

    private ImportHandlingResult processImportedContact(CreateContactDto dto) {
        if (dto == null || dto.getPerson() == null || dto.getPerson().getEmail() == null || dto.getPerson().getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }

        String email = dto.getPerson().getEmail().trim();
        String phone = extractPrimaryPhone(dto);

        Person personByEmail = personRepository.findByEmail(email).orElse(null);
        PhoneNumber matchedPhone = phone == null ? null : phoneNumberRepository.findByNumber(phone).orElse(null);

        if (personByEmail != null) {
            boolean personChanged = false;
            personChanged |= applyPersonIdentityUpdates(personByEmail, dto.getPerson());
            personChanged |= addPhoneIfMissing(personByEmail, phone);
            if (personChanged) {
                personRepository.save(personByEmail);
            }

            boolean contactChanged = applyContactUpdatesForPerson(personByEmail, dto);
            return (personChanged || contactChanged) ? ImportHandlingResult.UPDATED : ImportHandlingResult.SKIPPED;
        }

        if (matchedPhone != null) {
            Person phoneOwner = matchedPhone.getPerson();
            boolean personChanged = false;

            if (!email.equalsIgnoreCase(phoneOwner.getEmail())) {
                Person conflictingEmailOwner = personRepository.findByEmail(email).orElse(null);
                if (conflictingEmailOwner != null && !conflictingEmailOwner.getId().equals(phoneOwner.getId())) {
                    throw new IllegalArgumentException("Email already exists for another person: " + email);
                }
                phoneOwner.setEmail(email);
                personChanged = true;
            }

            personChanged |= applyPersonIdentityUpdates(phoneOwner, dto.getPerson());
            if (personChanged) {
                personRepository.save(phoneOwner);
            }

            boolean contactChanged = applyContactUpdatesForPerson(phoneOwner, dto);
            return (personChanged || contactChanged) ? ImportHandlingResult.UPDATED : ImportHandlingResult.SKIPPED;
        }

        createWithSequenceRecovery(dto);
        return ImportHandlingResult.CREATED;
    }

    private void createWithSequenceRecovery(CreateContactDto dto) {
        try {
            create(dto);
        } catch (DataIntegrityViolationException ex) {
            if (isPrimaryKeyConflict(ex)) {
                reseedContactSequenceIfPossible();
                create(dto);
                return;
            }
            throw ex;
        }
    }

    private String extractPrimaryPhone(CreateContactDto dto) {
        if (dto.getPhoneNumbers() == null) {
            return null;
        }

        return dto.getPhoneNumbers().stream()
                .filter(phone -> phone != null && phone.getNumber() != null && !phone.getNumber().isBlank())
                .map(phone -> phone.getNumber().trim())
                .findFirst()
                .orElse(null);
    }

    private boolean addPhoneIfMissing(Person person, String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }

        PhoneNumber existingPhone = phoneNumberRepository.findByNumber(phone).orElse(null);
        if (existingPhone != null) {
            if (!existingPhone.getPerson().getId().equals(person.getId())) {
                throw new IllegalArgumentException("Phone number already exists for another person: " + phone);
            }
            return false;
        }

        List<PhoneNumber> phoneNumbers = person.getPhoneNumbers();
        if (phoneNumbers == null) {
            phoneNumbers = new ArrayList<>();
            person.setPhoneNumbers(phoneNumbers);
        }

        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber(phone);
        phoneNumber.setIsWhatsapp("false");
        phoneNumber.setPerson(person);
        phoneNumbers.add(phoneNumber);
        return true;
    }

    private boolean applyPersonIdentityUpdates(Person person, CreatePersonDto importedPerson) {
        if (person == null || importedPerson == null) {
            return false;
        }

        boolean changed = false;

        if (importedPerson.getFirstName() != null && !importedPerson.getFirstName().isBlank() && !importedPerson.getFirstName().equals(person.getFirstName())) {
            person.setFirstName(importedPerson.getFirstName());
            changed = true;
        }

        if (importedPerson.getLastName() != null && !importedPerson.getLastName().isBlank() && !importedPerson.getLastName().equals(person.getLastName())) {
            person.setLastName(importedPerson.getLastName());
            changed = true;
        }

        if (importedPerson.getLanguage() != null && !importedPerson.getLanguage().isBlank() && !importedPerson.getLanguage().equals(person.getLanguage())) {
            person.setLanguage(normalizeLanguage(importedPerson.getLanguage()));
            changed = true;
        }

        if (importedPerson.getCountry() != null && !importedPerson.getCountry().isBlank() && !importedPerson.getCountry().equals(person.getCountry())) {
            person.setCountry(importedPerson.getCountry());
            changed = true;
        }

        if (importedPerson.getCity() != null && !importedPerson.getCity().isBlank() && !importedPerson.getCity().equals(person.getCity())) {
            person.setCity(importedPerson.getCity());
            changed = true;
        }

        if (importedPerson.getTimezone() != null && !importedPerson.getTimezone().isBlank() && !importedPerson.getTimezone().equals(person.getTimezone())) {
            person.setTimezone(importedPerson.getTimezone());
            changed = true;
        }

        return changed;
    }

    private boolean applyContactUpdatesForPerson(Person person, CreateContactDto dto) {
        if (person == null || person.getId() == null) {
            return false;
        }

        Contact contact = contactRepository.findByPersonId(person.getId()).orElse(null);
        if (contact == null) {
            return false;
        }

        boolean changed = false;

        if (contact.isEnabled() != dto.isEnabled()) {
            contact.setEnabled(dto.isEnabled());
            changed = true;
        }

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Tag> existingTags = contact.getTags() == null ? new HashSet<>() : new HashSet<>(contact.getTags());
            Set<Tag> importedTags = new HashSet<>(tagRepository.findAllById(dto.getTagIds()));
            int sizeBefore = existingTags.size();
            existingTags.addAll(importedTags);
            if (existingTags.size() != sizeBefore) {
                contact.setTags(existingTags);
                changed = true;
            }
        }

        if (changed) {
            contactRepository.save(contact);
        }

        return changed;
    }

    private enum ImportHandlingResult {
        CREATED,
        UPDATED,
        SKIPPED
    }

    private boolean isPrimaryKeyConflict(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("duplicate key")
                && (normalized.contains("cnt_contacts_pkey") || normalized.contains("cnt_contats_pkey"));
    }

    private void reseedContactSequenceIfPossible() {
        Long nextId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM komflow.cnt_contacts",
                Long.class
        );

        if (nextId == null || nextId < 1) {
            nextId = 1L;
        }

        String sequenceName = findContactSequenceName();
        if (sequenceName == null || sequenceName.isBlank()) {
            return;
        }

        jdbcTemplate.execute("SELECT setval('" + sequenceName + "', " + nextId + ", false)");
    }

    private String findContactSequenceName() {
        String serialSequence = jdbcTemplate.query(
                "SELECT pg_get_serial_sequence('komflow.cnt_contacts', 'id')",
                rs -> rs.next() ? rs.getString(1) : null
        );

        if (serialSequence != null && !serialSequence.isBlank()) {
            return serialSequence;
        }

        String komflowHibernateSequence = jdbcTemplate.query(
                "SELECT CASE WHEN EXISTS (" +
                        "SELECT 1 FROM pg_class c " +
                        "JOIN pg_namespace n ON n.oid = c.relnamespace " +
                        "WHERE c.relkind = 'S' AND n.nspname = 'komflow' AND c.relname = 'hibernate_sequence'" +
                        ") THEN 'komflow.hibernate_sequence' ELSE NULL END",
                rs -> rs.next() ? rs.getString(1) : null
        );

        if (komflowHibernateSequence != null && !komflowHibernateSequence.isBlank()) {
            return komflowHibernateSequence;
        }

        return jdbcTemplate.query(
                "SELECT CASE WHEN EXISTS (" +
                        "SELECT 1 FROM pg_class c " +
                        "JOIN pg_namespace n ON n.oid = c.relnamespace " +
                        "WHERE c.relkind = 'S' AND n.nspname = 'public' AND c.relname = 'hibernate_sequence'" +
                        ") THEN 'public.hibernate_sequence' ELSE NULL END",
                rs -> rs.next() ? rs.getString(1) : null
        );
    }
}
