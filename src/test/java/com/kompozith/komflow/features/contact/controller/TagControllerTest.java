package com.kompozith.komflow.features.contact.controller;

import com.kompozith.komflow.configuration.security.JwtUtil;
import com.kompozith.komflow.features.contact.dto.TagDto;
import com.kompozith.komflow.features.contact.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.PathMatcher;

import java.util.List;

import static com.kompozith.komflow.features.core.util.AppConstants.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private TagService tagService;

    TagDto tagDto1;
    TagDto tagDto2;
    TagDto tagDto3;
    List<TagDto> tagDtoList;

    @Autowired
    private PathMatcher mvcPathMatcher;

    @BeforeEach
    void setUp() {

        tagDto1 = new TagDto();
        tagDto1.setId(1L);
        tagDto1.setName("Tag 1");
        tagDto1.setColorCode("F435A090");
        tagDto1.setDescription("Tag 1 Description");

        tagDto2 = new TagDto();
        tagDto2.setId(2L);
        tagDto2.setName("Tag 2");
        tagDto2.setColorCode("F435A091");
        tagDto2.setDescription("Tag 2 Description");

        tagDto3 = new TagDto();
        tagDto3.setId(3L);
        tagDto3.setName("Tag 3");
        tagDto3.setColorCode("F435A092");
        tagDto3.setDescription("Tag 3 Description");

        tagDtoList = List.of(tagDto1,tagDto2,tagDto3);

    }

    @Test
    @WithMockUser(authorities = {"TAG_LIST"})
    void shouldReturnTagList_whenUserIsAuthenticatedAndHasPermission() throws Exception {

        // Arrange : mock tagService
        when(tagService.findAll()).thenReturn(tagDtoList);

        // Act & Assert: perform the test request
        mockMvc.perform(get(API_PREFIX_V1+"/tag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(tagDtoList.size()))
                .andExpect(jsonPath("$.[0].id").value(tagDto1.getId()))
                .andExpect(jsonPath("$.[0].name").value(tagDto1.getName()))
                .andExpect(jsonPath("$.[1].id").value(tagDto2.getId()));

        verify(tagService).findAll();
    }

    @Test
    @WithMockUser
    void shouldReturn403ForbiddenError_whenUserIsAuthenticatedButDontHaveRequiredPermission() throws Exception {

        // Arrange : mock tagService
        when(tagService.findAll()).thenReturn(tagDtoList);

        // Act & Assert: perform the test request
        mockMvc.perform(get(API_PREFIX_V1+"/tag"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tagService);
    }

    @Test
    void shouldReturn401Error_whenUserIsNotAuthenticated() throws Exception {

        // Arrange : mock tagService
        when(tagService.findAll()).thenReturn(tagDtoList);

        // Act & Assert: perform the test request
        mockMvc.perform(get(API_PREFIX_V1+"/tag"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tagService);
    }

}