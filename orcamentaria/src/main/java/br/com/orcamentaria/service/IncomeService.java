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

@Service
@RequiredArgsConstructor
public class IncomeService {
    Logger logger = LoggerFactory.getLogger(IncomeService.class);
    private final IncomeRepository repository;
    private final ObjectMapper mapper;
    public IncomeDTO create(IncomeDTO dto) {
        if (dto == null) throw new RequiredObjectNotPresentException();
        Income income = mapper.convertValue(dto, Income.class);
        logger.info("New income created: " + income.getId());
        return mapper.convertValue(repository.save(income), IncomeDTO.class);
    }
}
