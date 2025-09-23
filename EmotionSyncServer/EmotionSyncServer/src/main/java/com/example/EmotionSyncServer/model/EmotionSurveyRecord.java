package com.example.EmotionSyncServer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "emotion_survey_records")
public class EmotionSurveyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long surveyId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "record_id", nullable = false)
    private EmotionRecord emotionRecord;

    @Column(nullable = false)
    private int question1;

    @Column(nullable = false)
    private int question2;

    @Column(nullable = false)
    private int question3;

    @Column(nullable = false)
    private int question4;

    @Column(nullable = false, length = 1000)
    private String question5;

    // 기본 생성자
    public EmotionSurveyRecord() {
    }

    // 생성자
    public EmotionSurveyRecord(User user, EmotionRecord emotionRecord, int question1, int question2,
                               int question3, int question4, String question5) {
        this.user = user;
        this.emotionRecord = emotionRecord;
        this.question1 = question1;
        this.question2 = question2;
        this.question3 = question3;
        this.question4 = question4;
        this.question5 = question5;
    }

    // Getter 메서드
    public Long getSurveyId() {
        return surveyId;
    }

    public User getUser() {
        return user;
    }

    public EmotionRecord getEmotionRecord() {
        return emotionRecord;
    }

    public int getQuestion1() {
        return question1;
    }

    public int getQuestion2() {
        return question2;
    }

    public int getQuestion3() {
        return question3;
    }

    public int getQuestion4() {
        return question4;
    }

    public String getQuestion5() {
        return question5;
    }

    // Setter 메서드
    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEmotionRecord(EmotionRecord emotionRecord) {
        this.emotionRecord = emotionRecord;
    }

    public void setQuestion1(int question1) {
        this.question1 = question1;
    }

    public void setQuestion2(int question2) {
        this.question2 = question2;
    }

    public void setQuestion3(int question3) {
        this.question3 = question3;
    }

    public void setQuestion4(int question4) {
        this.question4 = question4;
    }

    public void setQuestion5(String question5) {
        this.question5 = question5;
    }
}