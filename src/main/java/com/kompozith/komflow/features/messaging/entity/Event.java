package com.kompozith.komflow.features.messaging.entity;

import com.kompozith.komflow.features.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "msg_events")
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 220)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String location;

    @Column(length = 255)
    private String subtitle;

    @Column(length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_mode", nullable = true, length = 20)
    private EventMode mode;

    @Column(length = 1024)
    private String meetingUrl;

    @Column(length = 2048)
    private String bannerImageUrl;

    @Column(columnDefinition = "TEXT")
    private String highlights;

    @Column(columnDefinition = "TEXT")
    private String agenda;

    @Column(nullable = false)
    private Instant startAt;

    private Instant endAt;

    @Column(length = 100)
    private String timezone;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<EventRegistrationWorkflowStep> registrationWorkflowSteps = new ArrayList<>();
}
