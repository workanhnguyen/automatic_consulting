package com.nva.server.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "information")
@Inheritance(strategy = InheritanceType.JOINED)
public class Information implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "action_id", nullable = false)
    @NotNull(message = "Choose 1 option")
    private Action action;
    @ManyToOne
    @JoinColumn(name = "scope_id", nullable = false)
    @NotNull(message = "Choose 1 option")
    private Scope scope;
    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    @NotNull(message = "Choose 1 option")
    private Topic topic;
    @Column(nullable = false, length = 10000)
    @NotEmpty(message = "Must not be empty")
    private String content;
    @CreatedDate
    @Column(nullable = false)
    private Long createdDate;
    @LastModifiedDate
    private Long lastModifiedDate;
    @Lob
    private String note;

    @PrePersist
    protected void onCreate() {
        this.createdDate = System.currentTimeMillis();
    }
}
