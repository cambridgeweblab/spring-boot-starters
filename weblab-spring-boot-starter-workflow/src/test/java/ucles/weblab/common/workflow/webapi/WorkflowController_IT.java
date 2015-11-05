package ucles.weblab.common.workflow.webapi;

import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.activiti.spring.boot.JpaProcessEngineAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ucles.weblab.common.domain.ConfigurableEntitySupport;
import ucles.weblab.common.test.webapi.AbstractRestController_IT;
import ucles.weblab.common.workflow.config.WorkflowConfig;
import ucles.weblab.common.workflow.webapi.resource.WorkflowModelResource;

import java.time.Instant;
import javax.transaction.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test to check workflow operations with Activiti.
 *
 * @since 04/11/15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@WebIntegrationTest(value = "classpath:/public", randomPort = true)
@Transactional
public class WorkflowController_IT extends AbstractRestController_IT {
    @Configuration
    @ComponentScan(basePackageClasses = {WorkflowController.class})
    @Import({ConfigurableEntitySupport.class, JacksonAutoConfiguration.class, JpaProcessEngineAutoConfiguration.class, DataSourceProcessEngineAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, WorkflowConfig.class})
    @EnableAutoConfiguration
    public static class Config {
    }

    @Test
    public void testCreatingNewEmptyModel() throws Exception {
        mockMvc.perform(post("/api/workflow/models/")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json(new WorkflowModelResource("myKey", "myName", Instant.now()))))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/api/workflow/models/"))
                .andExpect(jsonPath("$.list[*].name", hasItem("myName")));
    }
}
