package com.kaspper.sistemarhkaspper.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VagaDTO implements BaseDTO {

    private int id;

    private String descricao;

    private int tipo;

    private BigDecimal salario;

    private PerfilDTO perfil;
}

