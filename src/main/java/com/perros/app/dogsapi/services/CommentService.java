package com.perros.app.dogsapi.services;

import com.perros.app.dogsapi.models.Comment;
import com.perros.app.dogsapi.models.Post;
import com.perros.app.dogsapi.models.User;
import com.perros.app.dogsapi.repositories.CommentRepository;
import com.perros.app.dogsapi.repositories.PostRepository;
import com.perros.app.dogsapi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // Obtener comentarios de un post
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    // Crear un nuevo comentario
    @Transactional
    public Comment createComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post no encontrado con id: " + postId));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + userId));

        Comment comment = new Comment(content, post, user);
        Comment savedComment = commentRepository.save(comment);
        
        // Incrementar el contador de comentarios del post
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
        
        return savedComment;
    }

    // Eliminar un comentario
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comentario no encontrado con id: " + commentId));
        
        Post post = comment.getPost();
        
        // Decrementar el contador de comentarios del post
        post.setCommentsCount(post.getCommentsCount() - 1);
        postRepository.save(post);
        
        commentRepository.delete(comment);
    }

    // Contar comentarios de un post
    public long countCommentsByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }
}

