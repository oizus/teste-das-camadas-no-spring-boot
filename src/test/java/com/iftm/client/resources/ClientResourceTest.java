package com.iftm.client.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ClientResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

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

        when(clientService.insert(any(ClientDTO.class))).thenReturn(clientDTO);

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

        doNothing().when(clientService).delete(idExistente);

        // Act & Assert
        mockMvc.perform(delete("/clients/{id}", idExistente))
                .andExpect(status().isNoContent());
    }

    @DisplayName("Verificar se delete retornar “not found” (código 404) quando o id não existir")
    @Test
    public void testarDeleteNotFound() throws Exception {

        long idNaoExixtente = 100l;

        doThrow(new ResourceNotFoundException("Id not found " + idNaoExixtente)).when(clientService).delete(idNaoExixtente);


        mockMvc.perform(delete("/clients/{id}", idNaoExixtente))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Verificar se findByIncomeGreaterThan retornar OK (código 200), bem como os clientes que tenham o Income informado.")
    @Test
    public void testarFindByIncomeGreaterThan() throws Exception {

        List<ClientDTO> clients = new ArrayList<>();

        clients.add(new ClientDTO(new Client(4l, "Carolina Maria de Jesus", "10419244771", 7500.0, Instant.parse("1996-12-23T07:00:00Z"), 0)));
        clients.add(new ClientDTO(new Client(8l, "Toni Morrison", "10219344681", 10000.0, Instant.parse("1940-02-23T07:00:00Z"), 0)));

        Page<ClientDTO> clientPage = new PageImpl<>(clients);

        when(clientService.findByIncomeGreaterThan(any(), anyDouble())).thenReturn(clientPage);

        mockMvc.perform(get("/clients/incomeGreaterThan/")
                        .param("income", "7000")
                        .param("page", "0")
                        .param("linesPerPage", "12")
                        .param("direction", "ASC")
                        .param("orderBy", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.*", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("Carolina Maria de Jesus"))
                .andExpect(jsonPath("$.content[0].income").value(7500.0))
                .andExpect(jsonPath("$.content[1].name").value("Toni Morrison"))
                .andExpect(jsonPath("$.content[1].income").value(10000.0));
    }

    @DisplayName("Verificar se update  retornar “ok” (código 200), bem como o json do produto atualizado para um id existente, verificando no mínimo dois atributos.")
    @Test
    public void testarUpdateIDExistente() throws Exception {
        long idExistente = 1l;

        ClientDTO dto = new ClientDTO();
        dto.setName("Nome Antigo");
        dto.setIncome(5000.0);

        ClientDTO updatedDto = new ClientDTO();
        updatedDto.setName("Nome Novo");
        updatedDto.setIncome(6000.0);

        when(clientService.update(eq(idExistente), any(ClientDTO.class))).thenReturn(updatedDto);

        // Act and Assert
        mockMvc.perform(put("/clients/{id}", idExistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedDto.getName()))
                .andExpect(jsonPath("$.income").value(updatedDto.getIncome()));
    }

    @DisplayName("Verificar se update  retornar “not found” (código 204) quando o id não existir. Fazer uma assertion para\n" +
            "verificar no json de retorno se o campo “error” contém a string “Resource not found”")
    @Test
    public void testarUpdateIDNaoExistente() throws Exception {
        long idNaoExistente = 100l;

        when(clientService.update(eq(idNaoExistente), any(ClientDTO.class))).thenThrow(ResourceNotFoundException.class);

        String json = objectMapper.writeValueAsString(new ClientDTO());

        mockMvc.perform(put("/clients/{id}", idNaoExistente)
                        .content(json)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Resource not found")));
    }
}

/*

.content(json): Essa linha define o corpo da requisição HTTP que será enviada no teste. No caso, você está definindo o
conteúdo do corpo como uma string JSON representada pela variável json. Essa string JSON será serializada e enviada no
corpo da requisição PUT.

.contentType(APPLICATION_JSON): Aqui você está definindo o tipo de conteúdo (Content-Type) da requisição HTTP como
application/json. Isso informa ao servidor que o conteúdo enviado no corpo da requisição é um payload JSON.

.accept(APPLICATION_JSON): Essa linha define o tipo de conteúdo (Accept) que o cliente espera receber na resposta do
servidor. Nesse caso, você está indicando que o cliente espera receber uma resposta com o tipo de conteúdo
application/json. Isso permite que o servidor saiba que deve retornar a resposta no formato JSON.

 */