package br.com.orcamentaria.service;

import br.com.orcamentaria.dto.IncomeDTO;
import br.com.orcamentaria.exception.RequiredObjectNotPresentException;
import br.com.orcamentaria.model.Income;
import br.com.orcamentaria.repository.IncomeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncomeService {
    Logger logger = LoggerFactory.getLogger(IncomeService.class);
    private final IncomeRepository repository;
    @Autowired
    private final ObjectMapper mapper;
    @Transactional
    public IncomeDTO create(IncomeDTO dto) {
        if (dto == null) throw new RequiredObjectNotPresentException();
        var parsedIncome = mapper.convertValue(dto, Income.class);
        parsedIncome.setId(null);
        var savedIncome = mapper.convertValue(repository.save(parsedIncome), IncomeDTO.class);
        logger.info("New Income created: " + savedIncome.getId());
        return savedIncome;
    }
    @Transactional
    public void disable(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            var income = repository.findById(uuid).orElseThrow(() -> new RequiredObjectNotPresentException("No record found for ID"));
            repository.delete(income);
            logger.info("Income disabled: " + id);
        } catch (IllegalArgumentException e){
            throw new RequiredObjectNotPresentException("No record found for ID");
        }

    }

    public IncomeDTO findById(String id) {
        try{
            UUID uuid = UUID.fromString(id);
            return mapper.convertValue(repository.findById(uuid).
                                            orElseThrow(() -> new RequiredObjectNotPresentException("No record found for ID")),
                                            IncomeDTO.class);
        } catch (IllegalArgumentException e){
            throw new RequiredObjectNotPresentException("No record found for ID");
        }
    }

    public IncomeDTO update(IncomeDTO dto) {
        if(dto == null) throw new RequiredObjectNotPresentException();

        Income oldIncome = repository.findById(dto.getId()).
                orElseThrow(() -> new RequiredObjectNotPresentException("No record found for ID"));

        UUID oldRecurrenceId = oldIncome.getRecurrence() != null ? oldIncome.getRecurrence().getId() : null;

        try {
            mapper.readerForUpdating(oldIncome).readValue(mapper.writeValueAsBytes(dto));

            //TODO:
            // Alguém sabe alguma forma melhor para eu não precisar garantir o id da recurrence de forma manual?
            // Precisei fazer isso para que não fosse gerado um novo objeto Reccurrence.
            if(oldRecurrenceId != null)
                oldIncome.getRecurrence().setId(oldRecurrenceId);

            Income income = repository.save(oldIncome);
            logger.info("Updating income: " + income.getId());
            return mapper.convertValue(repository.save(income), IncomeDTO.class);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar requisição");
        }
    }

    public Page<IncomeDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(income -> mapper.convertValue(income, IncomeDTO.class));
    }
}
