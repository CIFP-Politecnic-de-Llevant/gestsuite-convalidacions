package cat.politecnicllevant.convalidacions.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "categoria")
public @Data class Categoria {
    @Id
    @Column(name = "idcategoria")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idcategoria;

    @Column(name = "nom", nullable = true, length = 2048)
    private String nom;
}
