package com.kaspper.sistemarhkaspper.resource;

import com.kaspper.sistemarhkaspper.domain.dto.CandidatoDTO;
import com.kaspper.sistemarhkaspper.repository.CandidatoRepository;
import com.kaspper.sistemarhkaspper.service.CandidatoService;
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
@RequestMapping("/candidatos")
public class CandidatoResource {

    @Autowired
    private final CandidatoService candidatoService;

    @Autowired
    private final CandidatoRepository candidatoRepository;

    public CandidatoResource(CandidatoService candidatoService, CandidatoRepository candidatoRepository) {
        this.candidatoService = candidatoService;
        this.candidatoRepository = candidatoRepository;
    }

    @GetMapping
    public ResponseEntity<List<CandidatoDTO>> listarTodosCandidatos() {
        return ok(candidatoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<CandidatoDTO> salvar(@Valid @RequestBody CandidatoDTO candidatoDTO) {
        CandidatoDTO retorno = candidatoService.salvar(candidatoDTO);
        return status(CREATED).body(retorno);
    }

    @PutMapping("/{id:[1-9][0-9]*}")
    public ResponseEntity<CandidatoDTO> update(@PathVariable("id") Integer id,
                                             @RequestBody CandidatoDTO candidatoDTO) {
        CandidatoDTO retorno = candidatoService.update(id, candidatoDTO);
        if (!isNull(retorno)) {
            return ok().body(retorno);
        } else {
            return notFound().build();
        }
    }

    @DeleteMapping("/{id:[1-9][0-9]*}")
    public ResponseEntity <?> deletar(@PathVariable Integer id) {
        return candidatoRepository.findById(id)
                .map(candidato -> {
                    candidatoRepository.deleteById(id);
                    return ok().build();
                }).orElse(notFound().build());
    }


}