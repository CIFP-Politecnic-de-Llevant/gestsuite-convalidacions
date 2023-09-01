package cat.politecnicllevant.convalidacions.model;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "item")
public @Data class Item {
    @Id
    @Column(name = "iditem")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long iditem;

    @Column(name = "codi", nullable = true, length = 2048)
    private String codi;

    @Column(name = "nom", nullable = false, length = 2048)
    private String nom;

    @Column(name = "nom_original", nullable = false, length = 2048)
    private String nomOriginal;

    @Column(name = "impartitalcentre", nullable = false)
    private Boolean impartitAlCentre;

    @ManyToOne(optional = false)
    private Categoria categoria;

    @ManyToMany
    private Set<Item> composa = new HashSet<>();
}
