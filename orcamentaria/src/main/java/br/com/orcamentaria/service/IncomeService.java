package br.com.orcamentaria.service;

import br.com.orcamentaria.dto.IncomeDTO;
import br.com.orcamentaria.exception.RequiredObjectNotPresentException;
import br.com.orcamentaria.model.Income;
import br.com.orcamentaria.repository.IncomeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
}
