package com.iftm.client.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iftm.client.dto.ClientDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ClientResourceTestIntegration {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Verificar se o insert deveria retornar “created” (código 201), bem como o produto criado")
    @Test
    public void testarInsert() throws Exception {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(13L);
        clientDTO.setName("Thales");
        clientDTO.setCpf("12345678900");
        clientDTO.setIncome(5000.0);
        clientDTO.setBirthDate(Instant.parse("1990-01-01T00:00:00Z"));
        clientDTO.setChildren(2);

        String json = objectMapper.writeValueAsString(clientDTO);
        ResultActions result =
                mockMvc.perform(post("/clients/")
                                .content(json)
                                .contentType(APPLICATION_JSON)
                                .accept(APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").isNumber())
                        .andExpect(jsonPath("$.name").value("Thales"));
    }

    @DisplayName("Verificar se delete retornar “no content” (código 204) quando o id existir")
    @Test
    public void testarDeleteNoContent() throws Exception {
        // Arrange
        long idExistente = 1l;

        // Act & Assert
        mockMvc.perform(delete("/clients/{id}", idExistente))
                .andExpect(status().isNoContent());
    }

    @DisplayName("Verificar se delete retornar “not found” (código 404) quando o id não existir")
    @Test
    public void testarDeleteNotFound() throws Exception {
        // Arrange
        long idNaoExixtente = 100l;

        // Act & Assert
        mockMvc.perform(delete("/clients/{id}", idNaoExixtente))
                .andExpect(status().isNotFound());
    }


    @DisplayName("Verificar se findByIncomeGreaterThan retornar OK (código 200), bem como os clientes que tenham o Income informado.")
    @Test
    public void testarFindByIncomeGreaterThan() throws Exception {
        double income = 8000.00;

        ResultActions result = mockMvc.perform(get("/clients/incomeGreaterThan/")
                .param("income", String.valueOf(income))
                .accept(MediaType.APPLICATION_JSON));

        // Verifique se o status da resposta é 200 (ok)
        result.andExpect(status().isOk());

        // Verifique se o corpo da resposta contém os atributos desejados
        result.andExpect(jsonPath("$.content").exists());
        result.andExpect(jsonPath("$.totalElements").value(1));
        //result.andExpect(jsonPath("$.content[0].name").value("Carolina Maria de Jesus"));
        result.andExpect(jsonPath("$.content[0].name").value("Toni Morrison"));
    }


    @DisplayName("Verificar se update  retornar “ok” (código 200), bem como o json do produto atualizado para um id existente, verificando no mínimo dois atributos.")
    @Test
    public void testarUpdateIDExistente() throws Exception {
        long idExistente = 1l;

        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Thales");
        clientDTO.setCpf("12345678900");

        String json = objectMapper.writeValueAsString(clientDTO);
        mockMvc.perform(MockMvcRequestBuilders.put("/clients/{id}", idExistente)
                        .content(json)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Thales"))
                .andExpect(jsonPath("$.cpf").value("12345678900"));
    }


    @DisplayName("Verificar se update  retornar “not found” (código 204) quando o id não existir. Fazer uma assertion para\n" +
            "verificar no json de retorno se o campo “error” contém a string “Resource not found”")
    @Test
    public void testarUpdateIDNaoExistente() throws Exception {
        long idNaoExistente = 100l;

        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Thales");
        clientDTO.setCpf("12345678900");

        String json = objectMapper.writeValueAsString(clientDTO);
        mockMvc.perform(MockMvcRequestBuilders.put("/clients/{id}", idNaoExistente)
                        .content(json)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Resource not found")));
    }
}

