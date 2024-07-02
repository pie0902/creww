package org.example.creww.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardDetailsDTO {
    private Long boardId;
    private String name;
    private String description;
    private String ownerName;

}
