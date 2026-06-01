package com.perros.app.dogsapi.controllers;

import com.perros.app.dogsapi.dto.PostDTO;
import com.perros.app.dogsapi.dto.UserDTO;
import com.perros.app.dogsapi.models.Post;
import com.perros.app.dogsapi.models.User;
import com.perros.app.dogsapi.repositories.CommentRepository;
import com.perros.app.dogsapi.repositories.LikeRepository;
import com.perros.app.dogsapi.repositories.PostRepository;
import com.perros.app.dogsapi.repositories.UserRepository;
import com.perros.app.dogsapi.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private LikeRepository likeRepository;

    private PostDTO convertToDTO(Post post) {
        UserDTO userDTO = null;
        if (post.getUser() != null) {
            userDTO = new UserDTO(
                post.getUser().getId(),
                post.getUser().getUsername(),
                post.getUser().getAvatarUrl()
            );
        }
        return new PostDTO(
            post.getId(),
            post.getContent(),
            post.getImageUrl(),
            post.getLikesCount(),
            post.getCommentsCount(),
            post.getCreatedAt(),
            userDTO
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<PostDTO> getAllPosts(Pageable pageable) {
        Page<Post> postPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<PostDTO> dtos = postPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, postPage.getTotalElements());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PostDTO getPostById(@PathVariable Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));
        return convertToDTO(post);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public PostDTO createPost(@Valid @RequestBody Post postRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Post post = new Post();
        post.setContent(postRequest.getContent());
        post.setImageUrl(postRequest.getImageUrl());
        post.setUser(user);
        post.setLikesCount(0);
        post.setCommentsCount(0);
        
        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean isOwner = post.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream()
            .anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No tienes permiso para eliminar esta publicación");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        try {
            // Primero eliminar comentarios y likes
            commentRepository.deleteByPostId(id);
            likeRepository.deleteByPostId(id);
            
            // Luego eliminar la publicación
            postRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Publicación eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
