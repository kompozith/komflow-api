package com.kompozith.komflow.features.messaging.repository;

import com.kompozith.komflow.features.messaging.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

}