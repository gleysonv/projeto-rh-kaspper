package com.kaspper.sistemarhkaspper.service;

import com.kaspper.sistemarhkaspper.domain.dto.CandidatoDTO;
import com.kaspper.sistemarhkaspper.domain.model.Candidato;
import com.kaspper.sistemarhkaspper.repository.CandidatoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CandidatoService extends BaseService {

    private final CandidatoRepository candidatoRepository;

    @Autowired
    public CandidatoService(CandidatoRepository candidatoRepository) {
        this.candidatoRepository = candidatoRepository;
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public List<CandidatoDTO> listarTodos(){
        return candidatoRepository.findAll()
                .stream().map(candidato -> getConverter().map(candidato, CandidatoDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public CandidatoDTO salvar(final CandidatoDTO dto) {
        final Candidato candidato = getConverter().map(dto, Candidato.class);
        Candidato retorno = candidatoRepository.save(candidato);
        return getConverter().map(retorno, CandidatoDTO.class);
    }

    @Transactional
    public CandidatoDTO update(final Integer id, final CandidatoDTO dto) {
        Candidato colaboradorEditado = null;
        Optional<Candidato> candidatoEncontrado = candidatoRepository.findById(id);
        if (candidatoEncontrado.isPresent()) {
            colaboradorEditado = getConverter().map(dto, Candidato.class);
        }
        assert colaboradorEditado != null;
        return getConverter().map(candidatoRepository.save(colaboradorEditado), CandidatoDTO.class);
    }

}