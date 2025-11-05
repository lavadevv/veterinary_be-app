package ext.vnua.veterinary_beapp.modules.users.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "departments",
        uniqueConstraints = { @UniqueConstraint(name = "uk_departments_name", columnNames = {"name"}) })
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên phòng ban (duy nhất)
    @Column(name = "name", nullable = false, length = 255)
    private String name;
}
