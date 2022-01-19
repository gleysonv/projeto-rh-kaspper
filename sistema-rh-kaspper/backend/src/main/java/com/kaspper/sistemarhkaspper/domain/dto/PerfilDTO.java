package com.kaspper.sistemarhkaspper.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilDTO implements BaseDTO  {

    private int id;

    private String descricao;

    private String observacao;
}
