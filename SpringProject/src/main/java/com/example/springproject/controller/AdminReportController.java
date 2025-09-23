    package com.example.springproject.controller;

    import com.example.springproject.model.ReportedPostSummary;
    import com.example.springproject.model.User;
    import com.example.springproject.repository.ReportDao;
    import com.example.springproject.repository.UserDao;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    import java.sql.SQLException;
    import java.util.List;

    @Controller
    public class AdminReportController {

        @Autowired
        private ReportDao reportDao;

        @Autowired
        private UserDao userDao;

        @GetMapping("/reportList")
        public String reportList(Model model) throws SQLException {
            List<ReportedPostSummary> reportedPosts = reportDao.getReportedPostSummaries();
            model.addAttribute("reportedPosts", reportedPosts);
            return "reportList";
        }
        @PostMapping("/banUser")
        public String banUser(@RequestParam String userId, RedirectAttributes redirectAttributes) throws SQLException {
            System.out.println("banUser 호출됨, userId = " + userId);

            if ("administer1234".equals(userId)) {
                redirectAttributes.addFlashAttribute("message", "관리자 계정은 밴할 수 없습니다.");
                return "redirect:/reportList";
            }

            User user = userDao.findById(userId);

            if (user == null) {
                redirectAttributes.addFlashAttribute("message", "존재하지 않는 사용자입니다.");
            } else if (user.isBanned()) {
                redirectAttributes.addFlashAttribute("message", "이미 이용정지된 유저입니다.");
            } else {
                userDao.banUserById(userId);
                redirectAttributes.addFlashAttribute("message", "유저가 성공적으로 밴 처리되었습니다.");
            }

            return "redirect:/reportList";
        }




    }
