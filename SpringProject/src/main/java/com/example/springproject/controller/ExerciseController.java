package com.example.springproject.controller;

import com.example.springproject.model.Exercise;
import com.example.springproject.model.User;
import com.example.springproject.service.ExerciseService;
import com.example.springproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private UserService userService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(LocalDate.parse(text));
            }
        });
    }

    // 운동 페이지 로딩
    @GetMapping("/exercises")
    public String exercises(Model model, Principal principal, @RequestParam(value = "searchDate", required = false) String dateStr) {
        User user = userService.findById(principal.getName());
        LocalDate date;
        if (dateStr != null && !dateStr.isEmpty()) {
            date = LocalDate.parse(dateStr);
        } else {
            date = LocalDate.now();
        }
        List<Exercise> exercises = exerciseService.getExercisesByDate(user, date);
        int totalBurned = exerciseService.getTotalBurned(user, date);
        model.addAttribute("exerciseForm", new Exercise());
        model.addAttribute("exercises", exercises);
        model.addAttribute("totalBurned", totalBurned);
        model.addAttribute("today", date);
        return "exercises";
    }

    // 운동 추가
    @PostMapping("/exercises")
    public String addExercise(@ModelAttribute("exerciseForm") Exercise exercise, Principal principal) {
        User user = userService.findById(principal.getName());
        exerciseService.save(exercise, user);
        return "redirect:/exercises";
    }

    // 운동 삭제
    @GetMapping("/exercises/delete/{id}")
    public String deleteExercise(@PathVariable("id") Long id, Principal principal) {
        exerciseService.deleteById(id);
        return "redirect:/exercises";
    }

    // 운동 세부 설명 페이지 이동
    @GetMapping("/exercise/details")
    public String showExerciseDetails(@RequestParam("name") String name, Model model) {
        model.addAttribute("exerciseName", name);

        String viewName = convertToViewName(name);
        return viewName;
    }

    // 한글 운동명을 JSP 파일명으로 매핑
    private String convertToViewName(String name) {
        switch (name) {
            case "벤치프레스": return "chest/benchPress";
            case "체스트프레스": return "chest/chestPress";
            case "딥스": return "chest/dips";
            case "덤벨프레스": return "chest/dumbbellPress";
            case "스미스머신벤치프레스": return "chest/SmithMachineBenchPress";
            case "케이블 크로스오버": return "chest/cableCrossOver";
            case "펙덱 플라이": return "chest/PecDeckFly";
            case "덤벨 플라이": return "chest/DumbbellFly";
            case "디클라인 머신 프레스": return "chest/DeclineMachinePress";
            case "푸쉬업": return "chest/PushUp";

            case "랫풀다운": return "back/latPulldown";
            case "바벨로우": return "back/barbellRow";
            case "시티드로우": return "back/seatedRow";
            case "풀업": return "back/pullUp";
            case "덤벨로우": return "back/dumbbellRow";
            case "T바로우": return "back/TbarRow";
            case "머신로우": return "back/MachineRow";
            case "업라이트 로우": return "back/UprightRow";
            case "데드리프트": return "back/DeadLift";
            case "백 익스텐션": return "back/BackExtension";
            case "하이로우": return "back/HighRow";

            case "스쿼트": return "leg/squat";
            case "레그프레스": return "leg/legPress";
            case "런지": return "leg/lunge";
            case "레그 익스텐션": return "leg/LegExtension";
            case "불가리안 스플릿 스쿼트": return "leg/BulgarianSplitSquat";
            case "레그 컬": return "leg/LegCurl";
            case "카프 레이즈": return "leg/CalfRaise";

            case "덤벨 컬": return "arm/dumbbellCurl";
            case "바벨 컬": return "arm/barbellCurl";
            case "해머 컬": return "arm/hammerCurl";
            case "푸쉬다운": return "arm/pushDown";
            case "트라이셉스 익스텐션": return "arm/tricepsExtension";
            case "케이블 컬": return "arm/cableCurl";
            case "클로즈그립 벤치프레스": return "arm/closegripBenchPress";
            case "덤벨 킥백": return "arm/dumbbellKickBack";
            case "스컬크러셔": return "arm/skullCrusher";

            case "아놀드 프레스": return "shoulder/ArnoldPress";
            case "벤트오버 리어 델트 레이즈": return "shoulder/BentOverRearDeltRaise";
            case "케이블 레터럴 레이즈": return "shoulder/CableLateralRaise";
            case "페이스 풀": return "shoulder/FacePull";
            case "프론트 레이즈": return "shoulder/FrontRaise";
            case "밀리터리 프레스": return "shoulder/MilitaryPress";
            case "리버스 펙덱 플라이": return "shoulder/ReversePecDeckFly";
            case "사이드 레터럴 레이즈": return "shoulder/SideLateralRaise";

            default: return "exercise/notFound"; // 존재하지 않는 운동은 fallback
        }
    }
}
