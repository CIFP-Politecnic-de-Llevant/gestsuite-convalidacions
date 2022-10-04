package cat.iesmanacor.convalidacions.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "resolucio")
@EqualsAndHashCode(exclude={"solicitud"})
public @Data class Resolucio {
    @Id
    @Column(name = "idresolucio")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idresolucio;

    @Column(name = "estat", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResolucioEstat estat;

    @Column(name = "qualificacio", nullable = true)
    private String qualificacio;

    @ManyToOne(optional = false)
    private Item estudi;

    @Column(name = "observacions", nullable = true)
    @Type(type = "text")
    private String observacions;

    @ManyToOne(optional = false)
    @JsonBackReference
    private Solicitud solicitud;

}
