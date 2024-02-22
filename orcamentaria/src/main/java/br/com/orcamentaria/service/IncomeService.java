package br.com.orcamentaria.service;

import br.com.orcamentaria.dto.IncomeDTO;
import br.com.orcamentaria.exception.RequiredObjectNotPresentException;
import br.com.orcamentaria.model.Income;
import br.com.orcamentaria.repository.IncomeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncomeService {
    Logger logger = LoggerFactory.getLogger(IncomeService.class);
    private final IncomeRepository repository;
    private final ObjectMapper mapper;
    public IncomeDTO create(IncomeDTO dto) {
        if (dto == null) throw new RequiredObjectNotPresentException();
        var parsedIncome = mapper.convertValue(dto, Income.class);
        parsedIncome.setId(null);
        var savedIncome = mapper.convertValue(repository.save(parsedIncome), IncomeDTO.class);
        logger.info("New Income created: " + savedIncome.getId());
        return savedIncome;
    }
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

        Income parsedIncome = mapper.convertValue(dto, Income.class);

        try {
            ObjectReader reader = mapper.readerForUpdating(oldIncome);
            Income income = reader.readValue(mapper.writeValueAsBytes(parsedIncome));
            logger.info("Updating income: " + income.getId());
            return mapper.convertValue(repository.save(income), IncomeDTO.class);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar requisição");
        }
    }
}
