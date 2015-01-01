package com.kodility.service.challenge.action.postaction;

import com.kodility.domain.challenge.Challenge;
import com.kodility.domain.challenge.Solution;
import com.kodility.domain.user.User;
import com.kodility.enums.ProgrammingLanguage;
import com.kodility.interpreter.InterpreterResult;
import com.kodility.service.challenge.SolutionCountService;
import com.kodility.service.challenge.SolutionService;
import com.kodility.service.challenge.model.ChallengeEvaluationContext;
import com.kodility.service.user.AuthenticationService;
import com.kodility.testutils.builder.ChallengeBuilder;
import com.kodility.testutils.builder.SolutionBuilder;
import com.kodility.testutils.builder.UserBuilder;
import com.kodility.utils.Clock;
import com.kodility.utils.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SaveUserSolutionPostActionTest {

    @InjectMocks
    private SaveUserSolutionPostAction action;

    @Mock
    private SolutionService solutionService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private SolutionCountService solutionCountService;

    @Test
    public void shouldBeAbleToExecuteIfEvaluationIsSucceed() {
        ChallengeEvaluationContext context = new ChallengeEvaluationContext();
        context.setInterpreterResult(InterpreterResult.createSuccessResult());

        assertTrue(action.canExecute(context));
    }

    @Test
    public void shouldNotBeAbleToExecuteIfEvaluationIsFailed() {
        ChallengeEvaluationContext context = new ChallengeEvaluationContext();
        context.setInterpreterResult(InterpreterResult.createFailedResult());

        assertFalse(action.canExecute(context));
    }

    @Test
    public void shouldSaveUserSolutionIfUserDoesNotHaveSolutionForThatChallengeBefore() {
        Clock.freeze(DateUtils.toDate("10/12/2012"));

        User user = new UserBuilder().email("user@kodility.com").buildWithRandomId();
        Challenge challenge = new ChallengeBuilder().buildWithRandomId();

        ChallengeEvaluationContext context = new ChallengeEvaluationContext();
        context.setInterpreterResult(InterpreterResult.createSuccessResult());
        context.setChallenge(challenge);
        context.setLanguage(ProgrammingLanguage.Python);
        context.setSource("this is a solution of the user");

        when(authenticationService.getCurrentUser()).thenReturn(user);

        action.execute(context);

        ArgumentCaptor<Solution> solutionCaptor = ArgumentCaptor.forClass(Solution.class);
        verify(solutionService).saveSolution(solutionCaptor.capture());

        Solution capturedSolution = solutionCaptor.getValue();
        assertThat(capturedSolution.getChallenge(), equalTo(context.getChallenge()));
        assertThat(capturedSolution.getUser(), equalTo(user));
        assertThat(capturedSolution.getCreateDate(), equalTo(Clock.getTime()));
        assertThat(capturedSolution.getSolution(), equalTo("this is a solution of the user"));
        assertThat(capturedSolution.getProgrammingLanguage(), equalTo(ProgrammingLanguage.Python));

        verify(solutionCountService).clearCacheFor(challenge.getId());

        Clock.unfreeze();
    }

    @Test
    public void shouldUpdateUserCurrentSolutionIfUserAlreadyHaveSolutionForThatChallengeBefore() {
        Clock.freeze(DateUtils.toDate("10/12/2012"));

        User user = new UserBuilder().email("user@kodility.com").buildWithRandomId();
        Challenge challenge = new ChallengeBuilder().buildWithRandomId();
        Solution solution = new SolutionBuilder().id(1L).createDate(DateUtils.toDate("09/10/2012")).challenge(challenge).user(user).programmingLanguage(ProgrammingLanguage.Python).build();

        ChallengeEvaluationContext context = new ChallengeEvaluationContext();
        context.setInterpreterResult(InterpreterResult.createSuccessResult());
        context.setChallenge(challenge);
        context.setLanguage(ProgrammingLanguage.JavaScript);
        context.setSource("new solution of the user");

        when(authenticationService.getCurrentUser()).thenReturn(user);
        when(solutionService.getSolutionBy(challenge, user, ProgrammingLanguage.JavaScript)).thenReturn(solution);

        action.execute(context);

        ArgumentCaptor<Solution> solutionCaptor = ArgumentCaptor.forClass(Solution.class);
        verify(solutionService).updateSolution(solutionCaptor.capture());

        Solution capturedSolution = solutionCaptor.getValue();
        assertThat(capturedSolution.getId(), equalTo(solution.getId()));
        assertThat(capturedSolution.getChallenge(), equalTo(context.getChallenge()));
        assertThat(capturedSolution.getUser(), equalTo(user));
        assertThat(capturedSolution.getCreateDate(), equalTo(Clock.getTime()));
        assertThat(capturedSolution.getSolution(), equalTo("new solution of the user"));
        assertThat(capturedSolution.getProgrammingLanguage(), equalTo(ProgrammingLanguage.JavaScript));

        verifyZeroInteractions(solutionCountService);

        Clock.unfreeze();
    }

}