package ext.vnua.veterinary_beapp.modules.users.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "positions",
        uniqueConstraints = { @UniqueConstraint(name = "uk_positions_name", columnNames = {"name"}) })
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên chức vụ (duy nhất)
    @Column(name = "name", nullable = false, length = 255)
    private String name;
}
