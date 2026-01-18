package com.belyak.taskproject.api.v1.controller;

import com.belyak.taskproject.api.v1.dto.request.CreateCategoryRequest;
import com.belyak.taskproject.api.v1.dto.response.CategoryResponse;
import com.belyak.taskproject.api.v1.dto.response.CreateCategoryResponse;
import com.belyak.taskproject.api.v1.mapper.CategoryApiMapper;
import com.belyak.taskproject.config.SecurityConfig;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;
import com.belyak.taskproject.domain.service.CategoryService;
import com.belyak.taskproject.infrastructure.security.JwtAuthenticationEntryPoint;
import com.belyak.taskproject.infrastructure.security.JwtAuthenticationFilter;
import com.belyak.taskproject.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CategoryApiMapper categoryApiMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // ==========================================
    // GET (PUBLIC)
    // ==========================================

    @Test
    @DisplayName("GET /categories should allow anonymous access (200 OK)")
    void getAllCategories_shouldBePublic() throws Exception {
        var summary = new CategorySummary(UUID.randomUUID(), "Java", 5);
        var response = CategoryResponse.builder().id(summary.id()).name("Java").taskCount(5).build();

        when(categoryService.getAllCategories()).thenReturn(List.of(summary));
        when(categoryApiMapper.toResponseList(List.of(summary))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    // ==========================================
    // POST (PROTECTED)
    // ==========================================

    @Test
    @DisplayName("POST /categories should return 401 if user is NOT authenticated")
    void createCategory_shouldReturn401_whenAnonymous() throws Exception {
        var request = new CreateCategoryRequest("New Category");

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // Теперь здесь вернется реальный 401 от EntryPoint
    }

    @Test
    @DisplayName("POST /categories should return 201 if user IS authenticated")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCategory_shouldReturn201_whenAuthenticated() throws Exception {
        var request = new CreateCategoryRequest("New Category");
        var domainCategory = Category.builder().name("New Category").build();
        var savedCategory = Category.builder().id(UUID.randomUUID()).name("New Category").build();
        var responseDto = new CreateCategoryResponse(savedCategory.getId(), "New Category");

        when(categoryApiMapper.toDomain(request)).thenReturn(domainCategory);
        when(categoryService.createCategory(domainCategory)).thenReturn(savedCategory);
        when(categoryApiMapper.toCreateResponse(savedCategory)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    // ==========================================
    // DELETE (PROTECTED)
    // ==========================================

    @Test
    @DisplayName("DELETE /categories/{id} should return 401 if user is NOT authenticated")
    void deleteCategory_shouldReturn401_whenAnonymous() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/categories/{id}", id)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /categories/{id} should return 204 if user IS authenticated")
    @WithMockUser
    void deleteCategory_shouldReturn204_whenAuthenticated() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/categories/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}