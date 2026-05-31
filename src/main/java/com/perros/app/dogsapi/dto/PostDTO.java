package com.perros.app.dogsapi.dto;

import java.time.LocalDateTime;

public class PostDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private int likesCount;
    private int commentsCount;
    private LocalDateTime createdAt;
    private UserDTO user;

    public PostDTO() {}

    public PostDTO(Long id, String content, String imageUrl, int likesCount, int commentsCount, LocalDateTime createdAt, UserDTO user) {
        this.id = id;
        this.content = content;
        this.imageUrl = imageUrl;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.createdAt = createdAt;
        this.user = user;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}
