package br.com.orcamentaria.config;

import br.com.orcamentaria.desserializer.TransactionDesserializer;
import br.com.orcamentaria.model.Income;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Income.class, new TransactionDesserializer());
        mapper.registerModule(module);
        mapper.findAndRegisterModules();
        return mapper;
    }
}
