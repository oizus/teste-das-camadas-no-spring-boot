package com.iftm.client.services;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.repositories.ClientRepository;
import com.iftm.client.services.exceptions.DatabaseException;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SpringExtension.class)
class ClientServiceTest {

    private long idExistente = 1L, idNaoExistente = 1000L, idDependente = 4L;

    private Client clienteQuatro = new Client(4L,"Carolina Maria de Jesus", "10419244771", 7500.00, Instant.parse("1996-12-23T07:00:00Z"), 0);


    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;


    @BeforeEach
    void setUp() throws Exception {
        Mockito.doNothing().when(clientRepository).deleteById(idExistente);
        Mockito.doThrow(EmptyResultDataAccessException.class).when(clientRepository).deleteById(idNaoExistente);
        Mockito.doThrow(DataIntegrityViolationException.class).when(clientRepository).deleteById(idDependente);
    }

    @Test
    @DisplayName("Testar o método que deveria retornar uma página com os clientes que tenham o Income informado")
    void findByIncomeGreaterThanPage() {
        // Mock input
        double income = 2000.00;
        Pageable pageRequest = PageRequest.of(0, 10);


        List<Client> clients = new ArrayList<>();
        clients.add(new Client(1L, "Conceição Evaristo", "10619244881", 1500.00, Instant.parse("2020-07-13T20:50:00Z"), 2));
        clients.add(new Client(2L, "Lázaro Ramos", "10619244881", 2500.00, Instant.parse("1996-12-23T07:00:00Z"), 2));
        Page<Client> clientPage = new PageImpl<>(clients, pageRequest, 1);

        Mockito.when(clientRepository.findByIncomeGreaterThan(income, pageRequest)).thenReturn(clientPage);


        Page<ClientDTO> result = clientService.findByIncomeGreaterThan((PageRequest) pageRequest, income);


        Mockito.verify(clientRepository).findByIncomeGreaterThan(income, pageRequest);


        assertEquals(clientPage.getTotalElements(), result.getTotalElements());
        assertEquals(clientPage.getContent().size(), result.getContent().size());

    }

    /**
     * Exemplo Extra
     * Cenário de Teste : método findByIncomeGreaterThan retorna a página com clientes corretos
     * Entrada:
     * - Paginação:
     * - Pagina = 1;
     * - 2
     * - Asc
     * - Income
     * - Income: 4800.00
     * - Clientes:
     * Pagina: 0
     * {
     * "id": 7,
     * "name": "Jose Saramago",
     * "cpf": "10239254871",
     * "income": 5000.0,
     * "birthDate": "1996-12-23T07:00:00Z",
     * "children": 0
     * },
     * <p>
     * {
     * "id": 4,
     * "name": "Carolina Maria de Jesus",
     * "cpf": "10419244771",
     * "income": 7500.0,
     * "birthDate": "1996-12-23T07:00:00Z",
     * "children": 0
     * },
     * <p>
     * Pagina: 1
     * {
     * "id": 8,
     * "name": "Toni Morrison",
     * "cpf": "10219344681",
     * "income": 10000.0,
     * "birthDate": "1940-02-23T07:00:00Z",
     * "children": 0
     * }
     * Resultado:
     * Página não vazia
     * Página contendo um cliente
     * Página contendo o cliente da página 1
     */
    @Test
    @DisplayName("testar método findByIncomeGreaterThan retorna a página com clientes corretos")
    public void testarBuscaPorSalarioMaiorQueRetornaElementosEsperados() {
        //cenário de teste
        double entrada = 4800.00;
        int paginaApresentada = 0;
        int linhasPorPagina = 2;
        String ordemOrdenacao = "ASC";
        String campoOrdenacao = "income";
        Client clienteSete = new Client(7L, "Jose Saramago", "10239254871", 5000.0, Instant.parse("1996-12-23T07:00:00Z"), 0);
        Client clienteQuatro = new Client(4L, "Carolina Maria de Jesus", "10419244771", 7500.0, Instant.parse("1996-12-23T07:00:00Z"), 0);
        Client clienteOito = new Client(8L, "Toni Morrison", "10219344681", 10000.0, Instant.parse("1940-02-23T07:00:00Z"), 0);

        PageRequest pagina = PageRequest.of(paginaApresentada, linhasPorPagina,Sort.Direction.valueOf(ordemOrdenacao), campoOrdenacao);

        //configurar o Mock
        List<Client> lista = new ArrayList<>();
        lista.add(clienteSete);
        lista.add(clienteQuatro);
        Page<Client> paginaEsperada = new PageImpl<>(lista, pagina, 3);
        System.out.println(paginaEsperada.toList().size());
        Mockito.when(clientRepository.findByIncomeGreaterThan(entrada, pagina)).thenReturn(paginaEsperada);

        //testar se o método da service não retorna erro.
        //AtomicReference<Page<ClientDTO>> page = null;
        //Assertions.assertDoesNotThrow(()->{
        //page.set(servico.findByIncomeGreaterThan(pagina, entrada));},"Exception identificada");

        Page<ClientDTO> page = clientService.findByIncomeGreaterThan(pagina, entrada);
        Assertions.assertThat(page).isNotEmpty();
        Assertions.assertThat(page.getTotalElements()).isEqualTo(3);
        //Assertions.assertThat(page.getNumberOfElements()).isEqualTo(2);
        //Assertions.assertThat(page.getTotalPages()).isEqualTo(2);
        //Assertions.assertThat(page.toList().get(0).toEntity()).isEqualTo(clienteSete);
        //Assertions.assertThat(page.toList().get(0).toEntity()).isEqualTo(clienteQuatro);
        /*
        assertThat(page).isNotEmpty();
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getNumberOfElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.toList().get(0).toEntity()).isEqualTo(clienteSete);
        assertThat(page.toList().get(1).toEntity()).isEqualTo(clienteQuatro);

         */
        for (int i = 0; i < lista.size(); i++) {
            assertEquals(lista.get(i), page.toList().get(i).toEntity());
        }
    }


    @Test
    public void testarSeBuscarClientesPorCPFLikeRetornaUmaPaginaComClientesComCPFQueContemTextoInformado() {
        String cpf = "%447%";
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.valueOf("ASC"), "name");


        List<Client> listaClientes = new ArrayList<Client>();
        listaClientes.add(new Client(4l, "Carolina Maria de Jesus", "10419244771", 7500.0, Instant.parse("1996-12-23T07:00:00Z"), 0));

        Page<Client> clientes = new PageImpl<Client>(listaClientes);

        Mockito.when(clientRepository.findByCpfLike(cpf, pageRequest)).thenReturn(clientes);
        Page<ClientDTO> resultado = clientService.findByCpfLike(pageRequest, cpf);
        assertFalse(resultado.isEmpty());
        assertEquals(listaClientes.size(), resultado.getNumberOfElements());
        assertEquals(listaClientes.get(0), resultado.toList().get(0).toEntity());
        Mockito.verify(clientRepository, Mockito.times(1)).findByCpfLike(cpf, pageRequest);
        for (int i = 0; i < listaClientes.size(); i++) {
            assertEquals(listaClientes.get(i), resultado.toList().get(i).toEntity());
        }
    }


    @Test
    @DisplayName("Testar o método que deleta id existente")
    void deleteIDExistente() {
        //Mockito.doNothing().when(clientRepository).deleteById(idExistente);
        assertDoesNotThrow(()->{clientService.delete(idExistente);});
        Mockito.verify(clientRepository, Mockito.times(1)).deleteById(idExistente);
    }

    @Test
    @DisplayName("Testar o método que deleta id não existente")
    void deleteIDNaoExistente() {
        //Mockito.doThrow(ResourceNotFoundException.class).when(clientRepository).deleteById(idNaoExistente);
        assertThrows(ResourceNotFoundException.class, ()->{clientService.delete(idNaoExistente);});
        Mockito.verify(clientRepository, Mockito.times(1)).deleteById(idNaoExistente);
    }

    @Test
    @DisplayName("Testar o método que deleta id que tem dependencia")
    public void deleteIDTemDependencia() {
        assertThrows(DatabaseException.class, ()-> {clientService.delete(idDependente);});
        Mockito.verify(clientRepository, Mockito.times(1)).deleteById(idDependente);
    }


    @Test
    @DisplayName("Testar o método que deveria retornar uma página com todos os clientes")
    void findAllPaged() {

        List<Client> listaClientes = new ArrayList<Client>();
        listaClientes.add(new Client(1l,"Conceição Evaristo","10619244881",1500.00,Instant.parse("2020-07-13T20:50:00Z"), 2));
        listaClientes.add(new Client(2l,"Lázaro Ramos","10619244881",2500.00,Instant.parse("1996-12-23T07:00:00Z"), 2));
        listaClientes.add(new Client(3l,"Clarice Lispector","10919444522",3800.00,Instant.parse("1960-04-13T07:50:00Z"), 2));

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.valueOf("ASC"), "id");

        Page<Client> expectedPage = new PageImpl<>(listaClientes, pageRequest, 1);

        Mockito.when(clientRepository.findAll(pageRequest)).thenReturn(expectedPage);

        Page<ClientDTO> resultPage = clientService.findAllPaged(pageRequest);

        assertEquals(expectedPage, resultPage);
        Mockito.verify(clientRepository, Mockito.times(1)).findAll(pageRequest);
    }

    @Test
    @DisplayName("Testar o método que deveria retornar uma página com os clientes que tenham o Income informado")
    void findByIncomeGreaterThan() {
        // Mock input
        double income = 2000.00;
        Pageable pageRequest = PageRequest.of(0, 10);


        List<Client> clients = new ArrayList<>();
        clients.add(new Client(1L, "Conceição Evaristo", "10619244881", 1500.00, Instant.parse("2020-07-13T20:50:00Z"), 2));
        clients.add(new Client(2L, "Lázaro Ramos", "10619244881", 2500.00, Instant.parse("1996-12-23T07:00:00Z"), 2));
        Page<Client> clientPage = new PageImpl<>(clients, pageRequest, 1);

        Mockito.when(clientRepository.findByIncomeGreaterThan(income, pageRequest)).thenReturn(clientPage);


        Page<ClientDTO> result = clientService.findByIncomeGreaterThan((PageRequest) pageRequest, income);


        Mockito.verify(clientRepository).findByIncomeGreaterThan(income, pageRequest);


        assertEquals(clientPage.getTotalElements(), result.getTotalElements());
        assertEquals(clientPage.getContent().size(), result.getContent().size());

    }

    @Test
    @DisplayName("Testar o método que encontra id existente")
    void findByIdExistente() {
        // Arrange
        Client client = new Client();
        client.setId(idExistente);
        client.setName("Conceição Evaristo");
        client.setCpf("10619244881");
        Mockito.when(clientRepository.findById(idExistente)).thenReturn(java.util.Optional.of(client));
        //Mockito.when(clientRepository.findById(idExistente)).thenReturn(java.util.Optional.of(new Client()));

        // Act & Assert
        ClientDTO clientDTO =  clientService.findById(idExistente);

        // Verify
        assertEquals(client,clientDTO.toEntity());

        Mockito.verify(clientRepository, Mockito.times(1)).findById(idExistente);
    }


    @Test
    @DisplayName("Testar o método que encontra id não existente")
    void findByIdNãoExistente() {
        // Arrange
        Mockito.when(clientRepository.findById(idNaoExistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> clientService.findById(idNaoExistente));

        // Verify
        Mockito.verify(clientRepository, Mockito.times(1)).findById(idNaoExistente);
    }

    @Test
    void updateIDExistente(){
        // Arrange
        ClientDTO dto = new ClientDTO();
        //dto.setId(idExistente);
        dto.setName("Novo Nome");
        dto.setCpf("00987654321");
        dto.setIncome(5000.0);
        dto.setBirthDate(Instant.parse("1990-01-01T00:00:00Z"));
        dto.setChildren(2);

        Client client = new Client();
        //client.setId(idExistente);
        client.setName("Nome Antigo");
        client.setCpf("12345678900");
        client.setIncome(3000.00);
        client.setBirthDate(Instant.parse("1980-01-01T00:00:00Z"));
        client.setChildren(1);

        Mockito.when(clientRepository.getOne(idExistente)).thenReturn(client);
        Mockito.when(clientRepository.save(Mockito.any(Client.class))).thenReturn(client);

        // Act
        ClientDTO resultadoDTO = clientService.update(idExistente, dto);

        // Assert
        assertEquals(dto.getId(), resultadoDTO.getId());

        // Verify
        Mockito.verify(clientRepository, Mockito.times(1)).save(client);

    }

    @Test
    void updateIDNaoExistente(){
        ClientDTO clientdto = new ClientDTO();
        clientdto.setName("Novo Nome");
        clientdto.setCpf("00987654321");
        clientdto.setIncome(5000.0);
        clientdto.setBirthDate(Instant.parse("1990-01-01T00:00:00Z"));
        clientdto.setChildren(2);

        Mockito.when(clientRepository.getOne(idNaoExistente)).thenThrow(EntityNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> clientService.update(idNaoExistente, clientdto));

        Mockito.verify(clientRepository,Mockito.times(0)).save(Mockito.any(Client.class));
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

        Mockito.when(clientRepository.save(Mockito.any(Client.class))).thenReturn(client);

        // Act
        ClientDTO insert = clientService.insert(new ClientDTO(client));

        // Assert
        assertEquals(client.getId(), insert.getId());
        Mockito.verify(clientRepository, Mockito.times(1)).save(client);
    }
}