package cat.iesmanacor.convalidacions.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "solicitud")
public @Data class Solicitud {
    @Id
    @Column(name = "idsolicitud")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idsolicitud;

    @Column(name = "estat", nullable = false)
    @Enumerated(EnumType.STRING)
    private SolcititudEstat estat;

    @Column(name = "data_creacio", nullable = false)
    private Date dataCreacio;

    @Column(name = "data_signatura", nullable = true)
    private Date dataSignatura;

    @Column(name = "observacions", nullable = true)
    @Type(type = "text")
    private String observacions;

    @ManyToMany
    private Set<Item> estudisOrigen = new HashSet<>();

    @Column(name = "estudisorigenmanual", nullable = true)
    @Type(type = "text")
    private String estudisOrigenManual;

    @Column(name = "estudisorigenobservacions", nullable = true)
    @Type(type = "text")
    private String estudisOrigenObservacions;

    @ManyToOne(optional = false)
    private Item estudisEnCurs;

    @Column(name = "estudisencursobservacions", nullable = true)
    @Type(type = "text")
    private String estudisEnCursObservacions;

    @Column(name = "nomalumnemanual", nullable = true, length = 2048)
    private String nomAlumneManual;

    @Column(name = "cognomsalumnemanual", nullable = true, length = 2048)
    private String cognomsAlumneManual;

    @OneToMany(mappedBy="solicitud")
    @JsonManagedReference
    private Set<Resolucio> resolucions = new HashSet<>();


    //Microservei CORE
    @Column(name = "alumne_idusuari", nullable = true)
    private Long alumne;

    @Column(name = "fitxer_resolucio_idfitxer", nullable = true)
    private Long fitxerResolucio;

    @ElementCollection
    private Set<Long> fitxersAlumne = new HashSet<>();
}
