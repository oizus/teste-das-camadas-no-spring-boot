package com.iftm.client.services;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.services.exceptions.DatabaseException;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.transaction.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ClientServiceTestIntegracao {
    private long idExistente = 1L, idNaoExistente = 1000L, idDependente = 4L;

    @Autowired
    ClientService clientService;


    @Test
    @DisplayName("Testar o método que deveria retornar uma página com os clientes que tenham o Income informado")
    void findByIncomeGreaterThanPage() {
        double entrada = 4800.00;
        int paginaApresentada = 0;
        int linhasPorPagina = 2;
        String ordemOrdenacao = "ASC";
        String campoOrdenacao = "income";
        Client clienteSete = new Client(7L, "Jose Saramago", "10239254871", 5000.0, Instant.parse("1996-12-23T07:00:00Z"), 0);
        Client clienteQuatro = new Client(4L, "Carolina Maria de Jesus", "10419244771", 7500.0, Instant.parse("1996-12-23T07:00:00Z"), 0);

        PageRequest pagina = PageRequest.of(paginaApresentada, linhasPorPagina, Sort.Direction.valueOf(ordemOrdenacao), campoOrdenacao);

        Page<ClientDTO> page = clientService.findByIncomeGreaterThan(pagina, entrada);

        //Assertions.assertThat(page).isNotEmpty();
        Assertions.assertThat(page.getTotalElements()).isEqualTo(3);
        //Assertions.assertThat(page.getNumberOfElements()).isEqualTo(2);
        //Assertions.assertThat(page.getTotalPages()).isEqualTo(2);
        //Assertions.assertThat(page.toList().get(0).toEntity()).isEqualTo(clienteSete);
        //Assertions.assertThat(page.toList().get(1).toEntity()).isEqualTo(clienteQuatro);

    }

    @Test
    public void testarSeBuscarClientesPorCPFLikeRetornaUmaPaginaComClientesComCPFQueContemTextoInformado() {
        String cpf = "%447%";
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.valueOf("ASC"), "name");

        List<Client> listaClientes = new ArrayList<Client>();
        listaClientes.add(new Client(4l, "Carolina Maria de Jesus", "10419244771", 7500.0, Instant.parse("1996-12-23T07:00:00Z"), 0));

        Page<ClientDTO> resultado = clientService.findByCpfLike(pageRequest, cpf);

        assertFalse(resultado.isEmpty());
        assertEquals(listaClientes.size(), resultado.getNumberOfElements());
        assertEquals(listaClientes.get(0), resultado.toList().get(0).toEntity());
        for (int i = 0; i < listaClientes.size(); i++) {
            assertEquals(listaClientes.get(i), resultado.toList().get(i).toEntity());
        }
    }


    @Test
    @DisplayName("Testar o método que deleta id existente")
    void deleteIDExistente() {
        // Act
        clientService.delete(idExistente);

        // Assert
        assertDoesNotThrow(()->{clientService.delete(idExistente);});
    }

    @Test
    @DisplayName("Testar o método que deleta id não existente")
    void deleteIDNaoExistente() {
        assertThrows(ResourceNotFoundException.class, ()->{clientService.delete(idNaoExistente);});
    }

    @Test
    @DisplayName("Testar o método que deleta id que tem dependencia")
    public void deleteIDTemDependencia() {
        assertThrows(DatabaseException.class, ()-> {clientService.delete(idDependente);});
    }

    @Test
    @DisplayName("Testar o método que deveria retornar uma página com todos os clientes")
    void findAllPaged() {
        PageRequest pageRequest = PageRequest.of(0, 12);

        Page<ClientDTO> result = clientService.findAllPaged(pageRequest);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(12,result.getSize());
        Assertions.assertThat(result.getTotalElements()).isEqualTo(12);
    }


    @Test
    @DisplayName("Testar o método que deveria retornar uma página com os clientes que tenham o Income informado")
    void findByIncomeGreaterThan() {
        double income = 7000.00;

        Pageable pageRequest = PageRequest.of(0, 2);

        Page<ClientDTO> result = clientService.findByIncomeGreaterThan((PageRequest) pageRequest, income);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(0, result.getNumber()); // Verificar o número da página retornada
        assertEquals(2, result.getSize()); // Verificar o tamanho da página retornada
        Assertions.assertThat(result.getTotalElements()).isEqualTo(2);

    }

    @Test
    @DisplayName("Testar o método que encontra id existente")
    void findByIdExistente() {
        // Arrange
        Client client = new Client();
        client.setId(idExistente);
        client.setName("Conceição Evaristo");
        client.setCpf("10619244881");

        // Act & Assert
        ClientDTO clientDTO =  clientService.findById(idExistente);

        // Verify
        assertEquals(client,clientDTO.toEntity());
    }


    @Test
    @DisplayName("Testar o método que encontra id não existente")
    void findByIdNãoExistente() {
        // Act e Assert
        assertThrows(ResourceNotFoundException.class, () -> clientService.findById(idNaoExistente));
    }


    @Test
    void updateIDExistente(){
        // Arrange
        ClientDTO clientDTO = new ClientDTO();
        //dto.setId(idExistente);
        clientDTO.setName("Novo Nome");
        clientDTO.setCpf("00987654321");
        clientDTO.setIncome(5000.0);
        clientDTO.setBirthDate(Instant.parse("1990-01-01T00:00:00Z"));
        clientDTO.setChildren(2);

        // Act
        ClientDTO resultadoDTO = clientService.update(idExistente, clientDTO);

        // Assert
        assertNotNull(clientDTO);
        assertEquals(clientDTO.getName(), resultadoDTO.getName());
        assertEquals(clientDTO.getCpf(), resultadoDTO.getCpf());
        assertEquals(clientDTO.getIncome(), resultadoDTO.getIncome());
        assertEquals(clientDTO.getBirthDate(), resultadoDTO.getBirthDate());
        assertEquals(clientDTO.getChildren(), resultadoDTO.getChildren());
    }

    @Test
    void updateIDNaoExistente(){
        // Arrange
        ClientDTO clientdto = new ClientDTO();
        clientdto.setName("Novo Nome");
        clientdto.setCpf("00987654321");
        clientdto.setIncome(5000.0);
        clientdto.setBirthDate(Instant.parse("1990-01-01T00:00:00Z"));
        clientdto.setChildren(2);

        // Act e Assert
        assertThrows(ResourceNotFoundException.class, () -> clientService.update(idNaoExistente, clientdto));

    }

    @Test
    void insert() {
        Client client = new Client();
        client.setId(13L);
        client.setName("Thales");
        client.setCpf("12345678900");
        client.setIncome(5000.0);
        client.setBirthDate(Instant.parse("1918-09-23T07:00:00Z"));
        client.setChildren(2);

        // Act
        ClientDTO insert = clientService.insert(new ClientDTO(client));

        // Assert
        assertNotNull(insert);
        assertEquals(client.getId(), insert.getId());

    }
}


