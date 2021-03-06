package com.pickmebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickmebackend.domain.SelfInterview;
import com.pickmebackend.domain.dto.SelfInterviewDto;
import com.pickmebackend.repository.SelfInterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.pickmebackend.error.ErrorMessageConstant.SELFINTERVIEWNOTFOUND;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class SelfInterviewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SelfInterviewRepository selfInterviewRepository;

    @Autowired
    ModelMapper modelMapper;

    private final String selfInterviewUrl = "/api/selfInterviews/";

    @BeforeEach
    void setUp() {
        selfInterviewRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적으로 셀프 인터뷰 생성하기")
    void saveSelfInterview() throws Exception {
        SelfInterviewDto selfInterviewDto = new SelfInterviewDto();
        String title = "사람, 워라벨, 업무만족도, 연봉 중 중요한 순서대로 나열한다면?";
        String content = "사람 > 업무만족도 > 연봉 > 워라벨";
        selfInterviewDto.setTitle(title);
        selfInterviewDto.setContent(content);

        mockMvc.perform(post(selfInterviewUrl)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selfInterviewDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("title").value(title))
                .andExpect(jsonPath("content").value(content));
    }

    @Test
    @DisplayName("정상적으로 셀프 인터뷰 수정하기")
    void updateSelfInterview() throws Exception {
        SelfInterview selfInterview = createSelfInterview();
        String updateContent = "워라벨이 가장 중요한 것 같습니다.";

        assertNotNull(selfInterview.getId());
        assertNotNull(selfInterview.getTitle());
        assertNotNull(selfInterview.getContent());

        selfInterview.setContent(updateContent);

        SelfInterviewDto selfInterviewDto = modelMapper.map(selfInterview, SelfInterviewDto.class);
        mockMvc.perform(put(selfInterviewUrl + "{selfInterviewId}", selfInterview.getId())
                                        .accept(MediaTypes.HAL_JSON_VALUE)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(selfInterviewDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("title").value("회사를 고를 때 가장 중요하게 생각하는 것은?"))
                .andExpect(jsonPath("content").value(updateContent));
    }

    @Test
    @DisplayName("데이터베이스 저장되어 있지 않은 셀프 인터뷰의 수정을 요청")
    void updateSelfInterview_not_found() throws Exception {
        SelfInterview selfInterview = createSelfInterview();
        String updateContent = "워라벨이 가장 중요한 것 같습니다.";

        assertNotNull(selfInterview.getId());
        assertNotNull(selfInterview.getTitle());
        assertNotNull(selfInterview.getContent());

        selfInterview.setContent(updateContent);

        SelfInterviewDto selfInterviewDto = modelMapper.map(selfInterview, SelfInterviewDto.class);
        mockMvc.perform(put(selfInterviewUrl + "{selfInterviewId}", -1)
                                        .accept(MediaTypes.HAL_JSON_VALUE)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(selfInterviewDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(SELFINTERVIEWNOTFOUND));
    }

    @Test
    @DisplayName("정상적으로 셀프 인터뷰 삭제하기")
    void deleteSelfInterview() throws Exception {
        SelfInterview selfInterview = createSelfInterview();

        assertNotNull(selfInterview.getId());
        assertNotNull(selfInterview.getTitle());
        assertNotNull(selfInterview.getContent());

        mockMvc.perform(delete(selfInterviewUrl + "{selfInterviewId}", selfInterview.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("데이터베이스 저장되어 있지 않은 셀프 인터뷰의 삭제를 요청")
    void deleteSelfInterview_not_found() throws Exception {
        SelfInterview selfInterview = createSelfInterview();

        assertNotNull(selfInterview.getId());
        assertNotNull(selfInterview.getTitle());
        assertNotNull(selfInterview.getContent());

        mockMvc.perform(delete(selfInterviewUrl + "{selfInterviewId}", -1))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(SELFINTERVIEWNOTFOUND));
    }

    private SelfInterview createSelfInterview() {
        SelfInterview selfInterview = new SelfInterview();
        selfInterview.setTitle("회사를 고를 때 가장 중요하게 생각하는 것은?");
        selfInterview.setContent("배울 것이 많은 직장");
        return selfInterviewRepository.save(selfInterview);
    }
}