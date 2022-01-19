package com.kaspper.sistemarhkaspper.resource;

import com.kaspper.sistemarhkaspper.domain.dto.PerfilDTO;
import com.kaspper.sistemarhkaspper.repository.PerfilRepository;
import com.kaspper.sistemarhkaspper.service.PerfilService;
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
@RequestMapping("/perfis")
public class PerfilResource {

    @Autowired
    private final PerfilService perfilService;

    @Autowired
    private final PerfilRepository perfilRepository;

    public PerfilResource(PerfilService perfilService, PerfilRepository perfilRepository) {
        this.perfilService = perfilService;
        this.perfilRepository = perfilRepository;
    }

    @GetMapping
    public ResponseEntity<List<PerfilDTO>> listarTodosPerfils() {
        return ok(perfilService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<PerfilDTO> salvar(@Valid @RequestBody PerfilDTO perfilDTO) {
        PerfilDTO retorno = perfilService.salvar(perfilDTO);
        return status(CREATED).body(retorno);
    }

    @PutMapping("/{id:[1-9][0-9]*}")
    public ResponseEntity<PerfilDTO> update(@PathVariable("id") Integer id,
                                             @RequestBody PerfilDTO perfilDTO) {
        PerfilDTO retorno = perfilService.update(id, perfilDTO);
        if (!isNull(retorno)) {
            return ok().body(retorno);
        } else {
            return notFound().build();
        }
    }

    @DeleteMapping("/{id:[1-9][0-9]*}")
    public ResponseEntity <?> deletar(@PathVariable Integer id) {
        return perfilRepository.findById(id)
                .map(perfil -> {
                    perfilRepository.deleteById(id);
                    return ok().build();
                }).orElse(notFound().build());
    }


}
