package com.kaspper.sistemarhkaspper.service;

import com.kaspper.sistemarhkaspper.domain.dto.VagaDTO;
import com.kaspper.sistemarhkaspper.domain.model.Vaga;
import com.kaspper.sistemarhkaspper.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VagaService extends BaseService {

    private final VagaRepository vagaRepository;

    @Autowired
    public VagaService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public List<VagaDTO> listarTodos() {
        return vagaRepository.findAll()
                .stream().map(vaga -> getConverter().map(vaga, VagaDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public VagaDTO salvar(final VagaDTO dto) {
        final Vaga vaga = getConverter().map(dto, Vaga.class);
        Vaga retorno = vagaRepository.save(vaga);
        return getConverter().map(retorno, VagaDTO.class);
    }

    @Transactional
    public VagaDTO update(final Integer id, final VagaDTO dto) {
        Vaga colaboradorEditado = null;
        Optional<Vaga> vagaEncontrado = vagaRepository.findById(id);
        if (vagaEncontrado.isPresent()) {
            colaboradorEditado = getConverter().map(dto, Vaga.class);
        }
        assert colaboradorEditado != null;
        return getConverter().map(vagaRepository.save(colaboradorEditado), VagaDTO.class);
    }

}
