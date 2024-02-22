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
    @Operation(summary = "Create a new income", description = "Endpoint used to create a new income, no needed to send id",
                    responses = {@ApiResponse(description = "Success", responseCode = "201", content = {
                                    @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = IncomeDTO.class))
                                    )}),
                                @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                                @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
                                })
    public ResponseEntity<IncomeDTO> create(@Valid  @RequestBody IncomeDTO dto) {
        return new ResponseEntity<>(service.create(dto), HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Disable an income", description = "Endpoint used to disables an income",
            responses = {@ApiResponse(description = "No Content", responseCode = "204", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            })
    public ResponseEntity<?> delete(@PathVariable String id) {
        service.disable(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Find an income by ID", description = "Endpoint used to find a specific income",
            responses = {@ApiResponse(description = "Success", responseCode = "200", content = {
                            @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = IncomeDTO.class))
                            )}),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            })
    public ResponseEntity<?> getById(@PathVariable String id) {
        return new ResponseEntity<>(service.findById(id), HttpStatus.OK);
    }

    @PutMapping()
    @Operation(summary = "Update an income", description = "Endpoint used to update an income",
            responses = {@ApiResponse(description = "Success", responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = IncomeDTO.class))
                    )}),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            })
    public ResponseEntity<?> update(@Valid @RequestBody IncomeDTO dto) {
        return new ResponseEntity<>(service.update(dto), HttpStatus.OK);
    }
}
