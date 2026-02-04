package com.jareth.npd.devo.model;

import jakarta.persistence.Entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "devocionales")
@Data
public class Devo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private LocalDate date;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;

    private String passage;
    
    @Column(columnDefinition = "TEXT")
    private String verse;

    private String author;
    private String response;
    private String thought;
    private String bibleInYear;
}