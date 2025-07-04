package com.kompozith.komflow.features.personnel.repository;

import com.kompozith.komflow.features.personnel.dto.UserDetailsInterfaceDto;
import com.kompozith.komflow.features.personnel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByPersonId(Long personId);

    @Query(
            nativeQuery = true,
            value = "SELECT usr.id as id," +
                    "usr.username as userName," +
                    "prs.email as email," +
                    "prs.first_name as firstName," +
                    "prs.last_name as lastname," +
                    "usr.created_at as createdAt " +
                    "FROM komflow.prs_users as usr " +
                    "INNER JOIN komflow.prs_persons prs " +
                    "   ON usr.prs_person_id = prs.id "
    )
    Optional<UserDetailsInterfaceDto> findByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);
}
