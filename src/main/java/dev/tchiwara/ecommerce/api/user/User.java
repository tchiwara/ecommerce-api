package dev.tchiwara.ecommerce.api.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "created_at",updatable = false,insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at",updatable = false,insertable = false)
    private LocalDateTime updatedAt;

    /*
     * insertable = false:
     * Hibernate will not include this field in SQL INSERT statements.
     *
     * updatable = false:
     * Hibernate will not include this field in SQL UPDATE statements.
     *
     * Both timestamps are managed by the database:
     * - created_at uses DEFAULT CURRENT_TIMESTAMP
     * - updated_at uses DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     *
     * Hibernate only reads these values from the database.
     */


}
