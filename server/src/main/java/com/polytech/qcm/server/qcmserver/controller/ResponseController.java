package com.polytech.qcm.server.qcmserver.controller;

import com.polytech.qcm.server.qcmserver.data.Choice;
import com.polytech.qcm.server.qcmserver.data.ChoiceIds;
import com.polytech.qcm.server.qcmserver.data.QCM;
import com.polytech.qcm.server.qcmserver.data.Question;
import com.polytech.qcm.server.qcmserver.data.Response;
import com.polytech.qcm.server.qcmserver.exception.BadRequestException;
import com.polytech.qcm.server.qcmserver.exception.ForbiddenRequestException;
import com.polytech.qcm.server.qcmserver.exception.NotFoundException;
import com.polytech.qcm.server.qcmserver.repository.ChoiceRepository;
import com.polytech.qcm.server.qcmserver.repository.QcmRepository;
import com.polytech.qcm.server.qcmserver.repository.ResponseRepository;
import com.polytech.qcm.server.qcmserver.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/response")
@Api(value = "Controller to authenticate")
public class ResponseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResponseController.class);

  private final ChoiceRepository choiceRepository;
  private final UserRepository userRepository;
  private final ResponseRepository responseRepository;
  private final QcmRepository qcmRepository;
  private final Map<Integer, Integer> currentQuestionMap;

  public ResponseController(ChoiceRepository choiceRepository,
                            UserRepository userRepository,
                            ResponseRepository responseRepository,
                            QcmRepository qcmRepository,
                            Map<Integer, Integer> currentQuestionMap) {
    this.choiceRepository = choiceRepository;
    this.userRepository = userRepository;
    this.responseRepository = responseRepository;
    this.qcmRepository = qcmRepository;
    this.currentQuestionMap = Collections.unmodifiableMap(currentQuestionMap);
  }

  @PostMapping("/")
  @ApiOperation(value = "Posts responses", response = List.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successfully posted the response"),
    @ApiResponse(code = 403, message = "You are not a student"),
    @ApiResponse(code = 400, message = "The user has already responded for a given question"),
    @ApiResponse(code = 404, message = "A choice hasn't been found")
  })
  public ResponseEntity postResponse(Principal user, @RequestBody ChoiceIds cIds) {
    List<Response> responses = new ArrayList<>();
    for (int id : cIds.getIds()) {
      Choice choice = choiceRepository.findById(id).orElseThrow(() -> new NotFoundException("Choice with id " + id + " doesn't exists"));
      checkCanAnswer(choice);
      String username = user.getName();
      Question question = choice.getQuestion();
      List<Response> existingAnswers = responseRepository.findAllByUser_UsernameAndChoice_Question_Id(username, question.getId());
      if (existingAnswers.size() > 0) {
        throw new BadRequestException("User " + username + " has already answered question " + question);
      }
      responses.add(new Response(userRepository.findByUsername(user.getName()).get(), choice));
    }

    responses = responseRepository.saveAll(responses);
    responseRepository.flush();
    LOGGER.info("user {} submitted the given answers {}", user.getName(), responses);
    return ResponseEntity.ok(responses);
  }

  //can answer only if the choice is for a current question
  private void checkCanAnswer(Choice c) {
    QCM qcm = qcmRepository.findById(c.getQuestion().getQcm().getId()).get();
    Integer questionIndex = currentQuestionMap.get(qcm.getId());
    if (questionIndex == null) {
      throw new ForbiddenRequestException("The response given doesn't correspond to any current question");
    }
    Question currentQuestion = qcm.getQuestions().get(questionIndex);
    if (!currentQuestion.equals(c.getQuestion())) {
      throw new ForbiddenRequestException("Question '" + currentQuestion.getQuestion() + "' is not a current question");
    }
  }

}
