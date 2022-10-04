package cat.iesmanacor.convalidacions.model;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "convalidacio")
public @Data class Convalidacio {
    @Id
    @Column(name = "idconvalidacio")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idconvalidacio;

    @ManyToMany
    private Set<Item> origens = new HashSet<>();

    @ManyToMany
    private Set<Item> convalida = new HashSet<>();
}
