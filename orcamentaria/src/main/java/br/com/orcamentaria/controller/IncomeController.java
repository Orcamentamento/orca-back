package br.com.orcamentaria.controller;

import br.com.orcamentaria.dto.IncomeDTO;
import br.com.orcamentaria.service.IncomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {
    private final IncomeService service;
    @PostMapping
    @Operation(summary = "Create a new Income", description = "Endpoint used to create a new Income, no needed to send id",
                    responses = {@ApiResponse(description = "Success", responseCode = "201", content = {
                                    @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = IncomeDTO.class))
                                    )}),
                                @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                                @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
                                })
    public ResponseEntity<IncomeDTO> createIncome(@Valid  @RequestBody IncomeDTO incomeDTO) {
        return new ResponseEntity<>(service.create(incomeDTO), HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Disable an Income", description = "Endpoint used to disables an Income",
            responses = {@ApiResponse(description = "No Content", responseCode = "204", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            })
    public ResponseEntity<?> deleteIncome(@PathVariable String id) {
        service.disable(id);
        return ResponseEntity.noContent().build();
    }
}
