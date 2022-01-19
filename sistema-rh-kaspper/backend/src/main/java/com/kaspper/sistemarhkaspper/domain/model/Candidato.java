package com.kaspper.sistemarhkaspper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.File;
import java.math.BigDecimal;

@AttributeOverride(name = "id", column = @Column(name = "cd_candidato", unique = true, nullable = false, length = 4, precision = 10))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "candidato")
public class Candidato extends BaseEntity {

    @Column(name = "nm_candidato")
    private String nome;

    @Column(name = "vr_pretensao")
    private BigDecimal pretensao;

    @Column(name = "aq_curriculum")
    private File curriculum;

    @ManyToOne
    @JoinColumn(name = "cd_Perfil", referencedColumnName = "cd_Perfil")
    private Perfil perfil;

}
