    package com.scraply.rest.model;

    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    import java.time.Instant;

    @Entity
    @Table(name = "quick_link",
    indexes = {
            @Index(name="idx_code_unique", columnList = "code", unique = true),
            @Index(name = "idx_expiresAt", columnList = "expiresAt")
    })
    @Getter
    @Setter
    @NoArgsConstructor
    public class QuickLink {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        @Column(name = "code", unique = true, nullable = false)
        private String code;

        @Column(name="targetUrl", nullable = false, length = 2024)
        private String targetUrl;

        private Instant expiresAt;
        private long hits = 0;
        private Instant lastAccessedAt;

        @Column(name = "createdAt" , nullable = false, updatable = false)
        private Instant createdAt = Instant.now();

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = true)
        private User user;

        public QuickLink(String code, String targetUrl, Instant expiresAt) {
            this.code = code;
            this.targetUrl = targetUrl;
            this.expiresAt = expiresAt;
        }
    }
