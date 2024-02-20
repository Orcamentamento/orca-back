package br.com.orcamentaria.controller;

import br.com.orcamentaria.dto.IncomeDTO;
import br.com.orcamentaria.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {
    private final IncomeService service;
    @PostMapping
    public ResponseEntity<IncomeDTO> createIncome(@Valid  @RequestBody IncomeDTO incomeDTO) {
        return new ResponseEntity<>(service.create(incomeDTO), HttpStatus.CREATED);
    }
}
