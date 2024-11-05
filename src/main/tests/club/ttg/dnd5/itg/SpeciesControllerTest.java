package club.ttg.dnd5.itg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpeciesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private String jsonContent;

    @BeforeEach
    public void setUp() throws Exception {
        // Read the content of the create_aasimar_request.json file
        ClassPathResource resource = new ClassPathResource("json/request/create_aasimar_request.json");
        jsonContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    //TODO попытаться сделать динамичный, то есть меняется файлик, меняется и expected
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateSpecies() throws Exception {
        mockMvc.perform(post("http://localhost:8080/api/v2/species")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated());
    }
}
