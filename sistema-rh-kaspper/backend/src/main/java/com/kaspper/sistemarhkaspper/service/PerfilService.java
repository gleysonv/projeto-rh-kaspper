package com.kaspper.sistemarhkaspper.service;


import com.kaspper.sistemarhkaspper.domain.dto.PerfilDTO;
import com.kaspper.sistemarhkaspper.domain.model.Perfil;
import com.kaspper.sistemarhkaspper.repository.PerfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PerfilService extends BaseService {

    private final PerfilRepository perfilRepository;

    @Autowired
    public PerfilService(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public List<PerfilDTO> listarTodos(){
        return perfilRepository.findAll()
                .stream().map(perfil -> getConverter().map(perfil, PerfilDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public PerfilDTO salvar(final PerfilDTO dto) {
        final Perfil perfil = getConverter().map(dto, Perfil.class);
        Perfil retorno = perfilRepository.save(perfil);
        return getConverter().map(retorno, PerfilDTO.class);
    }

    @Transactional
    public PerfilDTO update(final Integer id, final PerfilDTO dto) {
        Perfil colaboradorEditado = null;
        Optional<Perfil> perfilEncontrado = perfilRepository.findById(id);
        if (perfilEncontrado.isPresent()) {
            colaboradorEditado = getConverter().map(dto, Perfil.class);
        }
        assert colaboradorEditado != null;
        return getConverter().map(perfilRepository.save(colaboradorEditado), PerfilDTO.class);
    }

}
