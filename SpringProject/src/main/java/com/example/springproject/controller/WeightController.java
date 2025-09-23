package com.example.springproject.controller;

import com.example.springproject.model.User;
import com.example.springproject.model.Weight;
import com.example.springproject.service.UserService;
import com.example.springproject.service.WeightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;
import java.security.Principal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Controller
public class WeightController {

    @Autowired
    private WeightService weightService;

    @Autowired
    private UserService userService;

    @GetMapping("/weightChange")
    public String weightPage(Model model, Principal principal,
                             @RequestParam(value = "editId", required = false) Long editId) throws SQLException {

        User user = userService.findById(principal.getName());
        List<Weight> weights = weightService.getWeightRecordsByUser(user);

        model.addAttribute("weights", weights);

        Weight weightToEdit;
        if (editId != null) {
            weightToEdit = weightService.getWeightRecordById(editId);
            if (weightToEdit == null || !weightToEdit.getUserId().equals(user.getId())) {
                weightToEdit = new Weight();
                weightToEdit.setDate(LocalDate.now());
                weightToEdit.setUserId(user.getId());
            }
        } else {
            weightToEdit = new Weight();
            weightToEdit.setDate(LocalDate.now());
            weightToEdit.setUserId(user.getId());
        }

        model.addAttribute("weightForm", weightToEdit);
        return "weightChange";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                } else {
                    setValue(LocalDate.parse(text));
                }
            }
        });
    }

    @PostMapping("/weightChange/save")
    public String saveWeight(@ModelAttribute("weightForm") Weight weight, Principal principal, Model model) throws SQLException {
        User user = userService.findById(principal.getName());
        weight.setUserId(user.getId());

        if (weight.getWeight() == null) {
            model.addAttribute("error", "몸무게를 입력하세요.");
            model.addAttribute("weights", weightService.getWeightRecordsByUser(user));
            model.addAttribute("weightForm", weight);
            return "weightChange";
        }

        if (weight.getDate() == null) {
            weight.setDate(LocalDate.now());
        }

        if (weight.getId() == null) {
            weightService.save(weight);
        } else {
            // 수정 전, 본인 기록인지 확인
            Weight original = weightService.getWeightRecordById(weight.getId());
            if (original != null && original.getUserId().equals(user.getId())) {
                weightService.update(weight);
            } else {
                model.addAttribute("error", "잘못된 요청입니다.");
                model.addAttribute("weights", weightService.getWeightRecordsByUser(user));
                model.addAttribute("weightForm", weight);
                return "weightChange";
            }
        }

        return "redirect:/weightChange";
    }

    @PostMapping("/weightChange/delete")
    public String deleteWeight(@RequestParam("id") Long id, Principal principal) throws SQLException {
        User user = userService.findById(principal.getName());
        Weight weight = weightService.getWeightRecordById(id);
        if (weight != null && weight.getUserId().equals(user.getId())) {
            weightService.deleteById(id);
        }
        return "redirect:/weightChange";
    }
}
