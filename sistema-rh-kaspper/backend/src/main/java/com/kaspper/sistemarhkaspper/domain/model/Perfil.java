package com.kaspper.sistemarhkaspper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AttributeOverride(name = "id", column = @Column(name = "cd_Perfil", unique = true, nullable = false, length = 4, precision = 10))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfil")
public class Perfil  extends BaseEntity {

    @Column(name = "ds_perfil")
    private String descricao;

    @Column(name = "ds_observacao")
    private String observacao;


}
