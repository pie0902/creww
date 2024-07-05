package org.example.creww.userBoard.entity;


//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_board")
public class UserBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long boardId;
    private boolean isExited = false;

    public UserBoard(Long userId, Long boardId) {
        this.userId = userId;
        this.boardId = boardId;
    }
    public boolean isExited() {
        return isExited;
    }
    //게시판 나가기
    public void setExited(){
        this.isExited = true;
    }
}
