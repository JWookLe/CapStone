    package com.example.springproject.controller;

    import com.example.springproject.model.User;
    import com.example.springproject.service.SecurityService;
    import com.example.springproject.service.UserService;
    import com.example.springproject.validator.UserValidator;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.validation.BindingResult;
    import org.springframework.web.bind.annotation.*;

    import javax.servlet.http.HttpSession;

    @Controller
    public class UserController {

        @Autowired
        private UserService userService;

        @Autowired
        private SecurityService securityService;

        @Autowired
        private UserValidator userValidator;

        @GetMapping("/registration")
        public String registration(Model model) {
            model.addAttribute("userForm", new User());
            return "registration";
        }

        @PostMapping("/registration")
        public String registration(@ModelAttribute("userForm") User userForm, BindingResult bindingResult) {
            userValidator.validate(userForm, bindingResult);

            if (bindingResult.hasErrors()) {
                return "registration";
            }

            userService.save(userForm);
            return "redirect:/login";
        }

        @GetMapping("/login")
        public String login(Model model,
                            @RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String banned,  // 추가
                            HttpSession session) {
            if (error != null && !error.isEmpty()) {
                model.addAttribute("error", "Your username and password is invalid.");
            }
            if (logout != null)
                model.addAttribute("message", "You have been logged out successfully.");
            if (banned != null)
                model.addAttribute("bannedMessage", "이용이 정지된 계정입니다.");
            if (session.getAttribute("passwordChanged") != null) {
                model.addAttribute("passwordChanged", true);
                session.removeAttribute("passwordChanged");
            }
            if (session.getAttribute("deleted") != null) {
                model.addAttribute("deleted", true);
                session.removeAttribute("deleted");
            }
            return "login";
        }

        @GetMapping("/welcome")
        public String welcome(HttpSession session) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName(); // 로그인한 사용자의 id

            User user = userService.findById(username);

            System.out.println("User banned? " + user.isBanned());

            if (user.isBanned()) {
                session.invalidate();
                return "redirect:/login?banned";
            }

            session.setAttribute("username", username);
            if ("administer1234".equals(username)) {
                session.setAttribute("isAdmin", true);
                return "redirect:/adminDashboard";
            } else {
                session.setAttribute("isAdmin", false);
                return "redirect:/dashboard";
            }
        }

    }
