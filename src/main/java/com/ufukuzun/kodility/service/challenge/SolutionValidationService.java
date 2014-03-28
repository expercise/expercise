package com.ufukuzun.kodility.service.challenge;

import com.ufukuzun.kodility.controller.challenge.model.SolutionFromUser;
import com.ufukuzun.kodility.domain.challenge.Challenge;
import com.ufukuzun.kodility.domain.challenge.Solution;
import com.ufukuzun.kodility.enums.ProgrammingLanguage;
import com.ufukuzun.kodility.interpreter.Interpreter;
import com.ufukuzun.kodility.interpreter.InterpreterResult;
import com.ufukuzun.kodility.service.challenge.model.SolutionValidationResult;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SolutionValidationService implements ApplicationContextAware {

    @Autowired
    private ChallengeService challengeService;

    private ApplicationContext applicationContext;

    // TODO ufuk: add test for this method
    public SolutionValidationResult validateSolution(SolutionFromUser solutionFromUser) {
        Challenge challenge = challengeService.findById(solutionFromUser.getChallengeId());
        Solution solution = challenge.getSolutionFor(solutionFromUser.getProgrammingLanguage());
        Interpreter interpreter = findInterpreterFor(solutionFromUser.getProgrammingLanguage());

        boolean success = true;
        for (List<String> inputs : challenge.getInputs()) {
            InterpreterResult resultForUser = interpreter.interpret(solution.getSolution(), solution.getTestCodeFor(inputs));
            InterpreterResult resultForSystem = interpreter.interpret(solutionFromUser.getSolution(), solution.getTestCodeFor(inputs));
            if (resultForSystem.isNotValidResult(resultForUser)) {
                success = false;
                break;
            }
        }

        return new SolutionValidationResult(success);
    }

    private Interpreter findInterpreterFor(ProgrammingLanguage programmingLanguage) {
        Map<String, Interpreter> interpreters = applicationContext.getBeansOfType(Interpreter.class);
        for (Interpreter interpreter : interpreters.values()) {
            if (interpreter.canInterpret(programmingLanguage)) {
                return interpreter;
            }
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}