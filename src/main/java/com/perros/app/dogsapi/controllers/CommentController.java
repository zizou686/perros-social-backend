package com.perros.app.dogsapi.controllers;

import com.perros.app.dogsapi.dto.CommentDTO;
import com.perros.app.dogsapi.dto.UserDTO;
import com.perros.app.dogsapi.models.Comment;
import com.perros.app.dogsapi.models.Post;
import com.perros.app.dogsapi.models.User;
import com.perros.app.dogsapi.repositories.CommentRepository;
import com.perros.app.dogsapi.repositories.PostRepository;
import com.perros.app.dogsapi.repositories.UserRepository;
import com.perros.app.dogsapi.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private CommentDTO convertToDTO(Comment comment) {
        UserDTO userDTO = null;
        if (comment.getUser() != null) {
            userDTO = new UserDTO(
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getUser().getAvatarUrl()
            );
        }
        return new CommentDTO(
            comment.getId(),
            comment.getContent(),
            comment.getCreatedAt(),
            userDTO
        );
    }

    @GetMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentDTO>> getCommentsByPost(@PathVariable Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        List<CommentDTO> dtos = comments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> requestBody) {
        
        try {
            String content = requestBody.get("content");
            
            if (content == null || content.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El comentario no puede estar vacío");
                return ResponseEntity.badRequest().body(error);
            }
            
            Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado con id: " + postId));
            
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Comment comment = new Comment();
            comment.setContent(content);
            comment.setPost(post);
            comment.setUser(user);
            comment.setCreatedAt(LocalDateTime.now());
            
            Comment savedComment = commentRepository.save(comment);
            
            post.setCommentsCount(post.getCommentsCount() + 1);
            postRepository.save(post);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedComment));
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comentario no encontrado con id: " + commentId));
        
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Solo el dueño del comentario o un admin pueden eliminar
        if (!comment.getUser().getId().equals(userDetails.getId()) && 
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No tienes permiso para eliminar este comentario");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        
        Post post = comment.getPost();
        post.setCommentsCount(post.getCommentsCount() - 1);
        postRepository.save(post);
        
        commentRepository.delete(comment);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Comentario eliminado correctamente");
        return ResponseEntity.ok(response);
    }
}
