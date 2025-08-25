package com.example.springbootecommerce.product.controller;

import com.example.springbootecommerce.auth.service.TokenBlacklistService;
import com.example.springbootecommerce.product.dto.CreateProductoDTO;
import com.example.springbootecommerce.product.dto.ProductoDTO;
import com.example.springbootecommerce.product.dto.ProductoSummaryDTO;
import com.example.springbootecommerce.product.dto.UpdateProductoDTO;
import com.example.springbootecommerce.product.service.ProductoService;
import com.example.springbootecommerce.shared.security.CustomUserDetailService;
import com.example.springbootecommerce.shared.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@Import(ProductoControllerTest.TestConfig.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductoService productoService;

    private ProductoDTO productoDTO;
    private ProductoSummaryDTO productoSummaryDTO;

    @BeforeEach
    void setUp() {
        productoDTO = ProductoDTO.builder()
                .id(1L)
                .name("Test Product")
                .sku("TP-01")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .build();

        productoSummaryDTO = ProductoSummaryDTO.builder()
                .id(1L)
                .name("Test Product Summary")
                .price(new BigDecimal("99.99"))
                .build();
    }

    static class TestConfig {
        @Bean
        ProductoService productoService() {
            return Mockito.mock(ProductoService.class);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        // Mocks for security dependencies that might be pulled in
        @Bean
        JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        CustomUserDetailService customUserDetailService() {
            return Mockito.mock(CustomUserDetailService.class);
        }

        @Bean
        TokenBlacklistService tokenBlacklistService() {
            return Mockito.mock(TokenBlacklistService.class);
        }
    }

    @Test
    void getProductoById_ShouldReturnProducto_WhenFound() throws Exception {
        when(productoService.getProductById(1L)).thenReturn(productoDTO);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(productoDTO.getName())));
    }

    @Test
    void searchProductos_ShouldReturnPageOfProducts() throws Exception {
        Page<ProductoSummaryDTO> page = new PageImpl<>(Collections.singletonList(productoSummaryDTO));
        when(productoService.searchProducts(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is(productoSummaryDTO.getName())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearProducto_ShouldCreateAndReturnProducto() throws Exception {
        CreateProductoDTO createDto = new CreateProductoDTO();
        createDto.setName("New Product");
        createDto.setPrice(new BigDecimal("10.00"));
        createDto.setSku("NP-01");
        createDto.setStockQuantity(50);
        createDto.setCategoryId(1L);

        when(productoService.createProduct(any(CreateProductoDTO.class))).thenReturn(productoDTO);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(productoDTO.getName())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizarProducto_ShouldUpdateAndReturnProducto() throws Exception {
        UpdateProductoDTO updateDto = new UpdateProductoDTO();
        updateDto.setName("Updated Product");
        updateDto.setPrice(new BigDecimal("12.00"));

        when(productoService.updateProduct(anyLong(), any(UpdateProductoDTO.class))).thenReturn(productoDTO);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(productoDTO.getName())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarProducto_ShouldDeleteAndReturnSuccess() throws Exception {
        doNothing().when(productoService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Producto eliminado exitosamente")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizarStock_ShouldUpdateStockAndReturnSuccess() throws Exception {
        doNothing().when(productoService).updateStock(1L, 50);

        mockMvc.perform(put("/api/v1/products/1/stock")
                        .param("quantity", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Stock actualizado exitosamente")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProductosStockBajo_ShouldReturnListOfProducts() throws Exception {
        List<ProductoSummaryDTO> lowStockList = Collections.singletonList(productoSummaryDTO);
        when(productoService.getLowStockProducts()).thenReturn(lowStockList);

        mockMvc.perform(get("/api/v1/products/stock-bajo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }
}
