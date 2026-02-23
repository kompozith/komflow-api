package com.kompozith.komflow.features.core.repository;

import com.kompozith.komflow.features.core.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    @Query(
            value = """
            SELECT *
            FROM komflow.core_file f
            WHERE (
                COALESCE(:search, '') = ''
                OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (
                :mediaType IS NULL
                OR (
                    CASE
                        WHEN LOWER(f.name) LIKE '%.jpg' OR LOWER(f.name) LIKE '%.jpeg' OR LOWER(f.name) LIKE '%.png'
                             OR LOWER(f.name) LIKE '%.gif' OR LOWER(f.name) LIKE '%.bmp' OR LOWER(f.name) LIKE '%.webp'
                             OR LOWER(f.name) LIKE '%.svg' THEN 'IMAGE'
                        WHEN LOWER(f.name) LIKE '%.mp4' OR LOWER(f.name) LIKE '%.mov' OR LOWER(f.name) LIKE '%.avi'
                             OR LOWER(f.name) LIKE '%.mkv' OR LOWER(f.name) LIKE '%.webm' OR LOWER(f.name) LIKE '%.m4v' THEN 'VIDEO'
                        WHEN LOWER(f.name) LIKE '%.mp3' OR LOWER(f.name) LIKE '%.wav' OR LOWER(f.name) LIKE '%.ogg'
                             OR LOWER(f.name) LIKE '%.aac' OR LOWER(f.name) LIKE '%.flac' OR LOWER(f.name) LIKE '%.m4a' THEN 'AUDIO'
                        WHEN LOWER(f.name) LIKE '%.pdf' OR LOWER(f.name) LIKE '%.doc' OR LOWER(f.name) LIKE '%.docx'
                             OR LOWER(f.name) LIKE '%.xls' OR LOWER(f.name) LIKE '%.xlsx' OR LOWER(f.name) LIKE '%.ppt'
                             OR LOWER(f.name) LIKE '%.pptx' OR LOWER(f.name) LIKE '%.txt' OR LOWER(f.name) LIKE '%.csv' THEN 'DOCUMENT'
                        WHEN LOWER(f.name) LIKE '%.zip' OR LOWER(f.name) LIKE '%.rar' OR LOWER(f.name) LIKE '%.7z'
                             OR LOWER(f.name) LIKE '%.tar' OR LOWER(f.name) LIKE '%.gz' THEN 'ARCHIVE'
                        ELSE 'OTHER'
                    END
                ) = :mediaType
            )
            AND (
                :orphanOnly = FALSE
                OR NOT EXISTS (
                    SELECT 1
                    FROM komflow.msg_message_attachments ma
                    WHERE ma.core_file_id = f.id
                )
            )
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM komflow.core_file f
            WHERE (
                COALESCE(:search, '') = ''
                OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (
                :mediaType IS NULL
                OR (
                    CASE
                        WHEN LOWER(f.name) LIKE '%.jpg' OR LOWER(f.name) LIKE '%.jpeg' OR LOWER(f.name) LIKE '%.png'
                             OR LOWER(f.name) LIKE '%.gif' OR LOWER(f.name) LIKE '%.bmp' OR LOWER(f.name) LIKE '%.webp'
                             OR LOWER(f.name) LIKE '%.svg' THEN 'IMAGE'
                        WHEN LOWER(f.name) LIKE '%.mp4' OR LOWER(f.name) LIKE '%.mov' OR LOWER(f.name) LIKE '%.avi'
                             OR LOWER(f.name) LIKE '%.mkv' OR LOWER(f.name) LIKE '%.webm' OR LOWER(f.name) LIKE '%.m4v' THEN 'VIDEO'
                        WHEN LOWER(f.name) LIKE '%.mp3' OR LOWER(f.name) LIKE '%.wav' OR LOWER(f.name) LIKE '%.ogg'
                             OR LOWER(f.name) LIKE '%.aac' OR LOWER(f.name) LIKE '%.flac' OR LOWER(f.name) LIKE '%.m4a' THEN 'AUDIO'
                        WHEN LOWER(f.name) LIKE '%.pdf' OR LOWER(f.name) LIKE '%.doc' OR LOWER(f.name) LIKE '%.docx'
                             OR LOWER(f.name) LIKE '%.xls' OR LOWER(f.name) LIKE '%.xlsx' OR LOWER(f.name) LIKE '%.ppt'
                             OR LOWER(f.name) LIKE '%.pptx' OR LOWER(f.name) LIKE '%.txt' OR LOWER(f.name) LIKE '%.csv' THEN 'DOCUMENT'
                        WHEN LOWER(f.name) LIKE '%.zip' OR LOWER(f.name) LIKE '%.rar' OR LOWER(f.name) LIKE '%.7z'
                             OR LOWER(f.name) LIKE '%.tar' OR LOWER(f.name) LIKE '%.gz' THEN 'ARCHIVE'
                        ELSE 'OTHER'
                    END
                ) = :mediaType
            )
            AND (
                :orphanOnly = FALSE
                OR NOT EXISTS (
                    SELECT 1
                    FROM komflow.msg_message_attachments ma
                    WHERE ma.core_file_id = f.id
                )
            )
            """,
            nativeQuery = true
    )
    Page<File> findWithFilters(
            @Param("search") String search,
            @Param("mediaType") String mediaType,
            @Param("orphanOnly") boolean orphanOnly,
            Pageable pageable
    );

    @Query(
            value = """
            SELECT
                CASE
                    WHEN LOWER(f.name) LIKE '%.jpg' OR LOWER(f.name) LIKE '%.jpeg' OR LOWER(f.name) LIKE '%.png'
                         OR LOWER(f.name) LIKE '%.gif' OR LOWER(f.name) LIKE '%.bmp' OR LOWER(f.name) LIKE '%.webp'
                         OR LOWER(f.name) LIKE '%.svg' THEN 'IMAGE'
                    WHEN LOWER(f.name) LIKE '%.mp4' OR LOWER(f.name) LIKE '%.mov' OR LOWER(f.name) LIKE '%.avi'
                         OR LOWER(f.name) LIKE '%.mkv' OR LOWER(f.name) LIKE '%.webm' OR LOWER(f.name) LIKE '%.m4v' THEN 'VIDEO'
                    WHEN LOWER(f.name) LIKE '%.mp3' OR LOWER(f.name) LIKE '%.wav' OR LOWER(f.name) LIKE '%.ogg'
                         OR LOWER(f.name) LIKE '%.aac' OR LOWER(f.name) LIKE '%.flac' OR LOWER(f.name) LIKE '%.m4a' THEN 'AUDIO'
                    WHEN LOWER(f.name) LIKE '%.pdf' OR LOWER(f.name) LIKE '%.doc' OR LOWER(f.name) LIKE '%.docx'
                         OR LOWER(f.name) LIKE '%.xls' OR LOWER(f.name) LIKE '%.xlsx' OR LOWER(f.name) LIKE '%.ppt'
                         OR LOWER(f.name) LIKE '%.pptx' OR LOWER(f.name) LIKE '%.txt' OR LOWER(f.name) LIKE '%.csv' THEN 'DOCUMENT'
                    WHEN LOWER(f.name) LIKE '%.zip' OR LOWER(f.name) LIKE '%.rar' OR LOWER(f.name) LIKE '%.7z'
                         OR LOWER(f.name) LIKE '%.tar' OR LOWER(f.name) LIKE '%.gz' THEN 'ARCHIVE'
                    ELSE 'OTHER'
                END AS media_type,
                COUNT(*) AS total
            FROM komflow.core_file f
            WHERE (
                COALESCE(:search, '') = ''
                OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (
                :orphanOnly = FALSE
                OR NOT EXISTS (
                    SELECT 1
                    FROM komflow.msg_message_attachments ma
                    WHERE ma.core_file_id = f.id
                )
            )
            GROUP BY media_type
            """,
            nativeQuery = true
    )
    List<Object[]> countByMediaType(
            @Param("search") String search,
            @Param("orphanOnly") boolean orphanOnly
    );

    @Query(
            value = """
            SELECT DISTINCT ma.core_file_id
            FROM komflow.msg_message_attachments ma
            WHERE ma.core_file_id IN (:fileIds)
            """,
            nativeQuery = true
    )
    List<Long> findReferencedIds(@Param("fileIds") List<Long> fileIds);
}
