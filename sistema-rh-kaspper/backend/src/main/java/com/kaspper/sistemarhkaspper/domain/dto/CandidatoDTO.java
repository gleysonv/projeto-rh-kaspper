package com.kaspper.sistemarhkaspper.domain.dto;

import com.kaspper.sistemarhkaspper.domain.model.Perfil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidatoDTO implements BaseDTO {

    private int id;

    private String nome;

    private BigDecimal pretensao;

    private File curriculum;

    private PerfilDTO perfil;
}
