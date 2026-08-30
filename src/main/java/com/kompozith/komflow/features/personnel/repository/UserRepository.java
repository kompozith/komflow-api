package com.kompozith.komflow.features.personnel.repository;

import com.kompozith.komflow.features.personnel.dto.UserDetailsInterfaceDto;
import com.kompozith.komflow.features.personnel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPersonId(Long personId);

    List<User> findByPersonEmailContainingIgnoreCase(String email);

    @Query(
            nativeQuery = true,
            value = "SELECT usr.id as id," +
                    "prs.email as email," +
                    "prs.first_name as firstName," +
                    "prs.last_name as lastname," +
                    "usr.created_at as createdAt " +
                    "FROM komflow.prs_users as usr " +
                    "INNER JOIN komflow.prs_persons prs " +
                    "   ON usr.prs_person_id = prs.id " +
                    "WHERE prs.email = :email"
    )
    Optional<UserDetailsInterfaceDto> findByEmail(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE u.person.email = :email")
    Optional<User> findByPersonEmailWithRolesAndPermissions(@Param("email") String email);

    long countByRoles_Id(Long roleId);
}
