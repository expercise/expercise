package com.expercise.controller.challenge;

import com.expercise.domain.challenge.Challenge;
import com.expercise.enums.ChallengeListingMode;
import com.expercise.service.challenge.ChallengeService;
import com.expercise.service.challenge.SolutionCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ChallengeListingController {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private SolutionCountService solutionCountService;

    @RequestMapping("/challenges/myChallenges")
    public ModelAndView listChallengesOfUser() {
        List<Challenge> challenges = challengeService.findAllChallengesOfUser();
        return prepareModelAndViewForListing(challenges, ChallengeListingMode.User.name());
    }

    private ModelAndView prepareModelAndViewForListing(List<Challenge> challenges, String mode) {
        ModelAndView modelAndView = new ModelAndView("challenge/challengeList");
        modelAndView.addObject("challenges", challenges);
        modelAndView.addObject("solutionCountMap", solutionCountService.prepareSolutionCountMapFor(challenges));
        modelAndView.addObject("mode", mode);
        return modelAndView;
    }

}
