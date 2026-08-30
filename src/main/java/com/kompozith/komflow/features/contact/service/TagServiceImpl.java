package com.kompozith.komflow.features.contact.service;

import com.kompozith.komflow.exception.ObjectExistException;
import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.contact.dto.TagDto;
import com.kompozith.komflow.features.contact.dto.TagWithContactCountDto;
import com.kompozith.komflow.features.contact.entity.Contact;
import com.kompozith.komflow.features.contact.entity.Tag;
import com.kompozith.komflow.features.contact.mapper.TagMapper;
import com.kompozith.komflow.features.contact.repository.ContactRepository;
import com.kompozith.komflow.features.contact.repository.TagRepository;
import com.kompozith.komflow.features.core.service.BaseService;
import com.kompozith.komflow.features.organization.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends BaseService implements TagService {
    private static final String EVENT_REGISTRATION_TAG_PREFIX = "EVENT-REG-";

    private final TagRepository tagRepository;
    private final ContactRepository contactRepository;
    private final TagMapper tagMapper;

    @Override
    @Transactional
    public TagDto create(TagDto tagDto) {
        Long orgId = TenantContext.getOrganizationId();
        // Check if tag with same name already exists in this org
        if (tagRepository.findByName(tagDto.getName()).filter(t -> orgId.equals(t.getOrganizationId())).isPresent()) {
            throw new ObjectExistException(Tag.class.getSimpleName(), "name", tagDto.getName());
        }

        Tag tag = tagMapper.tagDtoToTag(tagDto);
        tag.setEnabled(true);
        tag.setOrganizationId(orgId);
        Tag savedTag = tagRepository.save(tag);

        syncTagContacts(savedTag, tagDto.getContactIds());

        Tag refreshedTag = tagRepository.findById(savedTag.getId())
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), savedTag.getId()));

        TagDto dto = tagMapper.tagToTagDto(refreshedTag);
        dto.setContactCount(refreshedTag.getContacts() != null ? (long) refreshedTag.getContacts().size() : 0L);
        dto.setContactIds(extractContactIds(refreshedTag.getContacts()));
        return dto;
    }

    @Override
    public List<TagDto> findAll() {
        Long orgId = TenantContext.getOrganizationId();
        List<Object[]> results = tagRepository.findAllWithContactCountByOrg(orgId);
        return results.stream()
                .map(result -> {
                    Tag tag = (Tag) result[0];
                    Long contactCount = (Long) result[1];
                    TagDto dto = tagMapper.tagToTagDto(tag);
                    dto.setContactCount(contactCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<TagWithContactCountDto> findAll(Pageable pageable, String search, Instant startDate, Instant endDate, Boolean enabled) {
        Long orgId = TenantContext.getOrganizationId();
        return tagRepository.findWithFiltersAndContactCount(orgId, search, startDate, endDate, enabled, pageable)
                .map(tagMapper::projectionToTagWithContactCountDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TagDto findById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));
        TagDto dto = tagMapper.tagToTagDto(tag);
        // Calculate contact count for this tag
        dto.setContactCount(tag.getContacts() != null ? (long) tag.getContacts().size() : 0L);
        dto.setContactIds(extractContactIds(tag.getContacts()));
        return dto;
    }

    @Override
    @Transactional
    public TagDto update(Long id, TagDto tagDto) {
        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));

        // Check if another tag with same name exists
        tagRepository.findByName(tagDto.getName()).ifPresent(tag -> {
            if (!tag.getId().equals(id)) {
                throw new ObjectExistException(Tag.class.getSimpleName(), "name", tagDto.getName());
            }
        });

        existingTag.setName(tagDto.getName());
        existingTag.setDescription(tagDto.getDescription());
        existingTag.setColorCode(tagDto.getColorCode());
        // Note: enabled field not in TagDto, assuming it's not updated via this method

        Tag updatedTag = tagRepository.save(existingTag);

        syncTagContacts(updatedTag, tagDto.getContactIds());

        Tag refreshedTag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));
        TagDto dto = tagMapper.tagToTagDto(refreshedTag);
        dto.setContactCount(refreshedTag.getContacts() != null ? (long) refreshedTag.getContacts().size() : 0L);
        dto.setContactIds(extractContactIds(refreshedTag.getContacts()));
        return dto;
    }

    @Override
    public TagDto toggleStatus(Long id, boolean enabled) {
        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));

        existingTag.setEnabled(enabled);
        Tag updatedTag = tagRepository.save(existingTag);
        return tagMapper.tagToTagDto(updatedTag);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(Tag.class.getSimpleName(), id));
        if (tag.getName() != null && tag.getName().startsWith(EVENT_REGISTRATION_TAG_PREFIX)) {
            throw new IllegalStateException("Event registration tags cannot be deleted directly. Delete the linked event instead.");
        }
        tagRepository.deleteById(id);
    }

    private void syncTagContacts(Tag tag, List<Long> requestedContactIds) {
        Set<Long> desiredIds = requestedContactIds == null
                ? new HashSet<>()
                : new HashSet<>(requestedContactIds);

        Set<Contact> currentlyLinked = tag.getContacts() != null
                ? new HashSet<>(tag.getContacts())
                : new HashSet<>();

        for (Contact contact : currentlyLinked) {
            if (!desiredIds.contains(contact.getId())) {
                if (contact.getTags() != null) {
                    contact.getTags().removeIf(t -> t.getId().equals(tag.getId()));
                }
            }
        }

        if (!desiredIds.isEmpty()) {
            List<Contact> desiredContacts = contactRepository.findAllById(desiredIds);
            for (Contact contact : desiredContacts) {
                if (contact.getTags() == null) {
                    contact.setTags(new HashSet<>());
                }
                contact.getTags().add(tag);
            }
            contactRepository.saveAll(desiredContacts);
        }

        contactRepository.saveAll(currentlyLinked);
    }

    private List<Long> extractContactIds(Set<Contact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            return new ArrayList<>();
        }
        return contacts.stream().map(Contact::getId).toList();
    }
}
