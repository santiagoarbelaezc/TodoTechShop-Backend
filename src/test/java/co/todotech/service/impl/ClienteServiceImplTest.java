package co.todotech.service.impl;

import co.todotech.mapper.ClienteMapper;
import co.todotech.model.dto.cliente.ClienteDto;
import co.todotech.model.entities.Cliente;
import co.todotech.model.enums.TipoCliente;
import co.todotech.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteDto clienteDto;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteDto = new ClienteDto(
                "Juan Pérez", "123456789", "juan@example.com",
                "+573001234567", "Calle 123", TipoCliente.NATURAL, 10.0
        );

        cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .cedula("123456789")
                .correo("juan@example.com")
                .telefono("+573001234567")
                .direccion("Calle 123")
                .tipoCliente(TipoCliente.NATURAL)
                .descuentoAplicable(10.0)
                .build();
    }

    @Test
    @DisplayName("Crear cliente exitosamente")
    void testCrearClienteExitoso() throws Exception {
        when(clienteRepository.existsByCedula("123456789")).thenReturn(false);
        when(clienteRepository.existsByCorreo("juan@example.com")).thenReturn(false);
        when(clienteMapper.toEntity(clienteDto)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toDto(cliente)).thenReturn(clienteDto);

        ClienteDto resultado = clienteService.crearCliente(clienteDto);

        assertNotNull(resultado);
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Error al crear cliente con cédula duplicada")
    void testCrearClienteConCedulaDuplicada() {
        when(clienteRepository.existsByCedula("123456789")).thenReturn(true);

        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.crearCliente(clienteDto);
        });

        assertEquals("Ya existe un cliente con la cédula: 123456789", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Error al crear cliente con correo duplicado")
    void testCrearClienteConCorreoDuplicado() {
        when(clienteRepository.existsByCedula("123456789")).thenReturn(false);
        when(clienteRepository.existsByCorreo("juan@example.com")).thenReturn(true);

        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.crearCliente(clienteDto);
        });

        assertEquals("Ya existe un cliente con el correo: juan@example.com", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear cliente con correo nulo")
    void testCrearClienteConCorreoNulo() throws Exception {
        ClienteDto dtoSinCorreo = new ClienteDto(
                "Juan Pérez", "123456789", null, "+573001234567",
                "Calle 123", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.existsByCedula("123456789")).thenReturn(false);
        when(clienteMapper.toEntity(dtoSinCorreo)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toDto(cliente)).thenReturn(dtoSinCorreo);

        ClienteDto resultado = clienteService.crearCliente(dtoSinCorreo);

        assertNotNull(resultado);
        verify(clienteRepository, never()).existsByCorreo(anyString());
    }

    @Test
    @DisplayName("Actualizar cliente exitosamente")
    void testActualizarClienteExitoso() throws Exception {
        ClienteDto dtoActualizado = new ClienteDto(
                "Juan Pérez Actualizado", "123456789", "juan.actualizado@example.com",
                "+573001234568", "Calle Actualizada", TipoCliente.JURIDICO, 15.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCorreoAndIdNot("juan.actualizado@example.com", 1L)).thenReturn(false);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toDto(cliente)).thenReturn(dtoActualizado);

        ClienteDto resultado = clienteService.actualizarCliente(1L, dtoActualizado);

        assertNotNull(resultado);
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Error al actualizar cliente no encontrado")
    void testActualizarClienteNoEncontrado() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.actualizarCliente(1L, clienteDto);
        });

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Error al actualizar con cédula duplicada")
    void testActualizarClienteConCedulaDuplicada() {
        ClienteDto dtoNuevaCedula = new ClienteDto(
                "Juan Pérez", "987654321", "juan@example.com",
                "+573001234567", "Calle 123", TipoCliente.NATURAL, 10.0
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCedulaAndIdNot("987654321", 1L)).thenReturn(true);

        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.actualizarCliente(1L, dtoNuevaCedula);
        });

        assertEquals("Ya existe otro cliente con la cédula: 987654321", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Eliminar cliente exitosamente")
    void testEliminarCliente() throws Exception {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).delete(cliente);

        clienteService.eliminarCliente(1L);

        verify(clienteRepository).delete(cliente);
    }

    @Test
    @DisplayName("Error al eliminar cliente no encontrado")
    void testEliminarClienteNoEncontrado() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            clienteService.eliminarCliente(1L);
        });

        assertEquals("Cliente no encontrado con ID: 1", exception.getMessage());
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Obtener cliente por ID exitosamente")
    void testObtenerClientePorId() throws Exception {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toDto(cliente)).thenReturn(clienteDto);

        ClienteDto resultado = clienteService.obtenerClientePorId(1L);

        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.nombre());
    }

    @Test
    @DisplayName("Obtener cliente por cédula exitosamente")
    void testObtenerClientePorCedula() throws Exception {
        when(clienteRepository.findByCedula("123456789")).thenReturn(Optional.of(cliente));
        when(clienteMapper.toDto(cliente)).thenReturn(clienteDto);

        ClienteDto resultado = clienteService.obtenerClientePorCedula("123456789");

        assertNotNull(resultado);
        assertEquals("123456789", resultado.cedula());
    }

    @Test
    @DisplayName("Obtener cliente por correo exitosamente")
    void testObtenerClientePorCorreo() throws Exception {
        when(clienteRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(cliente));
        when(clienteMapper.toDto(cliente)).thenReturn(clienteDto);

        ClienteDto resultado = clienteService.obtenerClientePorCorreo("juan@example.com");

        assertNotNull(resultado);
        assertEquals("juan@example.com", resultado.correo());
    }

    @Test
    @DisplayName("Obtener clientes por tipo")
    void testObtenerClientesPorTipo() {
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findByTipoCliente(TipoCliente.NATURAL)).thenReturn(clientes);
        when(clienteMapper.toDto(cliente)).thenReturn(clienteDto);

        List<ClienteDto> resultados = clienteService.obtenerClientesPorTipo(TipoCliente.NATURAL);

        assertEquals(1, resultados.size());
        assertEquals(TipoCliente.NATURAL, resultados.get(0).tipoCliente());
    }

    @Test
    @DisplayName("Obtener todos los clientes")
    void testObtenerTodosLosClientes() {
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findAllOrderedByFechaRegistro()).thenReturn(clientes);
        when(clienteMapper.toDto(cliente)).thenReturn(clienteDto);

        List<ClienteDto> resultados = clienteService.obtenerTodosLosClientes();

        assertEquals(1, resultados.size());
        verify(clienteRepository).findAllOrderedByFechaRegistro();
    }

    @Test
    @DisplayName("Contar clientes por tipo")
    void testContarClientesPorTipo() {
        when(clienteRepository.countByTipoCliente(TipoCliente.NATURAL)).thenReturn(5L);

        long resultado = clienteService.contarClientesPorTipo(TipoCliente.NATURAL);

        assertEquals(5L, resultado);
        verify(clienteRepository).countByTipoCliente(TipoCliente.NATURAL);
    }
}