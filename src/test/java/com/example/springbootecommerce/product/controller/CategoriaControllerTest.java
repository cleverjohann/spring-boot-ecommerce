package com.example.springbootecommerce.product.controller;

import com.example.springbootecommerce.auth.service.TokenBlacklistService;
import com.example.springbootecommerce.product.dto.CategoriaDTO;
import com.example.springbootecommerce.product.dto.CategoriaTreeDTO;
import com.example.springbootecommerce.product.dto.CreateCategoriaDTO;
import com.example.springbootecommerce.product.service.CategoriaService;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(CategoriaController.class)
@Import(CategoriaControllerTest.TestConfig.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaService categoriaService;

    private CategoriaDTO categoriaDTO;

    @BeforeEach
    void setUp() {
        categoriaDTO = CategoriaDTO.builder()
                .id(1L)
                .name("Electronics")
                .build();
    }

    static class TestConfig {

        @Bean
        CategoriaService categoriaService() {
            return Mockito.mock(CategoriaService.class);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

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
    void getAllCategories_ShouldReturnListOfCategories() throws Exception {
        List<CategoriaDTO> categories = Collections.singletonList(categoriaDTO);
        when(categoriaService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is(categoriaDTO.getName())));
    }

    @Test
    void getRootCategoriesTree_ShouldReturnTree() throws Exception {
        CategoriaTreeDTO tree = new CategoriaTreeDTO();
        tree.setId(1L);
        tree.setName("Electronics");
        List<CategoriaTreeDTO> categoryTree = Collections.singletonList(tree);
        when(categoriaService.getCategoryTree()).thenReturn(categoryTree);

        mockMvc.perform(get("/api/v1/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Electronics")));
    }

    @Test
    void getCategoriaById_ShouldReturnCategory() throws Exception {
        when(categoriaService.getCategoryById(1L)).thenReturn(categoriaDTO);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(categoriaDTO.getName())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategoria_ShouldCreateAndReturnCategory() throws Exception {
        CreateCategoriaDTO createDto = new CreateCategoriaDTO();
        createDto.setName("New Category");

        when(categoriaService.createCategory(any(CreateCategoriaDTO.class))).thenReturn(categoriaDTO);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk()) //The controller returns 200 OK
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(categoriaDTO.getName())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategoria_ShouldUpdateAndReturnCategory() throws Exception {
        when(categoriaService.updateCategory(anyLong(), any(CategoriaDTO.class))).thenReturn(categoriaDTO);

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is(categoriaDTO.getName())));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategoria_ShouldDeleteAndReturnSuccess() throws Exception {
        doNothing().when(categoriaService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Categoria eliminada exitosamente")));
    }
}
