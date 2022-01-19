package com.kaspper.sistemarhkaspper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@AttributeOverride(name = "id", column = @Column(name = "cd_vaga", unique = true, nullable = false, length = 4, precision = 10))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vaga")
public class Vaga  extends BaseEntity {

    @Column(name = "ds_vaga")
    private String descricao;

    @Column(name = "tp_vaga")
    private int tipo;

    @Column(name = "vr_salario")
    private BigDecimal salario;

    @ManyToOne
    @JoinColumn(name = "cd_Perfil", referencedColumnName = "cd_Perfil")
    private Perfil perfil;

}
