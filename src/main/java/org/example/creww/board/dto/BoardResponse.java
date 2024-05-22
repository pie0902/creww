package org.example.creww.board.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.creww.user.entity.User;

@Getter
@NoArgsConstructor
public class BoardResponse{
    private Long id;
    private String boardName;
    private String description;
    private String ownerName;
    public BoardResponse(String boardName,Long id,String description,String ownerName){
        this.id = id;
        this.description = description;
        this.boardName = boardName;
        this.ownerName = ownerName;
    }
}
