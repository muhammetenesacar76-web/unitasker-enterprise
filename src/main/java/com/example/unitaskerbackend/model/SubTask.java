package com.example.unitaskerbackend.model;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class SubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private boolean completed = false;

    // Sonsuz döngüyü engellemek için JsonIgnore kullanıyoruz
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    public SubTask() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
}