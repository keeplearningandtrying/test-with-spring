package com.testwithspring.intermediate.web;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.testwithspring.intermediate.IntegrationTest;
import com.testwithspring.intermediate.IntegrationTestContext;
import com.testwithspring.intermediate.ReplacementDataSetLoader;
import com.testwithspring.intermediate.config.Profiles;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {IntegrationTestContext.class})
@AutoConfigureMockMvc
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class,
        ServletTestExecutionListener.class
})
@DatabaseSetup("/com/testwithspring/intermediate/empty-database.xml")
@DbUnitConfiguration(dataSetLoader = ReplacementDataSetLoader.class)
@Category(IntegrationTest.class)
@ActiveProfiles(Profiles.INTEGRATION_TEST)
public class ProcessCreateNewTaskFormWhenValidationFailTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnHttpStatusCodeOk() throws Exception {
        submitEmptyCreateTaskForm()
                .andExpect(status().isOk());
    }

    @Test
    public void shouldRenderCreateNewTaskView() throws Exception {
        submitEmptyCreateTaskForm()
                .andExpect(view().name(WebTestConstants.View.CREATE_TASK));
    }

    @Test
    public void shouldShowValidationErrorForEmptyTitle() throws Exception {
        submitEmptyCreateTaskForm()
                .andExpect(model().attributeHasFieldErrorCode(WebTestConstants.ModelAttributeName.TASK,
                        WebTestConstants.ModelAttributeProperty.Task.TITLE,
                        is(WebTestConstants.ValidationErrorCode.EMPTY_FIELD)
                ));
    }


    @Test
    public void shouldShowFieldValuesOfCreateTaskForm() throws Exception {
        submitEmptyCreateTaskForm()
                .andExpect(model().attribute(WebTestConstants.ModelAttributeName.TASK, allOf(
                        hasProperty(WebTestConstants.ModelAttributeProperty.Task.DESCRIPTION, is("")),
                        hasProperty(WebTestConstants.ModelAttributeProperty.Task.TITLE, is(""))
                )));
    }

    @Test
    public void shouldNotModifyHiddenIdParameter() throws Exception {
        submitEmptyCreateTaskForm()
                .andExpect(model().attribute(WebTestConstants.ModelAttributeName.TASK, allOf(
                        hasProperty(WebTestConstants.ModelAttributeProperty.Task.ID, nullValue())
                )));
    }

    @Test
    @ExpectedDatabase(value = "/com/testwithspring/intermediate/empty-database.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void shouldNotCreateNewTask() throws Exception {
        submitEmptyCreateTaskForm();
    }

    private ResultActions submitEmptyCreateTaskForm() throws Exception {
        return  mockMvc.perform(post("/task/create")
                .param(WebTestConstants.ModelAttributeProperty.Task.DESCRIPTION, "")
                .param(WebTestConstants.ModelAttributeProperty.Task.TITLE, "")
        );
    }
}