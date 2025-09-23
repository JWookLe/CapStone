package com.example.springproject.controller;

import com.example.springproject.model.BoardPost;
import com.example.springproject.model.BoardComment;
import com.example.springproject.model.Report;
import com.example.springproject.model.User;
import com.example.springproject.repository.BoardPostDao;
import com.example.springproject.repository.BoardCommentDao;
import com.example.springproject.repository.ReportDao;
import com.example.springproject.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/bulletinBoard")
public class BoardController {
    @Autowired
    private BoardPostDao postDao;
    @Autowired
    private BoardCommentDao commentDao;
    @Autowired
    private ReportDao reportDao;
    @Autowired
    private UserDao userDao;

    private String maskUserId(String userId) {
        if (userId == null || userId.length() <= 4) return userId;
        String visible = userId.substring(0, 4);
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < userId.length() - 4; i++) {
            masked.append("*");
        }
        return visible + masked.toString();
    }

    @GetMapping
    public String list(Model model) throws SQLException {
        List<BoardPost> postList = postDao.findAll();
        for (BoardPost post : postList) {
            post.setMaskedUserId(maskUserId(post.getUserId()));
        }
        model.addAttribute("postList", postList);
        return "boardList";
    }

    @GetMapping("/write")
    public String writeForm() {
        return "boardWrite";
    }

    @PostMapping("/write")
    public String write(@RequestParam String title, @RequestParam String content) throws SQLException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        BoardPost post = new BoardPost();
        post.setTitle(title);
        post.setUserId(userId);
        post.setContent(content);
        postDao.save(post);
        return "redirect:/bulletinBoard";
    }

    @GetMapping("/view")
    public String view(@RequestParam Long id, Model model) throws SQLException {
        postDao.increaseViewCount(id);
        BoardPost post = postDao.findById(id);
        post.setMaskedUserId(maskUserId(post.getUserId()));
        List<BoardComment> commentList = commentDao.findByPostId(id);
        for (BoardComment comment : commentList) {
            comment.setMaskedUserId(maskUserId(comment.getUserId()));
        }
        model.addAttribute("post", post);
        model.addAttribute("commentList", commentList);
        return "boardView";
    }

    @PostMapping("/comment")
    public String comment(@RequestParam Long postId, @RequestParam(required = false) Long parentId,
                          @RequestParam String content) throws SQLException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        BoardComment comment = new BoardComment();
        comment.setPostId(postId);
        comment.setParentId(parentId);
        comment.setUserId(userId);
        comment.setContent(content);
        commentDao.save(comment);
        return "redirect:/bulletinBoard/view?id=" + postId;
    }
    @PostMapping("/delete")
    public String delete(@RequestParam Long id) throws SQLException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        System.out.println("로그인한 사용자 ID: " + userId);

        BoardPost post = postDao.findById(id);
        if (post == null) {
            System.out.println("게시글이 존재하지 않습니다. id = " + id);
            return "redirect:/bulletinBoard/view?id=" + id;
        }

        System.out.println("게시글 작성자 ID: " + post.getUserId());

        User loginUser = userDao.findById(userId); // ← 이건 id 기준 조회
        if (loginUser == null) {
            System.out.println("해당 유저 정보를 찾을 수 없습니다.");
            return "redirect:/bulletinBoard/view?id=" + id;
        }

        // admin 역할 가진 사용자 가져오기
        User adminUser = userDao.findByRole("admin");

        boolean isAdmin = (adminUser != null && adminUser.getId().equals(userId));

        System.out.println("isAdmin 여부: " + isAdmin);

        // 본인 작성자거나, 로그인 사용자가 admin이면 삭제 가능
        if (!post.getUserId().equals(userId) && !isAdmin) {
            System.out.println("삭제 권한 없음: 본인도 아니고 관리자도 아님.");
            return "redirect:/bulletinBoard/view?id=" + id;
        }

        System.out.println("삭제 권한 확인됨, 게시글과 댓글 삭제 진행.");
        reportDao.deleteByPostId(id);
        commentDao.deleteByPostId(id);
        postDao.deleteById(id);
        return "redirect:/bulletinBoard";
    }


    @PostMapping("/report")
    public String report(@RequestParam Long postId, @RequestParam String reason) throws SQLException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        Report report = new Report();
        report.setPostId(postId);
        report.setReporterId(userId);
        report.setReason(reason);
        report.setCreatedAt(java.time.LocalDateTime.now());
        reportDao.save(report);
        return "redirect:/bulletinBoard/view?id=" + postId;
    }



} 