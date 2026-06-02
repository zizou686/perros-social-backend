package com.perros.app.dogsapi.controllers;

import com.perros.app.dogsapi.models.Like;
import com.perros.app.dogsapi.models.Post;
import com.perros.app.dogsapi.models.User;
import com.perros.app.dogsapi.repositories.LikeRepository;
import com.perros.app.dogsapi.repositories.PostRepository;
import com.perros.app.dogsapi.repositories.UserRepository;
import com.perros.app.dogsapi.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

    // ✅ Ahora recibe el emoji en el body: { "reactionType": "😍" }
    @PostMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            @RequestBody(required = false) Map<String, String> body) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));

        String reactionType = (body != null && body.containsKey("reactionType"))
                ? body.get("reactionType")
                : "❤️"; // default si no mandan nada

        Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(user.getId(), postId);
        Map<String, Object> response = new HashMap<>();

        if (existingLike.isPresent()) {
            Like like = existingLike.get();
            if (like.getReactionType() != null && like.getReactionType().equals(reactionType)) {
                // ── Misma reacción → quitar (unlike) ──────────────────────
                likeRepository.delete(like);
                post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
                response.put("liked", false);
                response.put("reactionType", null);
            } else {
                // ── Reacción diferente → solo actualizar el emoji, NO tocar el conteo ──
                like.setReactionType(reactionType);
                likeRepository.save(like);
                response.put("liked", true);
                response.put("reactionType", reactionType);
            }
        } else {
            // ── No existía → crear nuevo like ─────────────────────────────
            Like like = new Like(user, post, reactionType);
            likeRepository.save(like);
            post.setLikesCount(post.getLikesCount() + 1);
            response.put("liked", true);
            response.put("reactionType", reactionType);
        }

        postRepository.save(post);
        response.put("likesCount", post.getLikesCount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkLike(@PathVariable Long postId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Optional<Like> like = likeRepository.findByUserIdAndPostId(userDetails.getId(), postId);
        long count = likeRepository.countByPostId(postId);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", like.isPresent());
        response.put("reactionType", like.map(Like::getReactionType).orElse(null));
        response.put("likesCount", count);
        return ResponseEntity.ok(response);
    }
}
