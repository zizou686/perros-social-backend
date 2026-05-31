package com.perros.app.dogsapi.controllers;

import com.perros.app.dogsapi.models.Like;
import com.perros.app.dogsapi.models.Post;
import com.perros.app.dogsapi.models.User;
import com.perros.app.dogsapi.repositories.LikeRepository;
import com.perros.app.dogsapi.repositories.PostRepository;
import com.perros.app.dogsapi.repositories.UserRepository;
import com.perros.app.dogsapi.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long postId) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(user.getId(), postId);

        Map<String, Object> response = new HashMap<>();

        if (existingLike.isPresent()) {
            // Si ya existe like, lo eliminamos (unlike)
            likeRepository.delete(existingLike.get());
            post.setLikesCount(post.getLikesCount() - 1);
            response.put("liked", false);
        } else {
            // Si no existe, creamos el like
            Like like = new Like();
            like.setUser(user);
            like.setPost(post);
            likeRepository.save(like);
            post.setLikesCount(post.getLikesCount() + 1);
            response.put("liked", true);
        }

        postRepository.save(post);
        response.put("likesCount", post.getLikesCount());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkLike(@PathVariable Long postId) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        boolean liked = likeRepository.existsByUserIdAndPostId(userDetails.getId(), postId);
        long count = likeRepository.countByPostId(postId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likesCount", count);
        
        return ResponseEntity.ok(response);
    }
}
