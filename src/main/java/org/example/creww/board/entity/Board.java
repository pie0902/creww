package org.example.creww.board.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.creww.global.BaseEntity;
import org.example.creww.user.entity.User;

@Entity
@Getter
@NoArgsConstructor
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    public Board(String name,String description,Long owner) {
        this.name = name;
        this.description = description;
        this.ownerId = owner;
    }
}
