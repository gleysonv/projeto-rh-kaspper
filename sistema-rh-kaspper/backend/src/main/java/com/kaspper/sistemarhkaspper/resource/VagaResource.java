package com.kaspper.sistemarhkaspper.resource;

import com.kaspper.sistemarhkaspper.domain.dto.VagaDTO;
import com.kaspper.sistemarhkaspper.repository.VagaRepository;
import com.kaspper.sistemarhkaspper.service.VagaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.*;
import static org.springframework.http.ResponseEntity.status;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/vagas")
public class VagaResource {

    @Autowired
    private final VagaService vagaService;

    @Autowired
    private final VagaRepository vagaRepository;

    public VagaResource(VagaService vagaService, VagaRepository vagaRepository) {
        this.vagaService = vagaService;
        this.vagaRepository = vagaRepository;
    }

    @GetMapping
    public ResponseEntity<List<VagaDTO>> listarTodosVagas() {
        return ok(vagaService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<VagaDTO> salvar(@Valid @RequestBody VagaDTO vagaDTO) {
        VagaDTO retorno = vagaService.salvar(vagaDTO);
        return status(CREATED).body(retorno);
    }

    @PutMapping("/{id:[1-9][0-9]*}")
    public ResponseEntity<VagaDTO> update(@PathVariable("id") Integer id,
                                             @RequestBody VagaDTO vagaDTO) {
        VagaDTO retorno = vagaService.update(id, vagaDTO);
        if (!isNull(retorno)) {
            return ok().body(retorno);
        } else {
            return notFound().build();
        }
    }

    @DeleteMapping("/{id:[1-9][0-9]*}")
    public ResponseEntity <?> deletar(@PathVariable Integer id) {
        return vagaRepository.findById(id)
                .map(vaga -> {
                    vagaRepository.deleteById(id);
                    return ok().build();
                }).orElse(notFound().build());
    }


}